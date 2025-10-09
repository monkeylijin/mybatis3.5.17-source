/*
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.executor;

import static org.apache.ibatis.executor.ExecutionPlaceholder.EXECUTION_PLACEHOLDER;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.statement.StatementUtil;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**
 * Executor执行器抽象基类，采用模板方法模式实现
 * 定义了SQL执行的标准流程，子类只需实现具体的执行逻辑
 *
 * 核心功能：
 * 1. 一级缓存（本地缓存）管理
 * 2. 延迟加载机制实现
 * 3. 事务管理和生命周期控制
 * 4. 查询栈深度控制，防止无限递归
 * 5. 为子类提供通用的模板方法
 *
 * 设计模式：
 * - 模板方法模式：定义算法骨架，子类实现具体步骤
 * - 策略模式：不同的Executor实现不同的执行策略
 *
 * @author Clinton Begin
 */
public abstract class BaseExecutor implements Executor {

  private static final Log log = LogFactory.getLog(BaseExecutor.class);

  /** 事务管理器，用于管理数据库事务的提交、回滚等操作 */
  protected Transaction transaction;

  /** 执行器包装器，通常指向CachingExecutor，用于支持装饰器模式 */
  protected Executor wrapper;

  /** 延迟加载队列，存储需要延迟加载的对象，使用线程安全的ConcurrentLinkedQueue */
  protected ConcurrentLinkedQueue<DeferredLoad> deferredLoads;

  /** 一级缓存（本地缓存），Session级别的缓存，用于缓存查询结果 */
  protected PerpetualCache localCache;

  /** 存储过程输出参数缓存，用于缓存存储过程的OUT参数 */
  protected PerpetualCache localOutputParameterCache;

  /** MyBatis全局配置对象，包含所有配置信息 */
  protected Configuration configuration;

  /** 查询栈深度，用于控制嵌套查询的层次，防止无限递归和栈溢出 */
  protected int queryStack;

  /** 执行器是否已关闭的标志位 */
  private boolean closed;

  /**
   * BaseExecutor构造方法
   * 初始化执行器的所有必要组件，包括事务管理器、缓存和延迟加载等
   *
   * @param configuration MyBatis全局配置对象，包含所有配置信息
   * @param transaction 事务管理器，用于管理数据库事务
   */
  protected BaseExecutor(Configuration configuration, Transaction transaction) {
    // 设置事务管理器
    this.transaction = transaction;

    // 初始化延迟加载队列，使用线程安全的并发队列
    this.deferredLoads = new ConcurrentLinkedQueue<>();

    // 初始化一级缓存，使用永久缓存实现
    this.localCache = new PerpetualCache("LocalCache");

    // 初始化存储过程输出参数缓存
    this.localOutputParameterCache = new PerpetualCache("LocalOutputParameterCache");

    // 设置执行器为未关闭状态
    this.closed = false;

    // 保存全局配置引用
    this.configuration = configuration;

    // 初始化时，包装器指向自身，后续可能被装饰器更改
    this.wrapper = this;
  }

  @Override
  public Transaction getTransaction() {
    if (closed) {
      throw new ExecutorException("Executor was closed.");
    }
    return transaction;
  }

