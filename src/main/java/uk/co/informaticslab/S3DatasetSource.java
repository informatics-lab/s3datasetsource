package uk.co.informaticslab;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.retry.PredefinedRetryPolicies;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thredds.servlet.DatasetSource;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileSubclass;
import ucar.nc2.iosp.IOServiceProvider;
import ucar.nc2.iosp.hdf5.H5iosp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * {@link DatasetSource} implementation to read directly from s3
 */
public class S3DatasetSource implements DatasetSource {

    private static final Logger LOG = LoggerFactory.getLogger(S3DatasetSource.class);

    private static final String PREFIX = "/s3/";

    private final Timer rafTimer = Constants.METRICS.timer(name(S3DatasetSource.class, "rafTimer"));
    private final Timer ncTimer = Constants.METRICS.timer(name(S3DatasetSource.class, "ncTimer"));
    private final Counter s3DatasetSourceCounter = Constants.METRICS.counter(name(S3DatasetSource.class, "s3DatasetSourceCounter"));

    private final AmazonS3 client = Constants.getS3Client();

    private Map<String, byte[]> cache = new HashMap<String, byte[]>();
    private LinkedList<String> index = new LinkedList<String>();

    public S3DatasetSource() {
        s3DatasetSourceCounter.inc();
    }

    public boolean isMine(HttpServletRequest req) {
        String path = req.getPathInfo();
        boolean isMine = path.startsWith(PREFIX);
        LOG.debug("Path [{}] is mine [{}]", path, isMine);
        return isMine;
    }

    @Override
    public NetcdfFile getNetcdfFile(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String s3Url = createS3UrlFromPath(req.getPathInfo());
        LOG.debug("Accessing NetCDF file in S3 on url [{}]", s3Url);

        final Timer.Context rafContext = rafTimer.time();
        S3RandomAccessFile f = new S3RandomAccessFile(index, cache, client, s3Url);
        rafContext.stop();

        final Timer.Context ncContext = ncTimer.time();
        IOServiceProvider iosp = new H5iosp();
        NetcdfFile ncf = new NetcdfFileSubclass(iosp, f, null, null);
        ncContext.stop();

        return ncf;
    }

    private String createS3UrlFromPath(String path) {
        if (path.startsWith(PREFIX)) {
            path = path.substring(PREFIX.length());
        }
        if (!path.endsWith(".nc")) {
            path = path.substring(0, path.indexOf(".nc") + 3);
        }
        String s3Url = "s3://" + path;
        return s3Url;
    }
}
