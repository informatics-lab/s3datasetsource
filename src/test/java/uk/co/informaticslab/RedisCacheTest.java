package uk.co.informaticslab;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.embedded.RedisServer;
import uk.co.informaticslab.cache.RedisCache;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RedisCacheTest {

    private RedisServer redisServer;
    private RedisCache redisCache;
    private String redisAddress;

    @Before
    public void setUp() throws IOException {
        redisServer = new RedisServer(6379);
        redisServer.start();

        redisAddress = "redis://127.0.0.1:6379";
        redisCache = new RedisCache(redisAddress);
    }

    @After
    public void tearDown() {
        redisServer.stop();
    }

    @Test
    public void testCache() {
        byte[] data = "testing".getBytes();
        String key = "test";
        redisCache.put(key, data);

        assertEquals(true, redisCache.containsKey(key));

        byte[] result = redisCache.get(key);
        assertTrue(Arrays.equals(data, result));
    }

    @Test
    public void testPersistence() {
        byte[] data = "testing".getBytes();
        String key = "test";
        redisCache.put(key, data);

        RedisCache newCache = new RedisCache(redisAddress);
        assertEquals(true, newCache.containsKey(key));

        String newKey = "test2";
        newCache.put(newKey, data);
        assertEquals(true, redisCache.containsKey(newKey));
    }

}