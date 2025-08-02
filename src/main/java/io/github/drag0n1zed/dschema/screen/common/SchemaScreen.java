package io.github.drag0n1zed.dschema.screen.common;

import io.github.drag0n1zed.dschema.SchemaClient;
import io.github.drag0n1zed.universal.api.gui.AbstractScreen;
import io.github.drag0n1zed.universal.api.platform.Entrance;
import io.github.drag0n1zed.universal.api.text.Text;

public abstract class SchemaScreen extends AbstractScreen {
    protected SchemaScreen(Entrance entrance, Text title) {
        super(entrance, title);
    }

    @Override
    protected SchemaClient getEntrance() {
        return (SchemaClient) super.getEntrance();
    }
}
