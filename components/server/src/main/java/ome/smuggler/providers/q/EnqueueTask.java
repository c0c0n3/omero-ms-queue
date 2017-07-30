package ome.smuggler.providers.q;

import static java.util.Objects.requireNonNull;

import java.io.OutputStream;
import java.util.function.Function;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientProducer;

import kew.core.msg.ChannelMessage;
import kew.core.msg.MessageSource;
import util.io.SinkWriter;

/**
 * Puts messages on a queue, asynchronously.
 * Messages are durable by default but any other kind of message can be
 * constructed by providing a message builder function as message metadata.
 */
public class EnqueueTask<T> 
    implements MessageSource<Function<QueueConnector, ClientMessage>, T> {

    private final QueueConnector queue;
    private final ClientProducer producer;
    private final SinkWriter<T, OutputStream> serializer;

    /**
     * Creates a new instance.
     * @param queue provides access to the queue on which to put messages.
     * @param serializer serialises the message data, a {@code T}-value.
     * @throws ActiveMQException if a queue producer could not be created.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public EnqueueTask(QueueConnector queue,
                       SinkWriter<T, OutputStream> serializer)
            throws ActiveMQException {
        requireNonNull(queue, "queue");
        requireNonNull(serializer, "serializer");
        
        this.queue = queue;
        this.producer = queue.newProducer();
        this.serializer = serializer;
    }

    private void writeBody(ClientMessage sink, T data) {
        MessageBodyWriter bodyWriter = new MessageBodyWriter();
        bodyWriter.write(sink, out -> serializer.write(out, data));
    }

    @Override
    public void send(
            ChannelMessage<Function<QueueConnector, ClientMessage>, T> msg) 
                    throws Exception {
        requireNonNull(msg, "msg");
        
        Function<QueueConnector, ClientMessage> messageBuilder = 
                msg.metadata().orElse(QueueConnector::newDurableMessage);
        ClientMessage qMsg = messageBuilder.apply(queue);
        writeBody(qMsg, msg.data());
        producer.send(qMsg);
    }

}
