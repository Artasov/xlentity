package com.xlentity.main;

import com.xlentity.Core;
import com.xlentity.config.Config;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Core.MODID)
public final class DragonHandler {

    private DragonHandler() {
    }

    @SubscribeEvent
    public static void onDragonTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof EnderDragon dragon)) return;
        if (dragon.level().isClientSide()) return;

        float max = dragon.getMaxHealth();
        float current = dragon.getHealth();
        if (current >= max) return;

        double regenPerSecond = Config.DRAGON_REGEN_PER_SECOND;
        if (regenPerSecond <= 0.0) return;

        float heal = (float) (regenPerSecond / 20.0);
        if (heal <= 0.0f) return;

        dragon.heal(heal);
    }
}
