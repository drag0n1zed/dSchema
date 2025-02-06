package xaero.pac.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.claims.IClientClaimsManager;
import xaero.pac.client.claims.IClientDimensionClaimsManager;
import xaero.pac.client.claims.IClientRegionClaims;
import xaero.pac.client.claims.player.IClientPlayerClaimInfo;
import xaero.pac.client.command.util.CommandUtil;
import xaero.pac.client.controls.keybinding.IKeyBindingHelper;
import xaero.pac.client.gui.component.CachedComponentSupplier;
import xaero.pac.client.parties.party.IClientParty;
import xaero.pac.client.parties.party.IClientPartyAllyInfo;
import xaero.pac.client.parties.party.IClientPartyMemberDynamicInfoSyncableStorage;
import xaero.pac.client.parties.party.IClientPartyStorage;
import xaero.pac.client.world.capability.ClientWorldMainCapability;
import xaero.pac.client.world.capability.api.ClientWorldCapabilityTypes;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.packet.LazyPacketsConfirmationPacket;
import xaero.pac.common.parties.party.IPartyMemberDynamicInfoSyncable;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.platform.Services;
import xaero.pac.common.server.claims.command.ClaimsCommandRegister;
import xaero.pac.common.server.parties.command.PartyCommandRegister;
import xaero.pac.common.server.player.config.PlayerConfig;

import java.util.UUID;

public class MainMenu extends XPACScreen {

	public static final Component NO_HANDSHAKE = Component.translatable("gui.xaero_pac_ui_handshake_not_received");
	public static final Component NO_PARTIES = Component.translatable("gui.xaero_pac_ui_parties_disabled");
	public static final Component NO_CLAIMS = Component.translatable("gui.xaero_pac_ui_claims_disabled");
	public static final Component PARTY_SYNCING = Component.translatable("gui.xaero_pac_ui_party_syncing");
	public static final Component CLAIMS_SYNCING = Component.translatable("gui.xaero_pac_ui_claims_syncing");

	private static final Component ABOUT_PARTY_COMMAND = Component.literal("/" + PartyCommandRegister.COMMAND_PREFIX + " about");

	public static final Component CLAIM = Component.translatable("gui.xaero_pac_ui_claim");
	public static final Component UNCLAIM = Component.translatable("gui.xaero_pac_ui_unclaim");
	private static final Component CLAIM_COMMAND = Component.literal("/" + ClaimsCommandRegister.COMMAND_PREFIX + " claim");
	private static final Component UNCLAIM_COMMAND = Component.literal("/" + ClaimsCommandRegister.COMMAND_PREFIX + " unclaim");

	public static final Component FORCELOAD = Component.translatable("gui.xaero_pac_ui_forceload");
	public static final Component UNFORCELOAD = Component.translatable("gui.xaero_pac_ui_unforceload");
	private static final Component FORCELOAD_COMMAND = Component.literal("/" + ClaimsCommandRegister.COMMAND_PREFIX + " forceload");
	private static final Component UNFORCELOAD_COMMAND = Component.literal("/" + ClaimsCommandRegister.COMMAND_PREFIX + " unforceload");
	private static final CachedComponentSupplier partyNameSupplier = new CachedComponentSupplier(args -> {
		String currentPartyName = (String) args[0];
		return Component.translatable("gui.xaero_pac_ui_party_name", Component.literal(currentPartyName).withStyle(s -> s.withColor(0xFFAAAAAA)));
	});
	private static final CachedComponentSupplier ownerNameSupplier = new CachedComponentSupplier(args -> {
		String currentOwnerName = (String) args[0];
		return Component.translatable("gui.xaero_pac_ui_party_owner", Component.literal(currentOwnerName).withStyle(s -> s.withColor(0xFFAAAAAA)));
	});
	private static final CachedComponentSupplier memberCountSupplier = new CachedComponentSupplier(args -> {
		int currentMemberCount = (Integer) args[0];
		int currentMemberLimit = (Integer) args[1];
		return Component.translatable("gui.xaero_pac_ui_party_member_count", Component.literal(currentMemberCount + " / " + currentMemberLimit).withStyle(s -> s.withColor(0xFFAAAAAA)));
	});
	private static final CachedComponentSupplier allyCountSupplier = new CachedComponentSupplier(args -> {
		int currentAllyCount = (Integer) args[0];
		int currentAllyLimit = (Integer) args[1];
		return Component.translatable("gui.xaero_pac_ui_party_ally_count", Component.literal(currentAllyCount + " / " + currentAllyLimit).withStyle(s -> s.withColor(0xFFAAAAAA)));
	});
	private static final CachedComponentSupplier inviteCountSupplier = new CachedComponentSupplier(args -> {
		int currentInviteCount = (Integer) args[0];
		int currentInviteLimit = (Integer) args[1];
		return Component.translatable("gui.xaero_pac_ui_party_invite_count", Component.literal(currentInviteCount + " / " + currentInviteLimit).withStyle(s -> s.withColor(0xFFAAAAAA)));
	});

