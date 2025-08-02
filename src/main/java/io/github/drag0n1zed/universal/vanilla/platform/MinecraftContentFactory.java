package io.github.drag0n1zed.universal.vanilla.platform;

import java.util.Optional;

import com.google.auto.service.AutoService;

import io.github.drag0n1zed.universal.api.core.Item;
import io.github.drag0n1zed.universal.api.core.ItemStack;
import io.github.drag0n1zed.universal.api.core.ResourceLocation;
import io.github.drag0n1zed.universal.api.core.Stat;
import io.github.drag0n1zed.universal.api.core.StatType;
import io.github.drag0n1zed.universal.api.core.StatTypes;
import io.github.drag0n1zed.universal.api.core.fluid.Fluid;
import io.github.drag0n1zed.universal.api.core.fluid.Fluids;
import io.github.drag0n1zed.universal.api.platform.ContentFactory;
import io.github.drag0n1zed.universal.api.platform.OperatingSystem;
import io.github.drag0n1zed.universal.api.platform.PlatformReference;
import io.github.drag0n1zed.universal.api.sound.Sound;
import io.github.drag0n1zed.universal.api.sound.Sounds;
import io.github.drag0n1zed.universal.api.tag.InputStreamTagReader;
import io.github.drag0n1zed.universal.api.tag.OutputStreamTagWriter;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftFluid;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftItem;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftItemStack;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftResourceLocation;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftText;
import io.github.drag0n1zed.universal.vanilla.sound.MinecraftSound;
import io.github.drag0n1zed.universal.vanilla.tag.MinecraftRecordTag;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;

@AutoService(ContentFactory.class)
public class MinecraftContentFactory implements ContentFactory {

    @Override
    public ResourceLocation newResourceLocation(String namespace, String path) {
        return new MinecraftResourceLocation(new net.minecraft.resources.ResourceLocation(namespace, path));
    }

    @Override
    public Optional<Item> newOptionalItem(ResourceLocation location) {
        return BuiltInRegistries.ITEM.getOptional(location.<net.minecraft.resources.ResourceLocation>reference()).map(MinecraftItem::ofNullable);
    }

    @Override
    public ItemStack newItemStack() {
        return new MinecraftItemStack(net.minecraft.world.item.ItemStack.EMPTY);
    }

    @Override
    public ItemStack newItemStack(Item item, int count) {
        return new MinecraftItemStack(new net.minecraft.world.item.ItemStack((net.minecraft.world.item.Item) item.reference(), count));
    }

    @Override
    public Text newText() {
        return new MinecraftText(Component.empty());
    }

    @Override
    public Text newText(String text) {
        return new MinecraftText(Component.literal(text));
    }

    @Override
    public Text newTranslatableText(String text) {
        return new MinecraftText(Component.translatable(text));
    }

    @Override
    public Text newTranslatableText(String text, Object... args) {
        return new MinecraftText(Component.translatable(text, args));
    }

    @Override
    public InputStreamTagReader getInputStreamTagReader() {
        return input -> new MinecraftRecordTag(NbtIo.readCompressed(input));
    }

    @Override
    public OutputStreamTagWriter getOutputStreamTagWriter() {
        return (output, config) -> NbtIo.writeCompressed(config.reference(), output);
    }

    @Override
    public OperatingSystem getOperatingSystem() {
        return switch (Util.getPlatform()) {
            case LINUX -> OperatingSystem.LINUX;
            case SOLARIS -> OperatingSystem.SOLARIS;
            case WINDOWS -> OperatingSystem.WINDOWS;
            case OSX -> OperatingSystem.MACOS;
            case UNKNOWN -> OperatingSystem.UNKNOWN;
        };
    }

    @Override
    public Sound getSound(Sounds sounds) {
        var sound = switch (sounds) {
            case UI_BUTTON_CLICK -> SoundEvents.UI_BUTTON_CLICK.value();
            case UI_TOAST_IN -> SoundEvents.UI_TOAST_IN;
            case UI_TOAST_OUT -> SoundEvents.UI_TOAST_OUT;
        };
        return new MinecraftSound(sound);
    }

    @Override
    public Fluid getFluid(Fluids fluids) {
        var fluid = switch (fluids) {
            case EMPTY -> net.minecraft.world.level.material.Fluids.EMPTY;
            case FLOWING_WATER -> net.minecraft.world.level.material.Fluids.FLOWING_WATER;
            case WATER -> net.minecraft.world.level.material.Fluids.WATER;
            case FLOWING_LAVA -> net.minecraft.world.level.material.Fluids.FLOWING_LAVA;
            case LAVA -> net.minecraft.world.level.material.Fluids.LAVA;
        };
        return MinecraftFluid.ofNullable(fluid);
    }

    @Override
    public <T extends PlatformReference> StatType<T> getStatType(StatTypes statTypes) {
        var statType = switch (statTypes) {
            case ITEM_USED -> Stats.ITEM_USED;
            case ITEM_BROKEN -> Stats.ITEM_BROKEN;
            case ITEM_PICKED_UP -> Stats.ITEM_PICKED_UP;
            case ITEM_DROPPED -> Stats.ITEM_DROPPED;
        };
        return new StatType<T>() {
            @Override
            public Stat<T> get(T value) {
                return () -> statType.get(value.reference());
            }

            @Override
            public Object refs() {
                return statType;
            }
        };
    }
}
