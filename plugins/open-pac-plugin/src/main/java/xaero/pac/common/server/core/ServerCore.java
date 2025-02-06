package xaero.pac.common.server.core;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.effects.ReplaceDisk;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.entity.EntityData;
import xaero.pac.common.entity.IEntity;
import xaero.pac.common.entity.IItemEntity;
import xaero.pac.common.packet.ClientboundPacDimensionHandshakePacket;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.platform.Services;
import xaero.pac.common.reflect.Reflection;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.core.accessor.ICreateArmInteractionPoint;
import xaero.pac.common.server.core.accessor.ICreateContraption;
import xaero.pac.common.server.core.accessor.IServerCommonPacketListenerImpl;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.world.ServerLevelHelper;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ServerCore {

	public static Block DETECTING_ENTITY_BLOCK_COLLISION = null;
	public static BlockPos DETECTING_ENTITY_BLOCK_COLLISION_POS = null;
	private static final Component TRAIN_CONTROLS_MESSAGE = Component.translatable("gui.xaero_claims_protection_create_train_controls_protected").withStyle(s -> s.withColor(ChatFormatting.RED));

	public static void onServerTickStart(MinecraftServer server) {
		OpenPartiesAndClaims.INSTANCE.startupCrashHandler.check();
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(server);
		if(serverData != null)
			try {
				serverData.getServerTickHandler().onTick(serverData);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
	}

	public static void onServerWorldInfo(ServerPlayer player){
		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, new ClientboundPacDimensionHandshakePacket(ServerConfig.CONFIG.claimsEnabled.get(), ServerConfig.CONFIG.partiesEnabled.get()));
	}

	public static boolean canAddLivingEntityEffect(LivingEntity target, MobEffectInstance effect, @Nullable Entity source){
		if(source == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(source.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onEntityInteraction(serverData, source, source, target, null, null, true, false, true);
		return !shouldProtect;
	}

	public static boolean canSpreadFire(LevelReader levelReader, BlockPos pos){
		if(!(levelReader instanceof Level level))
			return true;
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(serverLevel.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onFireSpread(serverData, serverLevel, pos);
		return !shouldProtect;
	}

	public static boolean mayUseItemAt(Player player, BlockPos pos, Direction direction, ItemStack itemStack){
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(player.getServer());
		if(serverData == null)
			return true;
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(player.level());
		if(serverLevel == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onUseItemAt(serverData, player, serverLevel, pos, direction, itemStack, null, false, false, true);
		return !shouldProtect;
	}

	public static boolean replaceFluidCanPassThrough(boolean currentValue, BlockGetter blockGetter, BlockPos from, BlockPos to){
		if(!currentValue)
			return false;
		if(!(blockGetter instanceof Level level))
			return true;
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(serverLevel.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onFluidSpread(serverData, serverLevel, from, to);
		return !shouldProtect;
	}

	public static DispenseItemBehavior replaceDispenseBehavior(DispenseItemBehavior defaultValue, ServerLevel level, BlockPos blockPos) {
		if(defaultValue == DispenseItemBehavior.NOOP)
			return defaultValue;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return defaultValue;
		boolean shouldProtect = serverData.getChunkProtection().onDispenseFrom(serverData, level, blockPos);
		return shouldProtect ? DispenseItemBehavior.NOOP : defaultValue;
	}

	public static boolean canPistonPush(PistonStructureResolver pistonStructureResolver, Level level, BlockPos pistonPos, Direction direction, boolean extending){
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(serverLevel.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onPistonPush(serverData, serverLevel, pistonStructureResolver.getToPush(), pistonStructureResolver.getToDestroy(), pistonPos, direction, extending);
		return !shouldProtect;
	}

	private static boolean isCreateModAllowed(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, ServerLevel serverLevel, int posChunkX, int posChunkZ, BlockPos sourceOrAnchor, boolean checkNeighborBlocks, boolean affectsBlocks, boolean affectsEntities){
		boolean shouldProtect = serverData.getChunkProtection().onCreateMod(serverData, serverLevel, posChunkX, posChunkZ, sourceOrAnchor, checkNeighborBlocks, affectsBlocks, affectsEntities);
		return !shouldProtect;
	}

	private static boolean isCreateModAllowed(Level level, BlockPos pos, BlockPos sourceOrAnchor, boolean affectsBlocks, boolean affectsEntities){
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(serverLevel.getServer());
		if(serverData == null)
			return true;
		return isCreateModAllowed(serverData, serverLevel, pos.getX() >> 4, pos.getZ() >> 4, sourceOrAnchor, true, affectsBlocks, affectsEntities);
	}

	public static boolean isCreateModAllowed(Level level, BlockPos pos, ICreateContraption contraption){
		//cant rename
		//called when a contraption tries to move a block
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(serverLevel.getServer());
		if(serverData == null)
			return true;
		return isCreateModAllowed(serverData, serverLevel, pos.getX() >> 4, pos.getZ() >> 4, getEffectiveAnchor(contraption), true, true, false);
	}

	public static BlockPos CAPTURED_TARGET_POS;
	public static BlockState replaceBlockFetchOnCreateModBreak(BlockState actual, Level level, BlockPos sourceOrAnchor){
		if(!isCreateModAllowed(level, CAPTURED_TARGET_POS, sourceOrAnchor, true, false))
			return Blocks.BEDROCK.defaultBlockState();//fake bedrock won't be broken by create
		return actual;
	}

	public static BlockState replaceBlockFetchOnCreateModBreak(BlockState actual, Level level, ICreateContraption contraption){
		return replaceBlockFetchOnCreateModBreak(actual, level, getEffectiveAnchor(contraption));
	}

	public static Map<BlockPos, BlockState> CAPTURED_POS_STATE_MAP;
	public static void onCreateModSymmetryProcessed(Level level, Player player){
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(serverLevel.getServer());
		if(serverData == null)
			return;
		if(CAPTURED_POS_STATE_MAP == null)
			return;
		Iterator<BlockPos> posIterator = CAPTURED_POS_STATE_MAP.keySet().iterator();
		while(posIterator.hasNext()){
			BlockPos pos = posIterator.next();
			if(serverData.getChunkProtection().onEntityPlaceBlock(serverData, player, serverLevel, pos, null))
				posIterator.remove();
		}
	}

	public static boolean canCreateCannonPlaceBlock(BlockEntity placer, BlockPos pos){
		Level level = placer.getLevel();
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(serverLevel.getServer());
		if(serverData == null)
			return true;
		return isCreateModAllowed(serverData, serverLevel, pos.getX() >> 4, pos.getZ() >> 4, placer.getBlockPos(), false, true, false);
	}

	public static void onCreateCollideEntities(List<Entity> entities, Entity contraptionEntity, ICreateContraption contraption){
		Level level = contraptionEntity.level();
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(serverLevel.getServer());
		if(serverData == null)
			return;
		serverData.getChunkProtection().onCreateModAffectPositionedObjects(serverData, serverLevel, entities, Entity::chunkPosition, getEffectiveAnchor(contraption), true, true, false, true);
	}

	public static boolean isCreateMechanicalArmValid(BlockEntity arm, List<ICreateArmInteractionPoint> points){
		Level level = arm.getLevel();
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(serverLevel.getServer());
		if(serverData == null)
			return true;
		if(serverData.getChunkProtection().onCreateModAffectPositionedObjects(serverData, serverLevel, points, p -> new ChunkPos(p.xaero_OPAC_getPos()), arm.getBlockPos(), false, false, true, false)){
			points.clear();
			return false;
		}
		return true;
	}

	@Deprecated(forRemoval = true)
	public static boolean isCreateTileEntityPacketAllowed(BlockEntity tileEntity, ServerPlayer player){
		//only used on fabric which doesn't have the latest create version yet
		return isCreateTileEntityPacketAllowed(tileEntity.getBlockPos(), player);
	}

	public static boolean isCreateTileEntityPacketAllowed(BlockPos pos, ServerPlayer player){
		if(pos == null)//when "stop tracking" is selected
			return true;
		ServerLevel level = player.serverLevel();
		BlockEntity tileEntity = level.getBlockEntity(pos);
		if(tileEntity == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onBlockSpecialInteraction(serverData, player, level, pos);
		return !shouldProtect;
	}

	public static boolean isCreateContraptionInteractionPacketAllowed(int contraptionId, InteractionHand interactionHand, ServerPlayer player){
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(player.getServer());
		if(serverData == null)
			return true;
		Entity contraption = player.serverLevel().getEntity(contraptionId);
		boolean shouldProtect = serverData.getChunkProtection().onEntityInteraction(serverData, null, player, contraption, null, interactionHand, false, true, true);
		return !shouldProtect;
	}

	public static boolean isCreateTrainRelocationPacketAllowed(int contraptionId, BlockPos pos, ServerPlayer player) {
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(player.getServer());
		if(serverData == null)
			return true;
		Entity contraption = player.serverLevel().getEntity(contraptionId);
		boolean shouldProtect = serverData.getChunkProtection().onEntityInteraction(serverData, null, player, contraption, null, null, false, true, true);
		if(!shouldProtect)
			shouldProtect = serverData.getChunkProtection().onBlockInteraction(serverData, player.serverLevel().getBlockState(pos), player, null, null, player.serverLevel(), pos, Direction.UP, false, true);
		return !shouldProtect;
	}

	public static boolean isCreateTrainControlsPacketAllowed(int contraptionId, ServerPlayer player) {
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(player.getServer());
		if(serverData == null)
			return true;
		Entity contraption = player.serverLevel().getEntity(contraptionId);
		boolean shouldProtect = serverData.getChunkProtection().onEntityInteraction(serverData, null, player, contraption, null, null, false, false, true);
		if(shouldProtect)
			player.sendSystemMessage(serverData.getAdaptiveLocalizer().getFor(player, TRAIN_CONTROLS_MESSAGE));
		return !shouldProtect;
	}

	private static boolean isCreateDeployerBlockInteractionAllowed(Level level, BlockPos anchor, BlockPos pos){
		return isCreateModAllowed(level, pos, anchor, true, true);
	}

	public static boolean isCreateDeployerBlockInteractionAllowed(Level level, ICreateContraption contraption, BlockPos pos){
		return isCreateDeployerBlockInteractionAllowed(level, getEffectiveAnchor(contraption), pos);
	}

	public static boolean isCreateTileDeployerBlockInteractionAllowed(BlockEntity tileEntity){
		Direction direction = tileEntity.getBlockState().getValue(BlockStateProperties.FACING);
		BlockPos pos = tileEntity.getBlockPos().relative(direction, 2);
		return isCreateDeployerBlockInteractionAllowed(tileEntity.getLevel(), tileEntity.getBlockPos(), pos);
	}

	public static boolean isCreateGlueSelectionAllowed(BlockPos from, BlockPos to, ServerPlayer player) {
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(player.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onCreateGlueSelection(serverData,from, to, player);
		return !shouldProtect;
	}

	public static boolean isCreateGlueRemovalAllowed(int entityId, ServerPlayer player) {
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(player.getServer());
		if(serverData == null)
			return true;
		boolean shouldProtect = serverData.getChunkProtection().onCreateGlueRemoval(serverData, entityId, player);
		return !shouldProtect;
	}

	public static boolean isProjectileHitAllowed(Projectile entity, EntityHitResult hitResult){
		return !OpenPartiesAndClaims.INSTANCE.getCommonEvents().onProjectileImpact(hitResult, entity);
	}

	public static ProjectileDeflection checkProjectileHit(HitResult hitResult, Projectile entity){
		if(entity.getServer() == null)
			return null;
		if(hitResult == null || hitResult.getType() == HitResult.Type.MISS)
			return null;
		if(OpenPartiesAndClaims.INSTANCE.getCommonEvents().onProjectileImpact(hitResult, entity)) {
			entity.discard();
			return ProjectileDeflection.NONE;
		}
		return null;
	}

	private static Level CREATE_DISASSEMBLE_SUPER_GLUE_LEVEL;
	private static BlockPos CREATE_DISASSEMBLE_SUPER_GLUE_ANCHOR;

	public static void preCreateDisassembleSuperGlue(Level level, ICreateContraption contraption){
		BlockPos anchor = getEffectiveAnchor(contraption);
		CREATE_DISASSEMBLE_SUPER_GLUE_LEVEL = level;
		CREATE_DISASSEMBLE_SUPER_GLUE_ANCHOR = anchor;
	}

	public static void postCreateDisassembleSuperGlue(){
		CREATE_DISASSEMBLE_SUPER_GLUE_LEVEL = null;
		CREATE_DISASSEMBLE_SUPER_GLUE_ANCHOR = null;
	}

	public static BlockPos getFreshAddedSuperGlueAnchor(Level level){
		if(level != null && level == CREATE_DISASSEMBLE_SUPER_GLUE_LEVEL)
			return CREATE_DISASSEMBLE_SUPER_GLUE_ANCHOR;
		return null;
	}

	public static boolean canCreatePipeAffectBlock(Level level, BlockPos from, BlockPos to, boolean simulate){
		if(simulate)
			return true;
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(serverLevel.getServer());
		return isCreateModAllowed(serverData, serverLevel, to.getX() >> 4, to.getZ() >> 4, from, false, true, true);
	}

	public static boolean canCreatePloughPos(Level level, ICreateContraption contraption, BlockPos pos){
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return true;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(serverLevel.getServer());
		BlockPos anchor = getEffectiveAnchor(contraption);
		return isCreateModAllowed(serverData, serverLevel, pos.getX() >> 4, pos.getZ() >> 4, anchor, true, true, false);
	}

	private static boolean CREATE_PLACING_CONTRAPTION;
	private static int CREATE_PLACING_CONTRAPTION_TICK;
	private static boolean CREATE_PLACING_CONTRAPTION_USABLE = true;

	private static boolean checkCreatePlacingContraptionUsable(MinecraftServer server){
		if(CREATE_PLACING_CONTRAPTION && (server == null || server.getTickCount() != CREATE_PLACING_CONTRAPTION_TICK)){
			CREATE_PLACING_CONTRAPTION = false;
			CREATE_PLACING_CONTRAPTION_USABLE = false;
			OpenPartiesAndClaims.LOGGER.warn("Create mod's minecart contraption placement capture isn't working properly. Turning it off...");
		}
		return CREATE_PLACING_CONTRAPTION_USABLE;
	}

	public static void preMinecartContraptionPlaced(UseOnContext context){
		MinecraftServer server = context.getLevel().getServer();
		if(server == null)
			return;
		//if this is called twice before calling post, then it's already suspicious without a tick count check
		//using null server argument below
		if(!checkCreatePlacingContraptionUsable(null))
			return;
		CREATE_PLACING_CONTRAPTION_TICK = server.getTickCount();
		CREATE_PLACING_CONTRAPTION = true;
	}

	public static void postMinecartContraptionPlaced(){
		CREATE_PLACING_CONTRAPTION = false;
	}

	public static boolean isPlacingCreateContraption(MinecraftServer server){
		if(!checkCreatePlacingContraptionUsable(server))//tick check matters here
			return false;
		return CREATE_PLACING_CONTRAPTION;
	}

	public static BlockPos getEffectiveAnchor(ICreateContraption contraption){
		if(contraption.getXaero_OPAC_placementPos() == null)
			return contraption.getXaero_OPAC_anchor();
		return contraption.getXaero_OPAC_placementPos();
	}

	private static final ResourceKey<Item> CREATE_COUPLING = ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("create", "minecart_coupling"));

	public static boolean canCreateAddCoupling(Player player, Level world, int cartId1, int cartId2){
		if(player == null)
			return true;
		MinecraftServer server = player.getServer();
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(server);
		if(serverData == null)
			return true;
		Item couplingItem = BuiltInRegistries.ITEM.getValue(CREATE_COUPLING);
		if(couplingItem == null)
			return true;
		Entity cart1 = world.getEntity(cartId1);
		ItemStack heldItem = new ItemStack(couplingItem);
		if(cart1 != null && serverData.getChunkProtection().onEntityInteraction(serverData, null, player, cart1, heldItem, null, false, false, true))
			return false;
		Entity cart2 = world.getEntity(cartId2);
		return cart2 == null || !serverData.getChunkProtection().onEntityInteraction(serverData, null, player, cart2, heldItem, null, false, false, true);
	}

	public static boolean replaceEntityIsInvulnerable(boolean actual, DamageSource damageSource, Entity entity){
		if(!actual)
			return OpenPartiesAndClaims.INSTANCE.getCommonEvents().onLivingHurt(damageSource, entity);
		return actual;
	}

	public static boolean canDestroyBlock(Level level, BlockPos pos, Entity entity) {
		return entity == null || !OpenPartiesAndClaims.INSTANCE.getCommonEvents().onEntityDestroyBlock(level, pos, entity);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Entity> List<T> onEntitiesPushBlock(List<T> entities, Block block, BlockPos pos){
		if(entities.size() == 1) {
			if(onEntityPushBlock(block, entities.get(0), pos))
				return (List<T>) ENTITY_PUSH_BLOCK_LIST;//it's empty
			return entities;
		}
		if(entities.isEmpty())
			return entities;
		Entity firstEntity = entities.get(0);
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(firstEntity.getServer());
		if(serverData == null)
			return entities;
		Level level = firstEntity.level();
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return entities;
		serverData.getChunkProtection().onEntitiesPushBlock(serverData, serverLevel, pos, block, entities);
		return entities;
	}

	public static boolean onEntityPushBlock(Block block, Entity entity, BlockHitResult blockHitResult){
		return onEntityPushBlock(block, entity, blockHitResult.getBlockPos());
	}

	private static List<Entity> ENTITY_PUSH_BLOCK_LIST = new ArrayList<>(1);

	public static boolean onEntityPushBlock(Block block, Entity entity, BlockPos blockPos){
		if(entity == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(entity.getServer());
		if(serverData == null)
			return false;
		Level level = entity.level();
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(level);
		if(serverLevel == null)
			return false;
		ENTITY_PUSH_BLOCK_LIST.add(entity);
		serverData.getChunkProtection().onEntitiesPushBlock(serverData, serverLevel, blockPos, block, ENTITY_PUSH_BLOCK_LIST);
		boolean result = ENTITY_PUSH_BLOCK_LIST.isEmpty();
		ENTITY_PUSH_BLOCK_LIST.clear();
		return result;
	}

	public static final BlockPos.MutableBlockPos ENCHANTMENT_EFFECT_BLOCKPOS = new BlockPos.MutableBlockPos();
	private static BlockState ENCHANTMENT_EFFECT_BLOCKSTATE = null;
	private static WeakReference<ServerLevel> ENCHANTMENT_EFFECT_LEVEL = null;
	private static WeakReference<Entity> ENCHANTMENT_EFFECT_ENTITY = null;
	private static int ENCHANTMENT_EFFECT_I = 0;
	private static int HANDLING_ENCHANTMENT_ON_BLOCK = -1;
	private static int HANDLING_ENCHANTMENT_ON_DISK = -1;
	private static boolean ENCHANTMENT_ON_BLOCK_USABLE = true;
	private static boolean ENCHANTMENT_ON_DISK_USABLE = true;

	private static boolean checkEnchantmentOnBlockUsable(ServerLevel level){
		if(ENCHANTMENT_ON_BLOCK_USABLE && HANDLING_ENCHANTMENT_ON_BLOCK != -1 && HANDLING_ENCHANTMENT_ON_BLOCK != level.getServer().getTickCount()){
			OpenPartiesAndClaims.LOGGER.error("Enchantment effect on block capture isn't working properly. Likely a compatibility issue. Turning it off...");
			ENCHANTMENT_ON_BLOCK_USABLE = false;
		}
		return ENCHANTMENT_ON_BLOCK_USABLE;
	}

	private static boolean checkEnchantmentOnDiskUsable(ServerLevel level){
		if(ENCHANTMENT_ON_DISK_USABLE && HANDLING_ENCHANTMENT_ON_DISK != -1 && HANDLING_ENCHANTMENT_ON_DISK != level.getServer().getTickCount()){
			OpenPartiesAndClaims.LOGGER.error("Enchantment effect on disk capture isn't working properly. Likely a compatibility issue. Turning it off...");
			ENCHANTMENT_ON_DISK_USABLE = false;
		}
		return ENCHANTMENT_ON_DISK_USABLE;
	}

	public static boolean captureEnchantmentEffectLevel(ServerLevel level, Entity entity) {
		if(level.getServer() == null)
			return false;
		if(!level.getServer().isSameThread())
			return false;
		if(ServerConfig.CONFIG.alwaysProtectBlocksFromEnchantments.get())
			return true;
		ENCHANTMENT_EFFECT_LEVEL = new WeakReference<>(level);
		ENCHANTMENT_EFFECT_ENTITY = new WeakReference<>(entity);
		return false;
	}

	public static void enchantmentEffectStoreBlockState(BlockState state){
		//using these store/load methods in the neo/forge coremod to get around BlockState arguments on the stack
		ENCHANTMENT_EFFECT_BLOCKSTATE = state;
	}

	public static BlockState enchantmentEffectLoadBlockState(){
		return ENCHANTMENT_EFFECT_BLOCKSTATE;
	}

	public static void enchantmentEffectStoreInt(int i){
		//using these store/load methods in the neo/forge coremod to get around int arguments on the stack
		ENCHANTMENT_EFFECT_I = i;
	}

	public static int enchantmentEffectLoadInt(){
		return ENCHANTMENT_EFFECT_I;
	}

	public static BlockPos replaceEnchantmentEffectBlockPos(BlockPos actual) {
		ServerLevel level = ENCHANTMENT_EFFECT_LEVEL == null ? null : ENCHANTMENT_EFFECT_LEVEL.get();
		if(level == null)
			return actual;
		Entity entity = ENCHANTMENT_EFFECT_ENTITY == null ? null : ENCHANTMENT_EFFECT_ENTITY.get();
		if(entity == null)
			return actual;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return actual;
		checkEnchantmentOnBlockUsable(level);//checking but not using the value
		HANDLING_ENCHANTMENT_ON_BLOCK = level.getServer().getTickCount();
		if(serverData.getChunkProtection().onEnchantmentEffectOnBlock(serverData, entity, level, actual)) {
			ENCHANTMENT_EFFECT_BLOCKPOS.set(actual.getX(), level.getMaxY() + 1, actual.getZ());
			return ENCHANTMENT_EFFECT_BLOCKPOS;//outside build limit, so won't build
		}
		return actual;
	}

	public static BlockPos replaceEnchantmentEffectBlockPosDisk(BlockPos actual, ServerLevel level, Entity entity, int enchantmentLevel, ReplaceDisk effect) {
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return actual;
		checkEnchantmentOnDiskUsable(level);//checking but not using the value
		HANDLING_ENCHANTMENT_ON_DISK = level.getServer().getTickCount();
		if(serverData.getChunkProtection().onEnchantmentEffectOnBlockDisk(serverData, entity, level, actual, (int)effect.radius().calculate(enchantmentLevel))) {
			ENCHANTMENT_EFFECT_BLOCKPOS.set(actual.getX(), level.getMaxY() + 1, actual.getZ());
			return ENCHANTMENT_EFFECT_BLOCKPOS;//outside build limit, so won't build
		}
		return actual;
	}

	public static void postEnchantmentEffectOnBlock() {
		HANDLING_ENCHANTMENT_ON_BLOCK = -1;
	}

	public static void postEnchantmentEffectOnDisk() {
		HANDLING_ENCHANTMENT_ON_DISK = -1;
	}

	public static boolean isHandlingEnchantmentOnBlocks(ServerLevel level) {
		return checkEnchantmentOnBlockUsable(level) && HANDLING_ENCHANTMENT_ON_BLOCK != -1
				|| checkEnchantmentOnDiskUsable(level) && HANDLING_ENCHANTMENT_ON_DISK != -1;
	}

	private static final Field ENTITY_PORTAL_PROCESS_FIELD = Reflection.getFieldReflection(Entity.class, "portalProcess", "f_336952_", "field_51994", "Lnet/minecraft/class_9787;");

	public static boolean onHandleNetherPortal(Entity entity) {
		ServerLevel serverLevel = ServerLevelHelper.getServerLevel(entity.level());
		if(serverLevel == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(serverLevel.getServer());
		if(serverData == null)
			return false;
		if(serverData.getChunkProtection().onNetherPortal(serverData, entity, serverLevel, entity.blockPosition())){
			Profiler.get().pop();
			Reflection.setReflectFieldValue(entity, ENTITY_PORTAL_PROCESS_FIELD, null);
			return true;
		}
		return false;
	}

	private static boolean FINDING_RAID_SPAWN_POS;
	private static boolean RAID_SPAWN_POS_CAPTURE_USABLE = true;
	private static int RAID_SPAWN_POS_CAPTURE_TICK;
	public static void onFindRandomSpawnPosPre(Raid raid){//returns whether to completely disable (when capture isn't usable)
		if(!RAID_SPAWN_POS_CAPTURE_USABLE)
			return;
		FINDING_RAID_SPAWN_POS = true;
		RAID_SPAWN_POS_CAPTURE_TICK = raid.getLevel().getServer().getTickCount();
	}

	public static void onFindRandomSpawnPosPost(){
		FINDING_RAID_SPAWN_POS = false;
	}

	public static boolean replaceIsPositionEntityTicking(boolean currentReturn, ServerLevel level, BlockPos pos){
		if(!currentReturn)
			return false;
		if(FINDING_RAID_SPAWN_POS) {
			if(level.getServer().getTickCount() != RAID_SPAWN_POS_CAPTURE_TICK){
				OpenPartiesAndClaims.LOGGER.error("Raid spawn capture isn't working properly. Likely a compatibility issue. Turning off raid protection... Please use the disableRaids game rule to disable raids across the server.");
				RAID_SPAWN_POS_CAPTURE_USABLE = false;
				onFindRandomSpawnPosPost();
				return false;
			}
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData = ServerData.from(level.getServer());
			if (serverData == null)
				return true;
			return !serverData.getChunkProtection().onRaidSpawn(serverData, level, pos);
		}
		return true;
	}

	private static LivingEntity DYING_LIVING;
	private static DamageSource DYING_LIVING_FROM;
	private static boolean DYING_LIVING_USABLE = true;
	private static int DYING_LIVING_TICK;
	//below are the alternatives for DYING_LIVING stuff
	private static LivingEntity DROPPING_LOOT_LIVING;
	private static DamageSource DROPPING_LOOT_LIVING_FROM;
	private static boolean DROPPING_LOOT_LIVING_USABLE = true;
	private static int DROPPING_LOOT_LIVING_TICK;
	public static void onLivingEntityDiePre(LivingEntity living, DamageSource source) {
		if(living.getServer() != null && living.getServer().isSameThread()) {
			if(DYING_LIVING == null && DYING_LIVING_USABLE) {//checking that the current capture is null in case a mob kills other mobs on death
				DYING_LIVING_FROM = source;
				DYING_LIVING = living;
				DYING_LIVING_TICK = living.getServer().getTickCount();
			}
		}
	}

	public static void onLivingEntityDiePost(LivingEntity living) {
		if(living.getServer() != null && living.getServer().isSameThread() && DYING_LIVING == living) {
			DYING_LIVING_FROM = null;
			DYING_LIVING = null;
		}
	}

	public static void onLivingEntityDropDeathLootPre(LivingEntity living, DamageSource source) {
		if(living.getServer() != null && living.getServer().isSameThread()) {
			if(DROPPING_LOOT_LIVING == null && DROPPING_LOOT_LIVING_USABLE) {//checking that the current capture is null in case a mob kills other mobs on death
				DROPPING_LOOT_LIVING_FROM = source;
				DROPPING_LOOT_LIVING = living;
				DROPPING_LOOT_LIVING_TICK = living.getServer().getTickCount();
			}
		}
	}

	public static void onLivingEntityDropDeathLootPost(LivingEntity living) {
		if(living.getServer() != null && living.getServer().isSameThread() && DROPPING_LOOT_LIVING == living) {
			DROPPING_LOOT_LIVING_FROM = null;
			DROPPING_LOOT_LIVING = null;
		}
	}

	private static void testLivingLootCapture(int currentTickCount){
		boolean disabledCapture = false;
		if(DYING_LIVING_FROM != null && DYING_LIVING_TICK != currentTickCount) {
			disabledCapture = !DROPPING_LOOT_LIVING_USABLE;
			DYING_LIVING_USABLE = false;
			DYING_LIVING_FROM = null;
			DYING_LIVING = null;
		}
		if(DROPPING_LOOT_LIVING_FROM != null && DROPPING_LOOT_LIVING_TICK != currentTickCount) {
			disabledCapture = !DYING_LIVING_USABLE;
			DROPPING_LOOT_LIVING_USABLE = false;
			DROPPING_LOOT_LIVING_FROM = null;
			DROPPING_LOOT_LIVING = null;
		}
		if(disabledCapture)
			OpenPartiesAndClaims.LOGGER.error("Living entity loot capture isn't working properly. Likely a compatibility issue. Turning off loot-related protection... Please use keepInventory and other stuff, if this is an issue.");
	}

	public static DamageSource getDyingDamageSourceForCurrentEntitySpawns(int currentTickCount){
		testLivingLootCapture(currentTickCount);
		if(DYING_LIVING_FROM != null)
			return DYING_LIVING_FROM;
		return DROPPING_LOOT_LIVING_FROM;
	}

	public static LivingEntity getDyingLivingForCurrentEntitySpawns(int currentTickCount){
		testLivingLootCapture(currentTickCount);
		if(DYING_LIVING != null)
			return DYING_LIVING;
		return DROPPING_LOOT_LIVING;
	}

	private final static String LOOT_OWNER_KEY = "xaero_OPAC_lootOwnerId";
	private final static BiConsumer<IEntity, UUID> LOOT_OWNER_SETTER = (e, id) -> EntityData.from(e).setLootOwner(id);
	private final static Function<IEntity, UUID> LOOT_OWNER_GETTER = e -> EntityData.from(e).getLootOwner();
	private final static String DEAD_PLAYER_KEY = "xaero_OPAC_deadPlayer";
	private final static BiConsumer<IEntity, UUID> DEAD_PLAYER_SETTER = (e, id) -> EntityData.from(e).setDeadPlayer(id);
	private final static Function<IEntity, UUID> DEAD_PLAYER_GETTER = e -> EntityData.from(e).getDeadPlayer();
	private final static String THROWER_ACCESSOR_KEY = "xaero_OPAC_throwerAccessor";
	private final static BiConsumer<IEntity, UUID> THROWER_ACCESSOR_SETTER = (ie, id) -> ((IItemEntity)ie).setXaero_OPAC_throwerAccessor(id);
	private final static Function<IEntity, UUID> THROWER_ACCESSOR_GETTER = ie -> ((IItemEntity)ie).getXaero_OPAC_throwerAccessor();

	public static void setEntityGenericUUID(Entity entity, String key, UUID uuid, BiConsumer<IEntity, UUID> setter){
		setter.accept((IEntity) entity, uuid);
		CompoundTag persistentData = Services.PLATFORM.getEntityAccess().getPersistentData(entity);
		if(uuid == null)
			persistentData.remove(key);
		else
			persistentData.putUUID(key, uuid);
	}

	public static UUID getEntityGenericUUID(Entity entity, String key, Function<IEntity, UUID> getter, BiConsumer<IEntity, UUID> setter){
		UUID result = getter.apply((IEntity) entity);
		if(result == null) {
			CompoundTag persistentData = Services.PLATFORM.getEntityAccess().getPersistentData(entity);
			if(persistentData.contains(key)) {
				result = persistentData.getUUID(key);
				setter.accept((IEntity) entity, result);
			}
		}
		return result;
	}

	public static void setLootOwner(Entity entity, UUID lootOwner){
		setEntityGenericUUID(entity, LOOT_OWNER_KEY, lootOwner, LOOT_OWNER_SETTER);
	}

	public static UUID getLootOwner(Entity entity){
		return getEntityGenericUUID(entity, LOOT_OWNER_KEY, LOOT_OWNER_GETTER, LOOT_OWNER_SETTER);
	}

	public static void setDeadPlayer(Entity entity, UUID deadPlayer){
		setEntityGenericUUID(entity, DEAD_PLAYER_KEY, deadPlayer, DEAD_PLAYER_SETTER);
	}

	public static UUID getDeadPlayer(Entity entity){
		return getEntityGenericUUID(entity, DEAD_PLAYER_KEY, DEAD_PLAYER_GETTER, DEAD_PLAYER_SETTER);
	}

	public static void setThrowerAccessor(ItemEntity entity, UUID throwerAccessor){
		setEntityGenericUUID(entity, THROWER_ACCESSOR_KEY, throwerAccessor, THROWER_ACCESSOR_SETTER);
	}

	public static UUID getThrowerAccessor(ItemEntity entity){
		return getEntityGenericUUID(entity, THROWER_ACCESSOR_KEY, THROWER_ACCESSOR_GETTER, THROWER_ACCESSOR_SETTER);
	}

	public static boolean onEntityItemPickup(Entity entity, ItemEntity itemEntity) {
		return OpenPartiesAndClaims.INSTANCE.getCommonEvents().onItemPickup(entity, itemEntity);
	}

	public static boolean onMobItemPickup(ItemEntity itemEntity, Mob mob) {
		if (OpenPartiesAndClaims.INSTANCE.getCommonEvents().onItemPickup(mob, itemEntity)) {
			Profiler.get().pop();
			return true;
		}
		return false;
	}

	public static Player onExperiencePickup(Player player, ExperienceOrb orb) {
		if(orb == null || player == null)
			return player;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(orb.getServer());
		if (serverData == null)
			return player;
		if(serverData.getChunkProtection().onExperiencePickup(serverData, orb, player))
			return null;
		return player;
	}

	private static boolean MOB_GRIEFING_IS_FOR_ITEMS;
	private static boolean MOB_GRIEFING_IS_FOR_ITEMS_USABLE = true;
	private static int MOB_GRIEFING_IS_FOR_ITEMS_TICK;
	public static void forgePreItemMobGriefingCheck(Mob mob){
		if(!MOB_GRIEFING_IS_FOR_ITEMS_USABLE)
			return;
		if(mob.getServer() != null && mob.getServer().isSameThread()) {
			MOB_GRIEFING_IS_FOR_ITEMS = true;
			MOB_GRIEFING_IS_FOR_ITEMS_TICK = mob.getServer().getTickCount();
		}
	}

	public static void forgePostItemMobGriefingCheck(Mob mob){
		if(mob.getServer() != null && mob.getServer().isSameThread()) {
			MOB_GRIEFING_IS_FOR_ITEMS = false;
		}
	}

	public static boolean isMobGriefingForItems(int currentTick){
		if(MOB_GRIEFING_IS_FOR_ITEMS && MOB_GRIEFING_IS_FOR_ITEMS_TICK != currentTick){
			OpenPartiesAndClaims.LOGGER.error("Mob griefing rule check for item pickups capture is not working properly. Turning it off... If this is a problem, please manually configure which mobs grief dropped items in the main server config file with options \"nonBlockGriefingMobs\" and \"droppedItemGriefingMobs\".");
			MOB_GRIEFING_IS_FOR_ITEMS_USABLE = false;
			MOB_GRIEFING_IS_FOR_ITEMS = false;
		}
		return MOB_GRIEFING_IS_FOR_ITEMS;
	}

	private static Entity BEHAVIOR_UTILS_THROW_ITEM_LIVING;
	private static boolean BEHAVIOR_UTILS_THROW_ITEM_USABLE = true;
	private static int BEHAVIOR_UTILS_THROW_ITEM_TICK;
	public static void preThrowItem(Entity entity) {
		if(!BEHAVIOR_UTILS_THROW_ITEM_USABLE)
			return;
		if(entity != null && entity.getServer() != null && entity.getServer().isSameThread()) {
			if(BEHAVIOR_UTILS_THROW_ITEM_LIVING == null) {
				BEHAVIOR_UTILS_THROW_ITEM_LIVING = entity;
				BEHAVIOR_UTILS_THROW_ITEM_TICK = entity.getServer().getTickCount();
			} else if(BEHAVIOR_UTILS_THROW_ITEM_TICK != entity.getServer().getTickCount()){
				OpenPartiesAndClaims.LOGGER.error("Part of the non-player entity item toss capture isn't working properly. Turning it off...");
				BEHAVIOR_UTILS_THROW_ITEM_USABLE = false;
				BEHAVIOR_UTILS_THROW_ITEM_LIVING = null;
			}
		}
	}

	public static void onThrowItem(ItemEntity itemEntity) {
		if(BEHAVIOR_UTILS_THROW_ITEM_LIVING != null && itemEntity.getServer() != null && itemEntity.getServer().isSameThread()) {
			itemEntity.setThrower(BEHAVIOR_UTILS_THROW_ITEM_LIVING);
			BEHAVIOR_UTILS_THROW_ITEM_LIVING = null;
		}
	}

	private static boolean RESOURCES_DROP_OWNER_CAPTURE_USABLE = true;
	private static Entity RESOURCES_DROP_OWNER;
	private static int RESOURCES_DROP_OWNER_TICK;

	public static void preResourcesDrop(Entity entity){
		if(RESOURCES_DROP_OWNER_CAPTURE_USABLE && RESOURCES_DROP_OWNER == null && entity != null &&
				entity.getServer() != null && entity.getServer().isSameThread()) {
			RESOURCES_DROP_OWNER = entity;
			RESOURCES_DROP_OWNER_TICK = entity.getServer().getTickCount();
		}
	}

	public static void postResourcesDrop(Entity entity){
		if(entity == RESOURCES_DROP_OWNER && entity != null &&
				entity.getServer() != null && entity.getServer().isSameThread())
			RESOURCES_DROP_OWNER = null;
	}

	public static Entity getResourcesDropOwner() {
		if(RESOURCES_DROP_OWNER != null && RESOURCES_DROP_OWNER_TICK != RESOURCES_DROP_OWNER.getServer().getTickCount()) {
			OpenPartiesAndClaims.LOGGER.error("Block/entity resource drop owner capture isn't working properly. Likely a compatibility issue. Turning it off...");
			RESOURCES_DROP_OWNER = null;
			RESOURCES_DROP_OWNER_CAPTURE_USABLE = false;
		}
		return RESOURCES_DROP_OWNER;
	}

	public static void onFishingHookAddEntity(Entity entity, FishingHook hook){
		if(entity instanceof ItemEntity itemEntity) {
			preThrowItem(hook.getOwner());
			onThrowItem(itemEntity);
		}
	}

	public static boolean onItemMerge(ItemEntity first, ItemEntity second){
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(first.getServer());
		if (serverData == null)
			return false;
		return serverData.getChunkProtection().onItemStackMerge(serverData, first, second);
	}

	public static boolean onExperienceMerge(ExperienceOrb from, ExperienceOrb into){
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(into.getServer());
		if (serverData == null)
			return false;
		return serverData.getChunkProtection().onExperienceMerge(serverData, from, into);
	}

	public static boolean onSetFishingHookedEntity(FishingHook hook, Entity entity) {
		if(entity == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(entity.getServer());
		if (serverData == null)
			return false;
		return serverData.getChunkProtection().onFishingHookedEntity(serverData, hook, entity);
	}

	public static List<Entity> onEntitiesPushEntity(List<Entity> entities, Entity target){
		if(target == null)
			return entities;
		if(entities.isEmpty())
			return entities;
		if(!(target instanceof HangingEntity))
			return entities;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(target.getServer());
		if (serverData == null)
			return entities;
		serverData.getChunkProtection().onEntitiesCollideWithEntity(serverData, target, entities);
		return entities;
	}

	public static List<Entity> onEntityAffectsEntities(List<Entity> targets, Entity entity){
		if(entity == null)
			return targets;
		if(targets.isEmpty())
			return targets;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(entity.getServer());
		if (serverData == null)
			return targets;
		serverData.getChunkProtection().onEntityAffectsEntities(serverData, entity, targets);
		return targets;
	}

	public static boolean onEntityPushed(Entity target, MoverType moverType) {
		if(target == null)
			return false;
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(target.getServer());
		if (serverData == null)
			return false;
		return serverData.getChunkProtection().onEntityPushed(serverData, target, moverType);
	}

	public static void preServerLevelTick(ServerLevel level) {
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(level.getServer());
		if(serverData == null)
			return;
		if(serverData.getForceLoadManager().hasEnabledTickets(level))
			level.resetEmptyTime();//continue ticking entities in dimensions with no players
	}

	public static UUID getItemEntityThrower(ItemEntity itemEntity){
		return ((IItemEntity)itemEntity).getXaero_OPAC_thrower();
	}

	public static UUID getItemEntityOwner(ItemEntity itemEntity){
		return ((IItemEntity)itemEntity).getXaero_OPAC_target();
	}

	public static Connection getServerGamePacketListenerConnection(ServerGamePacketListenerImpl serverGamePacketListener){
		return ((IServerCommonPacketListenerImpl)serverGamePacketListener).getXaero_OPAC_connection();
	}

	public static void beforePressurePlateCheckPressed(Level level, Block block, BlockPos blockPos){
		if(ServerLevelHelper.getServerLevel(level) != null) {
			DETECTING_ENTITY_BLOCK_COLLISION = block;
			DETECTING_ENTITY_BLOCK_COLLISION_POS = blockPos;
		}
	}

	public static void afterPressurePlateCheckPressed(Level level){
		if(ServerLevelHelper.getServerLevel(level) != null)
			DETECTING_ENTITY_BLOCK_COLLISION = null;
	}

	private static Projectile PROJECTILE_HIT = null;
	private static int PROJECTILE_HIT_TICK = -1;
	private static boolean PROJECTILE_HIT_UNUSABLE = false;

	private static boolean testProjectileHitCapture(int currentTickCount){
		if(PROJECTILE_HIT_TICK != -1 && PROJECTILE_HIT_TICK != currentTickCount){
			OpenPartiesAndClaims.LOGGER.error("Projectile hit capture isn't working properly! Likely a compatibility issue. Turning it off...");
			PROJECTILE_HIT_UNUSABLE = true;
			PROJECTILE_HIT = null;
			PROJECTILE_HIT_TICK = -1;
			return true;
		}
		return false;
	}
	public static void preProjectileHit(Projectile projectile) {
		if(PROJECTILE_HIT_UNUSABLE)
			return;
		MinecraftServer server = projectile.getServer();
		if(server == null || !server.isSameThread())
			return;
		int currentTickCount = server.getTickCount();
		if(testProjectileHitCapture(currentTickCount))
			return;
		if(PROJECTILE_HIT != null)
			return;
		PROJECTILE_HIT = projectile;
		PROJECTILE_HIT_TICK = currentTickCount;
	}

	public static void postProjectileHit(Projectile projectile) {
		MinecraftServer server = projectile.getServer();
		if(server == null || !server.isSameThread())
			return;
		if(PROJECTILE_HIT_UNUSABLE || PROJECTILE_HIT == null)
			return;
		if(projectile != PROJECTILE_HIT)
			return;
		PROJECTILE_HIT = null;
		PROJECTILE_HIT_TICK = -1;
	}

	public static Projectile getHitProjectile(int currentTickCount){
		testProjectileHitCapture(currentTickCount);
		return PROJECTILE_HIT;
	}

	public static void onLightningCauseSet(LightningBolt bolt){
		IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
				serverData = ServerData.from(bolt.getServer());
		if(serverData == null)
			return;
		serverData.getChunkProtection().onLightningBolt(serverData, bolt);
	}

	public static void reset(){
		CAPTURED_TARGET_POS = null;
		CAPTURED_POS_STATE_MAP = null;
		ENCHANTMENT_EFFECT_BLOCKSTATE = null;
		ENCHANTMENT_EFFECT_ENTITY = null;
		ENCHANTMENT_EFFECT_LEVEL = null;
		FINDING_RAID_SPAWN_POS = false;
		DYING_LIVING = null;
		DYING_LIVING_FROM = null;
		DROPPING_LOOT_LIVING = null;
		DROPPING_LOOT_LIVING_FROM = null;
		MOB_GRIEFING_IS_FOR_ITEMS = false;
		BEHAVIOR_UTILS_THROW_ITEM_LIVING = null;
		RESOURCES_DROP_OWNER = null;
		DETECTING_ENTITY_BLOCK_COLLISION = null;
		DETECTING_ENTITY_BLOCK_COLLISION_POS = null;
		PROJECTILE_HIT = null;
		PROJECTILE_HIT_TICK = -1;
	}
}
