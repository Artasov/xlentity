package com.xlentity.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.xlentity.Core;
import com.xlentity.config.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = Core.MODID)
public final class ReloadCommand {

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent evt) {

        LiteralArgumentBuilder<CommandSourceStack> cmd =
                Commands.literal("xlentity")
                        .requires(src -> src.hasPermission(2))    // OP-level ≥ 2
                        .then(Commands.literal("reload")
                                .executes(ctx -> {
                                    Config.reload();
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("[xlentity] config reloaded ✔"),
                                            true);
                                    return 1;
                                }));

        evt.getDispatcher().register(cmd);
    }
}
