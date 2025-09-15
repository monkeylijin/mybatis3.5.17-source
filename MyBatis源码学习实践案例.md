# MyBatis 源码学习实践案例

## 🎯 实践案例设计原则

1. **循序渐进**: 从简单到复杂，逐步深入
2. **理论结合实践**: 每个概念都有对应的代码示例
3. **可运行验证**: 所有示例都可以直接运行和调试
4. **贴近实际**: 案例来源于实际开发场景

## 📚 基础实践案例

### 案例1：跟踪一个简单查询的执行流程

#### 目标
理解 MyBatis 从接口调用到数据库执行的完整流程

#### 实践步骤

**第1步：创建测试项目**
```java
// 1. 创建 Maven 项目，添加 MyBatis 依赖
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis</artifactId>
    <version>3.5.13</version>
</dependency>
```

**第2步：创建实体类和 Mapper**
```java
// User.java
public class User {
    private Integer id;
    private String name;
    private String email;
    // getter/setter...
}

// UserMapper.java
public interface UserMapper {
    User selectById(Integer id);
}
```

**第3步：创建配置文件**
```xml
<!-- mybatis-config.xml -->
<configuration>
    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://localhost:3306/test"/>
                <property name="username" value="root"/>
                <property name="password" value="password"/>
            </dataSource>
        </environment>
    </environments>
    <mappers>
        <mapper resource="UserMapper.xml"/>
    </mappers>
</configuration>
```

**第4步：设置断点跟踪**
```java
// 在以下关键位置设置断点
public class MyBatisTraceExample {
    public static void main(String[] args) throws IOException {
        // 断点1：SqlSessionFactory 创建
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        
        // 断点2：SqlSession 创建
        SqlSession session = sqlSessionFactory.openSession();
        
        // 断点3：Mapper 获取
        UserMapper mapper = session.getMapper(UserMapper.class);
        
        // 断点4：方法调用
        User user = mapper.selectById(1);
        
        System.out.println(user);
        session.close();
    }
}
```

**第5步：分析执行流程**
```java
// 在调试过程中，观察以下调用栈：
// 1. MapperProxy.invoke()
// 2. MapperMethod.execute()
// 3. SqlSession.selectOne()
// 4. Executor.query()
// 5. StatementHandler.query()
// 6. PreparedStatement.executeQuery()
```

#### 学习要点
- 理解动态代理的工作原理
- 掌握 SQL 执行的完整链路
- 学会使用调试器分析源码

### 案例2：自定义简单的执行器

#### 目标
理解 Executor 接口的设计，实现自定义执行器

#### 实践步骤

**第1步：创建自定义执行器**
```java
public class LoggingExecutor implements Executor {
    private final Executor delegate;
    private static final Logger logger = LoggerFactory.getLogger(LoggingExecutor.class);
    
    public LoggingExecutor(Executor delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, 
                           ResultHandler resultHandler) throws SQLException {
        long startTime = System.currentTimeMillis();
        logger.info("开始执行查询: {}", ms.getId());
        
        try {
            List<E> result = delegate.query(ms, parameter, rowBounds, resultHandler);
            long endTime = System.currentTimeMillis();
            logger.info("查询完成，耗时: {}ms, 结果数量: {}", 
                       endTime - startTime, result.size());
            return result;
        } catch (SQLException e) {
            logger.error("查询执行失败: {}", ms.getId(), e);
            throw e;
        }
    }
    
    // 其他方法委托给原始执行器
    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        return delegate.update(ms, parameter);
    }
    
    // ... 其他方法的实现
}
```

**第2步：创建执行器工厂**
```java
public class LoggingExecutorFactory {
    public static Executor createExecutor(Configuration configuration, Transaction transaction) {
        ExecutorType executorType = configuration.getDefaultExecutorType();
        Executor executor;
        
        switch (executorType) {
            case SIMPLE:
                executor = new SimpleExecutor(configuration, transaction);
                break;
            case REUSE:
                executor = new ReuseExecutor(configuration, transaction);
                break;
            case BATCH:
                executor = new BatchExecutor(configuration, transaction);
                break;
            default:
                executor = new SimpleExecutor(configuration, transaction);
        }
        
        // 包装日志执行器
        return new LoggingExecutor(executor);
    }
}
```

