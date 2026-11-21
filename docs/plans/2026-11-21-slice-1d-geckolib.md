# 切片 1d · GeckoLib 3D 水弹实施计划

2026-11-21 · 待评审。[资源交接与验收规格](../specs/2026-11-21-slice-1d-geckolib.md)

## Global Constraints

Java 21、Minecraft 1.21.1、NeoForge 21.1.267、ModDevGradle 2.0.148、Gradle Wrapper 9.2.1。构建和两端验收固定 GeckoLib 4.7.7。沿用 1c 的实体 ID、服务端模拟、碰撞箱与命中水花。模型与渲染器只在客户端加载。以下时间为实施工时，AZHI 审阅时间另计。

### 任务 1 · 验证 Blockbench MCP 链路（10–15 分钟，无前置）

用 Blockbench MCP 导出单立方体到 `src/main/resources/assets/mushoku_tensei_reborn/geo/mcp_probe.geo.json`。验收磁盘上的 JSON：顶层 `format_version` 为字符串 `"1.12.0"`，存在非空 `minecraft:geometry` 数组，每条均含 `description` 和 `bones`，骨骼中至少一条含 `pivot` 和 `cubes`。GeckoLib 4 使用 Bedrock 实体几何格式；原门槛误用了旧版 GeckoLib 3 的前缀。

原探针已按修订门槛通过，含一条骨骼和一个立方体。验收后从资源目录删除探针。

### 任务 2 · 接入 GeckoLib 依赖并验证 build（15–25 分钟，无前置，可与任务 1 并行）

在 Gradle 中加入 GeckoLib 的 Cloudsmith Maven 仓库和固定坐标 `implementation 'software.bernie.geckolib:geckolib-neoforge-1.21.1:4.7.7'`。在本项目的 `src/main/resources/META-INF/neoforge.mods.toml` 声明 `modId="geckolib"`、`type="required"`、`side="BOTH"`、`versionRange="[4.7.7,)"`。核对已解析 4.7.7 的实际入口，在模组初始化阶段接入要求的 `GeckoLib.initialize()`；若制品未提供该 API，记录真实入口及编译差异并提交裁决。记录解析版本，使用 Wrapper 运行 `./gradlew build`，保留退出码及日志末尾。此时沿用 1c 渲染。任务 1 失败即停止推进；任务 2 未取得退出码 0，同样不得开始任务 3。

### 任务 3 · 生成水弹模型与贴图（20–30 分钟，依赖 1、2）

用 MCP 建立可辨认头尾的水弹，展开 32×32 UV，再用脚本按 UV 岛绘制 32×32 RGBA PNG。将实际文件保存到 spec 交接表的路径。核对几何中心与 root 枢轴均为 (0,0,0)，弹头朝 −Z、+Y 朝上。报告实测三轴包围尺寸、横向直径（7.2 模型单位即 0.45 格）、枢轴、朝向、贴图像素尺寸，附预览供 AZHI 审阅调整。

### 任务 4 · 生成循环动画（10–20 分钟，依赖 3）

按审阅后的骨骼制作 `animation.water_ball.idle`，写入 spec 指定的动画 JSON。只绑定一个循环剪辑，设置 `loop: true`，周期短于 20 tick。预览完整周期，记录时长，核对首尾连续、根枢轴固定、弹头轴向稳定。

### 任务 5 · 替换渲染器（20–30 分钟，依赖 4）

在现有水弹实体实现 GeoEntity、实例缓存及单循环控制器。客户端 GeoModel 映射三份资源，GeoEntityRenderer 将模型局部 −Z 对齐同步飞行方向。用 Dist.CLIENT 注册唯一渲染器，删除 1c 的 WaterBallRenderer、染料贴片及旧注册。检查水弹的渲染注册只剩一处，公共实体类无客户端类引用。

### 任务 6 · 构建、runServer、双客户端与回归验收（20–30 分钟，依赖 5）

1. 运行 `./gradlew build`、`./gradlew test`、`./gradlew runGameTestServer`、`python tests/contracts.py`，记录退出码及 GameTest 通过数。检查 jar 内含三份正式资源且无探针。
2. 用 4.7.7 启动 `./gradlew runServer`，截取到 Done 的日志末尾并检查 NoClassDefFoundError。让 A、B 两客户端连接同一服务器。
3. A 水平、斜向各发射，A、B 对同枚水弹分 tick 截帧，覆盖一个完整动画周期。记录飞行向量与弹头方向、两视角的模型中心与 Pos 对齐情况、按碰撞箱或同深度方块换算的视觉直径。分别记录两端动画播放与循环结果。
4. 依据 1c 验收记录复测每 tick 1.2 格、23.99/24.00/24.01 格端点、薄板阻挡、单目标伤害 6、免伤与格挡、静止弹第 39/40 tick。逐项记录观测或对应 GameTest 结果。
