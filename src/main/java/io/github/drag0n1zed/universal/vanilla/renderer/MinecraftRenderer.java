package io.github.drag0n1zed.universal.vanilla.renderer;

import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.drag0n1zed.universal.api.core.BlockEntity;
import io.github.drag0n1zed.universal.api.core.BlockPosition;
import io.github.drag0n1zed.universal.api.core.BlockState;
import io.github.drag0n1zed.universal.api.core.ItemStack;
import io.github.drag0n1zed.universal.api.core.World;
import io.github.drag0n1zed.universal.api.gui.Typeface;
import io.github.drag0n1zed.universal.api.renderer.BufferSource;
import io.github.drag0n1zed.universal.api.renderer.MatrixStack;
import io.github.drag0n1zed.universal.api.renderer.RenderLayer;
import io.github.drag0n1zed.universal.api.renderer.Renderer;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftConvertor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public class MinecraftRenderer extends Renderer {

    private static final RandomSource RAND = RandomSource.create();
    private final Minecraft minecraftClient;
    private final PoseStack minecraftMatrixStack;
    private final MultiBufferSource.BufferSource minecraftBufferSource;
    private final GuiGraphics minecraftRendererProvider;

    public MinecraftRenderer(PoseStack minecraftMatrixStack) {
        this.minecraftClient = Minecraft.getInstance();
        this.minecraftMatrixStack = minecraftMatrixStack;
        this.minecraftBufferSource = minecraftClient.renderBuffers().bufferSource();
        this.minecraftRendererProvider = new GuiGraphics(Minecraft.getInstance(), this.minecraftMatrixStack, minecraftBufferSource);
    }

    @Override
    public MatrixStack matrixStack() {
        return new MinecraftMatrixStack(minecraftMatrixStack);
    }

    @Override
    protected void enableScissor(int x, int y, int width, int height) {
        RenderSystem.enableScissor(x, y, width, height);
    }

    @Override
    protected void disableScissor() {
        RenderSystem.disableScissor();
    }

    @Override
    public void setRsShaderColor(float red, float green, float blue, float alpha) {
//        minecraftRendererProvider.flushIfManaged();
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    @Override
    public BufferSource bufferSource() {
        return new MinecraftBufferSource(minecraftBufferSource);
    }

    @Override
    public void flush() {
        RenderSystem.disableDepthTest();
        minecraftBufferSource.endBatch();
        RenderSystem.enableDepthTest();
    }

    @Override
    protected int renderTextInternal(Typeface typeface, Text text, int x, int y, int color, int backgroundColor, boolean shadow, boolean seeThrough, int lightMap) {
        var minecraftTypeface = (Font) typeface.reference();
        var minecraftText = (Component) text.reference();
        return minecraftTypeface.drawInBatch(minecraftText,
                x,
                y,
                color,
                shadow,
                minecraftMatrixStack.last().pose(),
                minecraftBufferSource,
                seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL,
                backgroundColor,
                lightMap);
    }

    @Override
    public void renderItem(ItemStack stack, int x, int y) {
////        RenderSystem.getModelViewStack().pushPose();
////        RenderSystem.getModelViewStack().mulPoseMatrix(minecraftMatrixStack.last().pose());
////        RenderSystem.applyModelViewMatrix();
        minecraftRendererProvider.renderItem(stack.reference(), x, y);
////        RenderSystem.getModelViewStack().popPose();
////        RenderSystem.applyModelViewMatrix();
    }

    @Override
    public void renderTooltip(Typeface typeface, List<Text> list, int x, int y) {
        minecraftRendererProvider.renderTooltip(typeface.reference(), list.stream().map(text -> (Component) text.reference()).toList(), Optional.empty(), x, y);
    }

    @Override
    public void renderBlockState(RenderLayer renderLayer, World world, BlockPosition blockPosition, BlockState blockState) {
        var minecraftBlockRenderer = minecraftClient.getBlockRenderer();
        var minecraftWorld = (Level) world.reference();
        var minecraftRenderLayer = (RenderType) renderLayer.reference();
        var minecraftBlockState = (net.minecraft.world.level.block.state.BlockState) blockState.reference();
        var minecraftBlockPosition = MinecraftConvertor.toPlatformBlockPosition(blockPosition);

        minecraftBlockRenderer.getModelRenderer().tesselateBlock(
                minecraftWorld,
                minecraftBlockRenderer.getBlockModel(minecraftBlockState),
                minecraftBlockState,
                minecraftBlockPosition,
                minecraftMatrixStack,
                minecraftBufferSource.getBuffer(minecraftRenderLayer),
                false,
                RAND,
                minecraftBlockState.getSeed(minecraftBlockPosition),
                OverlayTexture.NO_OVERLAY);
    }

    @Override
    public void renderBlockEntity(RenderLayer renderLayer, World world, BlockPosition blockPosition, BlockEntity blockEntity) {
        var minecraftBlockEntityRenderDispatcher = minecraftClient.getBlockEntityRenderDispatcher();
        var minecraftBlockEntity = (net.minecraft.world.level.block.entity.BlockEntity) blockEntity.reference();

        minecraftBlockEntity.setLevel(world.reference());
        minecraftBlockEntityRenderDispatcher.render(minecraftBlockEntity, 0f, minecraftMatrixStack, minecraftBufferSource);


    }


}
