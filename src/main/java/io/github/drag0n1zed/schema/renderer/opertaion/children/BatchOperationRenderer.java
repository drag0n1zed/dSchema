package io.github.drag0n1zed.schema.renderer.opertaion.children;

import java.util.List;

import io.github.drag0n1zed.universal.api.renderer.Renderer;
import io.github.drag0n1zed.schema.building.operation.batch.BatchOperationResult;
import io.github.drag0n1zed.schema.renderer.opertaion.OperationsRenderer;

public final class BatchOperationRenderer implements OperationRenderer {

    private final List<OperationRenderer> previews;

    public BatchOperationRenderer(OperationsRenderer operationsRenderer, BatchOperationResult result) {
        this.previews = result.getResults().stream().map(operationsRenderer::createRenderer).toList();
    }

    @Override
    public void render(Renderer renderer, RenderContext renderContext, float deltaTick) {
        for (var preview : previews) {
            preview.render(renderer, renderContext, deltaTick);
        }
    }

}