  @Override
  public void close(boolean forceRollback) {
    try {
      try {
        rollback(forceRollback);
      } finally {
        if (transaction != null) {
          transaction.close();
        }
      }
    } catch (SQLException e) {
      // Ignore. There's nothing that can be done at this point.
      log.warn("Unexpected exception on closing transaction.  Cause: " + e);
    } finally {
      transaction = null;
      deferredLoads = null;
      localCache = null;
      localOutputParameterCache = null;
      closed = true;
    }
  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  /**
   * 模板方法：更新操作的标准流程
   * 包括INSERT、UPDATE、DELETE操作
   * 执行更新操作前会清空本地缓存，保证数据一致性
   *
   * @param ms SQL映射语句对象，包含所有SQL相关信息
   * @param parameter 执行参数对象
   * @return 影响的数据库记录数
   * @throws SQLException 数据库操作异常
   */
  @Override
  public int update(MappedStatement ms, Object parameter) throws SQLException {
    // 设置错误上下文，便于异常时的问题定位
    ErrorContext.instance().resource(ms.getResource()).activity("executing an update").object(ms.getId());

    // 检查执行器是否已关闭
    if (closed) {
      throw new ExecutorException("Executor was closed.");
    }

    // 清空本地缓存，因为更新操作可能影响缓存的数据一致性
    clearLocalCache();

    // 调用子类的具体实现方法
    return doUpdate(ms, parameter);
  }

  @Override
  public List<BatchResult> flushStatements() throws SQLException {
    return flushStatements(false);
  }

  public List<BatchResult> flushStatements(boolean isRollBack) throws SQLException {
    if (closed) {
      throw new ExecutorException("Executor was closed.");
    }
    return doFlushStatements(isRollBack);
  }

  /**
   * 模板方法：查询操作的标准流程（重载方法1）
   * 这是查询的入口方法，负责生成BoundSql和缓存Key
   *
   * @param ms SQL映射语句对象
   * @param parameter 查询参数
   * @param rowBounds 分页参数对象
   * @param resultHandler 结果处理器，用于自定义结果处理
   * @return 查询结果列表
   * @throws SQLException 数据库操作异常
   */
  @Override
  public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler)
      throws SQLException {
    // 根据参数生成动态SQL对象
    BoundSql boundSql = ms.getBoundSql(parameter);

    // 创建缓存Key，用于一级缓存的唯一标识
    CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);

