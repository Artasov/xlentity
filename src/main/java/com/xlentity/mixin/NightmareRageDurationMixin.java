package com.xlentity.mixin;

import com.xlentity.main.NightmareStalkerBalance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Pseudo
@Mixin(targets = "net.mcreator.borninchaosv.procedures.NightmareStalkerPriRanieniiSushchnostiProcedure", remap = false)
public abstract class NightmareRageDurationMixin {
    @ModifyConstant(method = "execute", constant = @Constant(intValue = 2000), remap = false)
    private static int xlentity$shortRage(int duration) {
        return NightmareStalkerBalance.rageDuration(duration);
    }
}
