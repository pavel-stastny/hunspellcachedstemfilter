package org.apache.lucene.analysis.hunspell.cache.internal;

import org.apache.lucene.analysis.hunspell.cache.CacheItem;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class FirstLevel extends AbstractCacheMap {

    public static final Logger LOGGER = Logger.getLogger(FirstLevel.class.getName());

    public final long DEFAULT_SURVIVAL_INTERVAL = 1000 * 3600;

    long surviveInterval = DEFAULT_SURVIVAL_INTERVAL;

    int surviveThreshold = 25;

    Map<String, CacheItem> map =  new HashMap<>();
    SecondLevel eden = null;

    public FirstLevel() {
        super();
    }

    public boolean isPresent(String term) {
        return map.containsKey(term);
    }

    public SecondLevel getEden() {
        return eden;
    }

    public void setEden(SecondLevel eden) {
        this.eden = eden;
    }

    @Override
    protected Map<String, CacheItem> getMap() {
        return this.map;
    }


    void moveItemAsNecessary(String term, CacheItem item) {
        if (item.getCounter() >= surviveThreshold) {
            if (getEden() != null) {
                LOGGER.fine("Moving item to eden '"+term+"'");
                this.map.remove(term);
                getEden().pushItem(term, item);
            }
        }
    }

    @Override
    public void cleanCache() {
        LOGGER.fine("Cleaning cache L1");
        this.map.clear();
        this.map = new HashMap<>();
        this.lastCleaningTimestamp = System.currentTimeMillis();
    }

    @Override
    public long getSurviveInterval() {
        return this.surviveInterval;
    }

    @Override
    public void setSurviveInterval(long si) {
        this.surviveInterval = si;
    }

    public int getSurviveThreshold() {
        return surviveThreshold;
    }

    public void setSurviveThreshold(int surviveThreshold) {
        this.surviveThreshold = surviveThreshold;
    }

    public CacheItem getItem(String term) {
        CacheItem item = this.map.get(term);
        if (item != null) {
            item.increment();
            this.moveItemAsNecessary(term, item);
            return item;
        } else return null;
    }

    @Override
    public String toString() {
        return "FirstLevel{" +
                "map=" + map +
                '}';
    }
}
