# 切片 1a · 魔力可见

2026-11-21 · 待审

## 硬约束

1. 玩家死亡后，重生时魔力重置为满。
2. 魔力上限固定为 100，本片写死在 core。上限成长留到切片 2。

## 范围

玩家首次加入时拥有 100 点魔力。服务器保存每位玩家的当前值，玩家在自己的快捷栏上方看到魔力条。调试者可以查询或修改一个在线玩家的魔力，并用 HUD 核对结果。

本片排除施法、按键、模组自定义网络包、法术、物品、粒子和 GeckoLib。扣减与回复仅定义纯计算函数，本片没有自然回复或周期扣减。

## 分层与数据流

| 层 | 职责 |
|---|---|
| core · `magic/core/` | 提供固定上限、当前值钳制、扣减和回复。函数只使用基本类型 `int` 作为入参和返回值，零 Minecraft、NeoForge 依赖。钳制区间为闭区间 [0, 100]。扣减和回复以已钳制的当前值为起点，数量为非负整数，结果分别饱和于 0 和 100，大额操作也不得溢出回绕。 |
| runtime · 服务端适配 | 注册玩家附件，在服务端初始化、读取、写入及持久化。命令写入前调用 core 钳制，并将最终值同步给目标玩家。死亡重生时恢复满值并同步；正常退出重进读取存档。 |
| runtime · 客户端适配 | HUD 渲染仅在物理客户端加载，只读本地玩家的已同步附件。快捷栏上方绘制魔力条，填充比例为当前值除以上限，同时显示“当前值 / 100”，便于核对。同步到达后下一次 HUD 绘制即反映新值。 |

### NeoForge 1.21.1 API 核对

核对依据为本项目构建产出的 `build/moddev/artifacts/neoforge-21.1.267-sources.jar` 中的 `AttachmentType.java`、`IAttachmentHolder.java`、`AttachmentSyncHandler.java` 与 `NeoForgeRegistries.java`。

- 实际类型为 `net.neoforged.neoforge.attachment.AttachmentType<Integer>`。为 `NeoForgeRegistries.Keys.ATTACHMENT_TYPES` 建立 `DeferredRegister`，在模组事件总线上挂接，登记已定名的 `mushoku_tensei_reborn:mana`。
- 用 `AttachmentType.builder` 提供满值默认值，使用 `serialize(Codec.INT)` 保存当前值。保留默认的死亡复制策略（不启用 `copyOnDeath()`）。服务端在首次进入和死亡重生时确保附件已初始化，同步重生后的新值。
- `getData` 可在缺少附件时创建默认值，`setData` 写入服务端权威值。通过构建器的 `sync(BiPredicate, StreamCodec)` 使用 NeoForge 内建附件同步，接收者限定为持有该附件的玩家。客户端等待同步副本后绘制，避免在登录期间把默认值误显示为存档值。

## 调试命令

本规格采用管理员权限等级 2，目标为单个在线玩家。两条命令均在服务端执行。

| 命令 | 行为与反馈 |
|---|---|
| `/mushoku mana get <player>` | 返回目标玩家名和服务端的“当前值 / 100”。 |
| `/mushoku mana set <player> <value>` | 接收有符号 int，先钳制再写入，返回目标玩家名和实际写入的值。越过魔力边界的整数也进入钳制流程。 |

## 游戏内验收

在开启作弊的测试世界或有命令权限的服务器执行。以下用 A、B 代表实际在线玩家名。

1. WHEN 首次进入该存档的 A 看见快捷栏，SHALL 在其上方看到满条和 100 / 100，且 `/mushoku mana get A` 返回相同数值。
2. WHEN 管理员执行 `/mushoku mana set A 37`，SHALL 得到写入值 37；A 保持在游戏中即可看到 HUD 更新为 37 / 100，条长为满条的 37%。随后 get 返回 37 / 100。
3. WHEN A 在魔力为 37 时正常退出并重新进入同一存档，SHALL 在 HUD 读到 37 / 100，get 也返回 37 / 100。
4. WHEN 管理员依次执行 `/mushoku mana set A -1` 和 `/mushoku mana set A 101`，SHALL 在两次操作后分别观察到空条 0 / 100 和满条 100 / 100，且各次 get 与 HUD 一致。
5. WHEN A 在魔力为 37 时死亡并点击重生，SHALL 在重生后的 HUD 看到满条 100 / 100，且 get 返回 100 / 100。
6. WHEN A 和 B 同时在线且 B 为 80 点，管理员将 A 设为 20，SHALL 看到 A 的 HUD 为 20 / 100，B 的 HUD 仍为 80 / 100；分别查询两人得到对应值。
