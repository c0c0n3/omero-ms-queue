package rndms.core.types;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public interface RenderingConfigSource {

    // look at ImportConfigSource for ideas on what to do...

    /**
     * @return path to the directory where to keep rendering logs.
     */
    Path rndLogDir();

    /**
     * @return how long to keep rendering logs before deleting them.
     */
    Duration logRetentionPeriod();

    /**
     * @return intervals at which to retry failed rendering tasks.
     */
    List<Duration> retryIntervals();

    /**
     * @return path to the directory where to keep logs of failed rendering
     * tasks.
     */
    Path failedRndLogDir();

}
