package com.example.mybatis.learning;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.*;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.executor.ReuseExecutor;
import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 第3篇：SqlSession的创建与生命周期实践案例
 * 
 * 这个案例通过实际代码演示：
 * 1. SqlSession的创建过程
 * 2. 不同Executor类型的使用
 * 3. SqlSession的生命周期管理
 * 4. 事务管理机制
 * 5. 资源释放机制
 */
public class SqlSessionLifecycleExample {
    
    public static void main(String[] args) {
        try {
            // 演示SqlSession创建过程
            demonstrateSqlSessionCreation();
            
            // 演示不同Executor类型
            demonstrateExecutorTypes();
            
            // 演示SqlSession生命周期管理
            demonstrateLifecycleManagement();
            
            // 演示事务管理
            demonstrateTransactionManagement();
            
        } catch (Exception e) {
            System.err.println("演示过程中出现错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 演示SqlSession创建过程
     */
    private static void demonstrateSqlSessionCreation() {
        System.out.println("=== 演示SqlSession创建过程 ===\n");
        
        try {
            // 第1步：创建SqlSessionFactoryBuilder
            System.out.println("第1步：创建SqlSessionFactoryBuilder");
            SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
            
            // 第2步：解析配置文件，创建SqlSessionFactory
            System.out.println("第2步：解析配置文件，创建SqlSessionFactory");
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = builder.build(inputStream);
            System.out.println("   - SqlSessionFactory创建完成");
            
            // 第3步：创建SqlSession
            System.out.println("第3步：创建SqlSession");
            SqlSession session = sqlSessionFactory.openSession();
            System.out.println("   - SqlSession创建完成");
            System.out.println("   - 配置信息: " + session.getConfiguration());
            System.out.println("   - 自动提交: " + session.getConnection().getAutoCommit());
            
            // 第4步：使用SqlSession
            System.out.println("第4步：使用SqlSession执行查询");
            // 模拟查询（实际使用时需要配置Mapper）
            System.out.println("   - 准备执行查询操作");
            
            // 第5步：关闭SqlSession
            System.out.println("第5步：关闭SqlSession");
            session.close();
            System.out.println("   - SqlSession已关闭");
            
        } catch (IOException e) {
            System.err.println("配置文件加载失败: " + e.getMessage());
        }
        
        System.out.println("\n=== SqlSession创建过程演示完成 ===\n");
    }
    
    /**
     * 演示不同Executor类型
     */
    private static void demonstrateExecutorTypes() {
        System.out.println("=== 演示不同Executor类型 ===\n");
        
        try {
            SqlSessionFactory sqlSessionFactory = createSqlSessionFactory();
            
            // 1. SimpleExecutor（默认）
            System.out.println("1. SimpleExecutor（默认执行器）");
            try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.SIMPLE)) {
                System.out.println("   - 创建SimpleExecutor类型的SqlSession");
                System.out.println("   - 特点：每次执行都创建新的Statement");
                System.out.println("   - 适用场景：单次执行，资源管理简单");
            }
            
            // 2. ReuseExecutor
            System.out.println("\n2. ReuseExecutor（重用执行器）");
            try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.REUSE)) {
                System.out.println("   - 创建ReuseExecutor类型的SqlSession");
                System.out.println("   - 特点：相同SQL重用Statement对象");
                System.out.println("   - 适用场景：频繁执行相同SQL，性能优化");
            }
            
            // 3. BatchExecutor
            System.out.println("\n3. BatchExecutor（批处理执行器）");
            try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
                System.out.println("   - 创建BatchExecutor类型的SqlSession");
                System.out.println("   - 特点：批量执行SQL语句");
                System.out.println("   - 适用场景：批量插入、更新、删除操作");
                
                // 模拟批量操作
                System.out.println("   - 模拟批量插入操作");
                for (int i = 1; i <= 3; i++) {
                    System.out.println("     * 准备插入第" + i + "条记录");
                }
                
                // 执行批量操作
                List<BatchResult> results = session.flushStatements();
                System.out.println("   - 批量执行完成，结果数量: " + results.size());
            }
            
        } catch (Exception e) {
            System.err.println("Executor类型演示失败: " + e.getMessage());
        }
        
        System.out.println("\n=== Executor类型演示完成 ===\n");
    }
    
    /**
     * 演示SqlSession生命周期管理
     */
    private static void demonstrateLifecycleManagement() {
        System.out.println("=== 演示SqlSession生命周期管理 ===\n");
        
        try {
            SqlSessionFactory sqlSessionFactory = createSqlSessionFactory();
            
            // 1. 手动管理生命周期
            System.out.println("1. 手动管理生命周期");
            SqlSession session = sqlSessionFactory.openSession();
            System.out.println("   - 手动创建SqlSession");
            
            try {
                // 执行业务操作
                System.out.println("   - 执行业务操作");
                System.out.println("   - 查询用户信息");
                
                // 提交事务
                System.out.println("   - 提交事务");
                session.commit();
                
            } catch (Exception e) {
                // 回滚事务
                System.out.println("   - 发生异常，回滚事务");
                session.rollback();
            } finally {
                // 关闭会话
                System.out.println("   - 关闭会话");
                session.close();
            }
            
            // 2. 自动管理生命周期（推荐方式）
            System.out.println("\n2. 自动管理生命周期（推荐方式）");
            try (SqlSession session2 = sqlSessionFactory.openSession()) {
                System.out.println("   - 使用try-with-resources自动管理");
                System.out.println("   - 执行业务操作");
                System.out.println("   - 提交事务");
                session2.commit();
                System.out.println("   - 自动关闭会话");
            } catch (Exception e) {
                System.err.println("   - 发生异常: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("生命周期管理演示失败: " + e.getMessage());
        }
        
        System.out.println("\n=== SqlSession生命周期管理演示完成 ===\n");
    }
    
    /**
     * 演示事务管理
     */
    private static void demonstrateTransactionManagement() {
        System.out.println("=== 演示事务管理 ===\n");
        
        try {
            SqlSessionFactory sqlSessionFactory = createSqlSessionFactory();
            
            // 1. 自动提交模式
            System.out.println("1. 自动提交模式");
            try (SqlSession session = sqlSessionFactory.openSession(true)) {
                System.out.println("   - 创建自动提交的SqlSession");
                System.out.println("   - 执行更新操作");
                System.out.println("   - 自动提交事务");
                System.out.println("   - 不需要手动commit()");
            }
            
            // 2. 手动提交模式
            System.out.println("\n2. 手动提交模式");
            try (SqlSession session = sqlSessionFactory.openSession(false)) {
                System.out.println("   - 创建手动提交的SqlSession");
                System.out.println("   - 执行多个更新操作");
                System.out.println("   - 手动提交事务");
                session.commit();
                System.out.println("   - 事务提交完成");
            }
            
            // 3. 事务回滚
            System.out.println("\n3. 事务回滚");
            try (SqlSession session = sqlSessionFactory.openSession(false)) {
                System.out.println("   - 创建手动提交的SqlSession");
                System.out.println("   - 执行更新操作");
                System.out.println("   - 模拟业务异常");
                System.out.println("   - 回滚事务");
                session.rollback();
                System.out.println("   - 事务回滚完成");
            }
            
            // 4. 强制提交/回滚
            System.out.println("\n4. 强制提交/回滚");
            try (SqlSession session = sqlSessionFactory.openSession(false)) {
                System.out.println("   - 创建手动提交的SqlSession");
                System.out.println("   - 执行更新操作");
                System.out.println("   - 强制提交事务");
                session.commit(true);
                System.out.println("   - 强制提交完成");
            }
            
        } catch (Exception e) {
            System.err.println("事务管理演示失败: " + e.getMessage());
        }
        
        System.out.println("\n=== 事务管理演示完成 ===\n");
    }
    
    /**
     * 演示Executor的创建过程
     */
    public static void demonstrateExecutorCreation() {
        System.out.println("=== 演示Executor的创建过程 ===\n");
        
        try {
            // 1. 创建Configuration
            System.out.println("1. 创建Configuration");
            Configuration configuration = new Configuration();
            System.out.println("   - Configuration创建完成");
            
            // 2. 创建Transaction
            System.out.println("2. 创建Transaction");
            Transaction transaction = createMockTransaction();
            System.out.println("   - Transaction创建完成");
            
            // 3. 创建不同类型的Executor
            System.out.println("3. 创建不同类型的Executor");
            
            // SimpleExecutor
            System.out.println("   - 创建SimpleExecutor");
            Executor simpleExecutor = configuration.newExecutor(transaction, ExecutorType.SIMPLE);
            System.out.println("     * SimpleExecutor创建完成");
            
            // ReuseExecutor
            System.out.println("   - 创建ReuseExecutor");
            Executor reuseExecutor = configuration.newExecutor(transaction, ExecutorType.REUSE);
            System.out.println("     * ReuseExecutor创建完成");
            
            // BatchExecutor
            System.out.println("   - 创建BatchExecutor");
            Executor batchExecutor = configuration.newExecutor(transaction, ExecutorType.BATCH);
            System.out.println("     * BatchExecutor创建完成");
            
            // 4. 创建带缓存的Executor
            System.out.println("4. 创建带缓存的Executor");
            Executor cachingExecutor = configuration.newExecutor(transaction, ExecutorType.SIMPLE);
            System.out.println("   - CachingExecutor创建完成");
            
        } catch (Exception e) {
            System.err.println("Executor创建演示失败: " + e.getMessage());
        }
        
        System.out.println("\n=== Executor创建过程演示完成 ===\n");
    }
    
    /**
     * 演示SqlSession的核心方法
     */
    public static void demonstrateSqlSessionMethods() {
        System.out.println("=== 演示SqlSession的核心方法 ===\n");
        
        try {
            SqlSessionFactory sqlSessionFactory = createSqlSessionFactory();
            
            try (SqlSession session = sqlSessionFactory.openSession()) {
                System.out.println("1. 查询方法");
                System.out.println("   - selectOne(): 查询单个对象");
                System.out.println("   - selectList(): 查询对象列表");
                System.out.println("   - selectMap(): 查询Map结果");
                System.out.println("   - selectCursor(): 查询游标结果");
                
                System.out.println("\n2. 更新方法");
                System.out.println("   - insert(): 插入操作");
                System.out.println("   - update(): 更新操作");
                System.out.println("   - delete(): 删除操作");
                
                System.out.println("\n3. 事务方法");
                System.out.println("   - commit(): 提交事务");
                System.out.println("   - rollback(): 回滚事务");
                
                System.out.println("\n4. 其他方法");
                System.out.println("   - getMapper(): 获取Mapper接口");
                System.out.println("   - getConnection(): 获取数据库连接");
                System.out.println("   - clearCache(): 清理缓存");
                System.out.println("   - flushStatements(): 刷新批量语句");
                
                System.out.println("\n5. 配置信息");
                System.out.println("   - 配置对象: " + session.getConfiguration());
                System.out.println("   - 自动提交: " + session.getConnection().getAutoCommit());
            }
            
        } catch (Exception e) {
            System.err.println("SqlSession方法演示失败: " + e.getMessage());
        }
        
        System.out.println("\n=== SqlSession核心方法演示完成 ===\n");
    }
    
    /**
     * 创建SqlSessionFactory
     */
    private static SqlSessionFactory createSqlSessionFactory() throws IOException {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        return new SqlSessionFactoryBuilder().build(inputStream);
    }
    
    /**
     * 创建模拟Transaction
     */
    private static Transaction createMockTransaction() {
        // 这里返回一个模拟的Transaction对象
        // 实际使用时需要配置真实的数据源
        return new JdbcTransaction(null, null, false);
    }
    
    /**
     * 演示SqlSession的线程安全性
     */
    public static void demonstrateThreadSafety() {
        System.out.println("=== 演示SqlSession的线程安全性 ===\n");
        
        System.out.println("重要提示：");
        System.out.println("- SqlSession不是线程安全的");
        System.out.println("- 每个线程应该使用独立的SqlSession");
        System.out.println("- 不要在多线程间共享SqlSession实例");
        
        System.out.println("\n正确的使用方式：");
        System.out.println("1. 每个请求创建新的SqlSession");
        System.out.println("2. 使用try-with-resources确保资源释放");
        System.out.println("3. 在方法级别管理SqlSession生命周期");
        
        System.out.println("\n错误的使用方式：");
        System.out.println("1. 将SqlSession作为类的成员变量");
        System.out.println("2. 在多线程间共享SqlSession");
        System.out.println("3. 忘记关闭SqlSession导致资源泄漏");
        
        System.out.println("\n=== 线程安全性说明完成 ===\n");
    }
    
    /**
     * 演示SqlSession的性能优化
     */
    public static void demonstratePerformanceOptimization() {
        System.out.println("=== 演示SqlSession的性能优化 ===\n");
        
        System.out.println("1. 连接池优化");
        System.out.println("   - 使用连接池管理数据库连接");
        System.out.println("   - 避免频繁创建和关闭连接");
        System.out.println("   - 合理配置连接池参数");
        
        System.out.println("\n2. 缓存优化");
        System.out.println("   - 启用二级缓存");
        System.out.println("   - 合理使用一级缓存");
        System.out.println("   - 避免缓存穿透");
        
        System.out.println("\n3. 执行器选择");
        System.out.println("   - 单次操作使用SimpleExecutor");
        System.out.println("   - 重复SQL使用ReuseExecutor");
        System.out.println("   - 批量操作使用BatchExecutor");
        
        System.out.println("\n4. 事务优化");
        System.out.println("   - 合理控制事务范围");
        System.out.println("   - 避免长时间事务");
        System.out.println("   - 及时提交或回滚事务");
        
        System.out.println("\n5. 资源管理");
        System.out.println("   - 及时关闭SqlSession");
        System.out.println("   - 使用try-with-resources");
        System.out.println("   - 避免资源泄漏");
        
        System.out.println("\n=== 性能优化说明完成 ===\n");
    }
}
