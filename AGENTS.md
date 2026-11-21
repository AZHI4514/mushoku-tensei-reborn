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

## 命令

```bash
./gradlew build          # 编译打包 → build/libs
./gradlew runClient      # 开发客户端
./gradlew runServer      # 无头服务端
./gradlew runData        # datagen（模型/方块状态/语言文件）
python tests/contracts.py  # 契约检查，交付前必须为 0
```

Windows 用 `./gradlew.bat`。

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

**当前：切片 0 项目骨架。** 已配置 Java 21、Gradle Wrapper 和 ModDevGradle，模组入口 Mushoku 挂接空物品注册表 ModItems。

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
切片1 魔力值 + 一个法术 + 咏唱施放（最小闭环）
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
