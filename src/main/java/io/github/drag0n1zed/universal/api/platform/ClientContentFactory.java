package io.github.drag0n1zed.universal.api.platform;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import io.github.drag0n1zed.universal.api.core.ItemStack;
import io.github.drag0n1zed.universal.api.input.KeyBinding;
import io.github.drag0n1zed.universal.api.text.Text;

public interface ClientContentFactory extends ContentFactory {

    static ClientContentFactory getInstance() {
        return PlatformLoader.getSingleton();
    }

    SearchTree<ItemStack> searchItemStack(SearchBy searchBy);

    @Deprecated
    <T> SearchTree<T> search(List<T> list, Function<T, Stream<Text>> keyExtractor);

    KeyBinding newKeyBinding(String name, String category, int code);

}
