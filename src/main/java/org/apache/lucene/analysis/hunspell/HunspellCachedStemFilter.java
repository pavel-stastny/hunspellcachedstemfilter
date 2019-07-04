package org.apache.lucene.analysis.hunspell;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.CharsRef;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Standard HunspelltemFilter enhanced by cahing possibility
 *
 * for original see {@link https://github.com/apache/lucene-solr/blob/branch_7x/lucene/analysis/common/src/java/org/apache/lucene/analysis/hunspell/HunspellStemFilter.java}
 */
public class HunspellCachedStemFilter extends TokenFilter  {

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);
    private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);
    private final StemmerCachedWrapper stemmer;

    private List<CharsRef> buffer;
    private State savedState;

    private final boolean dedup;
    private final boolean longestOnly;

    private final String dictionaryUniqueIdent;




    /** The same as HunspellStemFilter but enhanced by cache possibility - do not analyze the same term again
     *  @see #HunspellCachedStemFilter(TokenStream, Dictionary, boolean)
     *  @see HunspellStemFilter#HunspellStemFilter(TokenStream, Dictionary, boolean, boolean) */
    public HunspellCachedStemFilter(TokenStream input, Dictionary dictionary, String dictionaryUniqueIdent) {
        this(input, dictionary,dictionaryUniqueIdent, true);
    }

    /** The same as HunspellStemFilter but enhanced by cache possibility - do not analyze the same term again
     *  @see #HunspellCachedStemFilter(TokenStream, Dictionary, boolean, boolean)
     *  @see HunspellStemFilter#HunspellStemFilter(TokenStream, Dictionary, boolean, boolean) */
    public HunspellCachedStemFilter(TokenStream input, Dictionary dictionary, String dictionaryUniqueIdent, boolean dedup) {
        this(input, dictionary, dictionaryUniqueIdent, dedup, false);
    }

    /**
     * Creates a new HunspellStemFilter that will stem tokens from the given TokenStream using affix rules in the provided
     * Dictionary
     *
     * @param input TokenStream whose tokens will be stemmed
     * @param dictionary HunspellDictionary containing the affix rules and words that will be used to stem the tokens
     * @param longestOnly true if only the longest term should be output.
     */
    public HunspellCachedStemFilter(TokenStream input, Dictionary dictionary, String dictionaryUniqueIdent, boolean dedup, boolean longestOnly) {
        super(input);
        this.dedup = dedup && longestOnly == false; // don't waste time deduping if longestOnly is set
        this.stemmer = new StemmerCachedWrapper(new Stemmer(dictionary), dictionaryUniqueIdent);
        this.longestOnly = longestOnly;
        this.dictionaryUniqueIdent = dictionaryUniqueIdent;
    }



    @Override
    public boolean incrementToken() throws IOException {
        if (buffer != null && !buffer.isEmpty()) {
            CharsRef nextStem = buffer.remove(0);
            restoreState(savedState);
            posIncAtt.setPositionIncrement(0);
            termAtt.setEmpty().append(nextStem);
            return true;
        }

        if (!input.incrementToken()) {
            return false;
        }

        if (keywordAtt.isKeyword()) {
            return true;
        }

        buffer = dedup ? stemmer.uniqueStems(termAtt.buffer(), termAtt.length()) : stemmer.stem(termAtt.buffer(), termAtt.length());

        if (buffer.isEmpty()) { // we do not know this word, return it unchanged
            return true;
        }

        if (longestOnly && buffer.size() > 1) {
            Collections.sort(buffer, lengthComparator);
        }

        CharsRef stem = buffer.remove(0);
        termAtt.setEmpty().append(stem);

        if (longestOnly) {
            buffer.clear();
        } else {
            if (!buffer.isEmpty()) {
                savedState = captureState();
            }
        }

        return true;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        buffer = null;
    }

    static final Comparator<CharsRef> lengthComparator = new Comparator<CharsRef>() {
        @Override
        public int compare(CharsRef o1, CharsRef o2) {
            int cmp = Integer.compare(o2.length, o1.length);
            if (cmp == 0) {
                // tie break on text
                return o2.compareTo(o1);
            } else {
                return cmp;
            }
        }
    };

}
