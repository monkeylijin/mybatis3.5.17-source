# 第1篇：MyBatis 整体架构图

## 🏗️ 三层架构图

```mermaid
graph TB
    subgraph "接口层 (Interface Layer)"
        A[SqlSession] --> B[SqlSessionFactory]
        A --> C[Mapper接口]
        D[SqlSessionFactoryBuilder] --> B
    end
    
    subgraph "核心处理层 (Core Processing Layer)"
        E[Executor执行器] --> F[StatementHandler]
        F --> G[ParameterHandler]
        F --> H[ResultSetHandler]
        I[Configuration配置] --> J[MappedStatement]
        J --> K[BoundSql]
        J --> L[ResultMap]
    end
    
    subgraph "基础支持层 (Foundation Layer)"
        M[反射模块] --> N[类型转换]
        O[日志模块] --> P[多种日志框架集成]
        Q[IO模块] --> R[资源加载]
        S[解析器模块] --> T[XML解析]
        U[数据源模块] --> V[连接池管理]
        W[事务模块] --> X[事务管理]
        Y[缓存模块] --> Z[一级/二级缓存]
    end
    
    A --> E
    B --> I
    E --> U
    E --> W
    E --> Y
    I --> S
    I --> Q
    I --> O
    I --> M
```

## 🔄 SQL执行流程图

```mermaid
sequenceDiagram
    participant Client as 客户端
    participant SqlSession as SqlSession
    participant Executor as Executor
    participant StatementHandler as StatementHandler
    participant ParameterHandler as ParameterHandler
    participant ResultSetHandler as ResultSetHandler
    participant DB as 数据库
    
    Client->>SqlSession: 调用查询方法
    SqlSession->>Executor: 执行查询
    Executor->>Executor: 检查一级缓存
    alt 缓存命中
        Executor-->>SqlSession: 返回缓存结果
    else 缓存未命中
        Executor->>StatementHandler: 创建Statement
        StatementHandler->>ParameterHandler: 处理参数
        ParameterHandler->>DB: 设置参数
        StatementHandler->>DB: 执行SQL
        DB-->>StatementHandler: 返回结果集
        StatementHandler->>ResultSetHandler: 处理结果集
        ResultSetHandler->>ResultSetHandler: 结果映射
        ResultSetHandler-->>Executor: 返回映射结果
        Executor->>Executor: 存入一级缓存
        Executor-->>SqlSession: 返回结果
    end
    SqlSession-->>Client: 返回最终结果
```

## 🧩 核心组件关系图

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

## 🎯 组件职责分工图

```mermaid
mindmap
  root((MyBatis架构))
    接口层
      SqlSession
        会话管理
        CRUD操作
        事务控制
      SqlSessionFactory
        工厂模式
        创建SqlSession
        配置管理
      Mapper接口
        动态代理
        方法映射
        参数处理
    核心处理层
      Executor
        SQL执行
        缓存管理
        事务管理
      StatementHandler
        语句处理
        参数绑定
        结果处理
      ParameterHandler
        参数处理
        类型转换
        参数绑定
      ResultSetHandler
        结果映射
        对象创建
        延迟加载
    基础支持层
      反射模块
        反射优化
        元数据缓存
        对象创建
      类型转换
        类型映射
        参数转换
        结果转换
      日志模块
        日志集成
        调试支持
        性能监控
      缓存模块
        一级缓存
        二级缓存
        缓存策略
```

## 🔧 插件系统架构图

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

## 💾 缓存机制架构图

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

## 🎨 设计模式应用图

```mermaid
graph LR
    subgraph "创建型模式"
        A[工厂模式] --> B[SqlSessionFactory]
        C[建造者模式] --> D[Configuration.Builder]
        E[单例模式] --> F[Configuration]
    end
    
    subgraph "结构型模式"
        G[代理模式] --> H[Mapper接口代理]
        I[装饰器模式] --> J[缓存装饰器]
        K[适配器模式] --> L[日志适配器]
    end
    
    subgraph "行为型模式"
        M[模板方法模式] --> N[BaseExecutor]
        O[策略模式] --> P[Executor类型]
        Q[观察者模式] --> R[插件系统]
    end
```

## 📊 性能优化架构图

```mermaid
graph TB
    subgraph "连接池管理"
        A[DataSource] --> B[连接池]
        B --> C[连接复用]
        C --> D[性能提升]
    end
    
    subgraph "缓存优化"
        E[一级缓存] --> F[会话级缓存]
        G[二级缓存] --> H[应用级缓存]
        I[查询缓存] --> J[结果缓存]
    end
    
    subgraph "SQL优化"
        K[预编译语句] --> L[PreparedStatement]
        M[批量处理] --> N[BatchExecutor]
        O[延迟加载] --> P[懒加载机制]
    end
    
    subgraph "反射优化"
        Q[元数据缓存] --> R[Reflector缓存]
        S[对象池] --> T[ObjectFactory]
        U[类型处理] --> V[TypeHandler缓存]
    end
```

## 🎯 学习重点标注

### 1. 核心理解点
- **三层架构**：接口层、核心处理层、基础支持层
- **组件职责**：每个组件都有明确的职责分工
- **协作关系**：组件之间通过接口进行协作
- **生命周期**：不同组件有不同的生命周期管理

### 2. 设计模式应用
- **工厂模式**：SqlSessionFactory 创建 SqlSession
- **代理模式**：Mapper 接口动态代理
- **建造者模式**：Configuration 构建
- **装饰器模式**：缓存装饰器、日志装饰器

### 3. 性能优化点
- **连接池管理**：复用数据库连接
- **缓存机制**：多级缓存提升性能
- **预编译语句**：提升 SQL 执行效率
- **反射优化**：缓存反射元数据

## 📝 学习建议

1. **从整体到局部**：先理解整体架构，再深入具体组件
2. **理论与实践结合**：结合源码和实际使用
3. **绘制架构图**：通过绘图加深理解
4. **跟踪执行流程**：使用调试器跟踪执行流程
5. **编写示例代码**：通过编写代码验证理解

