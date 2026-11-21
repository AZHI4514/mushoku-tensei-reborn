#!/usr/bin/env python3
"""契约脚本：资源完整性对账。

不测游戏逻辑，只测「文件之间对不对得上」。
退出码 0=通过，1=有缺口（交付前必须为 0）。

用法: python tests/contracts.py [--root PATH]
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path

MOD_ID = "mushoku_tensei_reborn"
PKG = "com/azhi/mushoku"


def check(root: Path) -> tuple[list[str], list[str]]:
    errors: list[str] = []
    notes: list[str] = []
    assets = root / "src/main/resources/assets" / MOD_ID
    data = root / "src/main/resources/data" / MOD_ID

    # 1. 目录骨架
    for d in ("docs", "docs/modules", "docs/setting-bible", "tests",
              "src/main/java", assets, data):
        if not (root / d).is_dir():
            errors.append(f"缺少目录: {d}")

    # 2. 必读文档齐备且非空
    for f in ("AGENTS.md",
              "docs/VISION.md", "docs/PRINCIPLES.md", "docs/ROADMAP.md",
              "docs/modules/magic.md"):
        p = root / f
        if not p.is_file():
            errors.append(f"缺少文档: {f}")
        elif p.stat().st_size < 200:
            errors.append(f"文档过短（可能被清空）: {f}")
    if not errors:
        notes.append("必读链文档齐备")

    # 3. 优先级声明必须存在于 PRINCIPLES
    principles = root / "docs/PRINCIPLES.md"
    if principles.is_file():
        text = principles.read_text(encoding="utf-8", errors="ignore")
        if "优先级" not in text or ">" not in text:
            errors.append("PRINCIPLES.md 缺少优先级声明")
        else:
            notes.append("优先级声明存在")

    # 4. 设定集不得出现在必读链（AGENTS.md 不应把它列为必读文档）
    agents = root / "AGENTS.md"
    if agents.is_file():
        text = agents.read_text(encoding="utf-8", errors="ignore")
        if re.search(r"必读[^\n]*setting-bible", text):
            errors.append("AGENTS.md 把 setting-bible 标为必读（违反分层规则）")
        if "已实现边界" not in text:
            errors.append("AGENTS.md 缺少「已实现边界」小节")
        else:
            notes.append("已实现边界小节存在")

    # 5. lang 文件 key 数一致
    lang_dir = assets / "lang"
    counts: dict[str, int] = {}
    if lang_dir.is_dir():
        for f in sorted(lang_dir.glob("*.json")):
            try:
                counts[f.name] = len(json.loads(f.read_text(encoding="utf-8")))
            except Exception as e:
                errors.append(f"lang/{f.name} 解析失败: {e}")
        if len(set(counts.values())) > 1:
            errors.append(f"lang 文件 key 数不一致: {counts}")
        elif counts:
            notes.append(f"lang key 数一致 {counts}")

    # 6. 模型引用的贴图存在
    tex = assets / "textures"
    models = list((assets / "models").rglob("*.json")) if (assets / "models").is_dir() else []
    for m in models:
        try:
            body = json.loads(m.read_text(encoding="utf-8"))
        except Exception as e:
            errors.append(f"模型解析失败 {m.name}: {e}")
            continue
        for ref in (body.get("textures") or {}).values():
            if isinstance(ref, str) and ":" not in ref and not (tex / f"{ref}.png").is_file():
                errors.append(f"{m.name} 贴图缺失: textures/{ref}.png")

    # 7. 注册 id ↔ 模型 / lang 对账
    reg_src = root / "src/main/java" / PKG / "registry"
    if reg_src.is_dir():
        ids: set[str] = set()
        for f in reg_src.glob("*.java"):
            ids.update(re.findall(r'register\w*\(\s*"([a-z0-9_]+)"', f.read_text(encoding="utf-8")))
        lang_all: dict[str, set[str]] = {}
        if lang_dir.is_dir():
            for lf in lang_dir.glob("*.json"):
                lang_all[lf.name] = set(json.loads(lf.read_text(encoding="utf-8")).keys())
        for i in sorted(ids):
            if not (assets / "models/item" / f"{i}.json").is_file():
                errors.append(f"注册物品缺模型: models/item/{i}.json")
            for name, keys in lang_all.items():
                if f"item.{MOD_ID}.{i}" not in keys:
                    errors.append(f"注册物品缺 lang: item.{MOD_ID}.{i} ({name})")
        if ids:
            notes.append(f"对账 {len(ids)} 个注册物品")

    # 8. 法术表 id 与 datapack 文件一致
    magic = root / "docs/modules/magic.md"
    spell_dir = data / "spell"
    if magic.is_file() and spell_dir.is_dir():
        text = magic.read_text(encoding="utf-8", errors="ignore")
        listed = set(re.findall(r"`([a-z_]+)`", text))
        listed = {s for s in listed if "_" in s and not s.endswith(".md")}
        files = {p.stem for p in spell_dir.glob("*.json")}
        for s in sorted(listed & files or set()):
            pass
        missing = {s for s in files if s not in text}
        for s in sorted(missing):
            errors.append(f"datapack 法术 {s}.json 未在模块文档法术表登记")
        if files:
            notes.append(f"法术文件 {len(files)} 个与文档对账")

    return errors, notes


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".")
    args = ap.parse_args()
    root = Path(args.root).resolve()
    print(f"契约检查 mod={MOD_ID} root={root}")
    errors, notes = check(root)
    for n in notes:
        print(f"  ✓ {n}")
    if errors:
        print(f"\n{len(errors)} 个缺口:")
        for e in errors:
            print(f"  ✗ {e}")
        return 1
    print("\n全部通过。")
    return 0


if __name__ == "__main__":
    sys.exit(main())
