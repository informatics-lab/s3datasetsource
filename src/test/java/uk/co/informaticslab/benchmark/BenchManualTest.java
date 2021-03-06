package uk.co.informaticslab.benchmark;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.retry.PredefinedRetryPolicies;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Timer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import uk.co.informaticslab.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static org.junit.Assert.assertTrue;

public class BenchManualTest {

    private static final int TEST_COUNT = 100;
    private static final ConsoleReporter REPORTER = ConsoleReporter.forRegistry(Constants.METRICS)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.SECONDS)
            .build();

    private final Timer totalTimer = Constants.METRICS.timer(name(BenchManualTest.class, "totalTimer"));
    private final Timer readTimer = Constants.METRICS.timer(name(BenchManualTest.class, "readTimer"));
    private final Timer ncTimer = Constants.METRICS.timer(name(BenchManualTest.class, "ncTimer"));
    private final Timer dlTimer = Constants.METRICS.timer(name(BenchManualTest.class, "dlTimer"));

    private static final String BUCKET = "mogreps-g";
    private static final String KEY = "prods_op_mogreps-g_20160101_00_00_015.nc";

    public static AmazonS3 client;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void setUpOnce() {
        ClientConfiguration config = new ClientConfiguration();
        config.setMaxConnections(128);
        config.setMaxErrorRetry(16);
        config.setConnectionTimeout(100000);
        config.setSocketTimeout(100000);
        config.setRetryPolicy(PredefinedRetryPolicies.getDefaultRetryPolicyWithCustomMaxRetries(32));
        client = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_2).withClientConfiguration(config).build();
    }

    @AfterClass
    public static void tearDownOnce() {
        REPORTER.report();
    }

    @Test
    public void test() throws IOException {
        Timer.Context totalContext = totalTimer.time();
        File f = downloadS3File();
        for (int i = 0; i < TEST_COUNT; i++) {
            NetcdfFile ncf = getNetcdf(f);
            readVar(ncf);
        }
        totalContext.stop();
    }

    public void readVar(NetcdfFile ncf) throws IOException {
        List<Variable> all = ncf.getVariables();
        Timer.Context readContext = readTimer.time();
        for (Variable v : all) {
            Array data = v.read();
        }
        readContext.stop();
    }

    public NetcdfFile getNetcdf(File f) throws IOException {
        final Timer.Context ncContext = ncTimer.time();
        NetcdfFile ncf = NetcdfFile.open(f.getPath());
        ncContext.stop();
        return ncf;
    }

    public File downloadS3File() throws IOException {
        final Timer.Context dlContext = dlTimer.time();
        S3Object object = client.getObject(new GetObjectRequest(BUCKET, KEY));
        InputStream objectData = object.getObjectContent();
        File targetFile = folder.newFile();
        Files.copy(objectData, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        objectData.close();
        dlContext.stop();
        return targetFile;
    }
}
