package com.xlentity.main;

import com.xlentity.Core;
import com.xlentity.config.Config;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/** Server policy for Born in Chaos 1.7.6; other mobs retain their normal random bonuses. */
@EventBusSubscriber(modid = Core.MODID)
public final class NightmareStalkerBalance {
    private static final ResourceLocation ID = ResourceLocation.parse("born_in_chaos_v1:nightmare_stalker");

    private NightmareStalkerBalance() {}

    public static boolean manages(Entity entity) {
        return Config.NIGHTMARE_STALKER.enabled && ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }

    public static long combatDayTime(LevelAccessor level) {
        // Only combat procedures see this cap. Never change world time or natural spawning.
        return Config.NIGHTMARE_STALKER.enabled ? Math.min(level.dayTime(), 49L * 24000L) : level.dayTime();
    }

    public static int rageDuration(int original) {
        return Config.NIGHTMARE_STALKER.enabled ? 120 : original;
    }

    public static MobEffectInstance combatEffect(MobEffectInstance effect) {
        if (!Config.NIGHTMARE_STALKER.enabled) return effect;
        if (effect.is(MobEffects.REGENERATION)) {
            return new MobEffectInstance(MobEffects.REGENERATION, Math.min(60, effect.getDuration()), 0, false, false);
        }
        if (effect.is(MobEffects.DAMAGE_RESISTANCE)) {
            return new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, effect.getDuration(), 0, false, false);
        }
        return effect;
    }

    public static void applyAttributes(Mob mob, boolean freshSpawn) {
        var config = Config.NIGHTMARE_STALKER;
        float health = mob.getHealth();
        float previousMax = mob.getMaxHealth();
        setBase(mob, Attributes.MAX_HEALTH, valid(config.maxHealth, 60, 1, 1024));
        setBase(mob, Attributes.ATTACK_DAMAGE, valid(config.attackDamage, 6, 0, 100));
        setBase(mob, Attributes.MOVEMENT_SPEED, valid(config.movementSpeed, 0.25, 0.01, 1));
        setBase(mob, Attributes.FOLLOW_RANGE, valid(config.followRange, 32, 8, 128));
        // Preserve the injured fraction across chunk reloads; never heal an existing fight.
        mob.setHealth(freshSpawn ? mob.getMaxHealth()
                : Math.min(health, health * mob.getMaxHealth() / Math.max(1, previousMax)));
        mob.removeEffect(MobEffects.DAMAGE_BOOST);
        mob.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        mob.removeEffect(MobEffects.REGENERATION);
    }

    private static double valid(double value, double fallback, double min, double max) {
        return Double.isFinite(value) && value >= min && value <= max ? value : fallback;
    }

    private static void setBase(Mob mob, Holder<Attribute> attribute, double value) {
        var instance = mob.getAttribute(attribute);
        if (instance != null) instance.setBaseValue(value);
    }

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        // Native attacks on livestock otherwise stack Strength without a level limit.
        if (!event.getEntity().level().isClientSide() && manages(event.getEntity())
                && event.getEffectInstance().is(MobEffects.DAMAGE_BOOST)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }
}
