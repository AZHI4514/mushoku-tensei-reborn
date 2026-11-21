# 切片 1d · GeckoLib 3D 水弹

## 1. AZHI 资源交接清单

表中“我提供”指 AZHI，“你生成”指实施本片的 Codex。前三行是游戏实际读取的全部美术资源。

| 交付物 | 负责人 | 最终绝对路径 | 交接约定 |
|---|---|---|---|
| 水弹几何模型 | 我提供 | `D:/Develop/mushoku-tensei-reborn/src/main/resources/assets/mushoku_tensei_reborn/geo/water_ball.geo.json` | Blockbench 导出 GeckoLib 实体模型。静止姿态弹头沿局部 **−Z**，弹尾朝 +Z，+Y 朝上。弹体几何中心和根骨骼 `root` 的枢轴均在 (0,0,0)，该点对应水弹实体的位置。 |
| UV 贴图 | 我提供 | `D:/Develop/mushoku-tensei-reborn/src/main/resources/assets/mushoku_tensei_reborn/textures/entity/water_ball.png` | 建议 **32×32** RGBA PNG，模型的 UV 画布与 PNG 尺寸一致。简单轮廓、单色分区可用 16×16；只有细密纹理在游戏距离仍可见时才建议 64×64。 |
| 飞行动画 | 你生成 | `D:/Develop/mushoku-tensei-reborn/src/main/resources/assets/mushoku_tensei_reborn/animations/water_ball.animation.json` | 根据交付的骨骼生成唯一剪辑 `animation.water_ball.idle`，设置 `loop: true`。绕纵轴轻旋或做可见的轻微脉动，周期短于正常 20 tick 飞行时间。根枢轴固定，首尾姿态连续。 |

## 2. 依赖与任务顺序

1. **单独接入 GeckoLib 并验证构建。** 保持 Java 21、Minecraft 1.21.1、NeoForge 21.1.267、ModDevGradle 2.0.148、Gradle Wrapper 9.2.1。添加 Cloudsmith Maven 仓库，编译与运行依赖精确锁定 `software.bernie.geckolib:geckolib-neoforge-1.21.1:4.7.7`，在 NeoForge 模组元数据中将 `geckolib` 声明为双端必需、版本范围 `[4.7.7]`。检查实际解析版本，运行 `./gradlew build`，退出 0 且输出 `BUILD SUCCESSFUL` 后才开始任务 2。本项可以在美术资源交付前完成，保留 1c 渲染器以单独验证依赖。2026-11-21 查阅该制品的 Cloudsmith 版本列表，4.7.6、4.7.7 之后即进入 4.8。禁用动态版本及 4.8。[Cloudsmith Maven 元数据](https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/software/bernie/geckolib/geckolib-neoforge-1.21.1/maven-metadata.xml)
2. **接入资源与动画。** 依赖任务 1 通过及模型、贴图到位。校验实际骨骼名、UV 尺寸、局部轴向；生成表中动画，将其绑定到现有实体。
3. **一次替换客户端渲染。** 依赖任务 2。删除 1c 的 `WaterBallRenderer` 及其浅蓝染料贴片注册，为原有 `mushoku_tensei_reborn:water_ball` 注册唯一的 GeckoLib 渲染器。模型和渲染器只经 `Dist.CLIENT` 入口加载。
4. **构建、专用服、双客户端与回归验收。** 依赖任务 3，按下节逐项记录。

## 3. 三件套的职责

- **GeoEntity** — 现有 `WaterBallEntity` 实现 GeckoLib 的实体动画接口，每枚水弹持有自己的动画实例缓存，仅注册一个持续循环播放 idle 的控制器，无状态机、动画切换或新网络包。服务端继续负责生成、水弹运动、碰撞、伤害与销毁。公共实体类只引用双端可加载的 API。
- **GeoModel** — 客户端模型类将同一水弹映射到表中的几何、贴图、动画三个资源；资源命名空间均为 `mushoku_tensei_reborn`。
- **GeoRenderer** — 客户端使用 GeckoLib 的具体实体渲染基类 `GeoEntityRenderer<WaterBallEntity>` 绘制模型。根据同步的飞行向量确定水平朝向与俯仰，将模型 −Z 对准运动方向。旋转与动画只作用于绘制，不改变服务端坐标或碰撞箱。

## 4. 验收

| 场景（WHEN） | 结果（SHALL） |
|---|---|
| 依赖单独接入时，以及渲染替换完成后，各执行一次 `./gradlew build` | 两次均退出 0；最终产物包含表中的三份资源。 |
| 双端安装指定版本，执行 `./gradlew runServer` | 日志到达 `Done`，无 `NoClassDefFoundError`；记录启动日志末尾。 |
| A 在空旷直道向前、向上各发射一次，B 从侧面观察，同一枚弹体分多个飞行 tick 截帧 | A、B 均看到有体积的同一水弹模型；两端都能看到 idle 姿态随时间变化并循环。弹头随水平及俯仰方向飞行，侧面可见弹头与弹尾，无朝相机转动的旧贴片。 |
| 运行 `./gradlew test`、`./gradlew runGameTestServer` 和 `python tests/contracts.py` | 三项退出 0，保留 1c 的飞行、碰撞及寿命用例。 |
| 在 1c 双端直道重新发射并逐 tick 读位置、目标血量，结合既有 GameTest 核对边界 | 每 tick 直线前进 1.2 格，无下坠；23.99、24.00 格命中，24.01 格不命中。薄板在一步两端之间仍挡弹。首个无甲目标减 6，后靶及施法者免伤；正面举盾仍可格挡，命中即消失。静止弹第 39 tick 存在、第 40 tick 移除。 |

## 5. 范围外

命中音效、粒子替换、多动画状态和模型变体均留在本片之外。沿用 1c 的原版命中水花。
