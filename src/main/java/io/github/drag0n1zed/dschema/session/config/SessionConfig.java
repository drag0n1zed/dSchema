package io.github.drag0n1zed.dschema.session.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import io.github.drag0n1zed.universal.api.core.Player;

public record SessionConfig(
        ConstraintConfig globalConfig,
        Map<UUID, ConstraintConfig> playerConfigs,
        Map<String, ConstraintConfig> tiers
) {

    public static final SessionConfig EMPTY = new SessionConfig(ConstraintConfig.EMPTY, Map.of(), Map.of());

    public static SessionConfig defaultConfig() {
        Map<String, ConstraintConfig> defaultTiers = new LinkedHashMap<>();

        // Tier Alpha: Apprentice. Basic placement.
        defaultTiers.put("alpha", new ConstraintConfig(
                null, true, false, true, false, false, null,
                5, null, 32, null, null, null, null
        ));

        // Tier Beta: Journeyman. Adds block breaking and interaction.
        defaultTiers.put("beta", new ConstraintConfig(
                null, true, true, true, true, false, null,
                8, 64, 64, 64, null, null, null
        ));

        // Tier Gamma: Adept. Adds copy/paste.
        defaultTiers.put("gamma", new ConstraintConfig(
                null, true, true, true, true, true, null,
                16, 256, 256, 256, 256, null, null
        ));

        return new SessionConfig(ConstraintConfig.DEFAULT, Map.of(), Collections.unmodifiableMap(defaultTiers));
    }

    private <T> T getPlayerOrGlobalEntry(UUID id, Function<ConstraintConfig, T> entry) {
        return entry.apply(playerConfigs.get(id) == null || entry.apply(playerConfigs.get(id)) == null ? globalConfig : playerConfigs.get(id));
    }

    private <T> T getPlayerOrNullEntry(UUID id, Function<ConstraintConfig, T> entry) {
        return entry.apply(playerConfigs.get(id) == null || entry.apply(playerConfigs.get(id)) == null ? ConstraintConfig.NULL : playerConfigs.get(id));
    }

    public ConstraintConfig getGlobalConfig() {
        return globalConfig;
    }

    public Map<String, ConstraintConfig> getTiers() {
        return this.tiers;
    }

    public ConstraintConfig getByPlayer(Player player) {
        final ConstraintConfig playerConfig = this.playerConfigs.get(player.getId());

        ConstraintConfig tierConfig = null;
        for (String tag : player.getTags()) {
            if (tag.startsWith(io.github.drag0n1zed.dschema.command.TierCommand.TIER_TAG_PREFIX)) {
                String tierName = tag.substring(io.github.drag0n1zed.dschema.command.TierCommand.TIER_TAG_PREFIX.length());
                tierConfig = this.tiers.get(tierName);
                if (tierConfig != null) {
                    break;
                }
            }
        }
        final ConstraintConfig finalTierConfig = tierConfig;

        return new ConstraintConfig(
                playerConfig != null && playerConfig.useCommands() != null ? playerConfig.useCommands() : (finalTierConfig != null && finalTierConfig.useCommands() != null ? finalTierConfig.useCommands() : globalConfig.useCommands()),
                playerConfig != null && playerConfig.allowUseMod() != null ? playerConfig.allowUseMod() : (finalTierConfig != null && finalTierConfig.allowUseMod() != null ? finalTierConfig.allowUseMod() : globalConfig.allowUseMod()),
                playerConfig != null && playerConfig.allowBreakBlocks() != null ? playerConfig.allowBreakBlocks() : (finalTierConfig != null && finalTierConfig.allowBreakBlocks() != null ? finalTierConfig.allowBreakBlocks() : globalConfig.allowBreakBlocks()),
                playerConfig != null && playerConfig.allowPlaceBlocks() != null ? playerConfig.allowPlaceBlocks() : (finalTierConfig != null && finalTierConfig.allowPlaceBlocks() != null ? finalTierConfig.allowPlaceBlocks() : globalConfig.allowPlaceBlocks()),
                playerConfig != null && playerConfig.allowInteractBlocks() != null ? playerConfig.allowInteractBlocks() : (finalTierConfig != null && finalTierConfig.allowInteractBlocks() != null ? finalTierConfig.allowInteractBlocks() : globalConfig.allowInteractBlocks()),
                playerConfig != null && playerConfig.allowCopyPasteStructures() != null ? playerConfig.allowCopyPasteStructures() : (finalTierConfig != null && finalTierConfig.allowCopyPasteStructures() != null ? finalTierConfig.allowCopyPasteStructures() : globalConfig.allowCopyPasteStructures()),
                playerConfig != null && playerConfig.useProperToolsOnly() != null ? playerConfig.useProperToolsOnly() : (finalTierConfig != null && finalTierConfig.useProperToolsOnly() != null ? finalTierConfig.useProperToolsOnly() : globalConfig.useProperToolsOnly()),
                playerConfig != null && playerConfig.maxReachDistance() != null ? playerConfig.maxReachDistance() : (finalTierConfig != null && finalTierConfig.maxReachDistance() != null ? finalTierConfig.maxReachDistance() : globalConfig.maxReachDistance()),
                playerConfig != null && playerConfig.maxBlockBreakVolume() != null ? playerConfig.maxBlockBreakVolume() : (finalTierConfig != null && finalTierConfig.maxBlockBreakVolume() != null ? finalTierConfig.maxBlockBreakVolume() : globalConfig.maxBlockBreakVolume()),
                playerConfig != null && playerConfig.maxBlockPlaceVolume() != null ? playerConfig.maxBlockPlaceVolume() : (finalTierConfig != null && finalTierConfig.maxBlockPlaceVolume() != null ? finalTierConfig.maxBlockPlaceVolume() : globalConfig.maxBlockPlaceVolume()),
                playerConfig != null && playerConfig.maxBlockInteractVolume() != null ? playerConfig.maxBlockInteractVolume() : (finalTierConfig != null && finalTierConfig.maxBlockInteractVolume() != null ? finalTierConfig.maxBlockInteractVolume() : globalConfig.maxBlockInteractVolume()),
                playerConfig != null && playerConfig.maxStructureCopyPasteVolume() != null ? playerConfig.maxStructureCopyPasteVolume() : (finalTierConfig != null && finalTierConfig.maxStructureCopyPasteVolume() != null ? finalTierConfig.maxStructureCopyPasteVolume() : globalConfig.maxStructureCopyPasteVolume()),
                playerConfig != null && playerConfig.whitelistedItems() != null ? playerConfig.whitelistedItems() : (finalTierConfig != null && finalTierConfig.whitelistedItems() != null ? finalTierConfig.whitelistedItems() : globalConfig.whitelistedItems()),
                playerConfig != null && playerConfig.blacklistedItems() != null ? playerConfig.blacklistedItems() : (finalTierConfig != null && finalTierConfig.blacklistedItems() != null ? finalTierConfig.blacklistedItems() : globalConfig.blacklistedItems())
        );
    }

    public ConstraintConfig getByPlayer(UUID id) {
        return new ConstraintConfig(
                getPlayerOrGlobalEntry(id, ConstraintConfig::useCommands),
                getPlayerOrGlobalEntry(id, ConstraintConfig::allowUseMod),
                getPlayerOrGlobalEntry(id, ConstraintConfig::allowBreakBlocks),
                getPlayerOrGlobalEntry(id, ConstraintConfig::allowPlaceBlocks),
                getPlayerOrGlobalEntry(id, ConstraintConfig::allowInteractBlocks),
                getPlayerOrGlobalEntry(id, ConstraintConfig::allowCopyPasteStructures),
                getPlayerOrGlobalEntry(id, ConstraintConfig::useProperToolsOnly),
                getPlayerOrGlobalEntry(id, ConstraintConfig::maxReachDistance),
                getPlayerOrGlobalEntry(id, ConstraintConfig::maxBlockBreakVolume),
                getPlayerOrGlobalEntry(id, ConstraintConfig::maxBlockPlaceVolume),
                getPlayerOrGlobalEntry(id, ConstraintConfig::maxBlockInteractVolume),
                getPlayerOrGlobalEntry(id, ConstraintConfig::maxStructureCopyPasteVolume),
                getPlayerOrGlobalEntry(id, ConstraintConfig::whitelistedItems),
                getPlayerOrGlobalEntry(id, ConstraintConfig::blacklistedItems)
        );
    }

    public ConstraintConfig getPlayerConfigOrNull(Player player) {
        return getPlayerConfigOrNull(player.getId());
    }

    public ConstraintConfig getPlayerConfigOrNull(UUID id) {
        // This entire method was incorrect. It has been restored.
        return new ConstraintConfig(
                getPlayerOrNullEntry(id, ConstraintConfig::useCommands),
                getPlayerOrNullEntry(id, ConstraintConfig::allowUseMod),
                getPlayerOrNullEntry(id, ConstraintConfig::allowBreakBlocks),
                getPlayerOrNullEntry(id, ConstraintConfig::allowPlaceBlocks),
                getPlayerOrNullEntry(id, ConstraintConfig::allowInteractBlocks),
                getPlayerOrNullEntry(id, ConstraintConfig::allowCopyPasteStructures),
                getPlayerOrNullEntry(id, ConstraintConfig::useProperToolsOnly),
                getPlayerOrNullEntry(id, ConstraintConfig::maxReachDistance),
                getPlayerOrNullEntry(id, ConstraintConfig::maxBlockBreakVolume),
                getPlayerOrNullEntry(id, ConstraintConfig::maxBlockPlaceVolume),
                getPlayerOrNullEntry(id, ConstraintConfig::maxBlockInteractVolume),
                getPlayerOrNullEntry(id, ConstraintConfig::maxStructureCopyPasteVolume),
                getPlayerOrNullEntry(id, ConstraintConfig::whitelistedItems),
                getPlayerOrNullEntry(id, ConstraintConfig::blacklistedItems));
    }

    public SessionConfig withPlayerConfig(UUID id, ConstraintConfig config) {
        var map = new HashMap<>(playerConfigs);
        if (config == null) {
            map.remove(id);
        } else {
            map.put(id, config);
        }

        return new SessionConfig(
                globalConfig,
                Collections.unmodifiableMap(map),
                tiers
        );
    }

    public SessionConfig withPlayerConfig(Map<UUID, ConstraintConfig> config) {
        return new SessionConfig(
                globalConfig,
                config,
                tiers
        );
    }

    public SessionConfig withGlobalConfig(ConstraintConfig config) {
        return new SessionConfig(
                config,
                playerConfigs,
                tiers
        );
    }
}