package io.github.drag0n1zed.schema.building.pattern.raidal;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.github.drag0n1zed.universal.api.core.Axis;
import io.github.drag0n1zed.universal.api.math.MathUtils;
import io.github.drag0n1zed.universal.api.math.Range1i;
import io.github.drag0n1zed.universal.api.math.Vector3d;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.schema.building.operation.Operation;
import io.github.drag0n1zed.schema.building.operation.batch.DeferredBatchOperation;
import io.github.drag0n1zed.schema.building.pattern.RotateContext;
import io.github.drag0n1zed.schema.building.pattern.Transformer;
import io.github.drag0n1zed.schema.building.pattern.Transformers;

public record RadialTransformer(UUID id, Text name, Vector3d position, Axis axis, int slices, int radius, int length) implements Transformer {

    public static final int DEFAULT_SLICE = 4;
    public static final int DEFAULT_RADIUS = 16;
    public static final int DEFAULT_LENGTH = 128;
    public static final Range1i SLICE_RANGE = new Range1i(2, 360 * 8);
    public static final Range1i RADIUS_RANGE = new Range1i(1, 1024);
    public static final Range1i LENGTH_RANGE = new Range1i(1, 1024);

    public static final RadialTransformer ZERO = new RadialTransformer(Vector3d.ZERO, Axis.Y, 0, DEFAULT_RADIUS, DEFAULT_LENGTH);
    public static final RadialTransformer DEFAULT = new RadialTransformer(Vector3d.ZERO, Axis.Y, DEFAULT_SLICE, DEFAULT_RADIUS, DEFAULT_LENGTH);

    //    private final double start;
//    private final Axis axis;
//    private final int count;
//    private final double step;
//    private final boolean clockwise;
//    private final boolean alternate;
//    private final boolean drawLines;
//    private final boolean drawPlanes;

    public RadialTransformer(Vector3d position, Axis axis, int slice, int radius, int length) {
        this(UUID.randomUUID(), Text.empty(), position, axis, slice, radius, length);
    }


    @Override
    public Operation transform(Operation operation) {
        return new DeferredBatchOperation(operation.getContext(), () -> IntStream.range(1, slices + 1).mapToObj(i -> {
            var angle = 2 * MathUtils.PI / slices * (i % slices);
            return operation.rotate(new RotateContext(axis, position, angle, radius, length));
        }));
    }

    @Override
    public Text getName() {
        if (!name().getString().isEmpty()) {
            return name();
        }
        return Text.translate("effortless.transformer.radial.no_name");
    }

    @Override
    public Transformers getType() {
        return Transformers.RADIAL;
    }

    public RadialTransformer withPosition(Vector3d position) {
        return new RadialTransformer(id, name, position, axis, slices, radius, length);
    }

    public RadialTransformer withPositionX(double x) {
        return new RadialTransformer(id, name, new Vector3d(x, position.y(), position.z()), axis, slices, radius, length);
    }

    public RadialTransformer withPositionY(double y) {
        return new RadialTransformer(id, name, new Vector3d(position.x(), y, position.z()), axis, slices, radius, length);
    }

    public RadialTransformer withPositionZ(double z) {
        return new RadialTransformer(id, name, new Vector3d(position.x(), position.y(), z), axis, slices, radius, length);
    }

    public RadialTransformer withAxis(Axis axis) {
        return new RadialTransformer(id, name, position, axis, slices, radius, length);
    }

    public RadialTransformer withSlice(int slice) {
        return new RadialTransformer(id, name, position, axis, slice, radius, length);
    }

    public RadialTransformer withRadius(int radius) {
        return new RadialTransformer(id, name, position, axis, slices, radius, length);
    }

    public RadialTransformer withLength(int length) {
        return new RadialTransformer(id, name, position, axis, slices, radius, length);
    }

    @Override
    public Stream<Text> getSearchableTags() {
        return Stream.of(getName(), Text.text(position.toString()), Text.text(String.valueOf(slices)));
    }

    @Override
    public boolean isValid() {
        return POSITION_BOUND.containsIn(position) && SLICE_RANGE.contains(slices) && RADIUS_RANGE.contains(radius);
    }

    @Override
    public RadialTransformer withName(Text name) {
        return new RadialTransformer(id, name, position, axis, slices, radius, length);
    }

    @Override
    public RadialTransformer withId(UUID id) {
        return new RadialTransformer(id, name, position, axis, slices, radius, length);
    }

    @Override
    public float volumeMultiplier() {
        return slices;
    }

    @Override
    public List<Text> getDescriptions() {
        return List.of(Text.text("Position " + position.x() + " " + position.y() + " " + position.z()), Text.text("Slices " + slices), Text.text("Radius " + radius), Text.text("Length " + length));
    }
}
