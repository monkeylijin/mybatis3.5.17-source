package com.example.mybatis.learning;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.*;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.parameter.DefaultParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 第1篇：MyBatis 架构理解实践案例
 * 
 * 这个案例通过手动创建 MyBatis 的各个组件，帮助理解：
 * 1. 各个组件的职责
 * 2. 组件之间的协作关系
 * 3. 数据流转过程
 * 4. 架构设计的优势
 */
public class ArchitectureUnderstandingExample {
    
    public static void main(String[] args) {
        try {
            // 演示 MyBatis 架构的各个层次
            demonstrateArchitectureLayers();
            
            // 演示组件协作关系
            demonstrateComponentCollaboration();
            
            // 演示数据流转过程
            demonstrateDataFlow();
            
        } catch (Exception e) {
            System.err.println("演示过程中出现错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 演示 MyBatis 架构的各个层次
     */
    private static void demonstrateArchitectureLayers() {
        System.out.println("=== 演示 MyBatis 架构的各个层次 ===\n");
        
        // 1. 基础支持层 - 配置管理
        System.out.println("1. 基础支持层 - Configuration 配置管理");
        Configuration configuration = createConfiguration();
        System.out.println("   - 配置对象创建完成");
        System.out.println("   - 包含数据源、事务、类型处理器等配置");
        
        // 2. 核心处理层 - 执行器
        System.out.println("\n2. 核心处理层 - Executor 执行器");
        Executor executor = createExecutor(configuration);
        System.out.println("   - 执行器创建完成");
        System.out.println("   - 负责 SQL 执行和缓存管理");
        
        // 3. 接口层 - 会话管理
        System.out.println("\n3. 接口层 - SqlSession 会话管理");
        SqlSession sqlSession = createSqlSession(configuration, executor);
        System.out.println("   - 会话创建完成");
        System.out.println("   - 提供 CRUD 操作接口");
        
        System.out.println("\n=== 架构层次演示完成 ===\n");
    }
    
    /**
     * 演示组件协作关系
     */
    private static void demonstrateComponentCollaboration() {
        System.out.println("=== 演示组件协作关系 ===\n");
        
        // 创建配置
        Configuration configuration = createConfiguration();
        
        // 创建执行器
        Executor executor = createExecutor(configuration);
        
        // 创建会话
        SqlSession sqlSession = createSqlSession(configuration, executor);
        
        // 演示组件协作
        System.out.println("组件协作关系：");
        System.out.println("SqlSession → Executor → StatementHandler → ParameterHandler + ResultSetHandler");
        System.out.println("Configuration 为所有组件提供配置支持");
        
        // 关闭会话
        try {
            sqlSession.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("\n=== 组件协作关系演示完成 ===\n");
    }
    
    /**
     * 演示数据流转过程
     */
    private static void demonstrateDataFlow() {
        System.out.println("=== 演示数据流转过程 ===\n");
        
        // 模拟一个查询请求的数据流转
        System.out.println("数据流转过程：");
        System.out.println("1. 用户请求 → SqlSession.selectOne()");
        System.out.println("2. SqlSession → Executor.query()");
        System.out.println("3. Executor → StatementHandler.prepare()");
        System.out.println("4. StatementHandler → ParameterHandler.setParameters()");
        System.out.println("5. StatementHandler → 执行 SQL");
        System.out.println("6. 数据库 → ResultSetHandler.handleResultSets()");
        System.out.println("7. ResultSetHandler → Executor");
        System.out.println("8. Executor → SqlSession");
        System.out.println("9. SqlSession → 用户");
        
        System.out.println("\n=== 数据流转过程演示完成 ===\n");
    }
    
    /**
     * 创建配置对象
     */
    private static Configuration createConfiguration() {
        Configuration configuration = new Configuration();
        
        // 设置基本配置
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(true);
        configuration.setLazyLoadingEnabled(true);
        configuration.setMultipleResultSetsEnabled(true);
        configuration.setUseGeneratedKeys(true);
        configuration.setDefaultExecutorType(ExecutorType.SIMPLE);
        
        return configuration;
    }
    
    /**
     * 创建执行器
     */
    private static Executor createExecutor(Configuration configuration) {
        // 创建简单执行器
        SimpleExecutor simpleExecutor = new SimpleExecutor(configuration, null);
        
        // 包装缓存执行器
        CachingExecutor cachingExecutor = new CachingExecutor(simpleExecutor);
        
        return cachingExecutor;
    }
    
    /**
     * 创建会话
     */
    private static SqlSession createSqlSession(Configuration configuration, Executor executor) {
        return new DefaultSqlSession(configuration, executor, false);
    }
    
    /**
     * 演示组件职责分工
     */
    public static void demonstrateComponentResponsibilities() {
        System.out.println("=== 组件职责分工 ===\n");
        
        System.out.println("1. SqlSession 职责：");
        System.out.println("   - 提供 CRUD 操作接口");
        System.out.println("   - 管理事务");
        System.out.println("   - 获取 Mapper 接口");
        System.out.println("   - 管理会话生命周期");
        
        System.out.println("\n2. Executor 职责：");
        System.out.println("   - 执行 SQL 语句");
        System.out.println("   - 管理一级缓存");
        System.out.println("   - 管理事务");
        System.out.println("   - 创建 StatementHandler");
        
        System.out.println("\n3. StatementHandler 职责：");
        System.out.println("   - 准备 Statement");
        System.out.println("   - 参数化处理");
        System.out.println("   - 执行 SQL");
        System.out.println("   - 处理结果");
        
        System.out.println("\n4. ParameterHandler 职责：");
        System.out.println("   - 处理 SQL 参数");
        System.out.println("   - 类型转换");
        System.out.println("   - 参数绑定");
        
        System.out.println("\n5. ResultSetHandler 职责：");
        System.out.println("   - 处理查询结果集");
        System.out.println("   - 结果映射");
        System.out.println("   - 对象创建");
        System.out.println("   - 延迟加载");
        
        System.out.println("\n6. Configuration 职责：");
        System.out.println("   - 管理全局配置");
        System.out.println("   - 注册各种组件");
        System.out.println("   - 提供配置支持");
        
        System.out.println("\n=== 组件职责分工演示完成 ===\n");
    }
    
    /**
     * 演示设计模式应用
     */
    public static void demonstrateDesignPatterns() {
        System.out.println("=== 设计模式应用 ===\n");
        
        System.out.println("1. 工厂模式：");
        System.out.println("   - SqlSessionFactory 创建 SqlSession");
        System.out.println("   - ObjectFactory 创建对象");
        System.out.println("   - TypeHandlerFactory 创建类型处理器");
        
        System.out.println("\n2. 代理模式：");
        System.out.println("   - Mapper 接口动态代理");
        System.out.println("   - 插件系统动态代理");
        
        System.out.println("\n3. 建造者模式：");
        System.out.println("   - Configuration.Builder");
        System.out.println("   - MappedStatement.Builder");
        
        System.out.println("\n4. 装饰器模式：");
        System.out.println("   - CachingExecutor 装饰 SimpleExecutor");
        System.out.println("   - 各种 Cache 装饰器");
        
        System.out.println("\n5. 模板方法模式：");
        System.out.println("   - BaseExecutor 定义执行模板");
        System.out.println("   - BaseStatementHandler 定义处理模板");
        
        System.out.println("\n6. 策略模式：");
        System.out.println("   - 不同类型的 Executor");
        System.out.println("   - 不同类型的 StatementHandler");
        
        System.out.println("\n=== 设计模式应用演示完成 ===\n");
    }
    
    /**
     * 演示架构优势
     */
    public static void demonstrateArchitectureAdvantages() {
        System.out.println("=== 架构优势 ===\n");
        
        System.out.println("1. 分层设计优势：");
        System.out.println("   - 职责清晰，易于理解");
        System.out.println("   - 便于维护和扩展");
        System.out.println("   - 降低耦合度");
        
        System.out.println("\n2. 接口抽象优势：");
        System.out.println("   - 便于扩展和测试");
        System.out.println("   - 支持多种实现");
        System.out.println("   - 提高代码复用性");
        
        System.out.println("\n3. 设计模式优势：");
        System.out.println("   - 提高代码质量");
        System.out.println("   - 增强可维护性");
        System.out.println("   - 便于理解和学习");
        
        System.out.println("\n4. 性能优化优势：");
        System.out.println("   - 连接池管理");
        System.out.println("   - 多级缓存");
        System.out.println("   - 预编译语句");
        System.out.println("   - 反射优化");
        
        System.out.println("\n=== 架构优势演示完成 ===\n");
    }
}
