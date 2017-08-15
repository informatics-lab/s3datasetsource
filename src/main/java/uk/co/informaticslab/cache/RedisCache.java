package uk.co.informaticslab.cache;

public class RedisCache implements Cache<String, byte[]> {

    @Override
    public boolean containsKey(String key) {
        return false;
    }

    @Override
    public void put(String key, byte[] value) {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public byte[] get(String key) {
        return new byte[0];
    }

    @Override
    public Integer getMaxCacheSize() {
        return null;
    }
}
