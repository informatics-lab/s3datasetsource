package uk.co.informaticslab;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class LocalCache implements Cache<String, byte[]> {

    public static final int DEFAULT_MAX_CACHE_SIZE = Constants.MEGABYTE * 55;

    private final int maxCacheSize;

    private Map<String, byte[]> map = new HashMap<>();
    private LinkedList<String> index = new LinkedList<>();
    private int currentCacheSize = 0;

    public LocalCache() {
        this(DEFAULT_MAX_CACHE_SIZE);
    }

    public LocalCache(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public void put(String key, byte[] value) {
        map.put(key, value);
        index.add(key);
        assert (map.size() == index.size());
        currentCacheSize = currentCacheSize + value.length;

        while(currentCacheSize > maxCacheSize) {
            String k = index.removeFirst();
            byte[] v = map.remove(k);
            currentCacheSize = currentCacheSize - v.length;
        }
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
    public int getMaxCacheSize() {
        return maxCacheSize;
    }
}
