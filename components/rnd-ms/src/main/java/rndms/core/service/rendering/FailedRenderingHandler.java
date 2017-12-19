package rndms.core.service.rendering;

import rndms.core.types.QueuedRendering;

import java.util.function.Consumer;

public interface FailedRenderingHandler extends Consumer<QueuedRendering> {
    // look at FailedImportHandler in Smuggler
}
