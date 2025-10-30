package com.p1nero.invincible.api.forgeevent;

import com.p1nero.invincible.mixin.AnimationManagerAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class DuplicateAnimationRegistryEvent extends Event implements IModBusEvent {
    private List<DuplicateAnimationBuilder> builders = new ArrayList<>();
    private Set<String> namespaces = new HashSet<>();

    public void newBuilder(String namespace, Consumer<DuplicateAnimationBuilder> build) {
        if (this.namespaces.contains(namespace)) {
            throw new IllegalArgumentException("Animation builder namespace '" + namespace + "' already exists!");
        }

        this.namespaces.add(namespace);
        this.builders.add(new DuplicateAnimationBuilder(namespace, build));
    }

    public List<DuplicateAnimationBuilder> getBuilders() {
        return this.builders;
    }

    public record DuplicateAnimationBuilder(String namespace, Consumer<DuplicateAnimationBuilder> task) {

        public <T extends StaticAnimation> AnimationManager.AnimationAccessor<T> nextAccessor(AnimationManager.AnimationAccessor<T> original, Function<AnimationManager.AnimationAccessor<T>, T> onLoad) {
            ResourceLocation selfName = ResourceLocation.fromNamespaceAndPath(namespace, original.registryName().getPath());
            AnimationManager.AnimationAccessor<T> accessor = DuplicateAnimationAccessorImpl.create(original.registryName(), selfName, getInstance().getAnimations().size() + 1, true, onLoad);

            getInstance().getAnimationById().put(accessor.id(), accessor);
            getInstance().getAnimationByName().put(selfName, accessor);
            getInstance().getAnimations().put(accessor, null);

            return accessor;
        }

    }

    public record DuplicateAnimationAccessorImpl<A extends StaticAnimation>(ResourceLocation originalAnimationName,
                                                                            ResourceLocation registryName, int id,
                                                                            boolean inRegistry,
                                                                            Function<AnimationManager.AnimationAccessor<A>, A> onLoad) implements AnimationManager.AnimationAccessor<A> {
        private static <A extends StaticAnimation> AnimationManager.AnimationAccessor<A> create(ResourceLocation originalAnimationName, ResourceLocation registryName, int id, boolean inRegistry, Function<AnimationManager.AnimationAccessor<A>, A> onLoad) {
            return new DuplicateAnimationAccessorImpl<A>(originalAnimationName, registryName, id, inRegistry, onLoad);
        }

        @Override
        public A get() {
            if (!getInstance().getAnimations().containsKey(this)) {
                A anim = this.onLoad.apply(this);
                anim.setResourceLocation(originalAnimationName.getNamespace(), originalAnimationName.getPath());
                getInstance().getAnimations().put(this, anim);
            }

            return (A) getInstance().getAnimations().get(this);
        }

        public String toString() {
            return this.registryName.toString();
        }

        public int hashCode() {
            return this.registryName.hashCode();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj instanceof AnimationManager.AnimationAccessor<?> armatureAccessor) {
                return this.registryName.equals(armatureAccessor.registryName());
            } else if (obj instanceof ResourceLocation rl) {
                return this.registryName.equals(rl);
            } else if (obj instanceof String name) {
                return this.registryName.toString().equals(name);
            } else {
                return false;
            }
        }
    }

    public static AnimationManagerAccessor getInstance() {
        return ((AnimationManagerAccessor) AnimationManager.getInstance());
    }

}