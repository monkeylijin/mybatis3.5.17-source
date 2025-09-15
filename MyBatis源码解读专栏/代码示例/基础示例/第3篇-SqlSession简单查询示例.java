package com.example.mybatis.learning;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;

/**
 * 第3篇：SqlSession简单查询示例
 * 
 * 这个示例展示了SqlSession的基本使用方式：
 * 1. 创建SqlSessionFactory
 * 2. 创建SqlSession
 * 3. 执行查询操作
 * 4. 管理事务
 * 5. 关闭资源
 * 
 * 通过这个示例，我们可以跟踪SqlSession的完整执行流程
 */
public class SqlSessionSimpleQueryExample {
    
    public static void main(String[] args) {
        try {
            // 演示基本的SqlSession使用流程
            demonstrateBasicUsage();
            
            // 演示不同的事务模式
            demonstrateTransactionModes();
            
            // 演示资源管理
            demonstrateResourceManagement();
            
        } catch (Exception e) {
            System.err.println("示例执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 演示基本的SqlSession使用流程
     */
    private static void demonstrateBasicUsage() {
        System.out.println("=== 演示基本的SqlSession使用流程 ===\n");
        
        try {
            // 第一步：创建SqlSessionFactory
            System.out.println("第一步：创建SqlSessionFactory");
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            System.out.println("SqlSessionFactory创建完成");
            
            // 第二步：创建SqlSession
            System.out.println("\n第二步：创建SqlSession");
            SqlSession session = sqlSessionFactory.openSession();
            System.out.println("SqlSession创建完成");
            
            // 第三步：获取配置信息
            System.out.println("\n第三步：获取配置信息");
            Configuration configuration = session.getConfiguration();
            System.out.println("配置对象: " + configuration);
            
            Connection connection = session.getConnection();
            System.out.println("数据库连接: " + connection);
            System.out.println("自动提交状态: " + connection.getAutoCommit());
            
            // 第四步：执行查询操作（模拟）
            System.out.println("\n第四步：执行查询操作");
            System.out.println("准备执行查询: selectUser");
            System.out.println("查询参数: userId = 1");
            
            // 模拟查询结果
            User user = createMockUser();
            System.out.println("查询结果: " + user);
            
            // 第五步：关闭SqlSession
            System.out.println("\n第五步：关闭SqlSession");
            session.close();
            System.out.println("SqlSession已关闭");
            
        } catch (IOException e) {
            System.err.println("配置文件加载失败: " + e.getMessage());
        }
        
        System.out.println("\n=== 基本使用流程演示完成 ===\n");
    }
    
    /**
     * 演示不同的事务模式
     */
    private static void demonstrateTransactionModes() {
        System.out.println("=== 演示不同的事务模式 ===\n");
        
        try {
            SqlSessionFactory sqlSessionFactory = createSqlSessionFactory();
            
            // 1. 自动提交模式
            System.out.println("1. 自动提交模式");
            try (SqlSession session = sqlSessionFactory.openSession(true)) {
                System.out.println("   - 创建自动提交的SqlSession");
                Connection connection = session.getConnection();
                System.out.println("   - 自动提交状态: " + connection.getAutoCommit());
                
                System.out.println("   - 执行更新操作");
                System.out.println("   - 自动提交事务");
                System.out.println("   - 无需手动调用commit()");
            }
            
            // 2. 手动提交模式
            System.out.println("\n2. 手动提交模式");
            try (SqlSession session = sqlSessionFactory.openSession(false)) {
                System.out.println("   - 创建手动提交的SqlSession");
                Connection connection = session.getConnection();
                System.out.println("   - 自动提交状态: " + connection.getAutoCommit());
                
                System.out.println("   - 执行更新操作");
                System.out.println("   - 手动提交事务");
                session.commit();
                System.out.println("   - 事务提交完成");
            }
            
            // 3. 默认模式
            System.out.println("\n3. 默认模式");
            try (SqlSession session = sqlSessionFactory.openSession()) {
                System.out.println("   - 创建默认模式的SqlSession");
                Connection connection = session.getConnection();
                System.out.println("   - 自动提交状态: " + connection.getAutoCommit());
                
                System.out.println("   - 执行更新操作");
                System.out.println("   - 手动提交事务");
                session.commit();
                System.out.println("   - 事务提交完成");
            }
            
        } catch (Exception e) {
            System.err.println("事务模式演示失败: " + e.getMessage());
        }
        
        System.out.println("\n=== 事务模式演示完成 ===\n");
    }
    
    /**
     * 演示资源管理
     */
    private static void demonstrateResourceManagement() {
        System.out.println("=== 演示资源管理 ===\n");
        
        try {
            SqlSessionFactory sqlSessionFactory = createSqlSessionFactory();
            
            // 1. 手动资源管理
            System.out.println("1. 手动资源管理");
            SqlSession session = sqlSessionFactory.openSession();
            try {
                System.out.println("   - 手动创建SqlSession");
                System.out.println("   - 执行业务操作");
                System.out.println("   - 提交事务");
                session.commit();
            } catch (Exception e) {
                System.out.println("   - 发生异常，回滚事务");
                session.rollback();
            } finally {
                System.out.println("   - 手动关闭SqlSession");
                session.close();
            }
            
            // 2. 自动资源管理（推荐）
            System.out.println("\n2. 自动资源管理（推荐方式）");
            try (SqlSession session2 = sqlSessionFactory.openSession()) {
                System.out.println("   - 使用try-with-resources自动管理");
                System.out.println("   - 执行业务操作");
                System.out.println("   - 提交事务");
                session2.commit();
                System.out.println("   - 自动关闭SqlSession");
            } catch (Exception e) {
                System.err.println("   - 发生异常: " + e.getMessage());
            }
            
            // 3. 多资源管理
            System.out.println("\n3. 多资源管理");
            try (SqlSession session3 = sqlSessionFactory.openSession();
                 SqlSession session4 = sqlSessionFactory.openSession()) {
                
                System.out.println("   - 创建多个SqlSession");
                System.out.println("   - 分别执行业务操作");
                System.out.println("   - 提交事务");
                session3.commit();
                session4.commit();
                System.out.println("   - 自动关闭所有SqlSession");
            } catch (Exception e) {
                System.err.println("   - 发生异常: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("资源管理演示失败: " + e.getMessage());
        }
        
        System.out.println("\n=== 资源管理演示完成 ===\n");
    }
    
    /**
     * 演示SqlSession的核心方法
     */
    public static void demonstrateCoreMethods() {
        System.out.println("=== 演示SqlSession的核心方法 ===\n");
        
        try {
            SqlSessionFactory sqlSessionFactory = createSqlSessionFactory();
            
            try (SqlSession session = sqlSessionFactory.openSession()) {
                System.out.println("1. 查询方法");
                System.out.println("   - selectOne(String statement): 查询单个对象");
                System.out.println("   - selectOne(String statement, Object parameter): 带参数查询单个对象");
                System.out.println("   - selectList(String statement): 查询对象列表");
                System.out.println("   - selectList(String statement, Object parameter): 带参数查询对象列表");
                
                System.out.println("\n2. 更新方法");
                System.out.println("   - insert(String statement): 插入操作");
                System.out.println("   - insert(String statement, Object parameter): 带参数插入操作");
                System.out.println("   - update(String statement): 更新操作");
                System.out.println("   - update(String statement, Object parameter): 带参数更新操作");
                System.out.println("   - delete(String statement): 删除操作");
                System.out.println("   - delete(String statement, Object parameter): 带参数删除操作");
                
                System.out.println("\n3. 事务方法");
                System.out.println("   - commit(): 提交事务");
                System.out.println("   - commit(boolean force): 强制提交事务");
                System.out.println("   - rollback(): 回滚事务");
                System.out.println("   - rollback(boolean force): 强制回滚事务");
                
                System.out.println("\n4. 其他方法");
                System.out.println("   - getMapper(Class<T> type): 获取Mapper接口");
                System.out.println("   - getConnection(): 获取数据库连接");
                System.out.println("   - getConfiguration(): 获取配置对象");
                System.out.println("   - clearCache(): 清理缓存");
                System.out.println("   - flushStatements(): 刷新批量语句");
                
                System.out.println("\n5. 状态检查");
                System.out.println("   - 配置对象: " + session.getConfiguration());
                System.out.println("   - 数据库连接: " + session.getConnection());
                System.out.println("   - 自动提交状态: " + session.getConnection().getAutoCommit());
            }
            
        } catch (Exception e) {
            System.err.println("核心方法演示失败: " + e.getMessage());
        }
        
        System.out.println("\n=== 核心方法演示完成 ===\n");
    }
    
    /**
     * 演示SqlSession的异常处理
     */
    public static void demonstrateExceptionHandling() {
        System.out.println("=== 演示SqlSession的异常处理 ===\n");
        
        try {
            SqlSessionFactory sqlSessionFactory = createSqlSessionFactory();
            
            // 1. 正常情况
            System.out.println("1. 正常情况");
            try (SqlSession session = sqlSessionFactory.openSession()) {
                System.out.println("   - 执行正常操作");
                System.out.println("   - 提交事务");
                session.commit();
                System.out.println("   - 操作成功");
            }
            
            // 2. 异常情况
            System.out.println("\n2. 异常情况");
            try (SqlSession session = sqlSessionFactory.openSession()) {
                System.out.println("   - 执行操作");
                System.out.println("   - 模拟发生异常");
                throw new RuntimeException("模拟业务异常");
            } catch (Exception e) {
                System.out.println("   - 捕获异常: " + e.getMessage());
                System.out.println("   - 事务自动回滚");
            }
            
            // 3. 手动异常处理
            System.out.println("\n3. 手动异常处理");
            SqlSession session = sqlSessionFactory.openSession();
            try {
                System.out.println("   - 执行操作");
                System.out.println("   - 模拟发生异常");
                throw new RuntimeException("模拟业务异常");
            } catch (Exception e) {
                System.out.println("   - 捕获异常: " + e.getMessage());
                System.out.println("   - 手动回滚事务");
                session.rollback();
            } finally {
                System.out.println("   - 关闭SqlSession");
                session.close();
            }
            
        } catch (Exception e) {
            System.err.println("异常处理演示失败: " + e.getMessage());
        }
        
        System.out.println("\n=== 异常处理演示完成 ===\n");
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
     * 创建模拟用户对象
     */
    private static User createMockUser() {
        return new User(1, "张三", "zhangsan@example.com");
    }
    
    /**
     * 简单的用户实体类
     */
    static class User {
        private Integer id;
        private String name;
        private String email;
        
        public User() {}
        
        public User(Integer id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
        
        // getter 和 setter 方法
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        @Override
        public String toString() {
            return "User{id=" + id + ", name='" + name + "', email='" + email + "'}";
        }
    }
}
