package xaero.pac.common.server.player.config.dynamic;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.player.config.dynamic.PlayerConfigDynamicOptions;
import xaero.pac.common.server.claims.protection.ChunkProtectionExceptionType;
import xaero.pac.common.server.claims.protection.ExceptionElementType;
import xaero.pac.common.server.claims.protection.group.ChunkProtectionExceptionGroup;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigStaticListIterationOptionSpec;

public class PlayerConfigExceptionDynamicOptionsLoader {

	public static final String OPTION_ROOT = PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptionGroups.";
	public static final String TRANSLATION_ROOT = "gui.xaero_pac_player_config_" + PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptionGroups.";
	public static final String COMMENT_TRANSLATION_ROOT = "gui.xaero_pac_player_config_tooltip_" + PlayerConfig.PLAYER_CONFIG_ROOT_DOT + "claims.protection.exceptionGroups.";
	public static final String INTERACT = "interact";
	public static final String HAND_INTERACT = "handInteract";
	public static final String ANY_ITEM_INTERACT = "anyItemInteract";
	public static final String BREAK = "break";
	public static final String BARRIER = "barrier";
	public static final String BLOCK_ACCESS = "blockAccess";
	public static final String BLOCK_BREAK_ACCESS = "blockBreakAccess";
	public static final String BLOCK_INTERACT_ACCESS = "blockInteractAccess";
	public static final String ENTITY_ACCESS = "entityAccess";
	public static final String ENTITY_BREAK_ACCESS = "entityKillAccess";
	public static final String ENTITY_INTERACT_ACCESS = "entityInteractAccess";
	public static final String DROPPED_ITEM_ACCESS = "droppedItemAccess";

