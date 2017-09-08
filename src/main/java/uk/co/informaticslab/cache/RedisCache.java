package uk.co.informaticslab.cache;

import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.client.codec.JsonJacksonMapCodec;
import org.redisson.config.Config;
import uk.co.informaticslab.Constants;

public class RedisCache implements Cache<String, byte[]> {

    private RMapCache<String, byte[]> map;

    public RedisCache(String redisAddress) {
        Config config = new Config();

        // todo: work out how to properly connect to Redis cluster
        config.useSingleServer()
                .setAddress(redisAddress);
        RedissonClient redisson = Redisson.create(config);

        // todo: consider LocalCachedMap to reduce network trips
        map = redisson.getMapCache("opendap", new JsonJacksonMapCodec(String.class, byte[].class));
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public void put(String key, byte[] value) {
        map.fastPut(key, value);
        System.out.println(map.readAllValues());
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public byte[] get(String key) {
        return map.get(key);
    }

    @Override
    public Integer getMaxCacheSize() {
        return Constants.MEGABYTE * 1000; // redisson doesn't currently support setting max size
    }
}
