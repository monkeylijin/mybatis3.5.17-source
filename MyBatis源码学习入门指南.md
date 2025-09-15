# MyBatis 源码学习入门指南

## 🎯 学习目标确认

**你的目标**: 从不太高级的程序员成长为 MyBatis 专家，并写出高质量的源码解读专栏

**我的承诺**: 通过系统性的指导和实践，帮助你实现这个目标！

## 📋 学习前准备清单

### 1. 技术基础要求

- [ ] Java 基础扎实（集合、IO、多线程）
- [ ] 了解反射机制和动态代理
- [ ] 熟悉设计模式（至少了解工厂、代理、装饰器模式）
- [ ] 有 MyBatis 基本使用经验
- [ ] 了解 Maven 项目构建

### 2. 开发环境准备

- [ ] JDK 8+ 开发环境
- [ ] IntelliJ IDEA 或 Eclipse
- [ ] Maven 3.6+
- [ ] Git 版本控制
- [ ] 数据库（MySQL/PostgreSQL）

### 3. 学习工具准备

- [ ] 源码阅读工具（IDEA 的源码导航功能）
- [ ] 调试工具（断点调试）
- [ ] 绘图工具（ProcessOn、Draw.io）
- [ ] 笔记工具（Notion、语雀、或 Markdown）

## 🚀 学习路径规划

### 阶段一：基础巩固（2-3周）

#### 第1周：Java 基础强化

**目标**: 确保有足够的技术基础来理解 MyBatis 源码

**学习内容**:

1. **反射机制深入理解**

   ```java
   // 实践：手写一个简单的 ORM 框架
   public class SimpleORM {
       public <T> T selectOne(Class<T> clazz, Object id) {
           // 使用反射获取类信息
           // 构建 SQL 语句
           // 执行查询并映射结果
       }
   }
   ```
2. **动态代理原理**

   ```java
   // 实践：实现一个简单的代理
   public class MapperProxy implements InvocationHandler {
       @Override
       public Object invoke(Object proxy, Method method, Object[] args) {
           // 代理逻辑实现
       }
   }
   ```
3. **设计模式复习**

   - 工厂模式：理解 SqlSessionFactory
   - 代理模式：理解 Mapper 接口代理
   - 建造者模式：理解 Configuration 构建

**实践作业**:

- [ ] 实现一个简单的反射工具类
- [ ] 手写一个动态代理示例
- [ ] 用工厂模式实现一个简单的数据访问层

#### 第2周：MyBatis 使用熟练度

**目标**: 熟练使用 MyBatis，为源码学习做准备

**学习内容**:

1. **基础 CRUD 操作**
2. **动态 SQL 使用**
3. **结果映射配置**
4. **缓存配置**

**实践作业**:

- [ ] 创建一个完整的 MyBatis 项目
- [ ] 实现复杂的动态 SQL 查询
- [ ] 配置和使用二级缓存

#### 第3周：源码环境搭建

**目标**: 搭建 MyBatis 源码学习环境

**学习内容**:

1. **下载和编译 MyBatis 源码**
2. **创建测试项目**
3. **配置调试环境**

**实践作业**:

- [ ] 成功编译 MyBatis 源码
- [ ] 创建第一个源码调试项目
- [ ] 跟踪一个简单查询的执行流程

### 阶段二：源码入门（3-4周）

#### 第4周：整体架构理解

**学习重点**: MyBatis 的整体架构和核心组件

**学习内容**:

1. **三层架构分析**
2. **核心组件关系**
3. **配置文件解析流程**

**实践任务**:

```java
// 跟踪这个简单查询的执行流程
SqlSession session = sqlSessionFactory.openSession();
User user = session.selectOne("com.example.UserMapper.selectById", 1);
```

**学习成果**:

- [ ] 能够绘制 MyBatis 整体架构图
- [ ] 理解各个组件的职责分工
- [ ] 能够跟踪一个查询的完整执行流程

#### 第5周：SqlSession 和 Configuration

**学习重点**: 会话管理和配置管理

**学习内容**:

1. **SqlSessionFactory 创建过程**
2. **SqlSession 生命周期管理**
3. **Configuration 配置解析**

**实践任务**:

```java
// 分析这个过程的源码
String resource = "mybatis-config.xml";
InputStream inputStream = Resources.getResourceAsStream(resource);
SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
```

**学习成果**:

- [ ] 理解 SqlSession 的创建和管理
- [ ] 掌握配置文件的解析过程
- [ ] 能够自定义配置解析器

#### 第6周：Mapper 接口动态代理

**学习重点**: 理解 MyBatis 如何实现接口代理

**学习内容**:

1. **MapperProxy 实现原理**
2. **MapperMethod 方法解析**
3. **参数处理机制**

**实践任务**:

```java
// 分析这个接口是如何被代理的
public interface UserMapper {
    User selectById(Integer id);
}

UserMapper mapper = session.getMapper(UserMapper.class);
User user = mapper.selectById(1);
```

**学习成果**:

- [ ] 理解动态代理在 MyBatis 中的应用
- [ ] 掌握 Mapper 方法的解析过程
- [ ] 能够手写简单的 Mapper 代理

#### 第7周：Executor 执行器体系

**学习重点**: 理解 SQL 执行的核心机制

**学习内容**:

1. **BaseExecutor 基础执行器**
2. **SimpleExecutor 简单执行器**
3. **CachingExecutor 缓存执行器**

**实践任务**:

```java
// 跟踪这个查询在 Executor 中的执行过程
List<User> users = session.selectList("com.example.UserMapper.selectAll");
```

**学习成果**:

- [ ] 理解不同执行器的职责分工
- [ ] 掌握缓存机制的工作原理
- [ ] 能够自定义执行器

