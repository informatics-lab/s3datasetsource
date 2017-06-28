package uk.co.informaticslab;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.retry.PredefinedRetryPolicies;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
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

/**
 * {@link DatasetSource} implementation to read directly from s3
 */
public class S3DatasetSource implements DatasetSource {

    private static final Logger LOG = LoggerFactory.getLogger(S3DatasetSource.class);
    private static final Regions MY_S3_DATA_REGION = Regions.EU_WEST_2;
    private static final String PREFIX = "/s3/";

    private final AmazonS3 client;

    public S3DatasetSource() {
        ClientConfiguration config = new ClientConfiguration();
        config.setMaxConnections(128);
        config.setMaxErrorRetry(16);
        config.setConnectionTimeout(100000);
        config.setSocketTimeout(100000);
        config.setRetryPolicy(PredefinedRetryPolicies.getDefaultRetryPolicyWithCustomMaxRetries(32));
        this.client = AmazonS3ClientBuilder.standard().withRegion(MY_S3_DATA_REGION).withClientConfiguration(config).build();
    }

    public boolean isMine(HttpServletRequest req) {
        String path = req.getPathInfo();
        boolean isMine = path.startsWith(PREFIX);
        LOG.debug("Path [{}] is mine [{}]", path, isMine);
        return isMine;
    }

    public NetcdfFile getNetcdfFile(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String s3Url = createS3UrlFromPath(req.getPathInfo());
        LOG.debug("Accessing NetCDF file in S3 on url [{}]", s3Url);
        S3RandomAccessFile f = new S3RandomAccessFile(client, s3Url);
        IOServiceProvider iosp = new H5iosp();
        NetcdfFile ncf = new NetcdfFileSubclass(iosp, f, null, null);
        return ncf;
    }

    private String createS3UrlFromPath(String path) {
        if (path.startsWith(PREFIX)) {
            path = path.substring(PREFIX.length());
        }
        if (!path.endsWith(".nc")) {
            path = path.substring(0, path.indexOf(".nc"));
        }
        String s3Url = "s3://" + path;
        return s3Url;
    }
}