	<T> void handleGroup(ChunkProtectionExceptionGroup<T> group, PlayerConfigDynamicOptions.Builder builder, String category, String categoryPlural){
		String optionId;
		String comment;
		String translation;
		String commentTranslation;
		String interactionOptionsTooltip = "\n\n" + PlayerConfig.EXCEPTION_LEVELS_TOOLTIP;
		if(group.isOfSubjects() && group.getType() == ChunkProtectionExceptionType.INTERACTION) {
			optionId = OPTION_ROOT + category + "." + INTERACT;
			comment = "When enabled, claimed chunk protection makes an exception for interaction with the following " + categoryPlural + ": %1$s." + interactionOptionsTooltip;
			translation = TRANSLATION_ROOT + category + "." + INTERACT;
			commentTranslation = COMMENT_TRANSLATION_ROOT + category + "." + INTERACT;
		} else if(group.isOfSubjects() && group.getType() == ChunkProtectionExceptionType.EMPTY_HAND_INTERACTION) {
			optionId = OPTION_ROOT + category + "." + HAND_INTERACT;
			comment = "When enabled, claimed chunk protection makes an exception for interaction with an empty hand with the following " + categoryPlural + ": %1$s." + interactionOptionsTooltip;
			translation = TRANSLATION_ROOT + category + "." + HAND_INTERACT;
			commentTranslation = COMMENT_TRANSLATION_ROOT + category + "." + HAND_INTERACT;
		} else if(group.isOfSubjects() && group.getType() == ChunkProtectionExceptionType.ANY_ITEM_INTERACTION) {
			optionId = OPTION_ROOT + category + "." + ANY_ITEM_INTERACT;
			comment = "When enabled, claimed chunk protection makes an exception for interaction with any item held with the following " + categoryPlural + ": %1$s." + interactionOptionsTooltip;
			translation = TRANSLATION_ROOT + category + "." + ANY_ITEM_INTERACT;
			commentTranslation = COMMENT_TRANSLATION_ROOT + category + "." + ANY_ITEM_INTERACT;
		} else if(group.isOfSubjects() && group.getType() == ChunkProtectionExceptionType.BREAK){
			optionId = OPTION_ROOT + category + "." + BREAK;
			comment = "When enabled, claimed chunk protection makes an exception for destruction of the following " + categoryPlural + ": %1$s." + interactionOptionsTooltip;
			translation = TRANSLATION_ROOT + category + "." + BREAK;
			commentTranslation = COMMENT_TRANSLATION_ROOT + category + "." + BREAK;
		} else if(group.getType() == ChunkProtectionExceptionType.BARRIER){
			optionId = OPTION_ROOT + category + "." + BARRIER;
			comment = "When enabled, claimed chunk protection prevents the following additional " + categoryPlural + " from entering the claim (except wilderness): %1$s.\n\n" + PlayerConfig.PROTECTION_LEVELS_TOOLTIP;
			translation = TRANSLATION_ROOT + category + "." + BARRIER;
			commentTranslation = COMMENT_TRANSLATION_ROOT + category + "." + BARRIER;
		} else if(!group.isOfSubjects() && group.getType() == ChunkProtectionExceptionType.FULL){
			String accessType =
					group.getSubjectType() == Block.class ? BLOCK_ACCESS :
					group.getSubjectType() == EntityType.class ? ENTITY_ACCESS :
							DROPPED_ITEM_ACCESS;
			String accessName =
					group.getSubjectType() == Block.class ? "block" :
					group.getSubjectType() == EntityType.class ? "entity" :
							"dropped item";
			optionId = OPTION_ROOT + category + "." + accessType;
			comment = "When enabled, claimed chunk protection makes an exception for unlimited " + accessName +
					" access by the following " + categoryPlural + ": %1$s. If the " + accessName +
					" protection is based on the mob griefing rule check, then the claimed neighbor chunks must also allow the " +
					accessName + " access." + interactionOptionsTooltip;
			translation = TRANSLATION_ROOT + category + "." + accessType;
			commentTranslation = COMMENT_TRANSLATION_ROOT + category + "." + accessType;
		} else if(!group.isOfSubjects()){
			String accessType;
			String accessName;
			if(group.getType() == ChunkProtectionExceptionType.BREAK) {
				accessType = group.getSubjectType() == Block.class ? BLOCK_BREAK_ACCESS :
								ENTITY_BREAK_ACCESS;
				accessName = group.getSubjectType() == Block.class ? "block breaking" :
								"entity killing";
			} else {
				accessType = group.getSubjectType() == Block.class ? BLOCK_INTERACT_ACCESS :
								ENTITY_INTERACT_ACCESS;
				accessName = group.getSubjectType() == Block.class ? "block interaction" :
								"entity interaction";
			}
			optionId = OPTION_ROOT + category + "." + accessType;
			comment = "When enabled, claimed chunk protection makes an exception for " + accessName +
					" by the following " + categoryPlural + ": %1$s. If the " + accessName +
					" protection is based on the mob griefing rule check, then the claimed neighbor chunks must also allow the " +
					accessName + "." + interactionOptionsTooltip;
			translation = TRANSLATION_ROOT + category + "." + accessType;
			commentTranslation = COMMENT_TRANSLATION_ROOT + category + "." + accessType;
		} else {
			OpenPartiesAndClaims.LOGGER.error("Invalid group type " + group.getType() + " for " + category + " exception group " + group.getName());
			return;
		}
		optionId += "." + group.getName();
		comment = String.format(comment, group.getContentString());
		PlayerConfigStaticListIterationOptionSpec<Integer> option = PlayerConfigStaticListIterationOptionSpec.Builder.begin(Integer.class)
				.setId(optionId)
				.setList(PlayerConfig.PROTECTION_LEVELS)
				.setTranslation(translation, group.getName())
				.setCommentTranslation(commentTranslation, group.getContentString())
				.setDefaultValue(0)
				.setComment(comment)
				.setCategory(group.getOptionCategory())
				.setDynamic(true)
				.build(null);
		group.setPlayerConfigOption(option);
		builder.addOption(option);
	}

}
