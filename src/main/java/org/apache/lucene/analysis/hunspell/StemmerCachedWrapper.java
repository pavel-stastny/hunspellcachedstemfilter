package org.apache.lucene.analysis.hunspell;

import org.apache.lucene.util.CharsRef;

import java.io.IOException;
import java.util.*;

/**
 * This class encalupse standard stemmer and enhance it by cache functionality.
 */
public class StemmerCachedWrapper {

    static Map<String, Map<String, List<String>>> stemsCaches = Collections.synchronizedMap(new HashMap<>());
    static Map<String, Map<String, List<String>>> uniqueStemsCaches = Collections.synchronizedMap(new HashMap<>());

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

        Map<String, List<String>> analyzedStemCache = lookupStemCache();

        if (!analyzedStemCache.containsKey(chachedTerm)) {
            List<CharsRef> stem = stemmer.stem(word, length);
            cacheStem(analyzedStemCache, chachedTerm, stem);
            return stem;
        } else {
            List<String> strings = analyzedStemCache.get(chachedTerm);
            List<CharsRef> charsRefs = new ArrayList<>();
            strings.forEach(w -> {charsRefs.add(new CharsRef(w.toCharArray(),0, w.length())); });
            return charsRefs;
        }
    }

    private void cacheStem(Map<String, List<String>> analyzedStemCache, String chachedTerm, List<CharsRef> stem) {
        List<String> cloned = new ArrayList<>();
        stem.stream().forEach(charsRef -> {
            cloned.add(charsRef.toString());
        });
        analyzedStemCache.put(chachedTerm, cloned);
    }

    private void cacheUniqueStem( Map<String, List<String>> analyzedUniqueStemCache, String chachedTerm, List<CharsRef> stem) {
        List<String> cloned = new ArrayList<>();
        stem.stream().forEach(charsRef -> {
            cloned.add(charsRef.toString());
        });
        analyzedUniqueStemCache.put(chachedTerm, cloned);

    }


    public List<CharsRef> uniqueStems(char[] word, int length) {
        char[] nchars = new char[length];
        System.arraycopy(word, 0, nchars, 0, length);
        String chachedTerm = new String(nchars);
        Map<String, List<String>> analyzedUniqueStemCache = lookupUniqueStemCache();
        if (!analyzedUniqueStemCache.containsKey(chachedTerm)) {
            List<CharsRef> stem = stemmer.uniqueStems(word, length);
            cacheUniqueStem(analyzedUniqueStemCache, chachedTerm, stem);
            return stem;
        } else {
            List<String> strings = analyzedUniqueStemCache.get(chachedTerm);
            List<CharsRef> charsRefs = new ArrayList<>();
            strings.forEach(w -> {charsRefs.add(new CharsRef(w.toCharArray(),0, w.length())); });
            return charsRefs;
        }
    }

    synchronized  Map<String, List<String>> lookupStemCache() {
        Map<String, List<String>> cache = stemsCaches.get(this.dictionaryUniqIdent);
        if (cache == null) {
            stemsCaches.put(this.dictionaryUniqIdent, Collections.synchronizedMap(new HashMap<>()));
        }
        return stemsCaches.get(this.dictionaryUniqIdent);
    }

    synchronized  Map<String, List<String>> lookupUniqueStemCache() {
        Map<String, List<String>> cache = uniqueStemsCaches.get(this.dictionaryUniqIdent);
        if (cache == null) {
            uniqueStemsCaches.put(this.dictionaryUniqIdent, Collections.synchronizedMap(new HashMap<>()));
        }
        return uniqueStemsCaches.get(this.dictionaryUniqIdent);

    }

    public List<CharsRef> applyAffix(char[] strippedWord, int length, int affix, int prefixFlag, int recursionDepth, boolean prefix, boolean circumfix, boolean caseVariant) throws IOException {
        return stemmer.applyAffix(strippedWord, length, affix, prefixFlag, recursionDepth, prefix, circumfix, caseVariant);
    }
}