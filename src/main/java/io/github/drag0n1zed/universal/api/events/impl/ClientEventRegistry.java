package io.github.drag0n1zed.universal.api.events.impl;

import io.github.drag0n1zed.universal.api.events.Event;
import io.github.drag0n1zed.universal.api.events.input.InteractionInput;
import io.github.drag0n1zed.universal.api.events.input.KeyInput;
import io.github.drag0n1zed.universal.api.events.input.RegisterKeys;
import io.github.drag0n1zed.universal.api.events.lifecycle.ClientStart;
import io.github.drag0n1zed.universal.api.events.lifecycle.ClientTick;
import io.github.drag0n1zed.universal.api.events.render.RegisterShader;
import io.github.drag0n1zed.universal.api.events.render.RenderGui;
import io.github.drag0n1zed.universal.api.events.render.RenderWorld;

public class ClientEventRegistry extends EventRegistry {

    public Event<RegisterKeys> getRegisterKeysEvent() {
        return get();
    }

    public Event<KeyInput> getKeyInputEvent() {
        return get();
    }

    public Event<InteractionInput> getInteractionInputEvent() {
        return get();
    }

    public Event<ClientStart> getClientStartEvent() {
        return get();
    }

    public Event<ClientTick> getClientTickEvent() {
        return get();
    }

    public Event<RenderGui> getRenderGuiEvent() {
        return get();
    }

    public Event<RenderWorld> getRenderWorldEvent() {
        return get();
    }

    public Event<RegisterShader> getRegisterShaderEvent() {
        return get();
    }

}
