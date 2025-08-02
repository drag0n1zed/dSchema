package io.github.drag0n1zed.universal.vanilla.platform;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.auto.service.AutoService;

import io.github.drag0n1zed.universal.api.core.ItemStack;
import io.github.drag0n1zed.universal.api.input.KeyBinding;
import io.github.drag0n1zed.universal.api.platform.ClientContentFactory;
import io.github.drag0n1zed.universal.api.platform.SearchBy;
import io.github.drag0n1zed.universal.api.platform.SearchTree;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftItemStack;
import io.github.drag0n1zed.universal.vanilla.input.MinecraftKeyBinding;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.searchtree.PlainTextSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTabs;

@AutoService(ClientContentFactory.class)
public class MinecraftClientContentFactory extends MinecraftContentFactory implements ClientContentFactory {

    @Override
    public SearchTree<ItemStack> searchItemStack(SearchBy searchBy) {
        var minecraftPlayer = Minecraft.getInstance().player;
        CreativeModeTabs.tryRebuildTabContents(minecraftPlayer.connection.enabledFeatures(), true, minecraftPlayer.clientLevel.registryAccess());

        var minecraftSearchTree = Minecraft.getInstance().getSearchTree(
                switch (searchBy) {
                    case NAME -> SearchRegistry.CREATIVE_NAMES;
                    case TAG -> SearchRegistry.CREATIVE_TAGS;
                }
        );
        return query -> minecraftSearchTree.search(query).stream().map(itemStack -> new MinecraftItemStack(itemStack)).collect(Collectors.toList());
    }

    @Override
    public <T> SearchTree<T> search(List<T> list, Function<T, Stream<Text>> keyExtractor) {
        return query -> PlainTextSearchTree.create(list, item -> keyExtractor.apply(item).map(text -> ((Component) text.reference()).getString())).search(query);
    }

    @Override
    public KeyBinding newKeyBinding(String name, String category, int code) {
        return new MinecraftKeyBinding(new KeyMapping(name, code, category));
    }

}
