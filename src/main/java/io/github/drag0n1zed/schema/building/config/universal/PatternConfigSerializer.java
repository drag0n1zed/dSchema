package io.github.drag0n1zed.schema.building.config.universal;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.github.drag0n1zed.schema.building.pattern.Pattern;
import io.github.drag0n1zed.universal.api.config.ConfigSerializer;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;

public class PatternConfigSerializer implements ConfigSerializer<Pattern> {

    public static final PatternConfigSerializer INSTANCE = new PatternConfigSerializer();
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_TRANSFORMERS = "transformers";

    private PatternConfigSerializer() {
    }

    public static String randomIdString() {
        return UUID.randomUUID().toString();
    }

    public static boolean isIdCorrect(Object string) {
        try {
            UUID.fromString((String) string);
            return true;
        } catch (ClassCastException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public ConfigSpec getSpec(Config config) {
        var spec = new ConfigSpec();
        spec.define(KEY_ENABLED, () -> getDefault().enabled(), Boolean.class::isInstance);
        spec.defineList(KEY_TRANSFORMERS, () -> getDefault().transformers(), Config.class::isInstance);
        return spec;
    }

    @Override
    public Pattern deserialize(Config config) {
        validate(config);
        return new Pattern(
                config.get(KEY_ENABLED),
                config.<List<Config>>get(KEY_TRANSFORMERS).stream().map(TransformerConfigSerializer.INSTANCE::deserialize).filter(Objects::nonNull).toList()
        );
    }

    @Override
    public Config serialize(Pattern pattern) {
        var config = Config.inMemory();
        config.set(KEY_ENABLED, pattern.enabled());
        config.set(KEY_TRANSFORMERS, pattern.transformers().stream().map(TransformerConfigSerializer.INSTANCE::serialize).toList());
        validate(config);
        return config;
    }

    @Override
    public Pattern getDefault() {
        return Pattern.DISABLED;
    }

}
