package com.xlentity.test;

import com.xlentity.config.Config;
import com.xlentity.main.EntitySpawnHandler;
import com.xlentity.main.NightmareStalkerBalance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("xlentity_stalker_test")
public final class StalkerNativeTest {
    private static final String PROCEDURES = "net.mcreator.borninchaosv.procedures.";

    public StalkerNativeTest() {
        if (!Boolean.getBoolean("xlentity.stalkerTest")) throw new IllegalStateException("Local test flag required");
        NeoForge.EVENT_BUS.addListener(StalkerNativeTest::started);
    }

    private static void started(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        if (!"127.0.0.1".equals(server.getLocalIp()) || server.getPlayerCount() != 0
                || !server.getWorldData().getLevelName().equals("stalker-test-world-20261004")) {
            throw new IllegalStateException("Stalker test is restricted to its empty loopback world");
        }
        server.execute(() -> {
            try {
                verify(server.overworld());
                System.out.println("[STALKER-TEST] PASS: native attributes, load idempotence, old-world effects, rage, vanilla control, disabled policy");
            } catch (Throwable error) {
                System.err.println("[STALKER-TEST] FAIL");
                error.printStackTrace();
            } finally {
                server.halt(false);
            }
        });
    }

    private static void verify(ServerLevel level) throws Exception {
        level.setDayTime(9117L * 24000 + 18000);
        long actualTime = level.dayTime();
        Mob stalker = (Mob) BuiltInRegistries.ENTITY_TYPE.get(
                ResourceLocation.parse("born_in_chaos_v1:nightmare_stalker")).create(level);
        check(stalker != null && NightmareStalkerBalance.manages(stalker), "Native stalker selected");
        stalker.setPos(0, 80, 0);
        NightmareStalkerBalance.applyAttributes(stalker, true);
        check(stalker.getMaxHealth() == 60 && stalker.getHealth() == 60, "60 health");
        check(stalker.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) == 6, "6 base damage");
        check(stalker.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == 0.25, "Speed");
        check(stalker.getAttributeBaseValue(Attributes.FOLLOW_RANGE) == 32, "Detection range");

        stalker.setHealth(30);
        EntitySpawnHandler.onEntityJoin(new EntityJoinLevelEvent(stalker, level));
        EntitySpawnHandler.onEntityJoin(new EntityJoinLevelEvent(stalker, level));
        check(stalker.getHealth() == 30, "Chunk load never heals or compounds");
        check(!stalker.hasEffect(MobEffects.REGENERATION), "No random regeneration");

        procedure("NightmareStalkerGonProcedure", level, stalker);
        check(!stalker.hasEffect(MobEffects.DAMAGE_BOOST), "No old-world Strength");
        check(!stalker.hasEffect(MobEffects.DAMAGE_RESISTANCE), "No old-world Resistance");
        check(!stalker.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 5)), "Livestock Strength blocked");

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(1, 80, 0);
        player.removeAllEffects();
        attackProcedure("NightmareStalkerDamProcedure", level, player, stalker);
        var regeneration = stalker.getEffect(MobEffects.REGENERATION);
        check(regeneration != null && regeneration.getAmplifier() == 0 && regeneration.getDuration() == 60, "Regeneration I for 3s");
        var wither = player.getEffect(MobEffects.WITHER);
        check(wither != null && wither.getAmplifier() == 0 && wither.getDuration() == 200, "Wither I, not old-world II");

        stalker.setHealth(25);
        stalker.getPersistentData().putDouble("ragescale", 7);
        attackProcedure("NightmareStalkerPriRanieniiSushchnostiProcedure", level, stalker, player);
        var rage = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse("born_in_chaos_v1:terrifying_presence")).orElseThrow();
        check(stalker.hasEffect(rage) && stalker.getEffect(rage).getDuration() == 120, "Rage 6s");
        procedure("TerrifyingPresenceKazhdyiTikVoVriemiaEffiektaProcedure", level, stalker);
        check(stalker.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() == 0, "Rage Resistance I");
        check(level.dayTime() == actualTime, "World time unchanged");

        Mob zombie = EntityType.ZOMBIE.create(level);
        check(!NightmareStalkerBalance.manages(zombie), "Other mob excluded");
        check(zombie.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2)), "Other mob Strength preserved");

        Config.NIGHTMARE_STALKER.enabled = false;
        check(!NightmareStalkerBalance.manages(stalker), "Disable attribute profile");
        Mob nativeControl = (Mob) stalker.getType().create(level);
        nativeControl.setPos(0, 80, 0);
        procedure("NightmareStalkerGonProcedure", level, nativeControl);
        var strength = nativeControl.getEffect(MobEffects.DAMAGE_BOOST);
        var resistance = nativeControl.getEffect(MobEffects.DAMAGE_RESISTANCE);
        check(strength != null && strength.getAmplifier() == 2, "Native old-world Strength restored when disabled; dayTime=" + level.dayTime());
        check(resistance != null && resistance.getAmplifier() == 2, "Native old-world Resistance restored when disabled");
        Config.NIGHTMARE_STALKER.enabled = true;
    }

    private static void procedure(String name, ServerLevel level, Entity entity) throws Exception {
        Class.forName(PROCEDURES + name).getMethod("execute", LevelAccessor.class, double.class, double.class, double.class, Entity.class)
                .invoke(null, level, entity.getX(), entity.getY(), entity.getZ(), entity);
    }

    private static void attackProcedure(String name, ServerLevel level, Entity victim, Entity attacker) throws Exception {
        Class.forName(PROCEDURES + name).getMethod("execute", LevelAccessor.class, double.class, double.class, double.class, Entity.class, Entity.class)
                .invoke(null, level, victim.getX(), victim.getY(), victim.getZ(), victim, attacker);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
