# AGENTS.md — mushoku_tensei_reborn

> 项目入口。任何 agent 开工前必读本文，再按链接取所需文档。
> 本文只指路，不放设计内容。

## 一句话

以《无职转生》魔术观为骨架的 Minecraft NeoForge 内容 mod：把「魔术从玄学
变成可计算、可优化、可超越的技艺」这条成长曲线做成可玩的系统。

## 技术栈（钉死，不随切片变动）

| 项 | 值 |
|---|---|
| mod id | `mushoku_tensei_reborn` |
| Java 包 | `com.azhi.mushoku` |
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1（构建取当时最新 21.1.x，参考 21.1.267） |
| Java | 21（Temurin JDK，Gradle toolchain 可自动下载） |
| 构建 | Gradle wrapper（`./gradlew`，禁止裸 `gradle`） |
| Gradle 插件 | ModDevGradle |
| 3D 动画库 | GeckoLib 4.7.x（NeoForge 1.21.1），硬依赖 |

## 命令

```bash
./gradlew build          # 编译打包 → build/libs
./gradlew runClient      # 开发客户端
./gradlew runServer      # 无头服务端
./gradlew runData        # datagen（模型/方块状态/语言文件）
./gradlew test              # 单元测试，秒级，日常改完就跑
./gradlew runGameTestServer  # GameTest，分钟级，交付前跑
python tests/contracts.py  # 契约检查，交付前必须为 0
```

Windows 用 `./gradlew.bat`。
`test` 是日常反馈，`runGameTestServer` 不进常规 `build`。

## 目录地图

```
AGENTS.md                      ← 你在这里
docs/VISION.md                 宪法·愿景（写一次）
docs/PRINCIPLES.md             宪法·裁决原则 + 优先级声明
docs/HOW-TO-WORK.md            人类操作手册（AI 不必读，AZHI 自己看）
docs/ROADMAP.md                模块顺序 + 设定→模块映射
docs/modules/magic.md          魔术模块（第一模块，可执行设计）
docs/modules/<future>.md       后续模块：做之前才建
docs/specs/YYYY-MM-DD-*.md     切片 spec（交付后留档不改）
docs/plans/YYYY-MM-DD-*.md     切片实施计划
docs/setting-bible/*.md        设定集原文（**不进必读链**，见下）
tests/contracts.py             契约脚本（资源完整性对账）
src/main/java/com/azhi/mushoku/
  Mushoku.java                 @Mod 入口
  registry/                    每类一个 DeferredRegister（ModItems/ModBlocks/...）
  api/                         对外公开接口
  internal/                    不公开实现
  magic/                       魔术模块实现
  client/                      仅客户端
  config/                      配置
src/main/resources/
  assets/mushoku_tensei_reborn/  贴图/模型/blockstates/lang/sounds
  data/mushoku_tensei_reborn/    recipe/loot_table/tags/advancement
```

## 设定集不是需求（最重要的一条）

`docs/setting-bible/` 是原作资料存档，**不属于任何一层文档**。

- 不要把它当需求读。设定里提到 ≠ 本项目要做。
- 需要从设定推导玩法时，结论必须写进 `docs/modules/<模块>.md` 才算决定；
  只存在于设定集的玩法 = 未决定。
- 设定里的 `[citation:n]` 是原资料出处标记，与项目无关，不要引用。

## 裁决优先级

> **宪法（VISION + PRINCIPLES）> 模块文档 > 当前切片 spec > 临时指令。**

写进 PRINCIPLES 顶部，此处重申。冲突时按此顺序，不得自行协商。

## 已实现边界

> 权威清单：只有列在这里的才算真实存在。没列 = 不存在 = 不得引用。

**当前：切片 0 项目骨架、切片 0.5 测试架构、切片 1a 魔力可见、切片 1b 徒手咏唱、切片 1c 水弹弹体、切片 1d GeckoLib 3D 弹体、切片 1e 施法锚点、切片 1f 施法手臂动作与切片 1g 第一人称手臂修复。** L1 契约、L2 JUnit 5 和独立 L3 GameTest 哨兵均已跑通。 已配置 Java 21、Gradle Wrapper 和 ModDevGradle，模组入口 Mushoku 挂接空物品注册表 ModItems。

