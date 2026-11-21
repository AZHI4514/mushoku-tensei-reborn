# 切片 0.5 · 测试架构实施计划

日期 2026-11-21。验收口径见[同日规格](../specs/2026-11-21-test-architecture.md)。

## Global Constraints

| 工具 | 锁定版本 |
|---|---|
| Java | 21（Temurin） |
| NeoForge | 21.1.267（Minecraft 1.21.1） |
| ModDevGradle | 2.0.148 |
| Gradle | 9.2.1，使用仓库 Wrapper |

L2 使用普通 Jupiter 测试任务，关闭对游戏环境的依赖。L3 通过独立 gameTest 源集与 GameTestServer 运行。本片只验证测试基础设施，每层一个哨兵。

## 任务与依赖

| 任务 | 耗时 | 前置 | 实施与完成条件 |
|---|---|---|---|
| 1. 验证 L1 哨兵 | 10 分钟 | 无 | 运行现有契约脚本。确认真实仓库退出 0。 |
| 2. 接通 L2 | 20 分钟 | 无 | 在 build.gradle 为 test 声明 JUnit 5 BOM、Jupiter 及运行期 Platform Launcher，启用 useJUnitPlatform()。在 src/test/java 添加一个纯 Java 哨兵。运行 test 并核对 XML 用例名、数量和结果。 |
| 3. 接通 L3 源集 | 25 分钟 | 2（两步编辑同一构建文件） | 声明 gameTest 源集，接入 main 输出与 NeoForge 编译依赖。在 neoForge.mods 对同一模组绑定 main 和 gameTest；在 runs 注册 gameTestServer 并设置类型与命名空间。用 Wrapper 的 tasks --all 确认 runGameTestServer 已生成。 |
| 4. 执行世界内哨兵 | 25 分钟 | 3 | 在独立源集中添加带 GameTestHolder 的测试类和对应结构模板。核对模板命名空间、资源路径与类名前缀策略。运行 runGameTestServer，核对哨兵通过及退出码 0。 |
| 5. 验收三层与生产 JAR | 15 分钟 | 1、2、4 | 逐条执行规格第五节命令，记录用例结果并检查 JAR 可读性和清单。核对 gameTest 输出未进入 jar，核对 L2 无 Minecraft 导入。按实测结果更新项目入口的测试命令与已实现边界。 |

## 执行约定

日常运行 `./gradlew test`。L3 独立于常规构建，交付前运行 `./gradlew runGameTestServer`。若准备模板超过 20 分钟，改用已有模板，仅验证世界加载、tick 回调和 succeed()。

在 src/main/java/com/azhi/mushoku/magic/core/ 和同级 runtime/ 各放一个 .gitkeep，并在 AGENTS.md 登记分层规则。
