package com.p1nero.invincible.capability.item;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import com.p1nero.invincible.api.skill.ComboNode;
import com.p1nero.invincible.gameassets.InvincibleSkills;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

public class InvincibleWeaponCapability extends CapabilityItem {

    protected final Map<Style, ComboNode> comboMap;
    protected final int consumption;
    protected final int maxStacks;
    protected final List<String> skillDescriptions;
    protected final boolean drawSkillIcon;
    protected final int maxPressTime;
    protected final int maxReserveTime;
    protected final int maxProtectTime;
    protected final int resetTime;
    protected final ResourceLocation skillTextureLocation;

	protected final Function<LivingEntityPatch<?>, Style> stylegetter;
	
	protected final Function<LivingEntityPatch<?>, Boolean> weaponCombinationPredicator;
	
	protected final Skill passiveSkill;
	protected final SoundEvent smashingSound;
	protected final SoundEvent hitSound;
	protected final HitParticleType hitParticle;

	protected final Map<Style, List<AnimationAccessor<? extends AttackAnimation>>> autoAttackMotions;
	
	protected final Map<Style, Function<ItemStack, Skill>> innateSkill;
	
	protected final Map<Style, Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>>> livingMotionModifiers;
	protected final boolean canBePlacedOffhand;
	protected final ZoomInType zoomInType;
	protected final float reach;

    protected Set<ResourceLocation> customTags;
	
	protected InvincibleWeaponCapability(CapabilityItem.Builder builder) {
		super(builder);
		
		InvincibleWeaponCapability.Builder weaponBuilder = (InvincibleWeaponCapability.Builder)builder;

		this.autoAttackMotions = weaponBuilder.autoAttackMotionMap;
        comboMap = weaponBuilder.comboMap;
		this.consumption = weaponBuilder.consumption;
		this.maxStacks = weaponBuilder.maxStacks;
		this.skillDescriptions = List.copyOf(weaponBuilder.skillDescriptions);
		this.drawSkillIcon = weaponBuilder.drawSkillIcon;
		this.maxPressTime = weaponBuilder.maxPressTime;
		this.maxReserveTime = weaponBuilder.maxReserveTime;
		this.maxProtectTime = weaponBuilder.maxProtectTime;
		this.resetTime = weaponBuilder.resetTime;
		this.skillTextureLocation = weaponBuilder.skillTextureLocation;
		this.innateSkill = weaponBuilder.innateSkillByStyle;
		this.livingMotionModifiers = weaponBuilder.livingMotionModifiers;
		this.stylegetter = weaponBuilder.styleProvider;
		this.weaponCombinationPredicator = weaponBuilder.weaponCombinationPredicator;
		this.passiveSkill = weaponBuilder.passiveSkill;
		this.smashingSound = weaponBuilder.swingSound;
		this.hitParticle = weaponBuilder.hitParticle;
		this.hitSound = weaponBuilder.hitSound;
		this.canBePlacedOffhand = weaponBuilder.canBePlacedOffhand;
		this.zoomInType = weaponBuilder.zoomInType;
		this.reach = weaponBuilder.reach;
		this.customTags = Collections.unmodifiableSet(weaponBuilder.customTags);
	}
	
	@Override
	public final List<AnimationAccessor<? extends AttackAnimation>> getAutoAttackMotion(PlayerPatch<?> playerpatch) {
		return this.autoAttackMotions.getOrDefault(this.getStyle(playerpatch), this.autoAttackMotions.get(Styles.COMMON));
	}

    public ComboNode getCombo(Style style) {
        return this.comboMap.get(style);
    }

    public int getConsumption() {
        return this.consumption;
    }

    public int getMaxStacks() {
        return this.maxStacks;
    }

    public List<String> getSkillDescriptions() {
        return this.skillDescriptions;
    }

    public boolean shouldDrawSkillIcon() {
        return this.drawSkillIcon;
    }

    public int getMaxPressTime() {
        return this.maxPressTime;
    }

    public int getMaxReserveTime() {
        return this.maxReserveTime;
    }

    public int getMaxProtectTime() {
        return this.maxProtectTime;
    }

    public int getResetTime() {
        return this.resetTime;
    }

    public ResourceLocation getSkillTextureLocation() {
        return this.skillTextureLocation;
    }
	
	@Override
	public final Skill getInnateSkill(PlayerPatch<?> playerpatch, ItemStack itemstack) {
		Function<ItemStack, Skill> innateProvider = this.innateSkill.getOrDefault(this.getStyle(playerpatch), this.innateSkill.get(Styles.COMMON));
		return innateProvider == null ? null : innateProvider.apply(itemstack);
	}


	@Override 
	public Skill getPassiveSkill() {
		return this.passiveSkill;
	}
	
	@Override 
	public final List<AnimationAccessor<? extends AttackAnimation>> getMountAttackMotion() {
		return this.autoAttackMotions.get(Styles.MOUNT);
	}
	
