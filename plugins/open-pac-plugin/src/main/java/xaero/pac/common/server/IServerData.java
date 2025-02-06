package xaero.pac.common.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.forceload.ForceLoadTicketManager;
import xaero.pac.common.server.claims.player.expiration.ServerPlayerClaimsExpirationHandler;
import xaero.pac.common.server.claims.protection.ChunkProtection;
import xaero.pac.common.server.expiration.task.ObjectExpirationCheckSpreadoutTask;
import xaero.pac.common.server.info.ServerInfo;
import xaero.pac.common.server.info.io.ServerInfoHolderIO;
import xaero.pac.common.server.io.IOThreadWorker;
import xaero.pac.common.server.io.ObjectManagerLiveSaver;
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.party.PartyPlayerInfoUpdater;
import xaero.pac.common.server.parties.party.PlayerLogInPartyAssigner;
import xaero.pac.common.server.parties.party.expiration.PartyExpirationHandler;
import xaero.pac.common.server.parties.party.io.PartyManagerIO;
import xaero.pac.common.server.parties.system.IPlayerPartySystemManager;
import xaero.pac.common.server.player.PlayerLoginHandler;
import xaero.pac.common.server.player.PlayerLogoutHandler;
import xaero.pac.common.server.player.PlayerTickHandler;
import xaero.pac.common.server.player.PlayerWorldJoinHandler;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.io.PlayerConfigIO;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;
import xaero.pac.common.server.player.permission.IPlayerPermissionSystemManager;
import xaero.pac.common.server.player.permission.PlayerPermissionChangeHandler;
import xaero.pac.common.server.task.ServerSpreadoutQueuedTaskHandler;

public interface IServerData
<
	CM extends IServerClaimsManager<?, ?, ?>,
	P extends IServerParty<?, ?, ?>
>
extends IServerDataAPI {

	//internal API

	@Override
	public IPartyManager<P> getPartyManager();
	@Override
	public CM getServerClaimsManager();
	@Override
	public IPlayerConfigManager getPlayerConfigs();
	@Override
	public AdaptiveLocalizer getAdaptiveLocalizer();
	@Override
	public ChunkProtection<CM> getChunkProtection();

	public PlayerWorldJoinHandler getPlayerWorldJoinHandler();
	public PlayerLoginHandler getPlayerLoginHandler();
	public PlayerLogoutHandler getPlayerLogoutHandler();
	public PlayerPermissionChangeHandler getPlayerPermissionChangeHandler();
	public ForceLoadTicketManager getForceLoadManager();
	public ServerTickHandler getServerTickHandler();
	public PlayerTickHandler getPlayerTickHandler();
	public IOThreadWorker getIoThreadWorker();
	public PartyExpirationHandler getPartyExpirationHandler();
	public PartyManagerIO<?> getPartyManagerIO();
	public PlayerConfigIO<P, CM> getPlayerConfigsIO();
	public ObjectManagerLiveSaver getPartyLiveSaver();
	public ObjectManagerLiveSaver getPlayerConfigLiveSaver();
	public ObjectManagerLiveSaver getPlayerClaimInfoLiveSaver();
	public MinecraftServer getServer();
	public PlayerLogInPartyAssigner getPlayerPartyAssigner();
	public PartyPlayerInfoUpdater getPartyMemberInfoUpdater();
	public ServerStartingCallback getServerLoadCallback();
	public ServerInfo getServerInfo();
	public ServerInfoHolderIO getServerInfoIO();
	public ServerPlayerClaimsExpirationHandler getServerPlayerClaimsExpirationHandler();
	public ServerSpreadoutQueuedTaskHandler<ObjectExpirationCheckSpreadoutTask<?>> getObjectExpirationCheckTaskHandler();
	public IPlayerPermissionSystemManager getPlayerPermissionSystemManager();
	public IPlayerPartySystemManager getPlayerPartySystemManager();
	public void onStop();
	public void onServerResourcesReload(ResourceManager resourceManager);

}
