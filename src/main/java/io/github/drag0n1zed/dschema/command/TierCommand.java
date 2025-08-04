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
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.level.ServerPlayer;
import java.util.ArrayList;

public class TierCommand {

    public static final String TIER_TAG_PREFIX = "schem_tier_";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, Schema entrance) {
        dispatcher.register(Commands.literal("schematier")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("set")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("tier", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            SessionConfig config = entrance.getSessionConfigStorage().get();
                                            return SharedSuggestionProvider.suggest(config.getTiers().keySet(), builder);
                                        })
                                        .executes(context -> executeSet(context, entrance))
                                )
                        )
                )
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(TierCommand::executeGet)
                        )
                )
                .then(Commands.literal("clear")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> executeClear(context, entrance))
                        )
                )
        );
    }

    private static int executeSet(CommandContext<CommandSourceStack> context, Schema entrance) throws CommandSyntaxException {
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
        String tierName = StringArgumentType.getString(context, "tier");
        SessionConfig currentConfig = entrance.getSessionConfigStorage().get();

        ConstraintConfig tierConfig = currentConfig.getTiers().get(tierName);

        if (tierConfig == null) {
            context.getSource().sendFailure(Text.text("Tier '" + tierName + "' is not defined in dschema.toml.").reference());
            return 0;
        }

        new ArrayList<>(targetPlayer.getTags()).forEach(tag -> {
            if (tag.startsWith(TIER_TAG_PREFIX)) {
                targetPlayer.removeTag(tag);
            }
        });
        targetPlayer.addTag(TIER_TAG_PREFIX + tierName);

        SessionConfig newConfig = currentConfig.withPlayerConfig(targetPlayer.getUUID(), tierConfig);
        entrance.getSessionConfigStorage().set(newConfig);

        Server server = new MinecraftServer(context.getSource().getServer());
        for (var playerToUpdate : server.getPlayerList().getPlayers()) {
            entrance.getChannel().sendPacket(new SessionConfigPacket(newConfig), playerToUpdate);
        }

        context.getSource().sendSuccess(() -> Text.text("Set and synced tier '" + tierName + "' for " + targetPlayer.getGameProfile().getName()).reference(), true);
        return 1;
    }

    private static int executeGet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
        for (String tag : targetPlayer.getTags()) {
            if (tag.startsWith(TIER_TAG_PREFIX)) {
                String tierName = tag.substring(TIER_TAG_PREFIX.length());
                context.getSource().sendSuccess(() -> Text.text(targetPlayer.getGameProfile().getName() + " has tier: " + tierName).reference(), false);
                return 1;
            }
        }
        context.getSource().sendSuccess(() -> Text.text(targetPlayer.getGameProfile().getName() + " has no tier assigned (using global).").reference(), false);
        return 1;
    }

    private static int executeClear(CommandContext<CommandSourceStack> context, Schema entrance) throws CommandSyntaxException {
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");

        new ArrayList<>(targetPlayer.getTags()).forEach(tag -> {
            if (tag.startsWith(TIER_TAG_PREFIX)) {
                targetPlayer.removeTag(tag);
            }
        });

        SessionConfig currentConfig = entrance.getSessionConfigStorage().get();
        SessionConfig newConfig = currentConfig.withPlayerConfig(targetPlayer.getUUID(), null);
        entrance.getSessionConfigStorage().set(newConfig);

        Server server = new MinecraftServer(context.getSource().getServer());
        for (var playerToUpdate : server.getPlayerList().getPlayers()) {
            entrance.getChannel().sendPacket(new SessionConfigPacket(newConfig), playerToUpdate);
        }

        context.getSource().sendSuccess(() -> Text.text("Cleared tier for " + targetPlayer.getGameProfile().getName() + ". They will now use global permissions.").reference(), true);
        return 1;
    }
}