package rndms.core.service.rendering.impl;


import rndms.core.types.QueuedRendering;
import util.runtime.CommandBuilder;

import java.util.stream.Stream;


public class RenderingCommandBuilder implements CommandBuilder {

    // look at cmd builders in ome.smuggler.core.service.omero.impl

    private final QueuedRendering task;

    public RenderingCommandBuilder(QueuedRendering task) {
        this.task = task;
    }

    @Override
    public Stream<String> tokens() {
        String formattedId = String.format("Image:%s", task.imageId());
        return Stream.of("omero", "-q", "render", "test", formattedId);
    }
    // look at util.runtime.* for better options to assemble command lines...

}
