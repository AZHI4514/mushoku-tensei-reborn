# mushoku_tensei_reborn — 项目初始化包

《无职转生》主题 Minecraft NeoForge mod。第一模块：魔术系统。

本目录是**纯文档骨架**：不含代码与构建文件。Java/Gradle 侧由「切片 0」
（项目骨架）的 plan 生成。

## 文件清单

```
AGENTS.md                  入口：技术栈、命令、已实现边界、禁止事项、切片顺序
docs/VISION.md             宪法·愿景（核心命题 + 差异化 + 玩家节奏）
docs/PRINCIPLES.md         宪法·9 条裁决原则 + 优先级声明
docs/HOW-TO-WORK.md         人类操作手册（怎么指挥 AI，你必读）
docs/ROADMAP.md            模块顺序 + 设定→模块映射 + 切片路线 + 明确不做
docs/modules/magic.md      魔术模块：接口/等级矩阵/法术表/混合图/状态机/待决问题
docs/setting-bible/        原作设定原文（7 份，**不进必读链**）
tests/contracts.py         契约脚本（文档齐备/优先级/注册对账/法术对账）
docs/specs/ docs/plans/    空，等切片填充
```

## 设定是怎么切分的

七份设定没有直接塞进项目，而是按「这层文档该放什么」分流：

- **魔术设定 + 其他设定（魔力/咏唱部分）** → 提炼进 `docs/modules/magic.md`，
  并区分了【固】设定固定 /【决】本模块决策 /【待】未定。
- **装备、剑术、种族、魔物、地图** → 各自归入 ROADMAP 的后续模块，
  现在只登记映射关系，不建文档（没文档就写代码 = 违反优先级）。
- **七份原文** → `docs/setting-bible/` 存档。不进必读链，AI 不会把它
  当需求读。要查「原作怎么写」时才翻。

这就是「设定集 ≠ 模块文档」的落地：设定是资料，模块文档是可执行决策。

## 落地到开发机

1. 拷到开发机，`git init` + 首次提交（纯文档）。
2. 装 Temurin JDK 21 + IntelliJ。
3. 让 agent 读 AGENTS.md，下达第一片任务：
   「写 2026-XX-XX 切片0 项目骨架的 spec+plan 并实施，完成后
   `python tests/contracts.py` 必须通过」。

## 还没定的（都有标记，不用现在决定）

`docs/modules/magic.md` 第 10 节列了 6 个待决问题，每条由对应切片的
spec 裁决后回写模块文档。现在不需要答案。
