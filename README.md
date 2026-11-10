# Traveler's Backpack: Stack Upgrades

An addon for [Traveler's Backpack](https://www.curseforge.com/minecraft/mc-mods/travelers-backpack) (Fabric)
that adds five stack upgrades to the backpack sidebar. Each installed upgrade multiplies how
many items a single backpack slot can hold.

## Upgrades

| Upgrade | Multiplier | 64-stack items | 16-stack items | Non-stackable items |
|---|---|---|---|---|
| Starter | x1.5 | 96 | 24 | 1 (rounded down) |
| Tier 1 | x2 | 128 | 32 | 2 |
| Tier 2 | x4 | 256 | 64 | 4 |
| Tier 3 | x8 | 512 | 128 | 8 |
| Tier 4 | x16 | 1024 | 256 | 16 |

Slot capacity is `floor(item max stack size x total multiplier)`, capped at 1,073,741,824.

## How the multipliers combine

- Every installed stack upgrade multiplies the slot capacity again — the multipliers are
  **multiplicative, not additive**.
- Different tiers stack with each other: all five tiers installed give x1536, so a 64-stack
  item fits 98,304 per slot.
- The same tier can be installed more than once; six tier-4 upgrades (every upgrade slot of a
  netherite backpack) give 64 x 16^6 = **1,073,741,824 items per slot**.
- Duplicates can be disabled in the config; the cap can be lowered as well.

## Recipes

All recipes use vanilla materials plus Traveler's Backpack's blank upgrade. `X` is the metal
block of the tier, `S` is the previous upgrade (or the blank upgrade for tier 1).

```
Starter            Tier 1             Tier 1 (alt)
 C C C              I                X X X
 C B C            I S I              X S X
 C C C              I                X X X
```

| Result | Recipe |
|---|---|
| Starter | 8 copper blocks around 1 blank upgrade |
| Tier 1 | 1 starter + 4 iron ingots, or 8 iron blocks around 1 blank upgrade |
| Tier 2 | 8 gold blocks around 1 tier 1 |
| Tier 3 | 8 diamond blocks around 1 tier 2 |
| Tier 4 | 8 netherite blocks around 1 tier 3 |

## Installation

Install [Fabric Loader](https://fabricmc.net/use/) and
[Fabric API](https://modrinth.com/mod/fabric-api), then drop
[Traveler's Backpack](https://www.curseforge.com/minecraft/mc-mods/travelers-backpack) and this
mod into the `mods/` folder. Both the client and the server need the mod.

## Configuration

`config/tbstackupgrades.properties` is generated on first launch:

| Key | Default | Meaning |
|---|---|---|
| `allowNonStackableItems` | `true` | Let items with a max stack size of 1 (tools, armor) use the multiplier |
| `allowDuplicateStackUpgrades` | `true` | Allow installing the same tier more than once |
| `maxSlotLimit` | `1073741824` | Hard cap for a single slot (64 - 1073741824) |
| `configVersion` | `2` | Internal marker used for config migration; leave it alone |

## Behaviour

- Taking items out of the backpack is limited to one vanilla stack (64) at a time, so
  oversized stacks never end up in a player inventory. Inserting is not limited.
- The sort button keeps oversized stacks in place instead of splitting or merging them.
- An upgrade cannot be removed while a stack in the backpack is larger than the capacity that
  would remain after removing it; a red warning is drawn above the backpack screen instead.
- Hovering an item inside the backpack shows `amount / capacity` in cyan while the capacity is
  above 64.
- The placed backpack block uses the same storage and behaves identically.
- Items from other mods use their own max stack size as the base, so any stackable item is
  affected.
- Slot counts above three digits are scaled down so they stay inside the slot.

## Licence

Released under **LGPL-3.0-or-later OR MIT** (dual licence): the Mixin injection layer is
`LGPL-3.0-or-later OR MIT`, everything else is MIT. Both licence texts are in
[`LICENSE`](https://github.com/Huziyang520/TravelersBackpack-StackUpgrades/blob/main/LICENSE).

- [Traveler's Backpack](https://www.curseforge.com/minecraft/mc-mods/travelers-backpack) is
  licensed [LGPL-3.0](https://www.gnu.org/licenses/lgpl-3.0.html) and is a required dependency.
  None of its code, textures, models or language files are copied or bundled; it is loaded
  from the player's own installation, and its public API and Mixin targets are used at runtime.
- [Fabric API](https://modrinth.com/mod/fabric-api) (Apache-2.0) is a required dependency and is
  not bundled either.
- Gameplay is inspired by [Sophisticated Backpacks](https://www.curseforge.com/minecraft/mc-mods/sophisticated-backpacks)'
  stack upgrade (All Rights Reserved). No code, textures, models, sounds or language files from
  it are used, and no affiliation is implied.
- All of this mod's textures are original pixel art.

## Issues

<https://github.com/Huziyang520/TravelersBackpack-StackUpgrades/issues>

---

# 旅行者背包：堆叠升级

为 [旅行者背包](https://www.curseforge.com/minecraft/mc-mods/travelers-backpack)（Fabric）新增
五种堆叠升级插件，放进背包侧栏后即可提升背包内单个格子的叠放上限。

## 升级等级

| 升级 | 倍率 | 原本 64 个/组 | 原本 16 个/组 | 不可堆叠物品 |
|---|---|---|---|---|
| 入门 | ×1.5 | 96 | 24 | 1（向下取整） |
| 一级 | ×2 | 128 | 32 | 2 |
| 二级 | ×4 | 256 | 64 | 4 |
| 三级 | ×8 | 512 | 128 | 8 |
| 四级 | ×16 | 1024 | 256 | 16 |

单格上限 = `floor(物品自身最大堆叠数 × 总倍率)`，硬顶 1,073,741,824。

## 倍率叠加规则

- 每装一个堆叠升级就再乘一次倍率，即**乘算**而非加算；
- 不同等级之间同样相乘：五个等级全装 = ×1536，64 个/组的物品单格可放 98,304 个；
- 同一等级也可以重复安装：六个四级插件（下界合金背包的全部升级槽）= 64 × 16⁶ =
  单格 **1,073,741,824** 个；
- 重复安装与上限都可以在配置里关掉或调低。

## 合成配方

全部只使用原版材料与旅行者背包的空白升级。`X` 为该等级的金属块，`S` 为上一级升级
（一级也可用空白升级）。

```
入门               一级               一级（备用）
 C C C              I                X X X
 C B C            I S I              X S X
 C C C              I                X X X
```

| 产物 | 配方 |
|---|---|
| 入门 | 8 个铜块围 1 个空白升级 |
| 一级 | 1 个入门 + 4 个铁锭，或 8 个铁块围 1 个空白升级 |
| 二级 | 8 个金块围 1 个一级 |
| 三级 | 8 个钻石块围 1 个二级 |
| 四级 | 8 个下界合金块围 1 个三级 |

## 安装

先装好 [Fabric Loader](https://fabricmc.net/use/) 与 [Fabric API](https://modrinth.com/mod/fabric-api)，
再把 [旅行者背包](https://www.curseforge.com/minecraft/mc-mods/travelers-backpack) 与本 MOD
一起放进 `mods/` 目录。客户端与服务端都要安装。

## 配置

首次启动生成 `config/tbstackupgrades.properties`：

| 键 | 默认值 | 说明 |
|---|---|---|
| `allowNonStackableItems` | `true` | 最大堆叠数为 1 的物品（工具、护甲）是否也吃倍率 |
| `allowDuplicateStackUpgrades` | `true` | 同等级的堆叠升级是否允许重复安装 |
| `maxSlotLimit` | `1073741824` | 单格上限硬顶（64–1073741824） |
| `configVersion` | `2` | 配置迁移用的内部标记，不要手改 |

## 行为说明

- 从背包取出时每次最多取一个原版堆叠（64 个），剩余留在格子里，玩家背包里不会出现超量堆叠；
  放入不受此限制。
- 排序按钮不会拆分或合并超大堆叠，会保持整组待在原格子里。
- 背包内若还有超过「拆下后上限」的堆叠，点击侧栏图标拆下插件会被拒绝，并在背包界面上方
  （空间不足时下方）显示红字提示。
- 悬停背包内物品时，提示里多出青色的 `数量 / 上限`，仅在装了升级且上限高于 64 时显示。
- 背包放地上打开时共用同一套存储，行为一致。
- 其它 MOD 的可堆叠物品按自身最大堆叠数计算，同样获得提升。
- 单格数量超过 3 位时数字会等比缩小，不会盖住相邻格子。

## 许可证

本 MOD 采用 **LGPL-3.0-or-later OR MIT 双许可**：Mixin 注入层为
`LGPL-3.0-or-later OR MIT`，其余全部代码与资源为 MIT，两份协议全文见
[`LICENSE`](https://github.com/Huziyang520/TravelersBackpack-StackUpgrades/blob/main/LICENSE)。

- [旅行者背包](https://www.curseforge.com/minecraft/mc-mods/travelers-backpack) 采用
  [LGPL-3.0](https://www.gnu.org/licenses/lgpl-3.0.html)，是必需前置；本 MOD **不复制、不打包**
  它的任何代码、贴图、模型或语言文件，运行时由玩家自行安装，只通过其公开 API 与 Mixin 注入点协作。
- [Fabric API](https://modrinth.com/mod/fabric-api)（Apache-2.0）同为必需前置，同样不打包。
- 玩法灵感来自 [精妙背包](https://www.curseforge.com/minecraft/mc-mods/sophisticated-backpacks)
  的堆叠升级（All Rights Reserved）；未使用其任何代码、贴图、模型、音效或语言文件，
  也不暗示任何官方关联。
- 本 MOD 的全部贴图均为原创像素画。

## 问题反馈

<https://github.com/Huziyang520/TravelersBackpack-StackUpgrades/issues>
