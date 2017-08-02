package uk.co.informaticslab.benchmark;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Timer;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import uk.co.informaticslab.Constants;
import uk.co.informaticslab.S3DatasetSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static org.junit.Assert.assertEquals;

@RunWith(mockit.integration.junit4.JMockit.class)
public class BenchS3DatasetSourceTest {

    private static final int TEST_COUNT = 100;
    private static final String TEST_PATH = "/s3/mogreps-g/prods_op_mogreps-g_20160101_00_00_015.nc";

    private static final ConsoleReporter REPORTER = ConsoleReporter.forRegistry(Constants.METRICS)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.SECONDS)
            .build();

    private static final CsvReporter CSV_REPORTER = CsvReporter.forRegistry(Constants.METRICS)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.SECONDS)
            .build(new File("./"));

    private final Timer varTimer = Constants.METRICS.timer(name(BenchS3DatasetSourceTest.class, "varTimer"));
    private final Timer totalTimer = Constants.METRICS.timer(name(BenchS3DatasetSourceTest.class, "totalTimer"));

    @Tested
    private S3DatasetSource ds;

    @Mocked
    private HttpServletRequest mockServletRequest;

    @AfterClass
    public static void tearDownOnce() {
        REPORTER.report();
        CSV_REPORTER.report();
    }

    @Before
    public void setUp() {
        ds = new S3DatasetSource();

        new Expectations() {{
            mockServletRequest.getPathInfo();
            result = TEST_PATH;
        }};

    }

    @Test
    public void test(@Mocked HttpServletResponse mockServletResponse) throws IOException {
        Timer.Context totalContext = totalTimer.time();
        for (int i = 0; i < TEST_COUNT; i++) {
            makeRequest(mockServletRequest, mockServletResponse);
        }
        totalContext.stop();
    }

    public void makeRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
        NetcdfFile ncf = ds.getNetcdfFile(req, res);
        final Timer.Context varContext = varTimer.time();
        readVar(ncf);
        varContext.stop();
    }

    public void readVar(NetcdfFile ncf) throws IOException {
        List<Variable> all = ncf.getVariables();
        for (Variable v : all) {
            Array data = v.read();
        }
    }

}