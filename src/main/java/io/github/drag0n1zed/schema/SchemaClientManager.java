package io.github.drag0n1zed.schema;

import java.util.Stack;

import io.github.drag0n1zed.universal.api.core.Direction;
import io.github.drag0n1zed.universal.api.core.Interaction;
import io.github.drag0n1zed.universal.api.core.InteractionHand;
import io.github.drag0n1zed.universal.api.core.InteractionType;
import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.events.EventResult;
import io.github.drag0n1zed.universal.api.events.input.KeyRegistry;
import io.github.drag0n1zed.universal.api.events.lifecycle.ClientTick;
import io.github.drag0n1zed.universal.api.events.render.RegisterShader;
import io.github.drag0n1zed.universal.api.gui.Screen;
import io.github.drag0n1zed.universal.api.input.InputKey;
import io.github.drag0n1zed.universal.api.input.Keys;
import io.github.drag0n1zed.universal.api.input.OptionKeys;
import io.github.drag0n1zed.universal.api.platform.Client;
import io.github.drag0n1zed.universal.api.platform.ClientManager;
import io.github.drag0n1zed.universal.api.platform.Platform;
import io.github.drag0n1zed.universal.api.renderer.Renderer;
import io.github.drag0n1zed.universal.api.renderer.Shaders;
import io.github.drag0n1zed.universal.api.text.ChatFormatting;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.schema.building.clipboard.SnapshotTransform;
import io.github.drag0n1zed.schema.renderer.BlockShaders;
import io.github.drag0n1zed.schema.renderer.opertaion.OperationsRenderer;
import io.github.drag0n1zed.schema.renderer.outliner.OutlineRenderer;
import io.github.drag0n1zed.schema.renderer.pattern.PatternRenderer;
import io.github.drag0n1zed.schema.renderer.tooltip.TooltipRenderer;
import io.github.drag0n1zed.schema.screen.clipboard.EffortlessClipboardScreen;
import io.github.drag0n1zed.schema.screen.pattern.EffortlessPatternScreen;
import io.github.drag0n1zed.schema.screen.settings.EffortlessSettingsScreen;
import io.github.drag0n1zed.schema.screen.structure.EffortlessStructureScreen;
import io.github.drag0n1zed.schema.screen.test.EffortlessTestScreen;

public final class SchemaClientManager implements ClientManager {

    private final Stack<Screen> screenStack = new Stack<>();

    private final SchemaClient entrance;
    private final TooltipRenderer tooltipRenderer;

    private final OperationsRenderer operationsRenderer;
    private final OutlineRenderer outlineRenderer;
    private final PatternRenderer patternRenderer;

    private Client client;

    private int interactionCooldown = 0;

    public SchemaClientManager(SchemaClient entrance) {
        this.entrance = entrance;
        this.tooltipRenderer = new TooltipRenderer(entrance);

        this.operationsRenderer = new OperationsRenderer(entrance);
        this.outlineRenderer = new OutlineRenderer();
        this.patternRenderer = new PatternRenderer(entrance);

        getEntrance().getEventRegistry().getRegisterKeysEvent().register(this::onRegisterKeys);
        getEntrance().getEventRegistry().getKeyInputEvent().register(this::onKeyInput);
        getEntrance().getEventRegistry().getInteractionInputEvent().register(this::onInteractionInput);

        getEntrance().getEventRegistry().getClientStartEvent().register(this::onClientStart);
        getEntrance().getEventRegistry().getClientTickEvent().register(this::onClientTick);

        getEntrance().getEventRegistry().getRenderGuiEvent().register(this::onRenderGui);
        getEntrance().getEventRegistry().getRenderWorldEvent().register(this::onRenderEnd);

        getEntrance().getEventRegistry().getRegisterShaderEvent().register(this::onRegisterShader);
    }

    private SchemaClient getEntrance() {
        return entrance;
    }

    private Player getPlayer() {
        return getEntrance().getClient().getPlayer();
    }

    @Override
    public Client getRunningClient() {
        return client;
    }

    @Override
    public void setRunningClient(Client client) {
        this.client = client;
    }

    public OperationsRenderer getOperationsRenderer() {
        return operationsRenderer;
    }

    public OutlineRenderer getOutlineRenderer() {
        return outlineRenderer;
    }

    public PatternRenderer getPatternRenderer() {
        return patternRenderer;
    }

    @Override
    public void pushScreen(Screen screen) {
        if (screen == null) {
            screenStack.clear();
        } else {
            screenStack.push(getRunningClient().getPanel());
        }
        getRunningClient().setPanel(screen);
    }

