package com.p1nero.invincible.conditions;

import com.p1nero.invincible.capability.InvincibleCapabilityProvider;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import java.util.List;

public class PlayerPhaseCondition implements Condition<ServerPlayerPatch> {

    private int min, max;

    public PlayerPhaseCondition(int min, int max){
        this.min = min;
        this.max = max;
    }

    public PlayerPhaseCondition(){
        this.min = 1;
        this.max = 1;
    }

    @Override
    public Condition<ServerPlayerPatch> read(CompoundTag compoundTag) {
        if (!compoundTag.contains("min") && !compoundTag.contains("max")) {
            throw new IllegalArgumentException("custom player phase condition error: min or max not specified!");
        }  else {
            this.min = compoundTag.getInt("min");
            this.max = compoundTag.getInt("max");
            return this;
        }
    }

    @Override
    public CompoundTag serializePredicate() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("min", this.min);
        tag.putInt("max", this.max);
        return tag;
    }

    @Override
    public boolean predicate(ServerPlayerPatch serverPlayerPatch) {
        int phase = InvincibleCapabilityProvider.get(serverPlayerPatch.getOriginal()).getPhase();
        return phase >= min && phase <= max;
    }

    @Override
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return null;
    }

}
