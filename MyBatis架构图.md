# MyBatis-3 架构图

## 整体架构分层

```mermaid
graph TB
    subgraph "接口层 (Interface Layer)"
        A[SqlSession] --> B[SqlSessionFactory]
        A --> C[Mapper接口]
    end
    
    subgraph "核心处理层 (Core Processing Layer)"
        D[Executor执行器] --> E[StatementHandler]
        E --> F[ParameterHandler]
        E --> G[ResultSetHandler]
        H[Configuration配置] --> I[MappedStatement]
        I --> J[BoundSql]
        I --> K[ResultMap]
    end
    
    subgraph "基础支持层 (Foundation Layer)"
        L[反射模块] --> M[类型转换]
        N[日志模块] --> O[多种日志框架集成]
        P[IO模块] --> Q[资源加载]
        R[解析器模块] --> S[XML解析]
        T[数据源模块] --> U[连接池管理]
        V[事务模块] --> W[事务管理]
        X[缓存模块] --> Y[一级/二级缓存]
    end
    
    subgraph "插件系统 (Plugin System)"
        Z[Interceptor拦截器] --> AA[Plugin代理]
        AA --> BB[Invocation调用]
    end
    
    A --> D
    B --> H
    D --> T
    D --> V
    D --> X
    D --> Z
    H --> R
    H --> P
    H --> N
    H --> L
```

## SQL执行流程

```mermaid
sequenceDiagram
    participant Client as 客户端
    participant SqlSession as SqlSession
    participant Executor as Executor
    participant StatementHandler as StatementHandler
    participant DB as 数据库
    
    Client->>SqlSession: 调用查询方法
    SqlSession->>Executor: 执行查询
    Executor->>Executor: 检查一级缓存
    alt 缓存命中
        Executor-->>SqlSession: 返回缓存结果
    else 缓存未命中
        Executor->>StatementHandler: 创建Statement
        StatementHandler->>DB: 执行SQL
        DB-->>StatementHandler: 返回结果集
        StatementHandler->>StatementHandler: 结果映射
        StatementHandler-->>Executor: 返回映射结果
        Executor->>Executor: 存入一级缓存
        Executor-->>SqlSession: 返回结果
    end
    SqlSession-->>Client: 返回最终结果
```

## 核心组件关系

```mermaid
graph LR
    subgraph "配置解析"
        A[XMLConfigBuilder] --> B[Configuration]
        C[XMLMapperBuilder] --> D[MappedStatement]
    end
    
    subgraph "执行器类型"
        E[SimpleExecutor] --> F[BaseExecutor]
        G[ReuseExecutor] --> F
        H[BatchExecutor] --> F
        I[CachingExecutor] --> F
    end
    
    subgraph "Statement处理器"
        J[PreparedStatementHandler] --> K[BaseStatementHandler]
        L[SimpleStatementHandler] --> K
        M[CallableStatementHandler] --> K
        N[RoutingStatementHandler] --> K
    end
    
    subgraph "结果处理"
        O[DefaultResultSetHandler] --> P[ResultSetHandler]
        Q[DefaultParameterHandler] --> R[ParameterHandler]
    end
    
    B --> E
    D --> J
    F --> O
    F --> Q
```

## 插件系统工作原理

```mermaid
graph TD
    A[Interceptor接口] --> B[plugin方法]
    B --> C[Plugin.wrap]
    C --> D[动态代理]
    D --> E[InvocationHandler]
    E --> F[intercept方法]
    F --> G[invocation.proceed]
    G --> H[原始方法执行]
    
    I[@Intercepts注解] --> J[Signature签名]
    J --> K[指定拦截目标]
    K --> A
    
    L[InterceptorChain] --> M[pluginAll方法]
    M --> N[遍历所有拦截器]
    N --> B
```

## 缓存机制

```mermaid
graph TB
    subgraph "一级缓存 (Local Cache)"
        A[PerpetualCache] --> B[SqlSession级别]
        B --> C[自动清理]
    end
    
    subgraph "二级缓存 (Global Cache)"
        D[Cache接口] --> E[Namespace级别]
        E --> F[手动配置]
        F --> G[LRU/FIFO等策略]
    end
    
    H[CachingExecutor] --> A
    H --> D
    
    I[CacheKey] --> J[SQL+参数+分页]
    J --> A
    J --> D
```

## 类型转换系统

```mermaid
graph LR
    A[Java类型] --> B[TypeHandler]
    B --> C[JDBC类型]
    C --> D[数据库]
    
    E[TypeHandlerRegistry] --> F[类型注册表]
    F --> B
    
    G[TypeAliasRegistry] --> H[别名注册表]
    H --> I[简化配置]
    
    J[ParameterMapping] --> K[参数映射]
    K --> B
    
    L[ResultMapping] --> M[结果映射]
    M --> B
```

