package io.github.drag0n1zed.schema.building.structure;

import java.awt.*;

import io.github.drag0n1zed.schema.Schema;
import io.github.drag0n1zed.universal.api.core.ResourceLocation;
import io.github.drag0n1zed.universal.api.text.ChatFormatting;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.schema.building.structure.builder.Structure;
import io.github.drag0n1zed.schema.building.structure.builder.standard.Circle;
import io.github.drag0n1zed.schema.building.structure.builder.standard.Cone;
import io.github.drag0n1zed.schema.building.structure.builder.standard.Cuboid;
import io.github.drag0n1zed.schema.building.structure.builder.standard.Cylinder;
import io.github.drag0n1zed.schema.building.structure.builder.standard.DiagonalLine;
import io.github.drag0n1zed.schema.building.structure.builder.standard.DiagonalWall;
import io.github.drag0n1zed.schema.building.structure.builder.standard.Disable;
import io.github.drag0n1zed.schema.building.structure.builder.standard.Floor;
import io.github.drag0n1zed.schema.building.structure.builder.standard.Line;
import io.github.drag0n1zed.schema.building.structure.builder.standard.Pyramid;
import io.github.drag0n1zed.schema.building.structure.builder.standard.Single;
import io.github.drag0n1zed.schema.building.structure.builder.standard.SlopeFloor;
import io.github.drag0n1zed.schema.building.structure.builder.standard.Sphere;
import io.github.drag0n1zed.schema.building.structure.builder.standard.Wall;

public enum BuildMode {
    DISABLED("disabled", new Disable(), Category.BASIC),
    SINGLE("single", new Single(), Category.BASIC /*, BuildOption.BUILD_SPEED*/),

    LINE("line", new Line(), Category.SQUARE, BuildFeatures.LINE_DIRECTION /*, OptionEnum.THICKNESS*/),
//    SQUARE("square", new Square(), Category.SQUARE, false, BuildFeatures.PLANE_FILLING, BuildFeatures.PLANE_FACING),
    WALL("wall", new Wall(), Category.SQUARE, BuildFeatures.PLANE_FILLING, BuildFeatures.PLANE_LENGTH),
    FLOOR("floor", new Floor(), Category.SQUARE, BuildFeatures.PLANE_FILLING, BuildFeatures.PLANE_LENGTH),
    CUBOID("cuboid", new Cuboid(), Category.SQUARE, BuildFeatures.CUBE_FILLING, BuildFeatures.PLANE_FACING, BuildFeatures.PLANE_LENGTH),

    DIAGONAL_LINE("diagonal_line", new DiagonalLine(), Category.DIAGONAL),
    DIAGONAL_WALL("diagonal_wall", new DiagonalWall(), Category.DIAGONAL, BuildFeatures.PLANE_LENGTH),
    SLOPE_FLOOR("slope_floor", new SlopeFloor(), Category.DIAGONAL, BuildFeatures.RAISED_EDGE, BuildFeatures.PLANE_LENGTH),

    CIRCLE("circle", new Circle(), Category.CIRCULAR, BuildFeatures.CIRCLE_START, BuildFeatures.PLANE_FILLING, BuildFeatures.PLANE_FACING, BuildFeatures.PLANE_LENGTH),
    CYLINDER("cylinder", new Cylinder(), Category.CIRCULAR, BuildFeatures.CIRCLE_START, BuildFeatures.PLANE_FILLING, BuildFeatures.PLANE_FACING, BuildFeatures.PLANE_LENGTH),
    SPHERE("sphere", new Sphere(), Category.CIRCULAR, BuildFeatures.CIRCLE_START, BuildFeatures.PLANE_FILLING, BuildFeatures.PLANE_FACING, BuildFeatures.PLANE_LENGTH),

    PYRAMID("pyramid", new Pyramid(), Category.ROOF, BuildFeatures.PLANE_FILLING, BuildFeatures.PLANE_LENGTH),
    CONE("cone", new Cone(), Category.ROOF, BuildFeatures.CIRCLE_START),
    ;

//    DOME("dome", new Dome(), Category.ROOF);

    private final Structure defaultStructure;
    private final Category category;
    private final BuildFeatures[] features;
    private final String name;
    private final boolean enabled;

    BuildMode(String name, Structure instance, Category category, BuildFeatures... features) {
        this.name = name;
        this.defaultStructure = instance;
        this.category = category;
        this.features = features;
        this.enabled = true;
    }

    BuildMode(String name, Structure defaultStructure, Category category, boolean enabled, BuildFeatures... features) {
        this.name = name;
        this.defaultStructure = defaultStructure;
        this.category = category;
        this.features = features;
        this.enabled = enabled;
    }

    public Structure getDefaultStructure() {
        return defaultStructure;
    }

    public Color getTintColor() {
        return category.getColor();
    }

    public BuildFeatures[] getSupportedFeatures() {
        return features;
    }

    public String getName() {
        return name;
    }

    public Text getDisplayName() {
        if (isDisabled()) {
            return Text.translate("effortless.mode.%s".formatted(name)).withStyle(ChatFormatting.GRAY);
        }
        return Text.translate("effortless.mode.%s".formatted(name));
    }

    public ResourceLocation getIcon() {
        return ResourceLocation.of(Schema.MOD_ID, "textures/mode/%s.png".formatted(name));
    }

    public boolean isDisabled() {
        return this == BuildMode.DISABLED;
    }

    public boolean isEnabled() {
        return this != BuildMode.DISABLED;
    }

    public enum Category {
        BASIC(new Color(0f, .5f, 1f, .5f)),
        SQUARE(new Color(1f, .54f, .24f, .5f)),
        DIAGONAL(new Color(0.56f, 0.28f, 0.87f, .5f)),
        CIRCULAR(new Color(0.29f, 0.76f, 0.3f, .5f)),
        ROOF(new Color(0.83f, 0.87f, 0.23f, .5f));

        private final Color color;

        Category(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }
    }

    public BuildMode next() {
        return next(false);
    }

    public BuildMode previous() {
        return next(true);
    }

    private BuildMode next(boolean reverse) {
        return BuildMode.values()[(ordinal() + (reverse ? -1 : 1) + BuildMode.values().length) % BuildMode.values().length];
    }

}