	private static final CachedComponentSupplier claimsNameSupplier = new CachedComponentSupplier(args -> {
		String currentClaimsName = (String) args[0];
		Component nameComponent = Component.literal(currentClaimsName).withStyle(s -> s.withColor(0xFFAAAAAA));
		return Component.translatable("gui.xaero_pac_ui_claims_name", nameComponent);
	});
	private static final CachedComponentSupplier claimCountSupplier = new CachedComponentSupplier(args -> {
		int currentClaimCount = (Integer) args[0];
		int currentClaimLimit = (Integer) args[1];
		Component numbers = Component.literal(currentClaimCount + " / " + currentClaimLimit).withStyle(s -> s.withColor(0xFFAAAAAA));
		return Component.translatable("gui.xaero_pac_ui_claim_count", numbers);
	});
	private static final CachedComponentSupplier forceloadCountSupplier = new CachedComponentSupplier(args -> {
		int currentForceloadCount = (Integer) args[0];
		int currentForceloadLimit = (Integer) args[1];
		return Component.translatable("gui.xaero_pac_ui_forceload_count", Component.literal(currentForceloadCount + " / " + currentForceloadLimit).withStyle(s -> s.withColor(0xFFAAAAAA)));
	});
	private static final CachedComponentSupplier claimsColorSupplier = new CachedComponentSupplier(args -> {
		int currentClaimColor = (Integer) args[0];
		Component colorComponent = Component.literal(Integer.toUnsignedString(currentClaimColor, 16).toUpperCase()).withStyle(s -> s.withColor(currentClaimColor));
		return Component.translatable("gui.xaero_pac_ui_claims_color", colorComponent);
	});

	private boolean serverHasMod;
	private boolean serverHasClaimsEnabled;
	private boolean serverHasPartiesEnabled;
	private Button configsButton;
	private Button aboutPartyButton;
	private Button claimButton;
	private Button forceloadButton;
	public static boolean TEST_TOGGLE;

	public MainMenu(Screen escape, Screen parent) {
		super(escape, parent, Component.translatable("gui.xaero_pac_ui_main_menu"));
	}

	@Override
	protected void init() {
		super.init();
		addRenderableWidget(configsButton = Button.builder(Component.translatable("gui.xaero_pac_ui_config_menu"), this::onConfigsButton).bounds(width / 2 - 100, height / 7 + 8, 200, 20).build());

		aboutPartyButton = Button.builder(Component.translatable("gui.xaero_pac_ui_about_party"), this::onAboutPartyButton).tooltip(Tooltip.create(ABOUT_PARTY_COMMAND)).bounds(width / 2 - 100, height / 7 + 40, 70, 20).build();

		claimButton = Button.builder(CLAIM, this::onClaimButton).tooltip(Tooltip.create(CLAIM_COMMAND)).bounds(width / 2 - 100, height / 7 + 112, 70, 20).build();

		forceloadButton = Button.builder(FORCELOAD, this::onForceloadButton).tooltip(Tooltip.create(FORCELOAD_COMMAND)).bounds(width / 2 - 100, height / 7 + 136, 70, 20).build();

		addRenderableWidget(Button.builder(Component.translatable("gui.xaero_pac_back"), this::onBackButton).bounds(width / 2 - 100, this.height / 6 + 168, 200, 20).build());

		//addRenderableWidget(Button.builder(0, 0, 40, 20, Component.literal("test toggle"), this::onTestToggle));

		updateButtons();

		if(serverHasPartiesEnabled){
			addRenderableWidget(aboutPartyButton);
		}
		if(serverHasClaimsEnabled){
			addRenderableWidget(claimButton);
			addRenderableWidget(forceloadButton);
		}
	}

	private void onTestToggle(Button button) {
		TEST_TOGGLE = !TEST_TOGGLE;
		if(!TEST_TOGGLE)
			OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(new LazyPacketsConfirmationPacket());
		OpenPartiesAndClaims.LOGGER.info("test toggle set to " + TEST_TOGGLE);
	}

