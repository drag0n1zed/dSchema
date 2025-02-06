package xaero.pac.common.server.player.config;

import net.minecraft.network.chat.Component;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.common.packet.config.ClientboundPlayerConfigDynamicOptionsPacket;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public final class PlayerConfigHexOptionSpec extends PlayerConfigOptionSpec<Integer> {

	private PlayerConfigHexOptionSpec(Class<Integer> type, String id, String shortenedId, List<String> path, Integer defaultValue, BiFunction<PlayerConfig<?>, Integer, Integer> defaultReplacer, String comment,
									  String translation, String[] translationArgs, String commentTranslation, String[] commentTranslationArgs, PlayerConfigOptionCategory category, Function<String, Integer> commandInputParser, Function<Integer, Component> commandOutputWriter, BiPredicate<PlayerConfig<?>, Integer> serverSideValidator, BiPredicate<PlayerConfigClientStorage, Integer> clientSideValidator, String tooltipPrefix, Predicate<PlayerConfigType> configTypeFilter, ClientboundPlayerConfigDynamicOptionsPacket.OptionType syncOptionType, boolean dynamic) {
		super(type, id, shortenedId, path, defaultValue, defaultReplacer, comment, translation, translationArgs, commentTranslation, commentTranslationArgs, category, commandInputParser, commandOutputWriter, serverSideValidator, clientSideValidator, tooltipPrefix, configTypeFilter, syncOptionType, dynamic);
	}

	public final static class Builder extends PlayerConfigOptionSpec.Builder<Integer, Builder> {

		protected Builder() {
			super(Integer.class);
		}

		@Override
		public Builder setDefault() {
			super.setDefault();
			setCommandOutputWriter(o -> Component.literal(Integer.toUnsignedString(o, 16).toUpperCase()));
			return self;
		}

		public static <T> Builder begin(){
			return new Builder().setDefault();
		}

		@Override
		public PlayerConfigHexOptionSpec build(Map<String, PlayerConfigOptionSpec<?>> dest) {
			if(tooltipPrefix == null)
				tooltipPrefix = "(hex code)";
			return (PlayerConfigHexOptionSpec) super.build(dest);
		}

		@Override
		protected PlayerConfigHexOptionSpec buildInternally(List<String> path, String shortenedId, Function<String, Integer> commandInputParser) {
			commandInputParser = s -> {
				try {
					return Integer.parseUnsignedInt(s, 16);
				} catch(NumberFormatException nfe) {
					throw new IllegalArgumentException(nfe);
				}
			};
			return new PlayerConfigHexOptionSpec(type, id, shortenedId, path, defaultValue, defaultReplacer, comment, translation, translationArgs, commentTranslation, commentTranslationArgs, category, commandInputParser, commandOutputWriter, serverSideValidator, clientSideValidator, tooltipPrefix, configTypeFilter, ClientboundPlayerConfigDynamicOptionsPacket.OptionType.HEX, dynamic);
		}

	}

}