    // 调用重载方法继续执行
    return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
  }

  /**
   * 模板方法：查询操作的核心实现（重载方法2）
   * 这是查询的核心方法，包含完整的缓存和执行逻辑
   *
   * 执行流程：
   * 1. 检查执行器状态和缓存刷新需求
   * 2. 尝试从一级缓存获取结果
   * 3. 缓存未命中时从数据库查询
   * 4. 处理延迟加载和缓存清理
   *
   * @param ms SQL映射语句对象
   * @param parameter 查询参数
   * @param rowBounds 分页参数
   * @param resultHandler 结果处理器
   * @param key 缓存Key
   * @param boundSql 绑定参数的SQL对象
   * @return 查询结果列表
   * @throws SQLException 数据库操作异常
   */
  @SuppressWarnings("unchecked")
  @Override
  public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
      CacheKey key, BoundSql boundSql) throws SQLException {
    // 设置错误上下文，便于调试和错误定位
    ErrorContext.instance().resource(ms.getResource()).activity("executing a query").object(ms.getId());

    // 检查执行器是否已关闭
    if (closed) {
      throw new ExecutorException("Executor was closed.");
    }

    // 如果是最外层查询且配置了刷新缓存，则清空本地缓存
    if (queryStack == 0 && ms.isFlushCacheRequired()) {
      clearLocalCache();
    }

    List<E> list;
    try {
      // 查询栈深度+1，用于处理嵌套查询和递归控制
      queryStack++;

      // 尝试从一级缓存中获取结果（仅当没有结果处理器时）
      // 有结果处理器时不使用缓存，因为结果处理器可能修改结果
      list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;

      if (list != null) {
        // 缓存命中，处理存储过程的输出参数
        handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
      } else {
        // 缓存未命中，从数据库查询
        list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
      }
    } finally {
      // 查询栈深度-1，保证正确的递归层次
      queryStack--;
    }

    // 如果回到最外层查询（没有嵌套查询）
    if (queryStack == 0) {
      // 执行所有延迟加载操作
      for (DeferredLoad deferredLoad : deferredLoads) {
        deferredLoad.load();
      }

      // 清空延迟加载队列（解决issue #601）
      deferredLoads.clear();

      // 如果配置为STATEMENT级别缓存，执行完成后清空缓存（解决issue #482）
      if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
        clearLocalCache();
      }
    }

    return list;
  }

  @Override
  public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
    BoundSql boundSql = ms.getBoundSql(parameter);
    return doQueryCursor(ms, parameter, rowBounds, boundSql);
  }

  @Override
  public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key,
      Class<?> targetType) {
    if (closed) {
      throw new ExecutorException("Executor was closed.");
    }
    DeferredLoad deferredLoad = new DeferredLoad(resultObject, property, key, localCache, configuration, targetType);
    if (deferredLoad.canLoad()) {
      deferredLoad.load();
    } else {
      deferredLoads.add(new DeferredLoad(resultObject, property, key, localCache, configuration, targetType));
    }
  }

  /**
   * 创建缓存Key - 用于一级缓存的唯一标识
   * 缓存Key由多个因素组成，确保不同查询的唯一性：
   * 1. MappedStatement ID（SQL映射语句标识）
   * 2. 分页参数（offset和limit）
   * 3. SQL语句内容
   * 4. 所有参数值
   * 5. 环境ID（区分不同数据源环境）
   *
   * @param ms SQL映射语句对象
   * @param parameterObject 参数对象
   * @param rowBounds 分页参数
   * @param boundSql 绑定参数的SQL对象
   * @return 缓存Key对象
   */
  @Override
  public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
    // 检查执行器是否已关闭
    if (closed) {
      throw new ExecutorException("Executor was closed.");
    }

    // 创建缓存Key对象
    CacheKey cacheKey = new CacheKey();

    // 添加SQL映射语句ID到缓存Key中
    cacheKey.update(ms.getId());

    // 添加分页参数到缓存Key中
    cacheKey.update(rowBounds.getOffset());
    cacheKey.update(rowBounds.getLimit());

    // 添加SQL语句到缓存Key中
    cacheKey.update(boundSql.getSql());

    // 获取参数映射列表
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    TypeHandlerRegistry typeHandlerRegistry = ms.getConfiguration().getTypeHandlerRegistry();

    // 模拟DefaultParameterHandler的逻辑，获取参数值
    MetaObject metaObject = null;

    // 遍历所有参数映射，将参数值添加到缓存Key中
    for (ParameterMapping parameterMapping : parameterMappings) {
      // 只处理输入参数，不处理输出参数
      if (parameterMapping.getMode() != ParameterMode.OUT) {
        Object value;
        String propertyName = parameterMapping.getProperty();

        // 参数值获取的优先级判断：
        if (boundSql.hasAdditionalParameter(propertyName)) {
          // 1. 从额外参数中获取（优先级最高）
          value = boundSql.getAdditionalParameter(propertyName);
        } else if (parameterObject == null) {
          // 2. 参数对象为空
          value = null;
        } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
          // 3. 参数对象有对应的类型处理器（通常是基本类型）
          value = parameterObject;
        } else {
          // 4. 复杂对象，通过反射获取属性值
          if (metaObject == null) {
            metaObject = configuration.newMetaObject(parameterObject);
          }
          value = metaObject.getValue(propertyName);
        }

        // 将参数值添加到缓存Key中
        cacheKey.update(value);
      }
    }

    // 将环境ID添加到缓存Key中（解决issue #176）
    // 用于区分不同数据源环境的缓存
    if (configuration.getEnvironment() != null) {
      cacheKey.update(configuration.getEnvironment().getId());
    }

    return cacheKey;
  }

  @Override
  public boolean isCached(MappedStatement ms, CacheKey key) {
    return localCache.getObject(key) != null;
  }

  /**
   * 提交事务
   * 执行顺序：清空缓存 -> 刷新语句 -> 提交事务
   *
   * @param required 是否强制提交事务
   * @throws SQLException 数据库操作异常
   */
  @Override
  public void commit(boolean required) throws SQLException {
    if (closed) {
      throw new ExecutorException("Cannot commit, transaction is already closed");
    }
    // 清空本地缓存，保证数据一致性
    clearLocalCache();
    // 刷新所有未执行的语句（主要针对BatchExecutor）
    flushStatements();
    // 如果需要，提交数据库事务
    if (required) {
      transaction.commit();
    }
  }

  /**
   * 回滚事务
   * 执行顺序：清空缓存 -> 刷新语句(回滚模式) -> 回滚事务
   *
   * @param required 是否强制回滚事务
   * @throws SQLException 数据库操作异常
   */
  @Override
  public void rollback(boolean required) throws SQLException {
    if (!closed) {
      try {
        // 清空本地缓存
        clearLocalCache();
        // 刷新语句（回滚模式）
        flushStatements(true);
      } finally {
        // 如果需要，回滚数据库事务
        if (required) {
          transaction.rollback();
        }
      }
    }
  }

  /**
   * 清空本地缓存
   * 清空一级缓存和存储过程输出参数缓存
   * 通常在更新操作后调用，保证数据一致性
   */
  @Override
  public void clearLocalCache() {
    if (!closed) {
      // 清空一级查询缓存
      localCache.clear();
      // 清空存储过程输出参数缓存
      localOutputParameterCache.clear();
    }
  }

  /**
   * 抽象方法：子类实现具体的数据库更新逻辑
   * 不同的执行器有不同的实现策略：
   * - SimpleExecutor：每次创建新Statement
   * - ReuseExecutor：重用Statement对象
   * - BatchExecutor：批量执行更新
   *
   * @param ms SQL映射语句对象
   * @param parameter 执行参数
   * @return 影响的数据库记录数
   * @throws SQLException 数据库操作异常
   */
  protected abstract int doUpdate(MappedStatement ms, Object parameter) throws SQLException;

  /**
   * 抽象方法：子类实现具体的批量操作刷新逻辑
   * 用于刷新所有缓存的批量操作，主要用于BatchExecutor
   *
   * @param isRollback 是否是回滚操作
   * @return 批量执行结果列表
   * @throws SQLException 数据库操作异常
   */
  protected abstract List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException;

  /**
   * 抽象方法：子类实现具体的数据库查询逻辑
   * 这是最核心的抽象方法，不同执行器的主要区别在此
   *
   * @param ms SQL映射语句对象
   * @param parameter 查询参数
   * @param rowBounds 分页参数
   * @param resultHandler 结果处理器
   * @param boundSql 绑定参数的SQL对象
   * @return 查询结果列表
   * @throws SQLException 数据库操作异常
   */
  protected abstract <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds,
      ResultHandler resultHandler, BoundSql boundSql) throws SQLException;

  /**
   * 抽象方法：子类实现具体的游标查询逻辑
   * 游标查询适用于大结果集的流式处理，避免占用大量内存
   *
   * @param ms SQL映射语句对象
   * @param parameter 查询参数
   * @param rowBounds 分页参数
   * @param boundSql 绑定参数的SQL对象
   * @return 游标对象
   * @throws SQLException 数据库操作异常
   */
  protected abstract <E> Cursor<E> doQueryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds,
      BoundSql boundSql) throws SQLException;

  /**
   * 安全关闭Statement对象
   * 忽略关闭过程中的异常，避免影响主逻辑的执行
   *
   * @param statement 需要关闭的Statement对象
   */
  protected void closeStatement(Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      } catch (SQLException e) {
        // 忽略关闭异常，避免影响主逻辑
      }
    }
  }

  /**
   * Apply a transaction timeout.
   *
   * @param statement
   *          a current statement
   *
   * @throws SQLException
   *           if a database access error occurs, this method is called on a closed <code>Statement</code>
   *
   * @since 3.4.0
   *
   * @see StatementUtil#applyTransactionTimeout(Statement, Integer, Integer)
   */
  protected void applyTransactionTimeout(Statement statement) throws SQLException {
    StatementUtil.applyTransactionTimeout(statement, statement.getQueryTimeout(), transaction.getTimeout());
  }

  private void handleLocallyCachedOutputParameters(MappedStatement ms, CacheKey key, Object parameter,
      BoundSql boundSql) {
    if (ms.getStatementType() == StatementType.CALLABLE) {
      final Object cachedParameter = localOutputParameterCache.getObject(key);
      if (cachedParameter != null && parameter != null) {
        final MetaObject metaCachedParameter = configuration.newMetaObject(cachedParameter);
        final MetaObject metaParameter = configuration.newMetaObject(parameter);
        for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
          if (parameterMapping.getMode() != ParameterMode.IN) {
            final String parameterName = parameterMapping.getProperty();
            final Object cachedValue = metaCachedParameter.getValue(parameterName);
            metaParameter.setValue(parameterName, cachedValue);
          }
        }
      }
    }
  }

  /**
   * 从数据库查询数据并缓存结果
   * 这是一级缓存的核心实现方法，处理缓存的存储和管理
   *
   * 执行流程：
   * 1. 在缓存中放入占位符，防止循环引用
   * 2. 调用子类的doQuery方法执行实际查询
   * 3. 无论成功失败，移除占位符
   * 4. 将查询结果放入缓存
   * 5. 如果是存储过程，缓存输出参数
   *
   * @param ms SQL映射语句对象
   * @param parameter 查询参数
   * @param rowBounds 分页参数
   * @param resultHandler 结果处理器
   * @param key 缓存Key
   * @param boundSql 绑定参数的SQL对象
   * @return 查询结果列表
   * @throws SQLException 数据库操作异常
   */
  private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds,
      ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    List<E> list;

    // 在缓存中放入占位符，防止循环引用和重复查询
    localCache.putObject(key, EXECUTION_PLACEHOLDER);

    try {
      // 调用子类的具体实现执行数据库查询
      list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
    } finally {
      // 无论成功还是失败，都要移除占位符
      localCache.removeObject(key);
    }

    // 将查询结果放入一级缓存
    localCache.putObject(key, list);

    // 如果是存储过程调用，需要缓存输出参数
    if (ms.getStatementType() == StatementType.CALLABLE) {
      localOutputParameterCache.putObject(key, parameter);
    }

    return list;
  }

  protected Connection getConnection(Log statementLog) throws SQLException {
    Connection connection = transaction.getConnection();
    if (statementLog.isDebugEnabled()) {
      return ConnectionLogger.newInstance(connection, statementLog, queryStack);
    }
    return connection;
  }

  @Override
  public void setExecutorWrapper(Executor wrapper) {
    this.wrapper = wrapper;
  }

  private static class DeferredLoad {

    private final MetaObject resultObject;
    private final String property;
    private final Class<?> targetType;
    private final CacheKey key;
    private final PerpetualCache localCache;
    private final ObjectFactory objectFactory;
    private final ResultExtractor resultExtractor;

    // issue #781
    public DeferredLoad(MetaObject resultObject, String property, CacheKey key, PerpetualCache localCache,
        Configuration configuration, Class<?> targetType) {
      this.resultObject = resultObject;
      this.property = property;
      this.key = key;
      this.localCache = localCache;
      this.objectFactory = configuration.getObjectFactory();
      this.resultExtractor = new ResultExtractor(configuration, objectFactory);
      this.targetType = targetType;
    }

    public boolean canLoad() {
      return localCache.getObject(key) != null && localCache.getObject(key) != EXECUTION_PLACEHOLDER;
    }

    public void load() {
      @SuppressWarnings("unchecked")
      // we suppose we get back a List
      List<Object> list = (List<Object>) localCache.getObject(key);
      Object value = resultExtractor.extractObjectFromList(list, targetType);
      resultObject.setValue(property, value);
    }

  }

}
