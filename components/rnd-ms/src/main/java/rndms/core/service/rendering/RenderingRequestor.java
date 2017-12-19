package rndms.core.service.rendering;

import java.util.stream.Stream;

public interface RenderingRequestor {

    // look at ImportRequestor in Smuggler

    void enqueue(Stream<Long> imageIds);

}
