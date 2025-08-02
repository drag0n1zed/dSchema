package io.github.drag0n1zed.dschema.building.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.drag0n1zed.dschema.building.pattern.Transformer;
import io.github.drag0n1zed.dschema.building.pattern.Transformers;
import io.github.drag0n1zed.dschema.building.pattern.array.ArrayTransformer;
import io.github.drag0n1zed.dschema.building.pattern.mirror.MirrorTransformer;
import io.github.drag0n1zed.dschema.building.pattern.raidal.RadialTransformer;
import io.github.drag0n1zed.dschema.building.pattern.randomize.ItemRandomizer;

public record PatternConfig(
        List<? extends Transformer> transformerPreset
) {

    public static PatternConfig DEFAULT = new PatternConfig();

    public PatternConfig() {
        this(List.of());
    }


    public PatternConfig(
            List<ArrayTransformer> arrayTransformers,
            List<MirrorTransformer> mirrorTransformers,
            List<RadialTransformer> radialTransformers,
            List<ItemRandomizer> itemRandomizers
    ) {
        this(Stream.of(arrayTransformers, mirrorTransformers, radialTransformers, itemRandomizers).flatMap(List::stream).collect(Collectors.toList()));
    }

    public List<ArrayTransformer> arrayTransformers() {
        return transformerPreset().stream().filter(ArrayTransformer.class::isInstance).map(ArrayTransformer.class::cast).collect(Collectors.toList());
    }

    public List<MirrorTransformer> mirrorTransformers() {
        return transformerPreset().stream().filter(MirrorTransformer.class::isInstance).map(MirrorTransformer.class::cast).collect(Collectors.toList());
    }

    public List<RadialTransformer> radialTransformers() {
        return transformerPreset().stream().filter(RadialTransformer.class::isInstance).map(RadialTransformer.class::cast).collect(Collectors.toList());
    }

    public List<ItemRandomizer> itemRandomizers() {
        return transformerPreset().stream().filter(ItemRandomizer.class::isInstance).map(ItemRandomizer.class::cast).collect(Collectors.toList());
    }

    public List<? extends Transformer> getByType(Transformers type) {
        return transformerPreset().stream().filter(t -> t.getType() == type).toList();
    }

    public Map<Transformers, List<? extends Transformer>> getByType() {
        return Arrays.stream(Transformers.values()).collect(Collectors.toMap(Function.identity(), this::getByType));
    }

    public static PatternConfig getBuiltInPresets() {
        return new PatternConfig(
            Transformer.getDefaultTransformers()
        );
    }
}
