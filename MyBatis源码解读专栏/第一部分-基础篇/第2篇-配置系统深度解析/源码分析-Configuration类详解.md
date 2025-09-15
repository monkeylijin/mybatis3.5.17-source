# 第2篇：Configuration 类源码深度分析

## 🎯 分析目标

通过本文档，你将深入理解：
1. Configuration 类的核心设计思想
2. 关键方法的源码实现
3. 配置项的管理机制
4. 性能优化的实现细节

## 📋 前置准备

在开始源码分析之前，请确保：
- 已完成第1篇的学习
- 已搭建好 MyBatis 3.x 源码环境
- 能够在 IDE 中调试 Configuration 相关源码

## 🔍 核心源码分析

### 1. Configuration 类结构分析

#### 1.1 类定义和继承关系

```java
public class Configuration {
    // 环境配置
    protected Environment environment;
    
    // 数据库相关配置
    protected boolean safeRowBoundsEnabled;
    protected boolean safeResultHandlerEnabled;
    protected boolean mapUnderscoreToCamelCase;
    protected boolean aggressiveLazyLoading;
    protected boolean multipleResultSetsEnabled;
    protected boolean useGeneratedKeys;
    protected boolean useColumnLabel;
    protected boolean callSettersOnNulls;
    protected boolean useActualParamName;
    protected boolean returnInstanceForEmptyRow;
    
    // 日志配置
    protected String logPrefix;
    protected Class<? extends Log> logImpl;
    
    // 缓存配置
    protected boolean cacheEnabled;
    protected LocalCacheScope localCacheScope;
    
    // 类型处理配置
    protected JdbcType jdbcTypeForNull;
    protected Set<String> lazyLoadTriggerMethods;
    
    // 超时配置
    protected Integer defaultStatementTimeout;
    protected Integer defaultFetchSize;
    protected ResultSetType defaultResultSetType;
    
    // 执行器配置
    protected ExecutorType defaultExecutorType;
    
    // 映射配置
    protected AutoMappingBehavior autoMappingBehavior;
    protected AutoMappingUnknownColumnBehavior autoMappingUnknownColumnBehavior;
    
    // 核心注册表
    protected ReflectorFactory reflectorFactory;
    protected ObjectFactory objectFactory;
    protected ObjectWrapperFactory objectWrapperFactory;
    protected MapperRegistry mapperRegistry;
    protected InterceptorChain interceptorChain;
    protected TypeHandlerRegistry typeHandlerRegistry;
    protected TypeAliasRegistry typeAliasRegistry;
    protected LanguageDriverRegistry languageRegistry;
    
    // 映射存储
    protected Map<String, MappedStatement> mappedStatements;
    protected Map<String, Cache> caches;
    protected Map<String, ResultMap> resultMaps;
    protected Map<String, ParameterMap> parameterMaps;
    protected Map<String, KeyGenerator> keyGenerators;
    
    // 其他配置
    protected Properties variables;
    protected Set<String> loadedResources;
    protected String databaseId;
    protected Class<?> configurationFactory;
    protected Map<String, String> cacheRefMap;
}
```

#### 1.2 构造函数分析

```java
public Configuration() {
    typeAliasRegistry = new TypeAliasRegistry();
    typeHandlerRegistry = new TypeHandlerRegistry();
    objectFactory = new DefaultObjectFactory();
    objectWrapperFactory = new DefaultObjectWrapperFactory();
    reflectorFactory = new DefaultReflectorFactory();
    
    settings = new Properties();
    variables = new Properties();
    interceptorChain = new InterceptorChain();
    resultMapRegistry = new ResultMapRegistry();
    parameterMapRegistry = new ParameterMapRegistry();
    keyGeneratorRegistry = new KeyGeneratorRegistry();
    
    mappedStatements = new StrictMap<MappedStatement>("Mapped Statements collection");
    caches = new StrictMap<Cache>("Caches collection");
    resultMaps = new StrictMap<ResultMap>("Result Maps collection");
    parameterMaps = new StrictMap<ParameterMap>("Parameter Maps collection");
    keyGenerators = new StrictMap<KeyGenerator>("Key Generators collection");
    
    loadedResources = new HashSet<String>();
    incompleteStatements = new LinkedList<XMLStatementBuilder>();
    incompleteResultMaps = new LinkedList<XMLResultMapBuilder>();
    incompleteCacheRefs = new LinkedList<XMLCacheRefBuilder>();
    
    // 设置默认值
    setDefaultSettings();
}
```

**关键点分析**：
1. **注册表初始化**：初始化各种注册表，用于管理不同类型的对象
2. **集合初始化**：使用 StrictMap 确保键的唯一性
3. **默认设置**：调用 setDefaultSettings() 设置默认配置

### 2. 核心方法源码分析

#### 2.1 配置项管理方法

