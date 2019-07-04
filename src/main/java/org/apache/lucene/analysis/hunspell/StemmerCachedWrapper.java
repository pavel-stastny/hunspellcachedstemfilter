package org.apache.lucene.analysis.hunspell;

import org.apache.lucene.analysis.hunspell.cache.CacheItem;
import org.apache.lucene.analysis.hunspell.cache.CacheMap;
import org.apache.lucene.util.CharsRef;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * This class encalupse standard stemmer and enhance it by cache functionality.
 */
public class StemmerCachedWrapper {

    public static final String PROPERTY_SURVIVE_L1="surviveL1";
    public static final String PROPERTY_SURVIVE_L2="surviveL2";
    public static final String PROPERTY_L1_L2_THRESHOLD="l1L2";

    public static final Logger LOGGER  = Logger.getLogger(StemmerCachedWrapper.class.getName());

    static Map<String, CacheMap> stemsCaches = Collections.synchronizedMap(new HashMap<>());
    static Map<String, CacheMap> uniqueStemsCaches = Collections.synchronizedMap(new HashMap<>());

    // internal isntance of stemmer
    private Stemmer stemmer;
    private String dictionaryUniqIdent;



    public StemmerCachedWrapper(Stemmer stemmer, String dictionaryUniqIdent) {
        this.stemmer = stemmer;
        this.dictionaryUniqIdent = dictionaryUniqIdent;
    }


    // look in the cache first
    public List<CharsRef> stem(String word) {
        return stemmer.stem(word);
    }



    //
    public List<CharsRef> stem(char[] word, int length) {
        char[] nchars = new char[length];
        System.arraycopy(word, 0, nchars, 0, length);
        String chachedTerm = new String(nchars);

        CacheMap analyzedStemCache = lookupStemCache();

        if (!analyzedStemCache.isPresent(chachedTerm)) {
            List<CharsRef> stem = stemmer.stem(word, length);
            cacheStem(analyzedStemCache, chachedTerm, stem);
            return stem;
        } else {
            CacheItem item = analyzedStemCache.getItem(chachedTerm);
            List<String> strings = item.getTransformedTerms();
            List<CharsRef> charsRefs = new ArrayList<>();
            strings.forEach(w -> {charsRefs.add(new CharsRef(w.toCharArray(),0, w.length())); });
            return charsRefs;
        }
    }

    private void cacheStem(CacheMap analyzedStemCache, String chachedTerm, List<CharsRef> stem) {
        List<String> cloned = new ArrayList<>();
        stem.stream().forEach(charsRef -> {
            cloned.add(charsRef.toString());
        });
        CacheItem item = new CacheItem(chachedTerm, cloned);
        analyzedStemCache.pushItem(chachedTerm, item);
    }

    private void cacheUniqueStem( CacheMap analyzedUniqueStemCache, String chachedTerm, List<CharsRef> stem) {
        List<String> cloned = new ArrayList<>();
        stem.stream().forEach(charsRef -> {
            cloned.add(charsRef.toString());
        });
        CacheItem item = new CacheItem(chachedTerm, cloned);
        analyzedUniqueStemCache.pushItem(chachedTerm, item);

    }


    public List<CharsRef> uniqueStems(char[] word, int length) {
        char[] nchars = new char[length];
        System.arraycopy(word, 0, nchars, 0, length);
        String chachedTerm = new String(nchars);
        CacheMap analyzedUniqueStemCache = lookupUniqueStemCache();

        if (!analyzedUniqueStemCache.isPresent(chachedTerm)) {
            List<CharsRef> stem = stemmer.uniqueStems(word, length);
            cacheUniqueStem(analyzedUniqueStemCache, chachedTerm, stem);
            return stem;
        } else {
            CacheItem item = analyzedUniqueStemCache.getItem(chachedTerm);
            List<String> strings = item.getTransformedTerms();
            List<CharsRef> charsRefs = new ArrayList<>();
            strings.forEach(w -> {charsRefs.add(new CharsRef(w.toCharArray(),0, w.length())); });
            return charsRefs;
        }
    }

    synchronized  CacheMap lookupStemCache() {
        CacheMap cache = stemsCaches.get(this.dictionaryUniqIdent);
        if (cache == null) {
            stemsCaches.put(this.dictionaryUniqIdent, configureCacheMap());
        }
        return stemsCaches.get(this.dictionaryUniqIdent);
    }

    private CacheMap configureCacheMap() {
        CacheMap cacheMap = new CacheMap();
        String surviveIntervalL1 = System.getProperty(PROPERTY_SURVIVE_L1, "28800000");
        String surviveIntervalL2 = System.getProperty(PROPERTY_SURVIVE_L2, "3600000");
        String l1L2Threshold = System.getProperty(PROPERTY_L1_L2_THRESHOLD, "40");
        LOGGER.info(" surviveIntervalL1 "+surviveIntervalL1);
        LOGGER.info(" surviveIntervalL2 "+surviveIntervalL2);
        LOGGER.info(" l1L2Threshold "+l1L2Threshold);

        cacheMap.getFirstLevel().setSurviveThreshold(Integer.parseInt(l1L2Threshold));
        cacheMap.getFirstLevel().setSurviveInterval(Long.parseLong(surviveIntervalL1));
        cacheMap.getSecondLevel().setSurviveInterval(Long.parseLong(surviveIntervalL2));
        return cacheMap;
    }

    synchronized  CacheMap lookupUniqueStemCache() {
        CacheMap cache = uniqueStemsCaches.get(this.dictionaryUniqIdent);
        if (cache == null) {
            uniqueStemsCaches.put(this.dictionaryUniqIdent, configureCacheMap());
        }
        return uniqueStemsCaches.get(this.dictionaryUniqIdent);

    }

    public List<CharsRef> applyAffix(char[] strippedWord, int length, int affix, int prefixFlag, int recursionDepth, boolean prefix, boolean circumfix, boolean caseVariant) throws IOException {
        return stemmer.applyAffix(strippedWord, length, affix, prefixFlag, recursionDepth, prefix, circumfix, caseVariant);
    }
}