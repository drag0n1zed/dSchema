package io.github.drag0n1zed.dschema.renderer.pattern;

import io.github.drag0n1zed.dschema.SchemaClient;
import io.github.drag0n1zed.universal.api.platform.ClientEntrance;
import io.github.drag0n1zed.universal.api.renderer.LightTexture;
import io.github.drag0n1zed.universal.api.renderer.Renderer;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.dschema.building.pattern.array.ArrayTransformer;

public class ArrayTransformerRenderer extends TransformerRenderer {

    private final ArrayTransformer transformer;

    public ArrayTransformerRenderer(ClientEntrance entrance, ArrayTransformer transformer) {
        super(entrance);
        this.transformer = transformer;
    }

    @Override
    public void render(Renderer renderer, float deltaTick) {

        var context = getEntrance().getStructureBuilder().getContextTraced(
                SchemaClient.getInstance().getClientManager().getRunningClient().getPlayer()
        );

        if (context.interactionsSize() == 0) {
            return;
        }
        var typeface = SchemaClient.getInstance().getClient().getTypeface();
        for (var result : context.interactions().results()) {
            if (result == null) {
                break; // global check
            }

            var interactionPosition = result.getBlockPosition().getCenter();

            for (var i = 0; i < transformer.copyCount(); i++) {

                var v1 = interactionPosition.add(transformer.offset().mul(i).toVector3d());
                var v2 = interactionPosition.add(transformer.offset().mul(i + 1).toVector3d());
                renderAACuboidLine(renderer, v1, v2, 1 / 32f, 0xFFFFFF, true);
                var cam = renderer.getCamera().position();
                renderer.pushPose();
                var mid = v1.add(v2).div(2);
                renderer.translate(mid.sub(cam));
                renderer.pushPose();

                renderer.rotate(renderer.getCamera().rotation());
                renderer.scale(-0.025F, -0.025F, 0.025F);
                var text = Text.text(transformer.offset().toString());
                renderer.renderText(typeface, text, -typeface.measureWidth(text) / 2, 0, 0xFFFFFFFF, 0, false, false, LightTexture.FULL_BRIGHT);
                renderer.popPose();
                renderer.popPose();
            }

        }

    }


}

