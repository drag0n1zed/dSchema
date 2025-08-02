package io.github.drag0n1zed.dschema.networking.serializer;

import io.github.drag0n1zed.universal.api.networking.NetByteBuf;
import io.github.drag0n1zed.universal.api.networking.NetByteBufSerializer;
import io.github.drag0n1zed.dschema.building.structure.BuildFeature;
import io.github.drag0n1zed.dschema.building.structure.BuildFeatures;
import io.github.drag0n1zed.dschema.building.structure.BuildMode;
import io.github.drag0n1zed.dschema.building.structure.CircleStart;
import io.github.drag0n1zed.dschema.building.structure.CubeFilling;
import io.github.drag0n1zed.dschema.building.structure.CubeLength;
import io.github.drag0n1zed.dschema.building.structure.LineDirection;
import io.github.drag0n1zed.dschema.building.structure.PlaneFacing;
import io.github.drag0n1zed.dschema.building.structure.PlaneFilling;
import io.github.drag0n1zed.dschema.building.structure.PlaneLength;
import io.github.drag0n1zed.dschema.building.structure.RaisedEdge;
import io.github.drag0n1zed.dschema.building.structure.builder.Structure;
import io.github.drag0n1zed.dschema.building.structure.builder.standard.Circle;
import io.github.drag0n1zed.dschema.building.structure.builder.standard.Cone;
import io.github.drag0n1zed.dschema.building.structure.builder.standard.Cuboid;
import io.github.drag0n1zed.dschema.building.structure.builder.standard.Cylinder;
import io.github.drag0n1zed.dschema.building.structure.builder.standard.DiagonalLine;
import io.github.drag0n1zed.dschema.building.structure.builder.standard.DiagonalWall;
import io.github.drag0n1zed.dschema.building.structure.builder.standard.Disable;
import io.github.drag0n1zed.dschema.building.structure.builder.standard.Floor;
import io.github.drag0n1zed.dschema.building.structure.builder.standard.Line;
import io.github.drag0n1zed.dschema.building.structure.builder.standard.Pyramid;
import io.github.drag0n1zed.dschema.building.structure.builder.standard.Single;
import io.github.drag0n1zed.dschema.building.structure.builder.standard.SlopeFloor;
import io.github.drag0n1zed.dschema.building.structure.builder.standard.Sphere;
import io.github.drag0n1zed.dschema.building.structure.builder.standard.Wall;

public class StructureSerializer implements NetByteBufSerializer<Structure> {

    @Override
    public Structure read(NetByteBuf byteBuf) {
        return (switch (byteBuf.readEnum(BuildMode.class)) {
            case DISABLED -> new Disable();
            case SINGLE -> new Single();
            case LINE -> new Line();
            case WALL -> new Wall();
            case FLOOR -> new Floor();
            case CUBOID -> new Cuboid();
            case DIAGONAL_LINE -> new DiagonalLine();
            case DIAGONAL_WALL -> new DiagonalWall();
            case SLOPE_FLOOR -> new SlopeFloor();
            case CIRCLE -> new Circle();
            case CYLINDER -> new Cylinder();
            case SPHERE -> new Sphere();
            case PYRAMID -> new Pyramid();
            case CONE -> new Cone();
        }).withFeatures(byteBuf.readList(new BuildFeatureReader()));
    }

    @Override
    public void write(NetByteBuf byteBuf, Structure structure) {
        byteBuf.writeEnum(structure.getMode());
        byteBuf.writeList(structure.getFeatures(), new BuildFeatureReader());
    }

    public static class BuildFeatureReader implements NetByteBufSerializer<BuildFeature> {

        @Override
        public BuildFeature read(NetByteBuf byteBuf) {
            return switch (byteBuf.readEnum(BuildFeatures.class)) {
                case CIRCLE_START -> byteBuf.readEnum(CircleStart.class);
                case CUBE_FILLING -> byteBuf.readEnum(CubeFilling.class);
                case CUBE_LENGTH -> byteBuf.readEnum(CubeLength.class);
                case PLANE_FACING -> byteBuf.readEnum(PlaneFacing.class);
                case PLANE_FILLING -> byteBuf.readEnum(PlaneFilling.class);
                case PLANE_LENGTH -> byteBuf.readEnum(PlaneLength.class);
                case LINE_DIRECTION -> byteBuf.readEnum(LineDirection.class);
                case RAISED_EDGE -> byteBuf.readEnum(RaisedEdge.class);
            };
        }

        @Override
        public void write(NetByteBuf byteBuf, BuildFeature buildFeature) {
            byteBuf.writeEnum(buildFeature.getType());
            switch (buildFeature.getType()) {
                case CIRCLE_START -> byteBuf.writeEnum((CircleStart) buildFeature);
                case CUBE_FILLING -> byteBuf.writeEnum((CubeFilling) buildFeature);
                case CUBE_LENGTH -> byteBuf.writeEnum((CubeLength) buildFeature);
                case PLANE_FACING -> byteBuf.writeEnum((PlaneFacing) buildFeature);
                case PLANE_FILLING -> byteBuf.writeEnum((PlaneFilling) buildFeature);
                case PLANE_LENGTH -> byteBuf.writeEnum((PlaneLength) buildFeature);
                case LINE_DIRECTION -> byteBuf.writeEnum((LineDirection) buildFeature);
                case RAISED_EDGE -> byteBuf.writeEnum((RaisedEdge) buildFeature);
            }
        }
    }

}
