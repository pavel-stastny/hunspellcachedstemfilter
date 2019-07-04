package org.apache.lucene.analysis.hunspell.cache;

import org.apache.lucene.analysis.hunspell.cache.internal.FirstLevel;
import org.apache.lucene.analysis.hunspell.cache.internal.SecondLevel;

import java.util.logging.Logger;


public class CacheMap implements Cache {

    public static final Logger LOGGER = Logger.getLogger(CacheMap.class.getName());

    private FirstLevel firstLevel;
    private SecondLevel secondLevel;

    public CacheMap() {
        this.firstLevel = new FirstLevel();
        this.secondLevel = new SecondLevel();
        this.firstLevel.setEden(secondLevel);
    }

    public FirstLevel getFirstLevel() {
        return firstLevel;
    }

    public SecondLevel getSecondLevel() {
        return secondLevel;
    }

    @Override
    public boolean isPresent(String term) {
        return this.firstLevel.isPresent(term) || this.secondLevel.isPresent(term);
    }

    @Override
    public CacheItem getItem(String term) {

        if (this.firstLevel.isPresent(term)) {
            CacheItem item = this.firstLevel.getItem(term);
            return item;
        } else if (this.secondLevel.isPresent(term)) {
            CacheItem item = this.secondLevel.getItem(term);
            return item;
        }


        return null;
    }

    @Override
    public void pushItem(String term, CacheItem item) {
        this.firstLevel.pushItem(term, item);
    }
}