##### 2.1.1 addMappedStatement 方法

```java
public void addMappedStatement(MappedStatement ms) {
    mappedStatements.put(ms.getId(), ms);
}
```

**方法分析**：
- **功能**：添加 MappedStatement 到配置中
- **参数**：MappedStatement 对象
- **实现**：直接存储到 mappedStatements Map 中
- **关键点**：使用 MappedStatement 的 ID 作为键

##### 2.1.2 getMappedStatement 方法

```java
public MappedStatement getMappedStatement(String id) {
    return mappedStatements.get(id);
}
```

**方法分析**：
- **功能**：根据 ID 获取 MappedStatement
- **参数**：MappedStatement 的 ID
- **返回值**：MappedStatement 对象或 null
- **关键点**：直接通过 Map 的 get 方法获取

##### 2.1.3 addMapper 方法

```java
public <T> void addMapper(Class<T> type) {
    mapperRegistry.addMapper(type);
}
```

**方法分析**：
- **功能**：添加 Mapper 接口到配置中
- **参数**：Mapper 接口的 Class 对象
- **实现**：委托给 MapperRegistry 处理
- **关键点**：使用泛型确保类型安全

#### 2.2 配置验证方法

##### 2.2.1 validate 方法

```java
public void validate() {
    if (environment == null) {
        throw new IllegalStateException("Environment was not set");
    }
    
    if (defaultExecutorType == null) {
        throw new IllegalStateException("Default executor type was not set");
    }
    
    if (defaultStatementTimeout != null && defaultStatementTimeout < 0) {
        throw new IllegalStateException("Default statement timeout must be non-negative");
    }
    
    if (defaultFetchSize != null && defaultFetchSize < 0) {
        throw new IllegalStateException("Default fetch size must be non-negative");
    }
    
    // 验证 MappedStatement
    for (MappedStatement ms : mappedStatements.values()) {
        if (ms.getCache() != null && ms.getCache().getClass().equals(PerpetualCache.class)) {
            if (ms.getCache().getSize() > 0) {
                throw new IllegalStateException("Perpetual cache size must be 0");
            }
        }
    }
}
```

**方法分析**：
- **功能**：验证配置的完整性和正确性
- **验证项**：环境配置、执行器类型、超时设置、缓存配置等
- **异常处理**：发现配置错误时抛出 IllegalStateException
- **关键点**：确保配置在运行时是有效的

#### 2.3 配置获取方法

##### 2.3.1 getMappedStatement 方法（重载）

```java
public MappedStatement getMappedStatement(String id, boolean validateIncompleteStatements) {
    if (validateIncompleteStatements) {
        validateIncompleteStatements();
    }
    return mappedStatements.get(id);
}
```

**方法分析**：
- **功能**：获取 MappedStatement，可选择是否验证未完成的语句
- **参数**：ID 和验证标志
- **实现**：先验证未完成的语句，再获取 MappedStatement
- **关键点**：支持延迟验证机制

##### 2.3.2 getMapper 方法

```java
public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    return mapperRegistry.getMapper(type, sqlSession);
}
```

**方法分析**：
- **功能**：获取 Mapper 接口的代理对象
- **参数**：Mapper 接口类型和 SqlSession
- **实现**：委托给 MapperRegistry 处理
- **关键点**：返回动态代理对象

### 3. 配置项存储机制分析

#### 3.1 StrictMap 实现分析

```java
public static class StrictMap<V> extends HashMap<String, V> {
    private static final long serialVersionUID = -4950446264854982944L;
    private String name;
    
    public StrictMap(String name, int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        this.name = name;
    }
    
    public StrictMap(String name, int initialCapacity) {
        super(initialCapacity);
        this.name = name;
    }
    
    public StrictMap(String name) {
        super();
        this.name = name;
    }
    
    public StrictMap(String name, Map<String, ? extends V> m) {
        super(m);
        this.name = name;
    }
    
    @SuppressWarnings("unchecked")
    public V put(String key, V value) {
        if (containsKey(key)) {
            throw new IllegalArgumentException(name + " already contains value for " + key);
        }
        if (key.contains(".")) {
            final String shortKey = getShortName(key);
            if (super.get(shortKey) == null) {
                super.put(shortKey, value);
            } else {
                super.put(shortKey, (V) new Ambiguity(shortKey));
            }
        }
        return super.put(key, value);
    }
    
    public V get(String key) {
        V value = super.get(key);
        if (value == null) {
            throw new IllegalArgumentException(name + " does not contain value for " + key);
        }
        if (value instanceof Ambiguity) {
            throw new IllegalArgumentException(name + " does not contain value for " + key + " (ambiguous)");
        }
        return value;
    }
    
    private String getShortName(String key) {
        final String[] keyParts = key.split("\\.");
        return keyParts[keyParts.length - 1];
    }
    
    protected static class Ambiguity {
        private String subject;
        
        public Ambiguity(String subject) {
            this.subject = subject;
        }
        
        @Override
        public String toString() {
            return subject;
        }
    }
}
```