	private void updateButtons() {
		ClientWorldMainCapability mainCap = (ClientWorldMainCapability) OpenPartiesAndClaims.INSTANCE.getCapabilityHelper().getCapability(minecraft.level, ClientWorldCapabilityTypes.MAIN_CAP);
		serverHasMod = configsButton.active = mainCap.getClientWorldData().serverHasMod();
		serverHasClaimsEnabled = mainCap.getClientWorldData().serverHasClaimsEnabled();
		serverHasPartiesEnabled = mainCap.getClientWorldData().serverHasPartiesEnabled();
		aboutPartyButton.active = serverHasMod && OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().getParty() != null;

		claimButton.active = forceloadButton.active = false;
		if(serverHasMod && !OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClaimsManager().isLoading()) {
			IPlayerChunkClaim currentClaim = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClaimsManager().get(minecraft.level.dimension().location(), minecraft.player.chunkPosition().x, minecraft.player.chunkPosition().z);
			boolean adminMode = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClaimsManager().isAdminMode();
			boolean serverMode = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClaimsManager().isServerMode();
			UUID claimTargetUUID = serverMode ? PlayerConfig.SERVER_CLAIM_UUID : minecraft.player.getUUID();
			claimButton.active = adminMode || (currentClaim == null || currentClaim.getPlayerId().equals(claimTargetUUID));
			boolean wouldClaim = wouldClaim(currentClaim);
			claimButton.setMessage(wouldClaim ? CLAIM : UNCLAIM);
			claimButton.setTooltip(Tooltip.create(wouldClaim ? CLAIM_COMMAND : UNCLAIM_COMMAND));

			forceloadButton.active = adminMode || currentClaim != null && currentClaim.getPlayerId().equals(claimTargetUUID);
			boolean wouldForceload = currentClaim == null || !currentClaim.isForceloadable();
			forceloadButton.setMessage(wouldForceload ? FORCELOAD : UNFORCELOAD);
			forceloadButton.setTooltip(Tooltip.create(wouldForceload ? FORCELOAD_COMMAND : UNFORCELOAD_COMMAND));
		}
	}

	private void onConfigsButton(Button b) {
		minecraft.setScreen(new ConfigMenu(escape, this));
	}

	private void onAboutPartyButton(Button b) {
		CommandUtil.sendCommand(minecraft, ABOUT_PARTY_COMMAND.getString().substring(1));
		minecraft.setScreen(null);
	}

	private boolean wouldClaim(IPlayerChunkClaim currentClaim){
		if(currentClaim == null)
			return true;
		IPlayerChunkClaim potentialClaimReflection = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClaimsManager().getPotentialClaimStateReflection();
		return !currentClaim.isSameClaimType(potentialClaimReflection);
	}

	private void onClaimButton(Button b) {
		IPlayerChunkClaim currentClaim = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClaimsManager().get(minecraft.level.dimension().location(), minecraft.player.chunkPosition().x, minecraft.player.chunkPosition().z);
		if(wouldClaim(currentClaim))
			CommandUtil.sendCommand(minecraft, CLAIM_COMMAND.getString().substring(1));
		else
			CommandUtil.sendCommand(minecraft, UNCLAIM_COMMAND.getString().substring(1));
		onClose();
	}

	private void onForceloadButton(Button b) {
		IPlayerChunkClaim currentClaim = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClaimsManager().get(minecraft.level.dimension().location(), minecraft.player.chunkPosition().x, minecraft.player.chunkPosition().z);
		if(currentClaim == null)
			return;
		if(!currentClaim.isForceloadable())
			CommandUtil.sendCommand(minecraft, FORCELOAD_COMMAND.getString().substring(1));
		else
			CommandUtil.sendCommand(minecraft, UNFORCELOAD_COMMAND.getString().substring(1));
		onClose();
	}

	private void onBackButton(Button b) {
		goBack();
	}

	private void drawPartyInfo(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial){
		IClientPartyStorage<IClientPartyAllyInfo, IClientParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>, IClientPartyMemberDynamicInfoSyncableStorage<IPartyMemberDynamicInfoSyncable>>
				partyStorage = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage();
		String actualPartyName = partyStorage.getPartyName();
		if(actualPartyName == null || actualPartyName.isEmpty())
			actualPartyName = "N/A";
		guiGraphics.drawString(font, partyNameSupplier.get(actualPartyName), width / 2 - 24, height / 7 + 42, -1);
		if(OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().getParty() != null) {
			String actualOwnerName = partyStorage.getParty().getOwner().getUsername();
			guiGraphics.drawString(font, ownerNameSupplier.get(actualOwnerName), width / 2 - 24, height / 7 + 54, -1);
			guiGraphics.drawString(font, memberCountSupplier.get(partyStorage.getUIMemberCount(), partyStorage.getMemberLimit()), width / 2 - 24, height / 7 + 66, -1);
			guiGraphics.drawString(font, allyCountSupplier.get(partyStorage.getUIAllyCount(), partyStorage.getAllyLimit()), width / 2 - 24, height / 7 + 78, -1);
			guiGraphics.drawString(font, inviteCountSupplier.get(partyStorage.getUIInviteCount(), partyStorage.getInviteLimit()), width / 2 - 24, height / 7 + 90, -1);
		}
	}

