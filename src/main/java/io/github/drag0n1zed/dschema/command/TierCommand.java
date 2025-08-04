package io.github.drag0n1zed.dschema.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.drag0n1zed.dschema.Schema;
import io.github.drag0n1zed.dschema.networking.packets.session.SessionConfigPacket;
import io.github.drag0n1zed.dschema.session.config.ConstraintConfig;
import io.github.drag0n1zed.dschema.session.config.SessionConfig;
import io.github.drag0n1zed.universal.api.platform.Server;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class TierCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, Schema entrance) {
        dispatcher.register(Commands.literal("schemaupdateplayerconfig")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> execute(context, entrance))
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context, Schema entrance) throws CommandSyntaxException {
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
        SessionConfig currentConfig = entrance.getSessionConfigStorage().get();

        // Get the "admin" tier config from the loaded TOML file
        ConstraintConfig adminConfig = currentConfig.tiers().get("admin");

        // Gracefully handle if the "admin" tier is not defined in the config
        if (adminConfig == null) {
            context.getSource().sendFailure(Text.text("The 'admin' tier is not defined in dschema.toml.").reference());
            return 0;
        }

        SessionConfig newConfig = currentConfig.withPlayerConfig(targetPlayer.getUUID(), adminConfig);

        entrance.getSessionConfigStorage().set(newConfig);

        Server server = new MinecraftServer(context.getSource().getServer());
        for (var playerToUpdate : server.getPlayerList().getPlayers()) {
            entrance.getChannel().sendPacket(new SessionConfigPacket(newConfig), playerToUpdate);
        }

        context.getSource().sendSuccess(() -> Text.text("Updated and synced 'admin' tier config for " + targetPlayer.getGameProfile().getName()).reference(), true);

        return 1;
    }
}