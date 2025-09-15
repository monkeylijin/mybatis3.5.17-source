package com.example.mybatis.learning;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * 第1篇：MyBatis 简单查询示例
 * 
 * 这个示例展示了 MyBatis 的基本使用流程：
 * 1. 创建 SqlSessionFactory
 * 2. 创建 SqlSession
 * 3. 获取 Mapper
 * 4. 执行查询
 * 
 * 通过这个示例，我们可以跟踪 MyBatis 的完整执行流程
 */
public class SimpleQueryExample {
    
    public static void main(String[] args) {
        try {
            // 第一步：创建 SqlSessionFactory
            System.out.println("=== 第一步：创建 SqlSessionFactory ===");
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            System.out.println("SqlSessionFactory 创建完成");
            
            // 第二步：创建 SqlSession
            System.out.println("\n=== 第二步：创建 SqlSession ===");
            SqlSession session = sqlSessionFactory.openSession();
            System.out.println("SqlSession 创建完成");
            
            // 第三步：获取 Mapper（这里我们直接使用 SqlSession 的方法）
            System.out.println("\n=== 第三步：执行查询 ===");
            // 模拟查询用户
            String statement = "com.example.UserMapper.selectById";
            Integer userId = 1;
            
            // 第四步：执行查询
            User user = session.selectOne(statement, userId);
            System.out.println("查询结果: " + user);
            
            // 关闭会话
            session.close();
            System.out.println("\n=== SqlSession 已关闭 ===");
            
        } catch (IOException e) {
            System.err.println("配置文件加载失败: " + e.getMessage());
        }
    }
    
    // 简单的用户实体类
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
