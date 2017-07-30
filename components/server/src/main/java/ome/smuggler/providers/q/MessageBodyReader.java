package ome.smuggler.providers.q;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.activemq.artemis.api.core.client.ClientMessage;

import util.io.SourceReader;


/**
 * Reads the body of a message from the underlying Artemis buffer into an
 * input stream.
 * @see MessageBodyWriter
 */
public class MessageBodyReader
        implements SourceReader<ClientMessage, InputStream> {

    @Override
    public InputStream read(ClientMessage source) {
        requireNonNull(source, "source");

        int length = source.getBodyBuffer().readInt();            // (*)
        byte[] buf = new byte[length];
        source.getBodyBuffer().readBytes(buf);

        return new ByteArrayInputStream(buf);  // (*)
    }
    /* NOTE. Large messages.
     * If we ever going to need large messages (I doubt it!) we could easily
     * give this code an upgrade using NIO channels and byte buffers. (In
     * that case, don't forget to use a long instead of an int for the
     * serialised buffer size!)
     * However, bear in mind that for large messages (think GBs), slurping
     * the whole thing into memory (as we do here) can cause severe indigestion
     * and possibly send Smuggler to the ER...So rather switch to stream
     * processing in that case!
     */
}
