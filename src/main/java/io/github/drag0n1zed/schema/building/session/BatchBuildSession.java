package io.github.drag0n1zed.schema.building.session;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.drag0n1zed.universal.api.core.BlockInteraction;
import io.github.drag0n1zed.universal.api.core.BlockState;
import io.github.drag0n1zed.universal.api.core.InteractionHand;
import io.github.drag0n1zed.universal.api.core.Items;
import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.core.World;
import io.github.drag0n1zed.universal.api.platform.Entrance;
import io.github.drag0n1zed.universal.api.tag.RecordTag;
import io.github.drag0n1zed.schema.building.BuildState;
import io.github.drag0n1zed.schema.building.BuildType;
import io.github.drag0n1zed.schema.building.Context;
import io.github.drag0n1zed.schema.building.Storage;
import io.github.drag0n1zed.schema.building.clipboard.Snapshot;
import io.github.drag0n1zed.schema.building.interceptor.BuildInterceptor;
import io.github.drag0n1zed.schema.building.interceptor.OpenPacInterceptor;
import io.github.drag0n1zed.schema.building.operation.OperationFilter;
import io.github.drag0n1zed.schema.building.operation.batch.BatchOperation;
import io.github.drag0n1zed.schema.building.operation.batch.BatchOperationResult;
import io.github.drag0n1zed.schema.building.operation.batch.DeferredBatchOperation;
import io.github.drag0n1zed.schema.building.operation.block.BlockInteractOperation;
import io.github.drag0n1zed.schema.building.operation.block.BlockOperation;
import io.github.drag0n1zed.schema.building.operation.block.BlockStateCopyOperation;
import io.github.drag0n1zed.schema.building.operation.block.BlockStateCopyOperationResult;
import io.github.drag0n1zed.schema.building.operation.block.BlockStateUpdateOperation;
import io.github.drag0n1zed.schema.building.pattern.randomize.ItemRandomizer;
import io.github.drag0n1zed.schema.networking.packets.player.PlayerSnapshotCapturePacket;

public class BatchBuildSession implements Session {

    private final Entrance entrance;
    private final Player player;
    private final World world;
    private final Context context;
    private final List<BuildInterceptor> interceptors;

    private BatchOperationResult lastResult;

    public BatchBuildSession(Entrance entrance, Player player, Context context) {
        this.entrance = entrance;
        this.world = player.getWorld();
        this.player = player;
        this.context = context;
        this.interceptors = createInterceptors(entrance, player.getWorld(), player, context);
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    private static List<BuildInterceptor> createInterceptors(Entrance entrance, World world, Player player, Context context) {
        return Stream.of(
                new OpenPacInterceptor(entrance)
        ).filter(BuildInterceptor::isEnabled).collect(Collectors.toUnmodifiableList());
    }

    public Entrance getEntrance() {
        return entrance;
    }

    protected BlockOperation createBlockPlaceOperationFromInteraction(Player player, World world, Context context, Storage storage, BlockInteraction interaction, BlockState blockState, RecordTag entityTag) {
        return new BlockStateUpdateOperation(this, context, storage, interaction, blockState, entityTag, context.extras().extras());
    }

    protected BlockOperation createBlockBreakOperationFromInteraction(Player player, World world, Context context, Storage storage, BlockInteraction interaction) {
        return new BlockStateUpdateOperation(this, context, storage, interaction, Items.AIR.item().getBlock().getDefaultBlockState(), null, context.extras().extras());
    }

    protected BlockOperation createBlockInteractOperationFromInteraction(Player player, World world, Context context, Storage storage, BlockInteraction interaction) {
        return new BlockInteractOperation(this, context, storage, interaction, context.extras().extras());
    }

    protected BlockOperation createBlockCopyOperationFromInteraction(Player player, World world, Context context, Storage storage, BlockInteraction interaction) {
        return new BlockStateCopyOperation(this, context, storage, interaction, context.extras().extras());
    }

    protected BatchOperation create(World world, Player player, Context context) {
        var storage = Storage.create(player, context.isPreviewType() || context.isBuildClientType()); // TODO: 21/5/24 use storage from context
        var inHandTransformer = ItemRandomizer.single(null, player.getItemStack(InteractionHand.MAIN).getItem());
        var operations = (BatchOperation) new DeferredBatchOperation(context, () -> switch (context.buildState()) {
            case IDLE -> Stream.<BlockOperation>empty();
            case BREAK_BLOCK ->
                    context.collectInteractions().map(interaction -> createBlockBreakOperationFromInteraction(player, world, context, storage, interaction));
            case PLACE_BLOCK ->
                    context.collectInteractions().map(interaction -> createBlockPlaceOperationFromInteraction(player, world, context, storage, interaction, Items.AIR.item().getBlock().getDefaultBlockState(), null));
            case INTERACT_BLOCK ->
                    context.collectInteractions().map(interaction -> createBlockInteractOperationFromInteraction(player, world, context, storage, interaction));
            case COPY_STRUCTURE ->
                    context.collectInteractions().map(interaction -> createBlockCopyOperationFromInteraction(player, world, context, storage, interaction));
            case PASTE_STRUCTURE -> {
                yield context.clipboard().snapshot().blockData().stream().map(blockSnapshot -> {
                    var interaction = context.getInteraction(0).withBlockPosition(context.getInteraction(0).blockPosition().add(blockSnapshot.blockPosition()));
                    return createBlockPlaceOperationFromInteraction(player, world, context, storage, interaction, blockSnapshot.blockState(), blockSnapshot.entityTag());
                });
            }
        });
        if (context.buildState() == BuildState.PLACE_BLOCK) {
            operations = (BatchOperation) inHandTransformer.transform(operations);
        }
        if (context.pattern().enabled()) {
            for (var transformer : context.pattern().transformers()) {
                if (transformer.isValid()) {
                    operations = (BatchOperation) transformer.transform(operations);
                }
            }
        }
        operations = operations.flatten().filter(Objects::nonNull).filter(OperationFilter.distinctBlockOperations());

        return operations;
    }

    public List<BuildInterceptor> getInterceptors() {
        return interceptors;
    }

    @Override
    public synchronized BatchOperationResult commit() {
        if (lastResult == null) {
            lastResult = create(world, player, context).commit();
            saveClipboard();
        }
        return lastResult;
    }

    protected void saveClipboard() {
        if (world.isClient()) {
            return;
        }
        if (context.buildState() != BuildState.COPY_STRUCTURE) {
            return;
        }
        if (context.buildType() != BuildType.BUILD) {
            return;
        }
        if (!context.clipboard().enabled()) {
            return;
        }
        var snapshot = new Snapshot("", System.currentTimeMillis(), lastResult.getResults().stream().map(BlockStateCopyOperationResult.class::cast).map(BlockStateCopyOperationResult::getBlockData).filter(blockData -> context.clipboard().copyAir() || (blockData.blockState() != null && !blockData.blockState().isAir())).toList());
        getEntrance().getChannel().sendPacket(new PlayerSnapshotCapturePacket(player.getId(), snapshot), player);
    }

}
