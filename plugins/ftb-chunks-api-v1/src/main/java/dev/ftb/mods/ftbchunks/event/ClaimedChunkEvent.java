package dev.ftb.mods.ftbchunks.event;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.ftb.mods.ftbchunks.data.ClaimResult;
import dev.ftb.mods.ftbchunks.data.ClaimedChunk;
import net.minecraft.commands.CommandSourceStack;

/**
 * @author LatvianModder
 */
public interface ClaimedChunkEvent {
	Event<Before> BEFORE_CLAIM = EventFactory.createCompoundEventResult();
	Event<Before> BEFORE_LOAD = EventFactory.createCompoundEventResult();
	Event<Before> BEFORE_UNCLAIM = EventFactory.createCompoundEventResult();
	Event<Before> BEFORE_UNLOAD = EventFactory.createCompoundEventResult();
	Event<After> AFTER_CLAIM = EventFactory.createLoop();
	Event<After> AFTER_LOAD = EventFactory.createLoop();
	Event<After> AFTER_UNCLAIM = EventFactory.createLoop();
	Event<After> AFTER_UNLOAD = EventFactory.createLoop();

	interface Before {
		CompoundEventResult<ClaimResult> before(CommandSourceStack s, ClaimedChunk a);
	}

	interface After {
		void after(CommandSourceStack s, ClaimedChunk a);
	}
}