	@Override
	public Style getStyle(LivingEntityPatch<?> entitypatch) {
		return this.stylegetter.apply(entitypatch);
	}
	
	@Override
	public SoundEvent getSmashingSound() {
		return this.smashingSound;
	}
	
	@Override
	public SoundEvent getHitSound() {
		return this.hitSound;
	}
	
	@Override
	public HitParticleType getHitParticle() {
		return this.hitParticle;
	}
	
	@Override
	public boolean canBePlacedOffhand() {
		return this.canBePlacedOffhand;
	}
	
	@Override
	public ZoomInType getZoomInType() {
		return this.zoomInType;
	}
	
	@Override
	public Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>> getLivingMotionModifier(LivingEntityPatch<?> player, InteractionHand hand) {
		if (this.livingMotionModifiers == null || hand == InteractionHand.OFF_HAND) {
			return super.getLivingMotionModifier(player, hand);
		}
		
		Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>> motions = this.livingMotionModifiers.getOrDefault(this.getStyle(player), Maps.newHashMap());
		this.livingMotionModifiers.getOrDefault(Styles.COMMON, Maps.newHashMap()).forEach(motions::putIfAbsent);
		
		return motions;
	}
	
	@Override
	public UseAnim getUseAnimation(LivingEntityPatch<?> playerpatch) {
		if (this.livingMotionModifiers != null) {
			Style style = this.getStyle(playerpatch);
			
			if (this.livingMotionModifiers.containsKey(style)) {
				if (this.livingMotionModifiers.get(style).containsKey(LivingMotions.BLOCK)) {
					return UseAnim.BLOCK;
				}
			}
		}
		
		return UseAnim.NONE;
	}
	
	@Override
	public boolean canHoldInOffhandAlone() {
		return false;
	}
	
	@Override
	public boolean checkOffhandValid(LivingEntityPatch<?> entitypatch) {
		return super.checkOffhandValid(entitypatch) || this.weaponCombinationPredicator.apply(entitypatch);
	}
	
	@Override 
	public boolean availableOnHorse() {
		return this.autoAttackMotions.containsKey(Styles.MOUNT);
	}
	
	@Override
	public float getReach() {
		return this.reach;
	}
	
	public boolean hasMatchingTag(ResourceLocation rl) {
        return customTags.contains(rl);
    }

    public Set<ResourceLocation> getTags() {
        return customTags;
    }
	
	public static InvincibleWeaponCapability.Builder builder() {
		return new InvincibleWeaponCapability.Builder();
	}
	
	public static class Builder extends CapabilityItem.Builder {
		
		Function<LivingEntityPatch<?>, Style> styleProvider;
		
		Function<LivingEntityPatch<?>, Boolean> weaponCombinationPredicator;
		
		Skill passiveSkill;
		SoundEvent swingSound;
		SoundEvent hitSound;
		HitParticleType hitParticle;
		
		Map<Style, List<AnimationAccessor<? extends AttackAnimation>>> autoAttackMotionMap;
        Map<Style, ComboNode> comboMap;
		
		Map<Style, Function<ItemStack, Skill>> innateSkillByStyle;
		
		Map<Style, Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>>> livingMotionModifiers;
		Function<Style, Boolean> comboCancel;
		int consumption;
		int maxStacks;
		List<String> skillDescriptions;
		boolean drawSkillIcon;
		int maxPressTime;
		int maxReserveTime;
		int maxProtectTime;
		int resetTime;
		ResourceLocation skillTextureLocation;
		boolean canBePlacedOffhand;
		ZoomInType zoomInType;
		float reach;
		Set<ResourceLocation> customTags = new HashSet<> ();
		
		protected Builder() {
			this.constructor(InvincibleWeaponCapability::new);
			this.styleProvider = (entitypatch) -> Styles.ONE_HAND;
			this.weaponCombinationPredicator = (entitypatch) -> false;
			this.passiveSkill = null;
			this.swingSound = EpicFightSounds.WHOOSH.get();
			this.hitSound = EpicFightSounds.BLUNT_HIT.get();
			this.hitParticle = EpicFightParticles.HIT_BLADE.get();
			this.autoAttackMotionMap = Maps.newHashMap();
            this.autoAttackMotionMap.put(Styles.COMMON, List.of(Animations.SWORD_AUTO1,  Animations.SWORD_AUTO2,  Animations.SWORD_AUTO3, Animations.SWORD_DASH, Animations.SWORD_AIR_SLASH));
			this.comboMap = Maps.newHashMap();
			this.innateSkillByStyle = Maps.newHashMap();
            this.innateSkillByStyle.put(Styles.COMMON, itemStack -> InvincibleSkills.SIMPLE_COMBO);
			this.livingMotionModifiers = null;
			this.consumption = 0;
			this.maxStacks = 0;
			this.skillDescriptions = List.of();
			this.drawSkillIcon = false;
			this.maxPressTime = 0;
			this.maxReserveTime = 0;
			this.maxProtectTime = 0;
			this.resetTime = 0;
			this.skillTextureLocation = null;
			this.canBePlacedOffhand = true;
			this.comboCancel = (style) -> true;
			this.zoomInType = ZoomInType.NONE;
			this.reach = 0.2F;
		}
		
