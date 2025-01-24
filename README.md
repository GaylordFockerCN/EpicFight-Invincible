# Invincible(无坚不摧）

## 致谢

感谢SettingDust，Cyber2049，和dfdyz的指导和帮助

## 简介

这是一个方便代码开发者和数据包开发者注册史诗战斗连击数据的支持库，可以很方便地构造新的武器模板和新的“技能”。本模组提供了四个按键，你可以对他们进行自由连招组合，每个连段最多支持两个键一起按下。通俗地讲就是，你可以做出ABAB，AABB这样的连招。同时也像坚不可摧一样支持许多特定功能。

## 代码示例

原理很简单，就是把武器连段数据作为一个技能存下来，并且替换掉原来的WeaponInnate技能，然后自己写输入处理，然后再发起技能执行请求。输入部分实现了延迟发包和预存输入，支持双键。[输入请求原理](https://github.com/GaylordFockerCN/EpicFight-Invincible/blob/master/src/main/java/com/p1nero/invincible/client/events/InputHandler.java)

[技能模板注册](https://github.com/GaylordFockerCN/EpicFight-Invincible/blob/master/src/main/java/com/p1nero/invincible/gameassets/InvincibleSkills.java)

[武器模板注册](https://github.com/GaylordFockerCN/EpicFight-Invincible/blob/master/src/main/java/com/p1nero/invincible/gameassets/InvincibleWeaponCapabilityPresets.java)

## 数据包示例

首先需要注册连段数据，本质上是注册一个新的技能：[示例](https://github.com/GaylordFockerCN/EpicFight-Invincible/tree/master/src/main/resources/data/invincible/capabilities/weapons/invincible_combos/demo.json)

接下来，和常规的武器模板一样，注册武器模板：[示例](https://github.com/GaylordFockerCN/EpicFight-Invincible/tree/master/src/main/resources/data/invincible/capabilities/weapons/types/datapack_demo.json)

需要注意的是，模板里的combos不应该放空，需要随便填一些，因为模板还是依赖于原本的方式。注意style要对应上，然后把上面注册的连段数据的
mod_id:name 填入模板的 innate skill

接下来就是应用到自己的武器上：[示例](https://github.com/GaylordFockerCN/EpicFight-Invincible/tree/master/src/main/resources/data/invincible/capabilities/weapons/datapack_debug.json)

## 目前提供的命令

* _/invincible entityAfterImage entity_ 在实体的位置留下残影。


* _/invincible groundSlam entity radius noSound noParticle hurtEntities_ 在实体的位置生成裂地效果后面四个为布尔值。


* _/invincible setPlayerPhase value_ 设置自定义玩家阶段，结合["invincible:phase"](#目前支持的condition解释)使用


* _/invincible setStack value_ 设置技能层数


* _/invincible consumeStack value_ 消耗技能层数


* _/invincible setConsumption value_ 设置充能值


* _/invincible consumeConsumption value_ 消耗充能值

## 目前提供的配置项

即config/invincible-common.toml

* _reset_tick_ 重置连段的时间，默认16tick


* _reserve_tick_ 预存输入时间，仅客户端有效，默认8tick


* _input_delay_tick_ 输入延迟时间，仅客户端有效，默认4tick

## 数据包目前支持的参数解释

如果你会使用indestructible（坚不可摧），那应该能很快上手

* "key"：
  本次动画所需的按键，共支持KEY_1, KEY_2, KEY_3, KEY_4, KEY_1_2, KEY_1_3, KEY_1_4, KEY_2_3, KEY_2_4, KEY_3_4;
  KEY_1_2代表KEY_1和KEY_2同时按下


* "animation"：
  本次播放的动画，某些动画可能由于它自身对武器的限制而无法使用。


* "speed_multiplier"：
  整数，本次攻击动画的播放倍速，仅对AttackAnimation有效。**值得注意的是，玩家的攻击动画播放速度受武器攻速的影响。**


* "convert_time"：
  整数，本次动画的过渡时间


* "damage_multiplier"：
  修改本次攻击动画的伤害值。共有setter，multiplier，adder可选。**值得注意的是，会替换动画原先的伤害倍率，并非叠加，但不影响武器基础数值。**


* "impact_multiplier"：
  修改本次攻击动画的冲击倍率，**值得注意的是，会替换动画原先的冲击倍率，而非叠加。**


* "stun_type"：
  修改本次攻击动画的硬直类型，共有NONE，SHORT，LONG，HOLD，KNOCKDOWN，NEUTRALIZE和FALL可选。具体含义请参考史诗战斗wiki。


* "hurt_damage_multiplier"：
  修改动画期间受到伤害的值。


* "set_phase"：
  整数，本次动画开始播放后，玩家进入的阶段。配合自定义条件中的 ["invincible:phase"](#目前支持的condition解释)使用


* "cooldown"：
  整数，本次动画开始播放后，玩家进入的冷却tick数。配合自定义条件中的 ["invincible:cooldown"](#目前支持的condition解释)使用


* "not_charge"：
  布尔值，本次动画造成伤害不会进行充能。适合作为技能的动画使用。


* "time_command_list"：
  时间戳事件列表，在本次动画播放的某个时间点执行。
  **受到播放速度的影响。**
  对于每一个元素，
    1. "time": 播放的时间点，为浮点数
    2. "command": 执行的命令，为字符串
    3. "execute_at_target": 是否在目标身上执行，为布尔值


* "hurt_command_list"：
  玩家受击事件列表，玩家在动画播放期间受击时执行
    1. "command": 执行的命令，为字符串
    2. "execute_at_target": 是否在目标身上执行，为布尔值


* "dodge_success_command_list"：
  玩家成功攻击事件列表，玩家在动画播放期间成功造成伤害时执行。

  参数同上。


* "dodge_success_command_list"：
  玩家闪避成功事件列表，玩家在动画播放期间闪避成功时执行。本次动画应为闪避动画，否则无效。

  参数同上。


* "conditions"：
  本次执行的条件限制。值得注意的是,无法根据不同condition控制一个输入播不同动画。如有需要请使用[condition_animations](#特殊参数-condition_animations)
    1. "predicate"： 本条件的类型，支持所有史诗战斗的Condition，也包括坚不可摧的Condition。
    2. 各个predicate所需的参数，invincible提供的将会在下面介绍。其他模组的condition请参阅他们的文档。

## 目前支持的Condition解释

此处列出的并非可用的，史诗战斗以及其他附属的Condition亦可使用。 [本附属使用的Conditions](https://github.com/GaylordFockerCN/EpicFight-Invincible/blob/master/src/main/java/com/p1nero/invincible/gameassets/InvincibleConditions.java)

* invincible:jumping
  玩家是否处于跳跃
  无参数，玩家在跳跃状态时执行。由于本技能会顶掉原版的跳跃攻击和冲刺攻击，所以以此作为补偿。


* invincible:sprinting
  玩家是否处于冲刺状态
  无参数，玩家在冲刺时执行。由于本技能会顶掉原版的跳跃攻击和冲刺攻击，所以以此作为补偿。


* invincible:stack_count
  检测玩家技能层数，一般结合命令 ["/invincible consumeStack 1"](#目前提供的命令) 来使用
    1. "min"：整数，玩家技能栈的最小值
    2. "max"：整数，玩家技能栈的最大值


* invincible:phase
  检测玩家自定义状态
    1. "min"：整数，玩家自定义状态的最小值
    2. "max"：整数，玩家自定义状态的最大值


* invincible:target_blocking
  检测目标是否在防御状态，仅与坚不可摧一起安装时有效
    1. "is_parry"：布尔值，true则请求是否处于完美招架状态。false则判断目标是否在防御。


* invincible:cooldown
  检测技能的冷却状态，一般与 ["cooldown"](#数据包目前支持的参数解释) 合用
    1. "inCooldown"：布尔值，请求玩家是否处于冷却。一般用false

## 特殊参数 "condition_animations"

参考[示例 第101行](https://github.com/GaylordFockerCN/EpicFight-Invincible/tree/master/src/main/resources/data/invincible/capabilities/weapons/invincible_combos/demo.json#L101)

此参数用于控制一个按键在不同的条件组下播放不同的动画。当condition_animations参数存在时，同级参数仅 "key" 和 "combos"
有效，其余会被忽略。

"condition_animations"
中的每一个元素，都可以使用上面所有参数。**值得注意的是，每个元素中也可以存在combos数组**，当每个对象中存在combos参数。当combos参数存在时，下一个连招表将以执行的动画同级的combos为准。若不存在则执行外层combos。