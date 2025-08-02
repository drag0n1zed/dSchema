package io.github.drag0n1zed.universal.api.core;

import java.util.Collection;
import java.util.Optional;

import io.github.drag0n1zed.universal.api.platform.PlatformReference;

public interface Property extends PlatformReference {

    String getName();

    String getName(PropertyValue value);

    Optional<PropertyValue> getValue(String value);

    Collection<PropertyValue> getPossibleValues();

    Class<?> getValueClass();

}