    @Override
    public void popScreen(Screen screen) {
        if (getRunningClient().getPanel() != screen) {
            return;
        }
        if (screenStack.isEmpty()) {
            getRunningClient().setPanel(null);
        } else {
            getRunningClient().setPanel(screenStack.pop());
        }
    }

    public TooltipRenderer getTooltipRenderer() {
        return tooltipRenderer;
    }

    private void tickCooldown() {
        if (OptionKeys.KEY_ATTACK.getKeyBinding().isDown() || OptionKeys.KEY_USE.getKeyBinding().isDown() || OptionKeys.KEY_PICK_ITEM.getKeyBinding().isDown()) {
            return;
        }
        this.interactionCooldown = Math.max(0, this.interactionCooldown - 1);
    }

    private boolean isInteractionCooldown() {
        return this.interactionCooldown == 0;
    }

    private void setInteractionCooldown(int tick) {
        this.interactionCooldown = tick; // for single build speed
    }

    private void resetInteractionCooldown() {
        setInteractionCooldown(1);
    }


    public void onRegisterKeys(KeyRegistry keyRegistry) {
        for (var key : SchemaKeys.values()) {
            keyRegistry.register(key);
        }
    }

    public void onKeyInput(InputKey key) {

        if (getRunningClient() == null) {
            return;
        }

        if (getRunningClient().getPlayer() == null) {
            return;
        }

        if (Keys.KEY_ESCAPE.isDown()) {
            getEntrance().getStructureBuilder().resetInteractions(getRunningClient().getPlayer());
        }

        if (SchemaKeys.BUILD_MODE_RADIAL.getKeyBinding().isDown()) {
            if (!(getRunningClient().getPanel() instanceof EffortlessStructureScreen)) {
                new EffortlessStructureScreen(getEntrance(), SchemaKeys.BUILD_MODE_RADIAL.getKeyBinding()).attach();
            }
        }
        if (SchemaKeys.UNDO.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            getEntrance().getStructureBuilder().undo(getRunningClient().getPlayer());
        }
        if (SchemaKeys.REDO.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            getEntrance().getStructureBuilder().redo(getRunningClient().getPlayer());
        }
        if (SchemaKeys.SETTINGS.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            new EffortlessSettingsScreen(getEntrance()).attach();
        }
        if (SchemaKeys.TOGGLE_CLIPBOARD.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            getEntrance().getStructureBuilder().setClipboard(getRunningClient().getPlayer(), getEntrance().getStructureBuilder().getContext(getRunningClient().getPlayer()).clipboard().toggled());
            getEntrance().getClient().getPlayer().sendMessage(Schema.getSystemMessage(Text.translate("effortless.message.building.server.toggle_clipboard", getEntrance().getStructureBuilder().getContext(getRunningClient().getPlayer()).clipboard().getNameText().withStyle(ChatFormatting.GOLD))));
        }
        if (SchemaKeys.TOGGLE_PATTERN.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            getEntrance().getStructureBuilder().setPattern(getRunningClient().getPlayer(), getEntrance().getStructureBuilder().getContext(getRunningClient().getPlayer()).pattern().toggled());
            getEntrance().getClient().getPlayer().sendMessage(Schema.getSystemMessage(Text.translate("effortless.message.building.server.toggle_pattern", getEntrance().getStructureBuilder().getContext(getRunningClient().getPlayer()).pattern().getNameText().withStyle(ChatFormatting.GOLD))));
        }
        if (SchemaKeys.TOGGLE_REPLACE.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            getEntrance().getStructureBuilder().setReplace(getRunningClient().getPlayer(), getEntrance().getStructureBuilder().getContext(getRunningClient().getPlayer()).replace().next());
            getEntrance().getClient().getPlayer().sendMessage(Schema.getSystemMessage(Text.translate("effortless.message.building.server.toggle_replace", getEntrance().getStructureBuilder().getContext(getRunningClient().getPlayer()).replace().getNameText().withStyle(ChatFormatting.GOLD))));
        }

        if (SchemaKeys.ROTATE_X.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.ROTATE_X);
        }

