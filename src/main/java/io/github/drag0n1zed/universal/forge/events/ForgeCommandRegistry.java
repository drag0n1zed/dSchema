package io.github.drag0n1zed.universal.forge.events;

import com.mojang.brigadier.CommandDispatcher;
import io.github.drag0n1zed.dschema.Schema;
import io.github.drag0n1zed.dschema.command.TierCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Schema.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommandRegistry {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // Use the singleton accessor to get the main mod instance
        Schema schemaInstance = Schema.getInstance();

        // Register the command
        TierCommand.register(dispatcher, schemaInstance);
    }
}