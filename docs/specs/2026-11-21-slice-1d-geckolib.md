# 切片 1d · GeckoLib 3D 水弹

## 1. 资源交接清单

下表列出游戏实际读取的三份美术资源及其交付责任。

| 交付物 | 负责人 | 最终绝对路径 | 交接约定 |
|---|---|---|---|
| 水弹几何模型 | Codex 通过已配置的 Blockbench MCP 生成，AZHI 审阅调整 | `D:/Develop/mushoku-tensei-reborn/src/main/resources/assets/mushoku_tensei_reborn/geo/water_ball.geo.json` | Blockbench 导出 GeckoLib 实体模型。静止姿态弹头沿局部 **−Z**，弹尾朝 +Z，+Y 朝上。弹体几何中心和根骨骼 `root` 的枢轴均在 (0,0,0)，该点对应水弹实体的位置。 |
| UV 贴图 | Codex 用脚本生成 PNG | `D:/Develop/mushoku-tensei-reborn/src/main/resources/assets/mushoku_tensei_reborn/textures/entity/water_ball.png` | 按模型展开的 UV 绘制 32×32 RGBA PNG，弹头与弹尾可辨。 |
| 飞行动画 | Codex 生成 | `D:/Develop/mushoku-tensei-reborn/src/main/resources/assets/mushoku_tensei_reborn/animations/water_ball.animation.json` | 根据交付的骨骼生成唯一剪辑 `animation.water_ball.idle`，设置 `loop: true`。绕纵轴轻旋或做可见的轻微脉动，周期短于正常 20 tick 飞行时间。根枢轴固定，首尾姿态连续。 |

MCP 生成的模型必须满足四项约定：根骨骼枢轴 (0,0,0)、弹头沿局部 −Z、视觉直径 0.45 格、UV 画布 32×32。直径按垂直于飞行轴的最大横截面量取，按 16 模型单位/格换算为 7.2 模型单位。几何中心与根枢轴重合。

## 2. 依赖与任务顺序

1. 以单立方体实测 Blockbench MCP 导出链路，检查真实导出文件及其格式字段。失败即停止并报告。
2. 独立接入 GeckoLib 并通过构建。任务 1、2 互不依赖，可并行。两项均通过后才进入正式模型制作。
3. Codex 通过 MCP 生成水弹几何与 UV，以脚本生成贴图，交 AZHI 审阅调整。
4. 按最终骨骼生成唯一循环动画。
5. 接入实体动画并以唯一的客户端 GeckoLib 渲染器替换 1c 临时渲染。
6. 构建、启动专用服、双客户端实测并回归 1c。

构建与本片双端实测精确使用 `software.bernie.geckolib:geckolib-neoforge-1.21.1:4.7.7`。NeoForge 元数据按约定声明双端硬依赖，加载器允许的版本范围为 `[4.7.7,)`。该范围允许更高版本，本片验证范围为 4.7.7。[Cloudsmith Maven 元数据](https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/software/bernie/geckolib/geckolib-neoforge-1.21.1/maven-metadata.xml)

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
| 冻结水平及斜向飞行的水弹，从侧面和另一视角对照相邻 tick 的位置差 | 弹头朝向与位移向量一致；绕水弹移动相机时弹头方向保持。 |
| 冻结弹体，读取实体 Pos，使用同深度坐标标记从两个正交视角截图 | 模型几何中心与实体 Pos 重合，旋转和循环动画期间保持对齐。 |
| 开启 F3+B，冻结水弹，从与飞行方向垂直的侧面用已知宽度的碰撞箱或同深度方块作比例尺 | 模型最大横向直径约 0.45 格；记录参照宽度、像素比和换算结果。沿用 1c 的雪球尺寸作为碰撞箱。 |
| 运行 `./gradlew test`、`./gradlew runGameTestServer` 和 `python tests/contracts.py` | 三项退出 0，保留 1c 的飞行、碰撞及寿命用例。 |
| 在 1c 双端直道重新发射并逐 tick 读位置、目标血量，结合既有 GameTest 核对边界 | 每 tick 直线前进 1.2 格，无下坠；23.99、24.00 格命中，24.01 格不命中。薄板在一步两端之间仍挡弹。首个无甲目标减 6，后靶及施法者免伤；正面举盾仍可格挡，命中即消失。静止弹第 39 tick 存在、第 40 tick 移除。 |

## 5. 范围外

命中音效、粒子替换、多动画状态和模型变体均留在本片之外。沿用 1c 的原版命中水花。