### 阶段三：深度理解（4-5周）

#### 第8-11周：核心组件深入分析

**学习重点**: 深入理解 MyBatis 的核心组件

**学习内容**:

- StatementHandler 语句处理器
- ParameterHandler 参数处理器
- ResultSetHandler 结果集处理器
- MappedStatement 映射语句

**实践任务**:

- 为每个组件编写自定义实现
- 分析组件的协作关系
- 优化组件性能

#### 第12周：插件系统

**学习重点**: 理解 MyBatis 的扩展机制

**学习内容**:

1. **Interceptor 拦截器接口**
2. **Plugin 代理实现**
3. **InterceptorChain 拦截器链**

**实践任务**:

```java
// 编写一个简单的插件
@Intercepts({
    @Signature(type = Executor.class, method = "update", 
               args = {MappedStatement.class, Object.class})
})
public class ExamplePlugin implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 实现插件逻辑
        return invocation.proceed();
    }
}
```

### 阶段四：实践应用（2-3周）

#### 第13-14周：综合实践

**学习重点**: 将所学知识应用到实际项目中

**实践项目**:

1. **自定义分页插件**
2. **性能监控插件**
3. **多数据源支持**
4. **自定义类型处理器**

## 📚 学习方法建议

### 1. 源码阅读技巧

#### 从使用入手

```java
// 不要一开始就深入源码，先理解这个调用链
SqlSession session = sqlSessionFactory.openSession();
User user = session.selectOne("selectUser", 1);
```

#### 使用调试器

- 在关键方法设置断点
- 单步执行，观察变量变化
- 理解方法调用栈

#### 画图理解

- 绘制类图关系
- 绘制时序图
- 绘制架构图

### 2. 笔记记录方法

#### 使用思维导图

```
MyBatis 源码学习
├── 整体架构
│   ├── 接口层
│   ├── 核心处理层
│   └── 基础支持层
├── 核心组件
│   ├── SqlSession
│   ├── Executor
│   └── StatementHandler
└── 扩展机制
    ├── 插件系统
    └── 类型转换
```

#### 记录关键代码

```java
// 记录重要的源码片段和你的理解
public class BaseExecutor implements Executor {
    // 一级缓存实现
    protected PerpetualCache localCache;
  
    // 查询方法的核心逻辑
    public <E> List<E> query(...) {
        // 1. 检查缓存
        // 2. 执行查询
        // 3. 处理结果
    }
}
```

### 3. 实践验证

#### 编写测试用例

```java
@Test
public void testExecutor() {
    // 测试执行器的行为
    // 验证你的理解是否正确
}
```

#### 自定义实现

```java
// 尝试实现一个简化版的 MyBatis
public class SimpleMyBatis {
    // 实现核心功能
}
```

## 🎯 学习成果检验

### 每周检验点

- [ ] 能够解释本周学习内容的核心概念
- [ ] 能够绘制相关的架构图或流程图
- [ ] 能够编写相关的代码示例
- [ ] 能够回答相关的技术问题

### 阶段性检验

- [ ] **第3周**: 能够跟踪一个简单查询的完整执行流程
- [ ] **第7周**: 能够解释 MyBatis 的整体架构和核心组件
- [ ] **第11周**: 能够编写自定义插件和扩展组件
- [ ] **第14周**: 能够独立分析和解决 MyBatis 相关问题

## 🚨 常见学习误区

### 1. 急于求成

❌ **错误做法**: 一开始就深入复杂的源码细节
✅ **正确做法**: 先理解整体架构，再深入具体实现

### 2. 只看不练

❌ **错误做法**: 只阅读源码，不进行实践
✅ **正确做法**: 每学习一个概念都要动手实践

### 3. 缺乏系统性

❌ **错误做法**: 跳跃式学习，缺乏连贯性
✅ **正确做法**: 按照学习路径，循序渐进

### 4. 忽视基础

❌ **错误做法**: 基础不扎实就开始学习源码
✅ **正确做法**: 先巩固 Java 基础和设计模式

## 💡 学习资源推荐

### 官方资源

- [MyBatis 官方文档](https://mybatis.org/mybatis-3/)
- [MyBatis GitHub 仓库](https://github.com/mybatis/mybatis-3)

### 学习工具

- **IDE**: IntelliJ IDEA（强大的源码导航功能）
- **调试**: IDEA 调试器
- **绘图**: ProcessOn、Draw.io
- **笔记**: Notion、语雀

### 参考书籍

- 《MyBatis 技术内幕》
- 《深入理解 MyBatis》
- 《Java 核心技术》

## 🎉 学习激励

### 设定小目标

- 每周完成一个学习任务
- 每月写一篇学习总结
- 每季度分享一次学习心得

### 建立学习社群

- 加入 MyBatis 学习群
- 参与技术讨论
- 分享学习笔记

### 实践项目

- 为开源项目贡献代码
- 写技术博客
- 做技术分享

## 📞 学习支持

在学习过程中，如果遇到困难，可以：

1. **查阅官方文档和源码注释**
2. **在技术社区提问**
3. **与我交流学习心得**
4. **参与开源项目讨论**

记住：**学习源码是一个长期的过程，需要耐心和坚持。通过系统性的学习和实践，你一定能够成为 MyBatis 专家！**

## 🏆 最终目标

完成这个学习计划后，你将能够：

1. **深入理解 MyBatis 的设计思想和实现原理**
2. **具备 MyBatis 性能优化和问题排查能力**
3. **能够开发 MyBatis 扩展组件**
4. **写出高质量的 MyBatis 源码解读专栏**
5. **成为团队中的 MyBatis 技术专家**

**加油！让我们一起开始这个精彩的学习之旅！** 🚀

