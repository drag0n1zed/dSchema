package io.github.drag0n1zed.universal.api.platform;

import java.util.Locale;
import java.util.Optional;

import io.github.drag0n1zed.universal.api.core.Item;
import io.github.drag0n1zed.universal.api.core.ItemStack;
import io.github.drag0n1zed.universal.api.core.Items;
import io.github.drag0n1zed.universal.api.core.ResourceLocation;
import io.github.drag0n1zed.universal.api.core.StatType;
import io.github.drag0n1zed.universal.api.core.StatTypes;
import io.github.drag0n1zed.universal.api.core.fluid.Fluid;
import io.github.drag0n1zed.universal.api.core.fluid.Fluids;
import io.github.drag0n1zed.universal.api.sound.Sound;
import io.github.drag0n1zed.universal.api.sound.Sounds;
import io.github.drag0n1zed.universal.api.tag.InputStreamTagReader;
import io.github.drag0n1zed.universal.api.tag.OutputStreamTagWriter;
import io.github.drag0n1zed.universal.api.text.Text;

public interface ContentFactory {

    static ContentFactory getInstance() {
        return PlatformLoader.getSingleton();
    }

    ResourceLocation newResourceLocation(String namespace, String path);

    Optional<Item> newOptionalItem(ResourceLocation location);

    default Item newItem(ResourceLocation location) {
        return newOptionalItem(location).orElseThrow();
    }

    ItemStack newItemStack();

    ItemStack newItemStack(Item item, int count);

    Text newText();

    Text newText(String text);

    Text newTranslatableText(String text);

    Text newTranslatableText(String text, Object... args);

    InputStreamTagReader getInputStreamTagReader();

    OutputStreamTagWriter getOutputStreamTagWriter();

    OperatingSystem getOperatingSystem();

    Sound getSound(Sounds sounds);

    default Optional<Item> getOptionalItem(Items items) {
        return newOptionalItem(ResourceLocation.of("minecraft", items.name().toLowerCase(Locale.ROOT)));
    }

    default Item getItem(Items items) {
        return getOptionalItem(items).orElseThrow();
    }

    Fluid getFluid(Fluids fluids);

    <T extends PlatformReference> StatType<T> getStatType(StatTypes statTypes);

}
