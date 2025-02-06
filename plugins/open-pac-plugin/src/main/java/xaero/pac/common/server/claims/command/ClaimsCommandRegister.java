package xaero.pac.common.server.claims.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ClaimsCommandRegister {

	public static final String COMMAND_PREFIX = "openpac-claims";

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		new ClaimsClaimCommand().register(dispatcher, environment);
		new ClaimsUnclaimCommand().register(dispatcher, environment);
		new ClaimsForceloadCommand().register(dispatcher, environment);
		new ClaimsUnforceloadCommand().register(dispatcher, environment);
		new ClaimsNonAllyModeCommand().register(dispatcher, environment);
		new ClaimsAboutCommand().register(dispatcher, environment);
		new ClaimsServerModeCommand().register(dispatcher, environment);
		new ClaimsSubClaimCurrentCommand().register(dispatcher, environment);
		new ClaimsSubClaimUseCommand().register(dispatcher, environment);

		//op commands
		new ClaimsServerClaimCommand().register(dispatcher, environment);
		new ClaimsServerUnclaimCommand().register(dispatcher, environment);
		new ClaimsServerForceloadCommand().register(dispatcher, environment);
		new ClaimsServerUnforceloadCommand().register(dispatcher, environment);
		new ClaimsAdminModeCommand().register(dispatcher, environment);
	}

}
