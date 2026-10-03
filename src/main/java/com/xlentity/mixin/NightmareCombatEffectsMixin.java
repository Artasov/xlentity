package com.xlentity.mixin;

import com.xlentity.main.NightmareStalkerBalance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = {
        "net.mcreator.borninchaosv.procedures.NightmareStalkerDamProcedure",
        "net.mcreator.borninchaosv.procedures.TerrifyingPresenceKazhdyiTikVoVriemiaEffiektaProcedure"
}, remap = false)
public abstract class NightmareCombatEffectsMixin {
    @Redirect(method = "execute*", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"),
            remap = false)
    private static boolean xlentity$boundedEffect(LivingEntity entity, MobEffectInstance effect) {
        return entity.addEffect(NightmareStalkerBalance.manages(entity)
                ? NightmareStalkerBalance.combatEffect(effect) : effect);
    }
}
