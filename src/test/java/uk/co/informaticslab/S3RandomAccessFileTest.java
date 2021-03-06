package uk.co.informaticslab;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class S3RandomAccessFileTest {

    private static final String URL = "s3://mogreps-g/prods_op_mogreps-g_20160101_00_00_015.nc";

    private AmazonS3 client;
    private S3RandomAccessFile raf;
    private Map<String, byte[]> cache;
    private LinkedList<String> index;


    @Before
    public void setUp() throws IOException {
        client = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();
        cache = new HashMap<>();
        index = new LinkedList<>();
        raf = new S3RandomAccessFile(index, cache, client, URL);
    }

    @Test
    public void testLength() throws IOException {
        assertEquals("file length", 35948814, raf.length());
        raf.close();
    }

}