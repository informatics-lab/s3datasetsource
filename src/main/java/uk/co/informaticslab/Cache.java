package uk.co.informaticslab;

public interface Cache<K, V> {

    /**
     * Checks if the cache contains key specified.
     * @param key
     * @return true if cache contains key, otherwise false
     */
    boolean containsKey(K key);

    /**
     * Inserts a key and associated value into the cache.
     * @param key
     * @param value
     */
    void put(K key, V value);

    /**
     * Gets the current number of elements in the cache.
     * @return number of elements in the cache
     */
    int size();

    /**
     * Gets a value from the cache specified by the given key.
     * @param key
     * @return value associated with given key
     */
    V get(K key);

    /**
     * Gets the maximum number of bytes that can be in the cache at any given moment.
     * @return maximum cache size
     */
    int getMaxCacheSize();

}
