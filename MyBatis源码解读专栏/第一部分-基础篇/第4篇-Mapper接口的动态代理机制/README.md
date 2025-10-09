# 第4篇：Mapper 接口的动态代理机制（导航）

## 目录
- 目标与要点
- 文档与示例
- 推荐阅读顺序
- 快速检查清单

## 目标与要点
- 弄清 `getMapper → MapperProxy → MapperMethod → SqlSession` 调用链。
- 掌握参数命名（`@Param`）、返回类型推断（单个/集合/Map/Cursor）。
- 了解 XML/注解并存的优先级与覆盖规则。
- 明确与缓存、事务边界的关系（会话级/语句级刷新）。
  
备注：正文中的时序图为简化版本，未完全展开 `ResultHandler/RowBounds/缓存` 路径，请结合源码断点验证。

## 文档与示例
- 正文：`文章内容.md`
- 学习检验与总结：`学习检验与总结.md`
- 相关源码参考：
  - `src/main/java/org/apache/ibatis/binding/MapperProxy*`
  - `src/main/java/org/apache/ibatis/binding/MapperMethod*`
  - `src/main/java/org/apache/ibatis/binding/MapperRegistry`
  - `src/main/java/org/apache/ibatis/session/DefaultSqlSession*`

## 推荐阅读顺序
1) 文章内容 → 2) 对照源码类 → 3) 动手调试断点 → 4) 学习检验与总结

## 快速检查清单
- [ ] 能画出 Mapper 调用时序图
- [ ] 能说明 `MapperMethod.execute` 的分支决策
- [ ] 能正确使用 `@Param` 命名多参数
- [ ] 能解释 XML 覆盖注解的原因与场景
- [ ] 能描述缓存命中/刷新时机

调试提示：在 `MapperProxy.invoke` 设置断点，观察 `methodCache` 命中；在 `MapperMethod.execute` 观察 `MethodSignature` 的返回类型判断与 `@MapKey` 解析。
