package io.github.drag0n1zed.dschema.building;

import io.github.drag0n1zed.dschema.Schema;
import io.github.drag0n1zed.universal.api.core.ResourceLocation;
import io.github.drag0n1zed.universal.api.lang.Lang;
import io.github.drag0n1zed.universal.api.text.Text;

public interface Option {

    String getName();

    String getCategory();

    default String getNameKey() {
        return Lang.asKey(Schema.MOD_ID, "action.%s".formatted(getName()));
    }

    default String getCategoryKey() {
        return Lang.asKey(Schema.MOD_ID, "option.%s".formatted(getCategory()));
    }

    default String getTooltipKey() {
        return Lang.asKey(Schema.MOD_ID, "action.%s.tooltip".formatted(getName()));
    }

    default Text getNameText() {
        return Text.translate(getNameKey());
    }

    default Text getCategoryText() {
        return Text.translate(getCategoryKey());
    }

    default Text getTooltipText() {
        if (Lang.hasKey(getTooltipKey())) {
            return Text.translate(getTooltipKey());
        } else {
            return Text.empty();
        }
    }

    default ResourceLocation getIcon() {
        return ResourceLocation.of(Schema.MOD_ID, "textures/option/%s.png".formatted(getName()));
    }

}
