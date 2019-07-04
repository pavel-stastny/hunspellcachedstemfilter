package org.apache.lucene.analysis.hunspell.cache;

/**
 * Implemenation of cache functionality
 */
public interface Cache {

    /**
     * Returns true if gien term is present in the cache
     * @param term
     * @return
     */
    boolean isPresent(String term);

    /**
     * Returns cached item
     * @param term
     * @return
     */
    CacheItem getItem(String term);

    /**
     * Push new item into cache
     * @param term
     * @param item
     */
    void pushItem(String term, CacheItem item);
}
