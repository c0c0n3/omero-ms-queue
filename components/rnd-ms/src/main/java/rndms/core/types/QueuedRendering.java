package rndms.core.types;


public class QueuedRendering {

    // look at QueuedImport for ideas on what to do...

    private final long imageId;

    public QueuedRendering(long imageId) {
        this.imageId = imageId;
    }

    public long imageId() {
        return imageId;
    }

}
