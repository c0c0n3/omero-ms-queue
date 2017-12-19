package rndms.core.service.rendering;

import kew.core.msg.RepeatConsumer;
import rndms.core.types.QueuedRendering;

public interface RenderingProcessor extends RepeatConsumer<QueuedRendering> {
    // look at ImportProcessor in smuggler for ideas...
}
