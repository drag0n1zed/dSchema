package io.github.drag0n1zed.dschema.screen.common;

import io.github.drag0n1zed.dschema.SchemaClient;
import io.github.drag0n1zed.universal.api.gui.AbstractPanelScreen;
import io.github.drag0n1zed.universal.api.platform.Entrance;
import io.github.drag0n1zed.universal.api.text.Text;

public abstract class SchemaPanelScreen extends AbstractPanelScreen {
    protected SchemaPanelScreen(Entrance entrance, Text title) {
        super(entrance, title);
    }



    @Override
    protected SchemaClient getEntrance() {
        return (SchemaClient) super.getEntrance();
    }
}