**第3步：测试自定义执行器**
```java
public class CustomExecutorTest {
    public static void main(String[] args) throws IOException {
        // 使用自定义执行器工厂
        Configuration configuration = new Configuration();
        Transaction transaction = new JdbcTransaction(dataSource, null, false);
        
        Executor executor = LoggingExecutorFactory.createExecutor(configuration, transaction);
        SqlSession session = new DefaultSqlSession(configuration, executor, false);
        
        UserMapper mapper = session.getMapper(UserMapper.class);
        User user = mapper.selectById(1);
        
        System.out.println("查询结果: " + user);
    }
}
```

#### 学习要点
- 理解装饰器模式在 MyBatis 中的应用
- 掌握执行器的职责和扩展点
- 学会自定义组件的方法

### 案例3：实现简单的插件

#### 目标
理解 MyBatis 插件系统的工作原理

#### 实践步骤

**第1步：创建性能监控插件**
```java
@Intercepts({
    @Signature(type = Executor.class, method = "query", 
               args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "update", 
               args = {MappedStatement.class, Object.class})
})
public class PerformancePlugin implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(PerformancePlugin.class);
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = invocation.getMethod().getName();
        String statementId = getStatementId(invocation);
        
        try {
            Object result = invocation.proceed();
            long endTime = System.currentTimeMillis();
            
            logger.info("SQL执行统计 - 方法: {}, 语句: {}, 耗时: {}ms", 
                       methodName, statementId, endTime - startTime);
            
            return result;
        } catch (Throwable e) {
            long endTime = System.currentTimeMillis();
            logger.error("SQL执行失败 - 方法: {}, 语句: {}, 耗时: {}ms, 错误: {}", 
                        methodName, statementId, endTime - startTime, e.getMessage());
            throw e;
        }
    }
    
    private String getStatementId(Invocation invocation) {
        Object[] args = invocation.getArgs();
        if (args.length > 0 && args[0] instanceof MappedStatement) {
            return ((MappedStatement) args[0]).getId();
        }
        return "unknown";
    }
}
```

**第2步：配置插件**
```xml
<!-- mybatis-config.xml -->
<configuration>
    <plugins>
        <plugin interceptor="com.example.PerformancePlugin">
            <property name="enabled" value="true"/>
        </plugin>
    </plugins>
    <!-- 其他配置... -->
</configuration>
```

**第3步：测试插件效果**
```java
public class PluginTest {
    public static void main(String[] args) throws IOException {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        
        SqlSession session = sqlSessionFactory.openSession();
        UserMapper mapper = session.getMapper(UserMapper.class);
        
        // 执行查询，观察日志输出
        User user = mapper.selectById(1);
        List<User> users = mapper.selectAll();
        
        session.close();
    }
}
```

#### 学习要点
- 理解插件系统的工作原理
- 掌握动态代理在插件中的应用
- 学会编写实用的插件

## 🔧 进阶实践案例

### 案例4：自定义类型处理器

#### 目标
理解 MyBatis 的类型转换机制

#### 实践步骤

**第1步：创建 JSON 类型处理器**
```java
public class JsonTypeHandler<T> extends BaseTypeHandler<T> {
    private final Type type;
    private final ObjectMapper objectMapper;
    
    public JsonTypeHandler() {
        this.type = getSuperclassTypeParameter(getClass());
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) 
            throws SQLException {
        try {
            String json = objectMapper.writeValueAsString(parameter);
            ps.setString(i, json);
        } catch (Exception e) {
            throw new SQLException("Error converting object to JSON", e);
        }
    }
    
    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }
    
    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }
    
    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }
    
    @SuppressWarnings("unchecked")
    private T parseJson(String json) throws SQLException {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return (T) objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new SQLException("Error parsing JSON", e);
        }
    }
    
    private Type getSuperclassTypeParameter(Class<?> clazz) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
            return parameterizedType.getActualTypeArguments()[0];
        }
        return Object.class;
    }
}
```

**第2步：创建用户扩展信息类**
```java
public class UserExtInfo {
    private String address;
    private String phone;
    private List<String> hobbies;
    
    // 构造函数、getter/setter...
}

public class User {
    private Integer id;
    private String name;
    private String email;
    private UserExtInfo extInfo; // JSON 字段
    
    // getter/setter...
}
```

