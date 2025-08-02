package io.github.drag0n1zed.schema.renderer.pattern;

import java.awt.*;

import io.github.drag0n1zed.universal.api.core.Axis;
import io.github.drag0n1zed.universal.api.platform.ClientEntrance;
import io.github.drag0n1zed.universal.api.renderer.Renderer;
import io.github.drag0n1zed.schema.building.pattern.mirror.MirrorTransformer;

public class MirrorTransformerRenderer extends TransformerRenderer {

    private final MirrorTransformer transformer;
    private final boolean renderPlanes;

    public MirrorTransformerRenderer(ClientEntrance entrance, MirrorTransformer transformer, boolean renderPlanes) {
        super(entrance);
        this.transformer = transformer;
        this.renderPlanes = renderPlanes;
    }

    @Override
    public void render(Renderer renderer, float deltaTick) {

        renderPlaneByAxis(renderer, transformer.position(), transformer.size(), transformer.axis(), new Color(0, 0, 0, 72));
        for (var value : Axis.values()) {
            if (value != transformer.axis()) {
                renderLineByAxis(renderer, transformer.position(), transformer.size(), value);
            }
        }
        renderBoundingBox(renderer, transformer.getPositionBoundingBox(), new Color(0, 0, 0, 140).getRGB());
    }

}

