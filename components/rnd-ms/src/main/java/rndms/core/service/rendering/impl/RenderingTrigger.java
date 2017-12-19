package rndms.core.service.rendering.impl;


import kew.core.msg.ChannelSource;
import rndms.core.service.rendering.RenderingRequestor;
import rndms.core.types.QueuedRendering;

import java.util.stream.Stream;

public class RenderingTrigger implements RenderingRequestor {

    // look at ImportTrigger in Smuggler

    private final ChannelSource<QueuedRendering> queue;

    public RenderingTrigger(ChannelSource<QueuedRendering> queue) {
        this.queue = queue;
    }

    @Override
    public void enqueue(Stream<Long> imageIds) {
        imageIds.map(QueuedRendering::new)
                .forEach(queue::uncheckedSend);
    }

}