切片 1a 已交付纯 Java 魔力计算、玩家魔力附件及持有者同步、客户端快捷栏上方魔力条和权限等级 2 的 `/mushoku mana get|set` 调试命令。魔力范围为 0–100，新玩家与死亡重生时为满，重登与下界传送门往返保留数值。首次同步前隐藏 HUD。

切片 1b 已交付默认 R 键徒手咏唱、服务端计时与本人及 48 格内旁观者的水花表现。咏唱 100 tick，咏唱期间不降低移动速度，完成扣 1 点；松键、死亡、离线和跨维度按中断清理。

切片 1c 已交付服务端水弹。咏唱完成后从右手前方发射，每 tick 扫描最多 1.2 格，射程 24 格，硬寿命 40 tick。首次撞块或命中非施法者目标即消失，基础伤害 6，沿用原版盾牌格挡。命中时显示原版水花。

切片 1d 已将临时渲染替换为 GeckoLib 4.7.7 半透明水立方体，沿飞行轴播放 16 tick 循环轻摆。已通过专用服和双客户端验收。

切片 1e、1f 已交付准星选点与共用右掌锚点。左右主手玩家均用右臂平举、左手搭右腕，起手与收势各 5 tick。两手持物随手运动，咏唱水花和无遮挡发射的水弹从右掌中心出现。

切片 1g 已修复第一人称转头时施法双臂脱离视野的问题。双臂随镜头转动，开启视角摇晃后随步伐起伏。施法者看到新水花与水弹从当帧屏幕右掌出现，旁观者看到同一枚水弹从世界右掌发射。

禁止把 ROADMAP / VISION / 模块文档里的计划描述成已实现，无论上下文多像。

## 禁止事项

1. **不继承其他 mod 的类**。跨 mod / 跨模块只走：共享 tag（`c:` 命名空间）、
   NeoForge 事件、`api/` 包显式发布的接口。
2. **Mixin 只用于不可避免的 vanilla 内部**，不作常规集成手段。
3. **能 JSON 不代码**：配方、掉落、tag、advancement 一律 `data/` 下 JSON。
   新建 Java 类前先问「JSON 表达不了吗」。
4. **注册即承诺**：`registry/` 的 id 一旦进构建产物不改名（存档、tag、
   其他 mod 都引用）。新 id 先在模块文档定名再写代码。
5. **不描述计划为已实现**。
6. **动某区域前先读对应文档**：
   - 动 `registry/` → 先读 `docs/modules/magic.md` 第 8 节注册表清单
   - 动数据文件 → 先读模块文档的数值表
   - 动 `api/` → 先读 PRINCIPLES 第 5 条
   - 想知道「设定里怎么说」→ 才去 `docs/setting-bible/`

## 切片顺序

```
切片0 项目骨架（Gradle/入口/空 registry/契约跑通）
切片1a 魔力（DataAttachment）
切片1b 徒手水弹（R 键咏唱、服务端判定）
切片1c 3D 动画弹体（GeckoLib）
切片2 等级与属性矩阵（多法术、熟练度）
切片3 咏唱缩短 → 省略咏唱（成长曲线）
切片4 混合魔术（水+土 泥沼）
后续 无咏唱 / 越级注入 / 魔法阵
```

垂直切片优先：先让一个闭环可玩，再扩内容量。不因「再加一个法术很容易」
横向铺开。

## 事实型规则登记处

> 格式：规则 | 来源事故 | 何时失效 |（无失效条件的不登记）
> `tests/` 能检查的写成检查，不进本节。

（暂无）

## 接手新会话三步

1. 读本文 + `docs/PRINCIPLES.md`
2. 按任务找模块文档 + `docs/specs/` 里最新的 spec/plan
3. 核对「已实现边界」，再动手。交接靠文件不靠对话记忆。

## 魔术代码分层与测试

- `src/main/java/com/azhi/mushoku/magic/core/` 存放纯 Java 状态判断与数值计算，零 Minecraft 或 NeoForge 依赖。只靠普通 Java 输入就能算出的逻辑归入 core。
- `src/main/java/com/azhi/mushoku/magic/runtime/` 存放 Minecraft 适配代码，读取游戏对象、调用 core 并将结果写回世界。
- 日常运行 `./gradlew test`。交付前另行运行 `./gradlew runGameTestServer` 和 `python tests/contracts.py`。GameTest 使用独立源集 `src/gameTest/`。