**关键点分析**：
1. **键唯一性**：确保键的唯一性，避免重复
2. **短名称支持**：支持使用短名称访问配置项
3. **歧义处理**：处理短名称可能产生的歧义
4. **异常处理**：提供清晰的错误信息

#### 3.2 配置项生命周期管理

```java
// 配置项添加
public void addMappedStatement(MappedStatement ms) {
    mappedStatements.put(ms.getId(), ms);
}

// 配置项获取
public MappedStatement getMappedStatement(String id) {
    return mappedStatements.get(id);
}

// 配置项移除
public void removeMappedStatement(String id) {
    mappedStatements.remove(id);
}

// 配置项清空
public void clearMappedStatements() {
    mappedStatements.clear();
}
```

### 4. 性能优化实现分析

#### 4.1 懒加载机制

```java
public boolean isLazyLoadingEnabled() {
    return lazyLoadingEnabled;
}

public void setLazyLoadingEnabled(boolean lazyLoadingEnabled) {
    this.lazyLoadingEnabled = lazyLoadingEnabled;
}

public boolean isAggressiveLazyLoading() {
    return aggressiveLazyLoading;
}

public void setAggressiveLazyLoading(boolean aggressiveLazyLoading) {
    this.aggressiveLazyLoading = aggressiveLazyLoading;
}
```

**优化点分析**：
1. **懒加载控制**：通过配置控制是否启用懒加载
2. **激进懒加载**：控制懒加载的激进程度
3. **性能平衡**：在性能和内存使用之间找到平衡

#### 4.2 缓存机制

```java
public boolean isCacheEnabled() {
    return cacheEnabled;
}

public void setCacheEnabled(boolean cacheEnabled) {
    this.cacheEnabled = cacheEnabled;
}

public LocalCacheScope getLocalCacheScope() {
    return localCacheScope;
}

public void setLocalCacheScope(LocalCacheScope localCacheScope) {
    this.localCacheScope = localCacheScope;
}
```

**优化点分析**：
1. **缓存开关**：控制是否启用缓存
2. **缓存作用域**：控制缓存的作用范围
3. **内存管理**：通过缓存作用域管理内存使用

### 5. 扩展机制分析

#### 5.1 插件系统集成

```java
public void addInterceptor(Interceptor interceptor) {
    interceptorChain.addInterceptor(interceptor);
}

public List<Interceptor> getInterceptors() {
    return interceptorChain.getInterceptors();
}
```

**扩展点分析**：
1. **插件注册**：支持动态添加插件
2. **插件链**：通过 InterceptorChain 管理插件
3. **AOP 支持**：提供面向切面的编程支持

#### 5.2 自定义配置支持

```java
public void setVariables(Properties variables) {
    this.variables = variables;
}

public Properties getVariables() {
    return variables;
}

public void addLoadedResource(String resource) {
    loadedResources.add(resource);
}

public boolean isResourceLoaded(String resource) {
    return loadedResources.contains(resource);
}
```

**扩展点分析**：
1. **变量支持**：支持自定义变量
2. **资源管理**：跟踪已加载的资源
3. **配置覆盖**：支持配置的覆盖和继承

## 🎯 实践建议

### 1. 源码调试技巧

1. **设置断点**：在关键方法设置断点
2. **观察变量**：观察配置对象的状态变化
3. **跟踪调用栈**：理解方法调用关系
4. **验证理解**：通过调试验证对源码的理解

### 2. 学习重点

1. **设计模式**：理解 Configuration 中使用的设计模式
2. **数据结构**：理解配置项的存储和管理机制
3. **性能优化**：理解性能优化的实现方式
4. **扩展机制**：理解扩展机制的设计思想

### 3. 实践验证

1. **创建测试**：创建测试用例验证理解
2. **修改配置**：尝试修改配置项观察效果
3. **扩展开发**：尝试开发简单的配置扩展
4. **性能测试**：测试配置系统的性能特点

## 📝 学习笔记

### 关键概念记录
- [ ] Configuration 类的核心职责
- [ ] 配置项的管理机制
- [ ] 性能优化的实现方式
- [ ] 扩展机制的设计思想

### 源码分析记录
- [ ] 关键方法的实现细节
- [ ] 设计模式的应用
- [ ] 性能优化的技巧
- [ ] 扩展点的设计

### 实践心得记录
- [ ] 调试过程中的发现
- [ ] 对设计思想的理解
- [ ] 遇到的问题和解决方案
- [ ] 后续学习的计划

---

**通过源码分析，深入理解 Configuration 类的设计思想和实现细节！** 🚀

