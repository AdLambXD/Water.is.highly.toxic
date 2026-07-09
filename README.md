# WaterIsHighlyToxic

> **"水是剧毒的"** —— 来源于《三体》的梗
>
> 一个 Folia 1.21.x 兼容的 Minecraft 插件：玩家在水中会受到随时间加重的惩罚。

## 简介

玩家接触水的时间越长，受到的惩罚越严重。离开水后计时会逐渐衰减，重新进水则继续累加。

默认有 4 个惩罚阶段，每个阶段的效果可在 `config.yml` 中自由配置。

## 默认惩罚阶段

| 阶段 | 触发时间 | 效果 |
|------|----------|------|
| 1 | 浸水 3 秒 | 反胃（眩晕）+ 失明 |
| 2 | 浸水 7 秒 | 中毒 + 警告消息 |
| 3 | 浸水 12 秒 | 凋零 + 召唤溺尸（2 只） |
| 4 | 浸水 20 秒 | 扣血 4❤ + 概率雷劈 + 杀戮命令 |

## 特性

- **Folia 原生兼容** — 使用 Folia 区域感知调度器，多线程安全
- **全配置化** — 每个阶段的触发时间、效果类型、参数均可在 `config.yml` 中自定义
- **6 种惩罚效果** — 药水 / 扣血 / 召唤 / 闪电 / 命令 / 消息
- **计时衰减系统** — 离开水后计时逐渐衰减，完全归零前重新进水继续累加
- **完整边界处理** — 死亡 / 重生 / 换世界 / 退出登录 / 入服即在水中 均有处理
- **PlaceholderAPI 支持** — 提供浸水时间、阶段等级等变量
- **权限控制** — `watertoxic.bypass` 可让指定玩家免疫，`watertoxic.admin` 管理命令

## 安装

1. 下载 `WaterIsHighlyToxic-1.0.0.jar`
2. 放入服务器 `plugins/` 目录
3. 重启服务器或 `/reload`
4. 编辑 `plugins/WaterIsHighlyToxic/config.yml` 修改配置
5. 使用 `/watertoxic reload` 重载配置

## 命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/watertoxic reload` | `watertoxic.admin` | 重载配置文件 |
| `/watertoxic status [玩家]` | `watertoxic.admin` | 查看玩家的浸水状态和当前阶段 |

别名：`/wt` `/waterex`

## 权限

| 权限节点 | 默认 | 说明 |
|---------|------|------|
| `watertoxic.admin` | op | 允许使用管理命令 |
| `watertoxic.bypass` | false | 完全免疫水的毒性效果 |

## PlaceholderAPI 变量

| 变量 | 类型 | 说明 |
|------|------|------|
| `%watertoxic_time%` | 浮点数 | 当前累计浸水秒数（如 `12.5`） |
| `%watertoxic_stage%` | 整数 | 当前已达成的最高阶段（0 ~ 4） |
| `%watertoxic_inwater%` | yes/no | 玩家当前是否在水中 |
| `%watertoxic_tick%` | 整数 | 当前累计浸水 tick 数 |

### 使用示例（PlaceholderAPI）

- `%watertoxic_time%s` → `12.5s`
- `%watertoxic_stage%` → `3`
- 在计分板、标题、NPC 对话等 PAPI 支持的地方均可使用

## 配置说明

完整注释见 `config.yml`，此处列出关键配置项：

```yaml
# 检测间隔（tick，20 = 1秒）
check-interval: 10

# 衰减倍率（1.0 = 和累积等速衰减）
decay-multiplier: 1.0

# 世界过滤（留空=全部）
enabled-worlds: []
disabled-worlds: []
```

每个阶段支持以下效果类型：

| 类型 | 参数 | 说明 |
|------|------|------|
| `POTION` | `effect`, `duration`, `amplifier` | 给予药水效果 |
| `DAMAGE` | `amount` | 直接扣血（半心） |
| `LIGHTNING` | `chance` | 概率召唤闪电 |
| `SUMMON` | `entity`, `amount`, `radius` | 召唤实体 |
| `COMMAND` | `command` | 执行控制台命令 |
| `MESSAGE` | `message` | 发送聊天消息 |

## 构建

```bash
./gradlew shadowJar
```

产物位于 `build/libs/WaterIsHighlyToxic-1.0.0.jar`

## 依赖

- **必须**: Folia 1.21.x 或 Paper 1.21.x
- **可选**: PlaceholderAPI 2.11+（开启变量支持）

## 许可

MIT
