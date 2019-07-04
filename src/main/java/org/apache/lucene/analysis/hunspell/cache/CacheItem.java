package org.apache.lucene.analysis.hunspell.cache;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * One cache item
 */
public class CacheItem {

    private String term;
    private List<String> transformedTerms;
    private int counter = 0;

    /**
     * Term followed by translated terms from hunspell
     * @param term
     * @param transformedTerms
     */
    public CacheItem(String term, List<String> transformedTerms) {
        this.term = term;
        this.transformedTerms = transformedTerms;
    }

    /**
     * Term followed by translated terms from hunspell
     * @param term
     * @param transformedTerms
     */
    public CacheItem(String term, String ... transformedTerms) {
        this.term = term;
        this.transformedTerms = Arrays.asList(transformedTerms);
    }

    public int getCounter() {
        return this.counter;
    }

    public void increment() {
        this.counter += 1;
    }

    public List<String> getTransformedTerms() {
        return transformedTerms;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheItem cacheItem = (CacheItem) o;
        return Objects.equals(term, cacheItem.term) &&
                Objects.equals(transformedTerms, cacheItem.transformedTerms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(term, transformedTerms);
    }

    @Override
    public String toString() {
        return "CacheItem{" +
                "counter=" + counter +
                '}';
    }
}