**第3步：注册类型处理器**
```xml
<!-- mybatis-config.xml -->
<configuration>
    <typeHandlers>
        <typeHandler handler="com.example.JsonTypeHandler" 
                     javaType="com.example.UserExtInfo"/>
    </typeHandlers>
    <!-- 其他配置... -->
</configuration>
```

**第4步：测试类型处理器**
```java
public class TypeHandlerTest {
    public static void main(String[] args) {
        User user = new User();
        user.setName("张三");
        user.setEmail("zhangsan@example.com");
        
        UserExtInfo extInfo = new UserExtInfo();
        extInfo.setAddress("北京市朝阳区");
        extInfo.setPhone("13800138000");
        extInfo.setHobbies(Arrays.asList("读书", "游泳", "编程"));
        
        user.setExtInfo(extInfo);
        
        // 保存用户
        userMapper.insert(user);
        
        // 查询用户
        User savedUser = userMapper.selectById(user.getId());
        System.out.println("扩展信息: " + savedUser.getExtInfo());
    }
}
```

#### 学习要点
- 理解类型处理器的工作原理
- 掌握 Java 类型与 JDBC 类型的转换
- 学会处理复杂数据类型的映射

### 案例5：实现分页插件

#### 目标
理解插件系统的实际应用

#### 实践步骤

**第1步：创建分页参数类**
```java
public class PageParam {
    private int pageNum = 1;
    private int pageSize = 10;
    
    public PageParam() {}
    
    public PageParam(int pageNum, int pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
    
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
    
    // getter/setter...
}

public class PageResult<T> {
    private List<T> records;
    private long total;
    private int pageNum;
    private int pageSize;
    private int pages;
    
    // 构造函数、getter/setter...
}
```

**第2步：创建分页插件**
```java
@Intercepts({
    @Signature(type = Executor.class, method = "query", 
               args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class PagePlugin implements Interceptor {
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        
        // 检查是否需要分页
        if (parameter instanceof PageParam) {
            PageParam pageParam = (PageParam) parameter;
            
            // 修改 SQL 添加 LIMIT 子句
            BoundSql boundSql = ms.getBoundSql(parameter);
            String originalSql = boundSql.getSql();
            String pageSql = addPageLimit(originalSql, pageParam);
            
            // 创建新的 BoundSql
            BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), pageSql, 
                                               boundSql.getParameterMappings(), parameter);
            
            // 创建新的 MappedStatement
            MappedStatement newMs = copyFromMappedStatement(ms, new BoundSqlSqlSource(newBoundSql));
            args[0] = newMs;
        }
        
        return invocation.proceed();
    }
    
    private String addPageLimit(String sql, PageParam pageParam) {
        StringBuilder pageSql = new StringBuilder(sql);
        pageSql.append(" LIMIT ").append(pageParam.getOffset())
               .append(", ").append(pageParam.getPageSize());
        return pageSql.toString();
    }
    
    private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(
            ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());
        
        builder.resource(ms.getResource())
               .fetchSize(ms.getFetchSize())
               .timeout(ms.getTimeout())
               .statementType(ms.getStatementType())
               .keyGenerator(ms.getKeyGenerator())
               .keyProperty(ms.getKeyProperties() != null ? String.join(",", ms.getKeyProperties()) : null)
               .keyColumn(ms.getKeyColumns() != null ? String.join(",", ms.getKeyColumns()) : null)
               .databaseId(ms.getDatabaseId())
               .lang(ms.getLang())
               .resultOrdered(ms.isResultOrdered())
               .resultSets(ms.getResultSets() != null ? String.join(",", ms.getResultSets()) : null)
               .resultMaps(ms.getResultMaps())
               .resultSetType(ms.getResultSetType())
               .flushCacheRequired(ms.isFlushCacheRequired())
               .useCache(ms.isUseCache());
        
        return builder.build();
    }
    
    // 内部类：自定义 SqlSource
    public static class BoundSqlSqlSource implements SqlSource {
        private final BoundSql boundSql;
        
        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }
        
        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }
}
```

