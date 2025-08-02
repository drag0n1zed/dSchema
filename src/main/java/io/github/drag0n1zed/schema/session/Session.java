package io.github.drag0n1zed.schema.session;

import java.util.List;

import io.github.drag0n1zed.universal.api.platform.LoaderType;
import io.github.drag0n1zed.universal.api.platform.Mod;

public record Session(
        LoaderType loaderType,
        String loaderVersion,
        String gameVersion,
        List<Mod> mods,
        int protocolVersion
) {

}
