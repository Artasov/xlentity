package com.xlentity.mixin;

import com.xlentity.main.NightmareStalkerBalance;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = {
        "net.mcreator.borninchaosv.procedures.NightmareStalkerGonProcedure",
        "net.mcreator.borninchaosv.procedures.NightmareStalkerDamProcedure",
        "net.mcreator.borninchaosv.procedures.TerrifyingPresenceKazhdyiTikVoVriemiaEffiektaProcedure"
}, remap = false)
public abstract class NightmareCombatAgeMixin {
    @Redirect(method = "execute*", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/LevelAccessor;dayTime()J"), remap = false)
    private static long xlentity$combatAge(LevelAccessor level) {
        return NightmareStalkerBalance.combatDayTime(level);
    }
}
