package io.github.drag0n1zed.dschema.renderer.pattern;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import io.github.drag0n1zed.dschema.SchemaClient;
import io.github.drag0n1zed.universal.api.platform.ClientEntrance;
import io.github.drag0n1zed.universal.api.renderer.RenderFadeEntry;
import io.github.drag0n1zed.universal.api.renderer.Renderer;
import io.github.drag0n1zed.dschema.building.Context;
import io.github.drag0n1zed.dschema.building.pattern.array.ArrayTransformer;
import io.github.drag0n1zed.dschema.building.pattern.mirror.MirrorTransformer;
import io.github.drag0n1zed.dschema.building.pattern.raidal.RadialTransformer;

public class PatternRenderer {

    private final Map<UUID, RenderFadeEntry<Context>> entries = Collections.synchronizedMap(new LinkedHashMap<>());
    private final ClientEntrance entrance;

    public PatternRenderer(ClientEntrance entrance) {
        this.entrance = entrance;
    }

    public SchemaClient getEntrance() {
        return (SchemaClient) entrance;
    }

    public void tick() {
        var iterator = entries.values().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            entry.tick();
            if (!entry.isAlive()) {
                iterator.remove();
            }
        }
    }

    public void showPattern(UUID uuid, Context context) {
        entries.put(uuid, new RenderFadeEntry<>(context));
    }

    public void render(Renderer renderer, float deltaTick) {
        entries.forEach((k, v) -> {
            if (!v.getValue().pattern().enabled()) return;
            for (var transformer : v.getValue().pattern().transformers()) {
                if (transformer instanceof MirrorTransformer mirrorTransformer) {
                    new MirrorTransformerRenderer(getEntrance(), mirrorTransformer, true).render(renderer, deltaTick);
                }
                if (transformer instanceof ArrayTransformer arrayTransformer) {
                    new ArrayTransformerRenderer(getEntrance(), arrayTransformer).render(renderer, deltaTick);
                }
                if (transformer instanceof RadialTransformer radialTransformer) {
                    new RadialTransformerRenderer(getEntrance(), radialTransformer, true).render(renderer, deltaTick);
                }
            }
        });
    }


}

