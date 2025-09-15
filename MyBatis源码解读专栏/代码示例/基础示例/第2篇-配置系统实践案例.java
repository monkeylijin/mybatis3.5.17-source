package com.example.mybatis.learning;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 第2篇：配置系统实践案例
 * 
 * 这个案例通过实际代码演示：
 * 1. 配置解析的完整流程
 * 2. 不同配置方式的对比
 * 3. 配置验证和错误处理
 * 4. 自定义配置扩展
 * 5. 配置性能优化
 */
public class ConfigurationSystemExample {
    
    public static void main(String[] args) {
        try {
            // 演示配置解析的完整流程
            demonstrateConfigurationParsing();
            
            // 演示不同配置方式的对比
            demonstrateConfigurationMethods();
            
            // 演示配置验证和错误处理
            demonstrateConfigurationValidation();
            
            // 演示自定义配置扩展
            demonstrateCustomConfiguration();
            
            // 演示配置性能优化
            demonstratePerformanceOptimization();
            
        } catch (Exception e) {
            System.err.println("演示过程中出现错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 演示配置解析的完整流程
     */
    private static void demonstrateConfigurationParsing() {
        System.out.println("=== 演示配置解析的完整流程 ===\n");
        
        try {
            // 第1步：创建 SqlSessionFactoryBuilder
            System.out.println("第1步：创建 SqlSessionFactoryBuilder");
            SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
            
            // 第2步：加载配置文件
            System.out.println("第2步：加载配置文件");
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
            System.out.println("   - 配置文件加载完成: " + resource);
        
            // 第3步：创建 XMLConfigBuilder
            System.out.println("第3步：创建 XMLConfigBuilder");
        XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, "development", null);
            System.out.println("   - XMLConfigBuilder 创建完成");
            
            // 第4步：解析配置文件
            System.out.println("第4步：解析配置文件");
            Configuration configuration = parser.parse();
            System.out.println("   - 配置解析完成");
            System.out.println("   - 配置对象: " + configuration);
            
            // 第5步：验证配置
            System.out.println("第5步：验证配置");
            configuration.validate();
            System.out.println("   - 配置验证通过");
            
            // 第6步：创建 SqlSessionFactory
            System.out.println("第6步：创建 SqlSessionFactory");
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
            System.out.println("   - SqlSessionFactory 创建完成");
            
            // 第7步：使用配置
            System.out.println("第7步：使用配置");
            System.out.println("   - 环境配置: " + configuration.getEnvironment());
            System.out.println("   - 数据源: " + configuration.getEnvironment().getDataSource());
            System.out.println("   - 事务工厂: " + configuration.getEnvironment().getTransactionFactory());
            System.out.println("   - 缓存启用: " + configuration.isCacheEnabled());
            System.out.println("   - 懒加载启用: " + configuration.isLazyLoadingEnabled());
            
        } catch (IOException e) {
            System.err.println("配置文件加载失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("配置解析失败: " + e.getMessage());
        }
        
        System.out.println("\n=== 配置解析流程演示完成 ===\n");
    }
    
    /**
     * 演示不同配置方式的对比
     */
    private static void demonstrateConfigurationMethods() {
        System.out.println("=== 演示不同配置方式的对比 ===\n");
        
        try {
            // 1. XML 配置方式
            System.out.println("1. XML 配置方式");
            demonstrateXmlConfiguration();
            
            // 2. 注解配置方式
            System.out.println("\n2. 注解配置方式");
            demonstrateAnnotationConfiguration();
            
            // 3. 混合配置方式
            System.out.println("\n3. 混合配置方式");
            demonstrateMixedConfiguration();
            
            // 4. 编程式配置方式
            System.out.println("\n4. 编程式配置方式");
            demonstrateProgrammaticConfiguration();
            
        } catch (Exception e) {
            System.err.println("配置方式演示失败: " + e.getMessage());
        }
        
        System.out.println("\n=== 配置方式对比演示完成 ===\n");
    }
    
    /**
     * 演示 XML 配置方式
     */
    private static void demonstrateXmlConfiguration() {
        System.out.println("   - 使用 XML 配置文件");
        System.out.println("   - 优点：配置清晰、易于维护、支持复杂配置");
        System.out.println("   - 缺点：文件较多、配置冗长");
        
        try {
            // 模拟 XML 配置解析
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            XMLConfigBuilder parser = new XMLConfigBuilder(inputStream);
            Configuration configuration = parser.parse();
            
            System.out.println("   - XML 配置解析成功");
            System.out.println("   - 配置项数量: " + getConfigurationItemCount(configuration));
            
        } catch (Exception e) {
            System.out.println("   - XML 配置解析失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示注解配置方式
     */
    private static void demonstrateAnnotationConfiguration() {
        System.out.println("   - 使用注解配置");
        System.out.println("   - 优点：配置简洁、类型安全、IDE 支持");
        System.out.println("   - 缺点：配置分散、不支持复杂配置");
        
        try {
            // 模拟注解配置解析
            Configuration configuration = new Configuration();
            MapperAnnotationBuilder annotationParser = new MapperAnnotationBuilder(configuration, UserMapper.class);
            annotationParser.parse();
            
            System.out.println("   - 注解配置解析成功");
            System.out.println("   - Mapper 接口: " + UserMapper.class.getName());
            
        } catch (Exception e) {
            System.out.println("   - 注解配置解析失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示混合配置方式
     */
    private static void demonstrateMixedConfiguration() {
        System.out.println("   - 使用混合配置（XML + 注解）");
        System.out.println("   - 优点：灵活性强、配置清晰、功能完整");
        System.out.println("   - 缺点：配置复杂、需要协调");
        
        try {
            // 模拟混合配置解析
            Configuration configuration = new Configuration();
            
            // 先解析 XML 配置
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
            XMLConfigBuilder xmlParser = new XMLConfigBuilder(inputStream);
            xmlParser.parse();
            
            // 再解析注解配置
            MapperAnnotationBuilder annotationParser = new MapperAnnotationBuilder(configuration, UserMapper.class);
            annotationParser.parse();
            
            System.out.println("   - 混合配置解析成功");
            System.out.println("   - XML 配置项: " + getConfigurationItemCount(configuration));
            System.out.println("   - 注解配置项: 已解析 UserMapper 接口");
            
        } catch (Exception e) {
            System.out.println("   - 混合配置解析失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示编程式配置方式
     */
    private static void demonstrateProgrammaticConfiguration() {
        System.out.println("   - 使用编程式配置");
        System.out.println("   - 优点：动态配置、灵活性强、易于测试");
        System.out.println("   - 缺点：代码冗长、配置分散");
        
        try {
            // 创建配置对象
            Configuration configuration = new Configuration();
            
            // 设置基本配置
            configuration.setCacheEnabled(true);
            configuration.setLazyLoadingEnabled(false);
            configuration.setAggressiveLazyLoading(false);
            configuration.setMultipleResultSetsEnabled(true);
            configuration.setUseColumnLabel(true);
            configuration.setUseGeneratedKeys(false);
            configuration.setAutoMappingBehavior(org.apache.ibatis.session.AutoMappingBehavior.PARTIAL);
            configuration.setDefaultExecutorType(org.apache.ibatis.session.ExecutorType.SIMPLE);
            configuration.setDefaultStatementTimeout(25);
            configuration.setDefaultFetchSize(100);
            configuration.setSafeRowBoundsEnabled(false);
            configuration.setMapUnderscoreToCamelCase(false);
            configuration.setLocalCacheScope(org.apache.ibatis.session.LocalCacheScope.SESSION);
            configuration.setJdbcTypeForNull(org.apache.ibatis.type.JdbcType.OTHER);
            configuration.setLazyLoadTriggerMethods(java.util.Arrays.asList("equals", "clone", "hashCode", "toString"));
            configuration.setDefaultScriptingLanguage(org.apache.ibatis.scripting.xmltags.XMLLanguageDriver.class);
            configuration.setDefaultEnumTypeHandler(org.apache.ibatis.type.EnumTypeHandler.class);
            configuration.setCallSettersOnNulls(false);
            configuration.setReturnInstanceForEmptyRow(false);
            
            System.out.println("   - 编程式配置创建成功");
            System.out.println("   - 缓存启用: " + configuration.isCacheEnabled());
            System.out.println("   - 懒加载启用: " + configuration.isLazyLoadingEnabled());
            System.out.println("   - 执行器类型: " + configuration.getDefaultExecutorType());
            System.out.println("   - 语句超时: " + configuration.getDefaultStatementTimeout());
            
        } catch (Exception e) {
            System.out.println("   - 编程式配置创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示配置验证和错误处理
     */
    private static void demonstrateConfigurationValidation() {
        System.out.println("=== 演示配置验证和错误处理 ===\n");
        
        // 1. 正确的配置
        System.out.println("1. 正确的配置");
        demonstrateValidConfiguration();
        
        // 2. 错误的配置
        System.out.println("\n2. 错误的配置");
        demonstrateInvalidConfiguration();
        
        // 3. 配置验证
        System.out.println("\n3. 配置验证");
        demonstrateConfigurationValidation();
        
        // 4. 错误处理
        System.out.println("\n4. 错误处理");
        demonstrateErrorHandling();
        
        System.out.println("\n=== 配置验证和错误处理演示完成 ===\n");
    }
    
    /**
     * 演示正确的配置
     */
    private static void demonstrateValidConfiguration() {
        System.out.println("   - 创建正确的配置");
        
        try {
            Configuration configuration = new Configuration();
            
            // 设置正确的配置项
            configuration.setCacheEnabled(true);
            configuration.setLazyLoadingEnabled(false);
            configuration.setDefaultExecutorType(org.apache.ibatis.session.ExecutorType.SIMPLE);
            configuration.setAutoMappingBehavior(org.apache.ibatis.session.AutoMappingBehavior.PARTIAL);
            
            // 验证配置
            configuration.validate();
            
            System.out.println("   - 配置验证通过");
            System.out.println("   - 缓存启用: " + configuration.isCacheEnabled());
            System.out.println("   - 懒加载启用: " + configuration.isLazyLoadingEnabled());
            
        } catch (Exception e) {
            System.out.println("   - 配置验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示错误的配置
     */
    private static void demonstrateInvalidConfiguration() {
        System.out.println("   - 创建错误的配置");
        
        try {
            Configuration configuration = new Configuration();
            
            // 设置错误的配置项
            configuration.setCacheEnabled(true);
            configuration.setLazyLoadingEnabled(false);
            // 故意设置错误的执行器类型
            // configuration.setDefaultExecutorType(null); // 这会导致错误
            
            // 验证配置
            configuration.validate();
            
            System.out.println("   - 配置验证通过");
            
        } catch (Exception e) {
            System.out.println("   - 配置验证失败: " + e.getMessage());
            System.out.println("   - 错误类型: " + e.getClass().getSimpleName());
        }
    }
    
    /**
     * 演示配置验证
     */
    private static void demonstrateConfigurationValidation() {
        System.out.println("   - 配置验证过程");
        
        try {
            Configuration configuration = new Configuration();
            
            // 设置配置项
            configuration.setCacheEnabled(true);
            configuration.setLazyLoadingEnabled(false);
            configuration.setDefaultExecutorType(org.apache.ibatis.session.ExecutorType.SIMPLE);
            
            // 执行验证
            System.out.println("   - 开始配置验证");
            configuration.validate();
            System.out.println("   - 配置验证完成");
            
            // 检查验证结果
            System.out.println("   - 验证结果: 通过");
            System.out.println("   - 配置项数量: " + getConfigurationItemCount(configuration));
            
        } catch (Exception e) {
            System.out.println("   - 验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示错误处理
     */
    private static void demonstrateErrorHandling() {
        System.out.println("   - 错误处理机制");
        
        try {
            // 模拟配置错误
            Configuration configuration = new Configuration();
            
            // 设置一些可能导致错误的配置
            configuration.setCacheEnabled(true);
            configuration.setLazyLoadingEnabled(false);
            
            // 故意触发错误
            try {
                configuration.validate();
                System.out.println("   - 配置验证通过");
            } catch (Exception e) {
                System.out.println("   - 捕获配置错误: " + e.getMessage());
                System.out.println("   - 错误类型: " + e.getClass().getSimpleName());
                System.out.println("   - 错误堆栈: " + e.getStackTrace()[0]);
            }
            
        } catch (Exception e) {
            System.out.println("   - 错误处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示自定义配置扩展
     */
    private static void demonstrateCustomConfiguration() {
        System.out.println("=== 演示自定义配置扩展 ===\n");
        
        // 1. 自定义配置类
        System.out.println("1. 自定义配置类");
        demonstrateCustomConfigurationClass();
        
        // 2. 自定义配置解析器
        System.out.println("\n2. 自定义配置解析器");
        demonstrateCustomConfigurationParser();
        
        // 3. 自定义配置验证器
        System.out.println("\n3. 自定义配置验证器");
        demonstrateCustomConfigurationValidator();
        
        // 4. 自定义配置扩展点
        System.out.println("\n4. 自定义配置扩展点");
        demonstrateCustomConfigurationExtension();
        
        System.out.println("\n=== 自定义配置扩展演示完成 ===\n");
    }
    
    /**
     * 演示自定义配置类
     */
    private static void demonstrateCustomConfigurationClass() {
        System.out.println("   - 创建自定义配置类");
        
        try {
            // 继承 Configuration 类
            CustomConfiguration customConfig = new CustomConfiguration();
            
            // 设置自定义配置项
            customConfig.setCustomProperty("customValue");
            customConfig.setCacheEnabled(true);
            customConfig.setLazyLoadingEnabled(false);
            
            System.out.println("   - 自定义配置类创建成功");
            System.out.println("   - 自定义属性: " + customConfig.getCustomProperty());
            System.out.println("   - 缓存启用: " + customConfig.isCacheEnabled());
            
        } catch (Exception e) {
            System.out.println("   - 自定义配置类创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示自定义配置解析器
     */
    private static void demonstrateCustomConfigurationParser() {
        System.out.println("   - 创建自定义配置解析器");
        
        try {
            // 创建自定义配置解析器
            CustomConfigurationParser parser = new CustomConfigurationParser();
            
            // 解析自定义配置
            Properties customProps = new Properties();
            customProps.setProperty("custom.setting1", "value1");
            customProps.setProperty("custom.setting2", "value2");
            
            Configuration configuration = parser.parseCustomConfiguration(customProps);
            
            System.out.println("   - 自定义配置解析成功");
            System.out.println("   - 解析的配置项数量: " + getConfigurationItemCount(configuration));
            
        } catch (Exception e) {
            System.out.println("   - 自定义配置解析失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示自定义配置验证器
     */
    private static void demonstrateCustomConfigurationValidator() {
        System.out.println("   - 创建自定义配置验证器");
        
        try {
            // 创建自定义配置验证器
            CustomConfigurationValidator validator = new CustomConfigurationValidator();
            
            // 验证配置
            Configuration configuration = new Configuration();
            configuration.setCacheEnabled(true);
            configuration.setLazyLoadingEnabled(false);
            
            boolean isValid = validator.validate(configuration);
            
            System.out.println("   - 自定义配置验证完成");
            System.out.println("   - 验证结果: " + (isValid ? "通过" : "失败"));
            
        } catch (Exception e) {
            System.out.println("   - 自定义配置验证失败: " + e.getMessage());
    }
}

/**
     * 演示自定义配置扩展点
     */
    private static void demonstrateCustomConfigurationExtension() {
        System.out.println("   - 使用自定义配置扩展点");
        
        try {
            // 创建配置对象
            Configuration configuration = new Configuration();
            
            // 使用扩展点
            configuration.setVariables(new Properties());
            configuration.getVariables().setProperty("custom.extension", "enabled");
            
            System.out.println("   - 自定义配置扩展点使用成功");
            System.out.println("   - 扩展属性: " + configuration.getVariables().getProperty("custom.extension"));
            
        } catch (Exception e) {
            System.out.println("   - 自定义配置扩展点使用失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示配置性能优化
     */
    private static void demonstratePerformanceOptimization() {
        System.out.println("=== 演示配置性能优化 ===\n");
        
        // 1. 配置缓存
        System.out.println("1. 配置缓存");
        demonstrateConfigurationCache();
        
        // 2. 懒加载
        System.out.println("\n2. 懒加载");
        demonstrateLazyLoading();
        
        // 3. 批量处理
        System.out.println("\n3. 批量处理");
        demonstrateBatchProcessing();
        
        // 4. 内存优化
        System.out.println("\n4. 内存优化");
        demonstrateMemoryOptimization();
        
        System.out.println("\n=== 配置性能优化演示完成 ===\n");
    }
    
    /**
     * 演示配置缓存
     */
    private static void demonstrateConfigurationCache() {
        System.out.println("   - 配置缓存机制");
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 第一次解析
            Configuration configuration1 = createConfiguration();
            long firstParseTime = System.currentTimeMillis() - startTime;
            
            // 第二次解析（模拟缓存）
            startTime = System.currentTimeMillis();
            Configuration configuration2 = createConfiguration();
            long secondParseTime = System.currentTimeMillis() - startTime;
            
            System.out.println("   - 第一次解析时间: " + firstParseTime + "ms");
            System.out.println("   - 第二次解析时间: " + secondParseTime + "ms");
            System.out.println("   - 性能提升: " + ((double)(firstParseTime - secondParseTime) / firstParseTime * 100) + "%");
            
        } catch (Exception e) {
            System.out.println("   - 配置缓存演示失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示懒加载
     */
    private static void demonstrateLazyLoading() {
        System.out.println("   - 懒加载机制");
        
        try {
            Configuration configuration = new Configuration();
            
            // 设置懒加载
            configuration.setLazyLoadingEnabled(true);
            configuration.setAggressiveLazyLoading(false);
            
            System.out.println("   - 懒加载启用: " + configuration.isLazyLoadingEnabled());
            System.out.println("   - 激进懒加载: " + configuration.isAggressiveLazyLoading());
            System.out.println("   - 懒加载触发方法: " + configuration.getLazyLoadTriggerMethods());
            
        } catch (Exception e) {
            System.out.println("   - 懒加载演示失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示批量处理
     */
    private static void demonstrateBatchProcessing() {
        System.out.println("   - 批量处理机制");
        
        try {
            Configuration configuration = new Configuration();
            
            // 设置批量执行器
            configuration.setDefaultExecutorType(org.apache.ibatis.session.ExecutorType.BATCH);
            
            System.out.println("   - 执行器类型: " + configuration.getDefaultExecutorType());
            System.out.println("   - 批量处理启用: " + (configuration.getDefaultExecutorType() == org.apache.ibatis.session.ExecutorType.BATCH));
            
        } catch (Exception e) {
            System.out.println("   - 批量处理演示失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示内存优化
     */
    private static void demonstrateMemoryOptimization() {
        System.out.println("   - 内存优化机制");
        
        try {
            Configuration configuration = new Configuration();
            
            // 设置内存优化相关配置
            configuration.setCacheEnabled(true);
            configuration.setLocalCacheScope(org.apache.ibatis.session.LocalCacheScope.STATEMENT);
            
            System.out.println("   - 缓存启用: " + configuration.isCacheEnabled());
            System.out.println("   - 本地缓存范围: " + configuration.getLocalCacheScope());
            
            // 模拟内存使用情况
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            System.out.println("   - 总内存: " + (totalMemory / 1024 / 1024) + "MB");
            System.out.println("   - 已用内存: " + (usedMemory / 1024 / 1024) + "MB");
            System.out.println("   - 空闲内存: " + (freeMemory / 1024 / 1024) + "MB");
            
        } catch (Exception e) {
            System.out.println("   - 内存优化演示失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取配置项数量
     */
    private static int getConfigurationItemCount(Configuration configuration) {
        // 这里简化计算，实际应该统计所有配置项
        int count = 0;
        if (configuration.isCacheEnabled()) count++;
        if (configuration.isLazyLoadingEnabled()) count++;
        if (configuration.isAggressiveLazyLoading()) count++;
        if (configuration.isMultipleResultSetsEnabled()) count++;
        if (configuration.isUseColumnLabel()) count++;
        if (configuration.isUseGeneratedKeys()) count++;
        if (configuration.isAutoMappingBehavior()) count++;
        if (configuration.getDefaultExecutorType() != null) count++;
        if (configuration.getDefaultStatementTimeout() != null) count++;
        if (configuration.getDefaultFetchSize() != null) count++;
        return count;
    }
    
    /**
     * 创建配置对象
     */
    private static Configuration createConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setCacheEnabled(true);
        configuration.setLazyLoadingEnabled(false);
        configuration.setDefaultExecutorType(org.apache.ibatis.session.ExecutorType.SIMPLE);
        return configuration;
    }
    
    /**
     * 自定义配置类
     */
    static class CustomConfiguration extends Configuration {
        private String customProperty;
        
        public String getCustomProperty() {
            return customProperty;
        }
        
        public void setCustomProperty(String customProperty) {
            this.customProperty = customProperty;
        }
    }
    
    /**
     * 自定义配置解析器
     */
    static class CustomConfigurationParser {
        public Configuration parseCustomConfiguration(Properties props) {
            Configuration configuration = new Configuration();
            
            // 解析自定义配置项
            for (String key : props.stringPropertyNames()) {
                if (key.startsWith("custom.")) {
                    // 处理自定义配置项
                    String value = props.getProperty(key);
                    configuration.getVariables().setProperty(key, value);
                }
            }
            
            return configuration;
        }
    }
    
    /**
     * 自定义配置验证器
     */
    static class CustomConfigurationValidator {
        public boolean validate(Configuration configuration) {
            // 自定义验证逻辑
            if (configuration.isCacheEnabled() && configuration.isLazyLoadingEnabled()) {
                // 缓存和懒加载不能同时启用
                return false;
            }
            
            if (configuration.getDefaultExecutorType() == null) {
                // 执行器类型必须指定
                return false;
            }
            
            return true;
        }
    }
    
    /**
     * 模拟 UserMapper 接口
     */
    interface UserMapper {
        // 这里只是用于演示，实际应该包含具体的注解
    }
}