	private void drawClaimsInfo(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial){
		IClientClaimsManager<IPlayerChunkClaim, IClientPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IClientDimensionClaimsManager<IClientRegionClaims>>
				claimsManager = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClaimsManager();
		if(claimsManager.hasPlayerInfo(minecraft.player.getUUID())) {
			IClientPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> playerInfo = claimsManager.getPlayerInfo(minecraft.player.getUUID());

			boolean shouldUseLoadingValues = claimsManager.isLoading() || claimsManager.getAlwaysUseLoadingValues();

			int claimCount = shouldUseLoadingValues ? claimsManager.getLoadingClaimCount() : playerInfo.getClaimCount();
			int claimLimit = claimsManager.getClaimLimit();
			int forceloadCount = shouldUseLoadingValues ? claimsManager.getLoadingForceloadCount() : playerInfo.getForceloadCount();
			int forceloadLimit = claimsManager.getForceloadLimit();
			int currentSubConfigIndex = claimsManager.getCurrentSubConfigIndex();
			String claimsName = playerInfo.getClaimsName(currentSubConfigIndex);
			if(claimsName == null && currentSubConfigIndex != -1)
				claimsName = playerInfo.getClaimsName();
			if(claimsName == null || claimsName.isEmpty())
				claimsName = "N/A";
			claimsName = claimsName + " (" + claimsManager.getCurrentSubConfigId() + ")";
			Integer claimsColor = playerInfo.getClaimsColor(currentSubConfigIndex);
			if(claimsColor == null && currentSubConfigIndex != -1)
				claimsColor = playerInfo.getClaimsColor();

			guiGraphics.drawString(font, claimCountSupplier.get(claimCount, claimLimit), width / 2 - 24, height / 7 + 114, -1);
			guiGraphics.drawString(font, forceloadCountSupplier.get(forceloadCount, forceloadLimit), width / 2 - 24, height / 7 + 126, -1);
			guiGraphics.drawString(font, claimsNameSupplier.get(claimsName), width / 2 - 24, height / 7 + 138, -1);
			guiGraphics.drawString(font, claimsColorSupplier.get(claimsColor), width / 2 - 24, height / 7 + 150, -1);
		}

	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
		super.renderBackground(guiGraphics, mouseX, mouseY, partial);
		guiGraphics.drawCenteredString(font, title, width / 2, 16, -1);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
		updateButtons();
		super.render(guiGraphics, mouseX, mouseY, partial);
		if(!serverHasMod)
			guiGraphics.drawCenteredString(font, NO_HANDSHAKE, width / 2, 27, 0xFFFF5555);
		else {
			if(serverHasPartiesEnabled) {
				if (OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().isLoading())
					guiGraphics.drawString(font, PARTY_SYNCING, width / 2 - 104 - font.width(PARTY_SYNCING), height / 7 + 42, -1);
				drawPartyInfo(guiGraphics, mouseX, mouseY, partial);
			} else
				guiGraphics.drawCenteredString(font, NO_PARTIES, width / 2, height / 7 + 42, 0xFFAAAAAA);

			if(serverHasClaimsEnabled) {
				if (OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClaimsManager().isLoading())
					guiGraphics.drawString(font, CLAIMS_SYNCING, width / 2 - 104 - font.width(CLAIMS_SYNCING), height / 7 + 114, -1);
				drawClaimsInfo(guiGraphics, mouseX, mouseY, partial);
			} else
				guiGraphics.drawCenteredString(font, NO_CLAIMS, width / 2, height / 7 + 114, 0xFFAAAAAA);
		}
	}

	@Override
	public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
		IKeyBindingHelper keyBindingHelper = Services.PLATFORM.getKeyBindingHelper();
		if(getFocused() == null && keyBindingHelper.getBoundKey(OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getKeyBindings().openModMenu).getType() == InputConstants.Type.KEYSYM
				&&
				p_96552_ == keyBindingHelper.getBoundKey(OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getKeyBindings().openModMenu).getValue()
				) {
			onClose();
			return true;
		}
		return super.keyPressed(p_96552_, p_96553_, p_96554_);
	}

	@Override
	public boolean mouseClicked(double p_94695_, double p_94696_, int p_94697_) {
		IKeyBindingHelper keyBindingHelper = Services.PLATFORM.getKeyBindingHelper();
		if(getFocused() == null && keyBindingHelper.getBoundKey(OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getKeyBindings().openModMenu).getType() == InputConstants.Type.MOUSE
				&&
				p_94697_ == keyBindingHelper.getBoundKey(OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getKeyBindings().openModMenu).getValue()
				) {
			onClose();
			return true;
		}
		return super.mouseClicked(p_94695_, p_94696_, p_94697_);
	}

}
