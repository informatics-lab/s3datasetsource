package uk.co.informaticslab;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class S3RandomAccessFileTest {

    private static final String URL = "s3://mogreps-g/prods_op_mogreps-g_20160101_00_00_015.nc";

    private AmazonS3 client;
    private S3RandomAccessFile raf;

    @Before
    public void setUp() throws IOException {
        client = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();
        raf = new S3RandomAccessFile(client, URL);
    }

    @Test
    public void testLength() throws IOException {
        assertEquals("file length", 35948814, raf.length());
        raf.close();
    }

}