        if (SchemaKeys.ROTATE_Y.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.ROTATE_Y);
        }

        if (SchemaKeys.ROTATE_Z.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.ROTATE_Z);
        }

        if (SchemaKeys.MOVE_BACKWARD.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            switch (Direction.fromYRot(getPlayer().getYRot())) {
                case NORTH ->
                        getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.INCREASE_Z);
                case SOUTH ->
                        getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.DECREASE_Z);
                case WEST ->
                        getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.INCREASE_X);
                case EAST ->
                        getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.DECREASE_X);
            }
        }

        if (SchemaKeys.MOVE_FORWARD.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            switch (Direction.fromYRot(getPlayer().getYRot())) {
                case NORTH ->
                        getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.DECREASE_Z);
                case SOUTH ->
                        getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.INCREASE_Z);
                case WEST ->
                        getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.DECREASE_X);
                case EAST ->
                        getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.INCREASE_X);
            }
        }

        if (SchemaKeys.MOVE_LEFT.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            switch (Direction.fromYRot(getPlayer().getYRot())) {
                case NORTH ->
                        getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.DECREASE_X);
                case SOUTH ->
                        getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.INCREASE_X);
                case WEST ->
                        getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.INCREASE_Z);
                case EAST ->
                        getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.DECREASE_Z);
            }
        }

        if (SchemaKeys.MOVE_RIGHT.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            switch (Direction.fromYRot(getPlayer().getYRot())) {
                case NORTH ->
                        getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.INCREASE_X);
                case SOUTH ->
                        getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.DECREASE_X);
                case WEST ->
                        getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.DECREASE_Z);
                case EAST ->
                        getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.INCREASE_Z);
            }
        }

        if (SchemaKeys.MOVE_UP.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.INCREASE_Y);
        }

        if (SchemaKeys.MOVE_DOWN.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.DECREASE_Y);
        }

        if (SchemaKeys.MIRROR_X.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.MIRROR_X);
        }

        if (SchemaKeys.MIRROR_Y.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.MIRROR_Y);
        }

        if (SchemaKeys.MIRROR_Z.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            getEntrance().getStructureBuilder().updateClipboard(getPlayer(), SnapshotTransform.MIRROR_Z);
        }

        if (SchemaKeys.EDIT_CLIPBOARD.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            new EffortlessClipboardScreen(getEntrance()).attach();
        }

        if (SchemaKeys.EDIT_PATTERN.getKeyBinding().consumeClick()) {
            getEntrance().getClient().getSoundManager().playButtonClickSound();
            new EffortlessPatternScreen(getEntrance()).attach();
        }

//        if (EffortlessKeys.EDIT_REPLACE.getKeyBinding().consumeClick()) {
//            getEntrance().getClient().getSoundManager().playButtonClickSound();
//        }

        if (Platform.getInstance().isDevelopment()) {
            if (Keys.KEY_LEFT_CONTROL.isDown() && Keys.KEY_ENTER.isDown()) {
                getEntrance().getClient().getSoundManager().playButtonClickSound();
                new EffortlessTestScreen(getEntrance()).attach();
            }
        }
    }

    public EventResult onInteractionInput(InteractionType type, InteractionHand hand) {

        if (getEntrance().getStructureBuilder().getContext(getRunningClient().getPlayer()).isDisabled()) {
            return EventResult.pass();
        }

        if (!isInteractionCooldown()) {
            return EventResult.interruptFalse();
        } else {
            resetInteractionCooldown();
        }

        var interaction = getEntrance().getClient().getLastInteraction();
        if (interaction != null && interaction.getTarget() == Interaction.Target.ENTITY) {
            return EventResult.interruptFalse();
        }

        return switch (type) {
            case ATTACK, USE_ITEM -> {
                yield getEntrance().getStructureBuilder().onPlayerInteract(getRunningClient().getPlayer(), type, hand);
            }
            case UNKNOWN -> EventResult.pass();
        };

    }

    public synchronized void onClientStart(Client client) {
        setRunningClient(client);
    }

    public void onClientTick(Client client, ClientTick.Phase phase) {
        switch (phase) {
            case START -> {
                tickCooldown();

                tooltipRenderer.tick();

                operationsRenderer.tick();
                outlineRenderer.tick();
                patternRenderer.tick();
            }
            case END -> {
            }
        }
    }

    public void onRenderGui(Renderer renderer, float deltaTick) {
        if (getRunningClient().getPanel() != null && !(getRunningClient().getPanel() instanceof EffortlessStructureScreen)) {
            return;
        }

        getTooltipRenderer().renderGuiOverlay(renderer, deltaTick);
    }

    public void onRenderEnd(Renderer renderer, float deltaTick) {
        patternRenderer.render(renderer, deltaTick);
        outlineRenderer.render(renderer, deltaTick);
        operationsRenderer.render(renderer, deltaTick);
    }

    public void onRegisterShader(RegisterShader.ShadersSink sink) {
        BlockShaders.TINTED_OUTLINE.register(sink);
        for (var value : Shaders.values()) {
            value.register(sink);
        }
    }
}
