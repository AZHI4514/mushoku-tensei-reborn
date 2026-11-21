# 切片 0.5 · 测试架构规格

日期 2026-11-21。切片 0 已提供构建入口。本片的交付目标是让三条测试管道各执行一个可辨认的哨兵，并保持生产 JAR 只收录主源集。

## 一、三层测试职责

| 层级与入口 | 测什么 | 不测什么 | 时间尺度 |
|---|---|---|---|
| L1 契约 · `tests/contracts.py` | 用 Python 直接读取仓库，核对文档、注册声明与资源的一致性 | 数值算法、Java 类加载、游戏世界行为 | 秒级，无需 JVM |
| L2 单元 · `src/test/java` · JUnit 5 | 用普通 Java 输入验证确定性结果，包括未来的状态判断和数值计算 | Minecraft 对象、注册表、世界交互；禁止 `import net.minecraft.*`，包括子包类型和静态导入 | 单用例毫秒级，另计 Gradle 和 JVM 启动 |
| L3 GameTest · `src/gameTest/java` | 在独立测试服务端观察真实世界中的方块、实体或 tick 行为 | 文档对账、纯算法的输入组合、客户端画面 | 包含服务端启动，分钟级 |

## 二、源集与生产包

声明独立 Gradle 源集 `gameTest`。所有 GameTest 类只能放在 `src/gameTest/java`，测试专用结构模板放在 `src/gameTest/resources`。禁止将 GameTest 类放入 `src/main/java`。为测试类统一使用含 `gametest` 的包路径，使归档清单能直接定位泄漏。

在 `neoForge.mods.mushoku_tensei_reborn` 中保留 `sourceSet sourceSets.main` 并追加 `sourceSet sourceSets.gameTest`，让开发运行时发现两个源集。为 gameTest 编译提供 main 输出及所需的 Minecraft、NeoForge 类路径。生产 `jar` 任务只打包 main 输出，禁止将 gameTest 输出合并进 main 或 jar。这样玩家安装的模组不会携带测试类或测试专用结构。

在 `neoForge.runs` 中声明以下独立服务端。命名空间与哨兵注册时使用的值一致。

~~~groovy
gameTestServer {
    type = "gameTestServer"
    systemProperty 'neoforge.enabledGameTestNamespaces', 'mushoku_tensei_reborn'
}
~~~

## 三、生产代码分层

- `src/main/java/com/azhi/mushoku/magic/core/` 放纯 Java 逻辑，零 Minecraft 或 NeoForge 依赖。一段逻辑只靠普通 Java 输入就能算出结果，即归入 core。
- `src/main/java/com/azhi/mushoku/magic/runtime/` 放 MC 适配。它读取游戏对象，将纯数据交给 core，再把结果应用到世界。依赖方向为 runtime → core。
- 例如，判断给定魔力是否足以支付消耗属于 core，从玩家读取魔力并写回属于 runtime。这是后续代码的归属判据，本片无需创建业务类。

## 四、范围与哨兵

仅建管道，每层一个哨兵。业务测试、覆盖率指标与 ModDevGradle 的 `unitTest`（为 JUnit 加载 Minecraft）均在本片范围外。

| 层级 | 哨兵与可观察结果 |
|---|---|
| L1 | 复用现有的优先级声明契约作为哨兵。在真实仓库运行脚本，退出码为 0 即通过。其余现有契约照常运行。 |
| L2 | 一个 Jupiter `@Test`，用 JDK 集合和断言验证固定输入的结果。测试报告能按名称找到该方法并显示通过。 |
| L3 | 一个必需的 `@GameTest`，通过 `@GameTestHolder` 注册。加载测试专用空结构后，在结构内放置石头，读回同一位置并断言方块为石头，随后报告成功。 |

## 五、实施验收

从仓库根目录执行。归档管道使用 Bash 或 Git Bash，Windows PowerShell 下的 Gradle 入口为 `.\gradlew.bat`。

| 命令 | 期望结果 |
|---|---|
| `./gradlew test` | `BUILD SUCCESSFUL`，`:test` 非 `NO-SOURCE`；在 `build/test-results/test/` 的 XML 中找到 L2 哨兵，1 个通过，0 个失败或跳过。复核缓存结果时可用 `./gradlew test --rerun-tasks`。 |
| `./gradlew runGameTestServer` | 独立服务端执行并退出，退出码 0；日志显示至少 1 个必需测试通过，可核对 L3 哨兵名称。 |
| `python tests/contracts.py` | 退出码 0，含 L1 优先级声明检查的通过记录。 |
| `./gradlew build` | `BUILD SUCCESSFUL`，在 `build/libs/` 生成可读取的生产 JAR。 |
| `unzip -l build/libs/*.jar \| grep -i gametest` | 无输出。当前构建仅产生一个生产 JAR；先确认 `unzip -t build/libs/*.jar` 返回 0，再核对 grep 返回 1（无匹配），避免将读取归档失败误判为通过。 |

## 执行约定

日常运行 `./gradlew test`。L3 独立于常规构建，交付前运行 `./gradlew runGameTestServer`。若准备模板超过 20 分钟，改用已有模板，仅验证世界加载、tick 回调和 succeed()。

在 src/main/java/com/azhi/mushoku/magic/core/ 和同级 runtime/ 各放一个 .gitkeep，并在 AGENTS.md 登记分层规则。
