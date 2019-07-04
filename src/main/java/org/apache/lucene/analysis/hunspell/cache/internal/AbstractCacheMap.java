package org.apache.lucene.analysis.hunspell.cache.internal;

import org.apache.lucene.analysis.hunspell.cache.Cache;
import org.apache.lucene.analysis.hunspell.cache.CacheItem;

import java.util.Map;
import java.util.logging.Logger;

public abstract  class AbstractCacheMap implements Cache {

    public static final Logger LOGGER = Logger.getLogger(AbstractCacheMap.class.getName());

    long lastCleaningTimestamp;

    public AbstractCacheMap() {
        lastCleaningTimestamp = System.currentTimeMillis();
    }

    public long getLastCleaningTimestamp() {
        return lastCleaningTimestamp;
    }

    public abstract void cleanCache();

    public abstract  long getSurviveInterval();

    public abstract void setSurviveInterval(long si);

    protected abstract Map<String, CacheItem> getMap();

    @Override
    public void pushItem(String term, CacheItem item) {
        this.triggerCleanCacheIfNecessary();
        getMap().put(term, item);
    }

    public void triggerCleanCacheIfNecessary() {
        if (getSurviveInterval() != -1) {
            long diff = System.currentTimeMillis() - getLastCleaningTimestamp();
            if (diff >= getSurviveInterval()) {
                LOGGER.fine("Diff  "+diff+" is more then predefined interval "+getSurviveInterval());
                this.cleanCache();
            }
        }
    }
}