**第3步：测试分页插件**
```java
public class PagePluginTest {
    public static void main(String[] args) {
        // 使用分页参数查询
        PageParam pageParam = new PageParam(2, 5); // 第2页，每页5条
        
        List<User> users = userMapper.selectByPage(pageParam);
        
        // 查询总数
        long total = userMapper.countUsers();
        
        PageResult<User> pageResult = new PageResult<>();
        pageResult.setRecords(users);
        pageResult.setTotal(total);
        pageResult.setPageNum(pageParam.getPageNum());
        pageResult.setPageSize(pageParam.getPageSize());
        pageResult.setPages((int) Math.ceil((double) total / pageParam.getPageSize()));
        
        System.out.println("分页结果: " + pageResult);
    }
}
```

#### 学习要点
- 理解插件如何修改 SQL 语句
- 掌握 MappedStatement 的构建过程
- 学会实现复杂的功能插件

## 🎯 综合实践项目

### 项目：实现一个简化版的 MyBatis

#### 项目目标
通过实现一个简化版的 MyBatis，深入理解其核心原理

#### 项目结构
```
simple-mybatis/
├── src/main/java/
│   ├── SimpleMyBatis.java          # 主入口类
│   ├── SimpleSqlSession.java       # 会话接口
│   ├── SimpleSqlSessionFactory.java # 会话工厂
│   ├── SimpleExecutor.java         # 执行器
│   ├── SimpleMapperProxy.java      # Mapper 代理
│   ├── SimpleConfiguration.java    # 配置类
│   └── SimpleMappedStatement.java  # 映射语句
└── src/test/java/
    └── SimpleMyBatisTest.java      # 测试类
```

#### 核心实现

**第1步：实现配置类**
```java
public class SimpleConfiguration {
    private DataSource dataSource;
    private Map<String, SimpleMappedStatement> mappedStatements = new HashMap<>();
    
    public void addMappedStatement(SimpleMappedStatement statement) {
        mappedStatements.put(statement.getId(), statement);
    }
    
    public SimpleMappedStatement getMappedStatement(String id) {
        return mappedStatements.get(id);
    }
    
    // getter/setter...
}
```

**第2步：实现映射语句**
```java
public class SimpleMappedStatement {
    private String id;
    private String sql;
    private Class<?> resultType;
    private Class<?> parameterType;
    
    // 构造函数、getter/setter...
}
```

**第3步：实现执行器**
```java
public class SimpleExecutor {
    private DataSource dataSource;
    
    public <T> T selectOne(String statementId, Object parameter) {
        // 1. 获取映射语句
        // 2. 处理参数
        // 3. 执行 SQL
        // 4. 映射结果
        return null; // 简化实现
    }
    
    // 其他方法...
}
```

**第4步：实现 Mapper 代理**
```java
public class SimpleMapperProxy implements InvocationHandler {
    private SimpleSqlSession sqlSession;
    private Class<?> mapperInterface;
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 1. 解析方法名
        // 2. 构建语句 ID
        // 3. 调用执行器
        return null; // 简化实现
    }
}
```

**第5步：实现会话工厂**
```java
public class SimpleSqlSessionFactory {
    private SimpleConfiguration configuration;
    
    public SimpleSqlSession openSession() {
        return new SimpleSqlSession(configuration);
    }
}
```

**第6步：实现主入口类**
```java
public class SimpleMyBatis {
    public static SimpleSqlSessionFactory build(InputStream inputStream) {
        // 1. 解析配置文件
        // 2. 构建配置对象
        // 3. 创建会话工厂
        return null; // 简化实现
    }
}
```

#### 学习要点
- 理解 MyBatis 的核心组件关系
- 掌握动态代理的实际应用
- 学会设计简单的 ORM 框架

## 📝 实践总结

### 学习成果检验
完成这些实践案例后，你应该能够：

1. **跟踪 MyBatis 的完整执行流程**
2. **理解各个组件的职责和协作关系**
3. **编写自定义的执行器和插件**
4. **实现类型处理器和分页功能**
5. **设计简单的 ORM 框架**

### 进阶学习建议
1. **阅读 MyBatis 官方文档和源码注释**
2. **参与 MyBatis 开源项目**
3. **写技术博客分享学习心得**
4. **在实际项目中应用所学知识**

记住：**实践是学习源码的最佳方式，通过动手实现，你能够更深入地理解 MyBatis 的设计思想！**