		@Override
		public Builder category(WeaponCategory category) {
			super.category(category);
			return this;
		}
		
		public Builder styleProvider(Function<LivingEntityPatch<?>, Style> styleProvider) {
			this.styleProvider = styleProvider;
			return this;
		}
		
		public Builder passiveSkill(Skill passiveSkill) {
			this.passiveSkill = passiveSkill;
			return this;
		}
		
		public Builder swingSound(SoundEvent swingSound) {
			this.swingSound = swingSound;
			return this;
		}
		
		public Builder hitSound(SoundEvent hitSound) {
			this.hitSound = hitSound;
			return this;
		}
		
		public Builder hitParticle(HitParticleType hitParticle) {
			this.hitParticle = hitParticle;
			return this;
		}
		
		public Builder collider(Collider collider) {
			super.collider(collider);
			return this;
		}
		
		public Builder canBePlacedOffhand(boolean canBePlacedOffhand) {
			this.canBePlacedOffhand = canBePlacedOffhand;
			return this;
		}
		
		public Builder reach(float reach) {
			this.reach = reach;
			return this;
		}
		
		public Builder addTag(ResourceLocation customTag) {
            this.customTags.add(customTag);
            return this;
        }

		public Builder consumption(int consumption) {
			this.consumption = consumption;
			return this;
		}

		public Builder maxStacks(int maxStacks) {
			this.maxStacks = maxStacks;
			return this;
		}

		public Builder addToolTipOnItem(List<String> translationKeys) {
			this.skillDescriptions = List.copyOf(translationKeys);
			return this;
		}

		public Builder drawSkillIcon(boolean drawSkillIcon) {
			this.drawSkillIcon = drawSkillIcon;
			return this;
		}

		public Builder maxPressTime(int maxPressTime) {
			this.maxPressTime = maxPressTime;
			return this;
		}

		public Builder maxReserveTime(int maxReserveTime) {
			this.maxReserveTime = maxReserveTime;
			return this;
		}

		public Builder maxProtectTime(int maxProtectTime) {
			this.maxProtectTime = maxProtectTime;
			return this;
		}

		public Builder resetTime(int resetTime) {
			this.resetTime = resetTime;
			return this;
		}

		public Builder skillTextureLocation(ResourceLocation skillTextureLocation) {
			this.skillTextureLocation = skillTextureLocation;
			return this;
		}
		
		public Builder livingMotionModifier(Style wieldStyle, LivingMotion livingMotion, AnimationAccessor<? extends StaticAnimation> animation) {
			if (AnimationManager.checkNull(animation)) {
				EpicFightMod.LOGGER.warn("Unable to put an empty animation to weapon capability builder: " + livingMotion + ", " + animation);
				return this;
			}
			
			if (this.livingMotionModifiers == null) {
				this.livingMotionModifiers = Maps.newHashMap();
			}
			
			if (!this.livingMotionModifiers.containsKey(wieldStyle)) {
				this.livingMotionModifiers.put(wieldStyle, Maps.newHashMap());
			}
			
			this.livingMotionModifiers.get(wieldStyle).put(livingMotion, animation);
			
			return this;
		}
		
		public Builder addStyleAttibutes(Style style, Pair<Attribute, AttributeModifier> attributePair) {
			super.addStyleAttibutes(style, attributePair);
			return this;
		}


		public final Builder newStyleCombo(Style style, ComboNode root) {
			this.comboMap.put(style, root);
			return this;
		}

		public final Builder autoAttackMotion(Style style, List<AnimationAccessor<? extends AttackAnimation>> motions) {
			this.autoAttackMotionMap.put(style, List.copyOf(motions));
			return this;
		}
		
		public Builder weaponCombinationPredicator(Function<LivingEntityPatch<?>, Boolean> predicator) {
			this.weaponCombinationPredicator = predicator;
			return this;
		}

		public Builder customCombo(Style style, Function<ItemStack, Skill> innateSkill) {
			this.innateSkillByStyle.put(style, innateSkill);
			return this;
		}
		
		public Builder zoomInType(ZoomInType zoomInType) {
			this.zoomInType = zoomInType;
			return this;
		}
		
		public Map<Style, List<AnimationAccessor<? extends AttackAnimation>>> getComboAnimations() {
			return ImmutableMap.copyOf(this.autoAttackMotionMap);
		}
	}
}
