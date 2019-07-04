package org.apache.lucene.analysis.hunspell.cache.internal;

import org.apache.lucene.analysis.hunspell.cache.CacheItem;
import org.apache.lucene.analysis.hunspell.cache.internal.AbstractCacheMap;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SecondLevel extends AbstractCacheMap {

    public static final Logger LOGGER = Logger.getLogger(SecondLevel.class.getName());

    public final long DEFAULT_SURVIVAL_INTERVAL = 8 * 1000 * 3600;

    long surviveInterval = DEFAULT_SURVIVAL_INTERVAL;
    Map<String, CacheItem> map =  new HashMap<>();


    public SecondLevel() {
        super();
    }

    @Override
    public boolean isPresent(String term) {
        return this.map.containsKey(term);
    }

    @Override
    public CacheItem getItem(String term) {
        CacheItem item = this.map.get(term);
        if (item != null) {
            item.increment();
            return item;
        } else return null;
    }

    @Override
    public void cleanCache() {
        LOGGER.fine("Cleaning cache L2");
        this.map.clear();
        this.map = new HashMap<>();
        this.lastCleaningTimestamp = System.currentTimeMillis();
    }

    @Override
    public long getSurviveInterval() {
        return this.surviveInterval;
    }

    @Override
    public void setSurviveInterval(long surviveInterval) {
        this.surviveInterval = surviveInterval;
    }

    @Override
    protected Map<String, CacheItem> getMap() {
        return this.map;
    }

    @Override
    public String toString() {
        return "SecondLevel{" +
                "map=" + map +
                '}';
    }
}
