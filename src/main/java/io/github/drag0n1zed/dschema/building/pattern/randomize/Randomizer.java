package io.github.drag0n1zed.dschema.building.pattern.randomize;

import java.util.Collection;
import java.util.UUID;

import io.github.drag0n1zed.universal.api.core.Item;
import io.github.drag0n1zed.universal.api.core.ItemStack;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.dschema.building.pattern.Transformer;

public interface Randomizer<T> extends Transformer {

    static Category extract(Object object) {
        if (object instanceof Item) {
            return Category.ITEM;
        }
        throw new IllegalArgumentException("Invalid object: " + object);
    }

    Order getOrder();

    Target getTarget();

    Category getCategory();

    Collection<Chance<T>> getChances();

    Producer<T> asProducer(long seed, boolean limitedProducer);

    @Override
    default float volumeMultiplier() {
        return 1f;
    }

    enum Order {
        SEQUENCE("sequence"),
        RANDOM("random");

        private final String name;

        Order(String name) {
            this.name = name;
        }

        public Text getDisplayName() {
            return Text.translate("dschema.transformer.randomizer.order.%s".formatted(name));
        }

        public String getName() {
            return name;
        }
    }

    enum Target {
        SINGLE("single"),
        GROUP("group");

        private final String name;

        Target(String name) {
            this.name = name;
        }

        public Text getDisplayName() {
            return Text.translate("dschema.transformer.randomizer.target.%s".formatted(name));
        }

        public String getName() {
            return name;
        }
    }

    enum Category {
        ITEM("item", ItemStack.class);

        private final String name;
        private final Class<?> clazz;

        Category(String name, Class<?> clazz) {
            this.name = name;
            this.clazz = clazz;
        }

        public Text getDisplayName() {
            return Text.translate("dschema.transformer.randomizer.category.%s".formatted(name));
        }

        public String getName() {
            return name;
        }
    }

    @Override
    Randomizer<T> withId(UUID id);

    @Override
    Randomizer<T> withName(Text name);

}
