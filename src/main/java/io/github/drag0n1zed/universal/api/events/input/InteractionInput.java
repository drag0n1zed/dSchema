package io.github.drag0n1zed.universal.api.events.input;

import io.github.drag0n1zed.universal.api.core.InteractionHand;
import io.github.drag0n1zed.universal.api.core.InteractionType;
import io.github.drag0n1zed.universal.api.events.EventResult;

public interface InteractionInput {
    EventResult onInteractionInput(InteractionType type, InteractionHand hand);
}
