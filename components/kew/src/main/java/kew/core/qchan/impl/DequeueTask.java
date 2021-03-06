package kew.core.qchan.impl;

import static java.util.Objects.requireNonNull;
import static kew.core.msg.ChannelMessage.message;
import static util.error.Exceptions.throwAsIfUnchecked;

import java.io.InputStream;

import kew.core.msg.ChannelMessage;
import kew.core.qchan.spi.HasReceiptAck;
import kew.core.qchan.spi.QConnector;
import kew.core.qchan.spi.QConsumer;
import kew.core.msg.ChannelSink;
import kew.core.msg.MessageSink;
import util.io.SourceReader;

/**
 * Receives messages asynchronously from a queue and dispatches them to a 
 * consumer.
 * @param <QM> the message type in the underlying middleware.
 * @param <T> the type of the message data.
 */
public class DequeueTask<QM extends HasReceiptAck, T>
    implements MessageSink<QM, InputStream> {
    
    private final QConsumer<QM> receiver;  // keep ref to avoid GC nuking it.
    private final MessageSink<QM, T> sink;
    private final boolean redeliverOnRecovery;
    private final SourceReader<InputStream, T> deserializer;
    
    /**
     * Creates a new instance.
     * @param queue provides access to the queue from which to fetch messages.
     * @param consumer consumes message data fetched from the queue.
     * @param deserializer de-serialises the message data, a {@code T}-value.
     * @param redeliverOnRecovery if {@code true} and the process terminates
     * abnormally (e.g. segfault, power failure) while the consumer is busy 
     * processing a message, the message will be delivered again once the
     * process is rebooted. If {@code false}, a message will only ever be 
     * delivered once to the consumer.
     * @throws Exception if an error occurs while setting up the underlying
     * middleware to receive messages on the specified queue.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public DequeueTask(QConnector<QM> queue,
                       ChannelSink<T> consumer,
                       SourceReader<InputStream, T> deserializer,
                       boolean redeliverOnRecovery)
            throws Exception {
        this(queue, MessageSink.forwardDataTo(consumer), deserializer,
             redeliverOnRecovery);
    }
    
    /**
     * Creates a new instance.
     * @param queue provides access to the queue from which to fetch messages.
     * @param consumer consumes message data and metadata fetched from the queue.
     * @param redeliverOnRecovery if {@code true} and the process terminates
     * abnormally (e.g. segfault, power failure) while the consumer is busy 
     * processing a message, the message will be delivered again once the
     * process is rebooted. If {@code false}, a message will only ever be 
     * delivered once to the consumer.
     * @param deserializer de-serialises the message data, a {@code T}-value.
     * @throws Exception if an error occurs while setting up the underlying
     * middleware to receive messages on the specified queue.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public DequeueTask(QConnector<QM> queue,
                       MessageSink<QM, T> consumer,
                       SourceReader<InputStream, T> deserializer,
                       boolean redeliverOnRecovery)
            throws Exception {
        requireNonNull(queue, "queue");
        requireNonNull(consumer, "consumer");
        requireNonNull(deserializer, "deserializer");

        this.sink = consumer;
        this.receiver = queue.newConsumer(this::handleMessage);
        this.redeliverOnRecovery = redeliverOnRecovery;
        this.deserializer = deserializer;
    }
    
    private void removeFromQueue(QM msg) {
        try {
            msg.removeFromQueue();
        } catch (Exception e) {
            throwAsIfUnchecked(e);
        }
    }
    
    private void consumeThenRemove(QM msg, T messageData) {
        try {
            sink.consume(message(msg, messageData));  // (*)
        } finally {
            removeFromQueue(msg);
        }
    }
    /* NOTE. If the process dies here, the message is still in the queue as it
     * hasn't been acknowledged yet. Artemis will deliver it again on reboot.
     * In fact, this works exactly the same as it used to in HornetQ.
     * See:
     * - http://stackoverflow.com/questions/15243991/what-happen-if-client-acknowledgment-not-done
     */
    
    private void removeThenConsume(QM msg, T messageData) {
        removeFromQueue(msg);
        sink.consume(message(msg, messageData));
    }

    private void handleMessage(QM msg, InputStream body) {
        T messageData = deserializer.uncheckedRead(body);
        if (redeliverOnRecovery) {
            consumeThenRemove(msg, messageData);
        } else {
            removeThenConsume(msg, messageData);
        }
    }

    /**
     * @return the underlying consumer used to receive messages from the
     * queue.
     */
    public QConsumer<QM> receiver() {
        return receiver;
    }

    @Override
    public void consume(ChannelMessage<QM, InputStream> msg) {
        requireNonNull(msg, "msg");
        handleMessage(msg.metadata().get(), msg.data());
    }
    /* NOTE
     * Implementing MessageSink so that the QChannelFactory can expose this
     * interface to clients rather than the concrete type of DequeueTask.
     */

}
