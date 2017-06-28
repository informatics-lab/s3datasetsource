package uk.co.informaticslab;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.nc2.NetcdfFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(mockit.integration.junit4.JMockit.class)
public class S3DatasetSourceTest {

    private static final String TEST_PATH = "/s3/mogreps-g/prods_op_mogreps-g_20160101_00_00_015.nc.html";

    @Tested
    private S3DatasetSource ds;

    @Mocked
    private HttpServletRequest mockServletRequest;

    @Before
    public void setUp() {
        ds = new S3DatasetSource();

        new Expectations() {{
            mockServletRequest.getPathInfo();
            result = TEST_PATH;
        }};
    }

    @Test
    public void testIsMine() {
        assertEquals("path is claimed", true, ds.isMine(mockServletRequest));
    }

    @Test
    public void testGetNetcdfFile(@Mocked HttpServletResponse mockServletResponse) throws Exception {
        NetcdfFile ncf = ds.getNetcdfFile(mockServletRequest, mockServletResponse);
        assertTrue(ncf instanceof NetcdfFile);
        System.out.println(ncf.getDimensions());
    }

}