package rndms.config.wiring.rendering;


import kew.core.msg.ChannelSource;
import kew.core.msg.MessageSink;
import kew.core.qchan.QChannelFactoryAdapter;
import kew.providers.artemis.ServerConnector;
import kew.providers.artemis.qchan.ArtemisMessage;
import kew.providers.artemis.qchan.ArtemisQChannel;
import org.apache.activemq.artemis.core.config.CoreQueueConfiguration;
import rndms.core.service.rendering.FailedRenderingHandler;
import rndms.core.service.rendering.RenderingProcessor;
import rndms.core.types.QueuedRendering;
import rndms.core.types.RenderingConfigSource;
import util.io.SinkWriter;
import util.io.SourceReader;

import java.io.InputStream;
import java.io.OutputStream;

public class RenderingQBeans {

    // look at the classes in smuggler.config.wiring
    // not using spring here tho...

    public QChannelFactoryAdapter<ArtemisMessage, QueuedRendering>
    renderingChannelFactory(ServerConnector connector) {

        CoreQueueConfiguration qConfig = new CoreQueueConfiguration()
                                        .setName("omero/rendering/q")
                                        .setAddress("omero/rendering/q");

        SinkWriter<QueuedRendering, OutputStream> serializer = null; // see TODO below
        SourceReader<InputStream, QueuedRendering> deserializer = null; // see TODO below
        return new ArtemisQChannel<>(connector,
                                     qConfig,
                                     serializer,
                                     deserializer);
    }
    /* TODO consider generalising SerializationFactory from
     * ome.smuggler.config.wiring.crypto and using it here.
     */

    public ChannelSource<QueuedRendering> renderingSourceChannel(
            QChannelFactoryAdapter<ArtemisMessage, QueuedRendering> factory)
            throws Exception {
        return factory.buildSource();
    }

    public MessageSink<ArtemisMessage, InputStream> dequeueRenderingTask(
            QChannelFactoryAdapter<ArtemisMessage, QueuedRendering> factory,
            RenderingConfigSource rndConfig,
            RenderingProcessor processor,
            FailedRenderingHandler failureHandler) throws Exception {
        return factory.buildRepeatSink(processor,
                rndConfig.retryIntervals(),
                failureHandler);
    }

}
