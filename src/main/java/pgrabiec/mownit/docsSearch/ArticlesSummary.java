package pgrabiec.mownit.docsSearch;

import java.util.List;
import java.util.Map;

public class ArticlesSummary {
    public final List<String> words;
    public final List<Integer> wordsCount;
    public final Map<String, Integer> wordsPositions;

    public ArticlesSummary(List<String> allWords, List<Integer> allWordsCount, Map<String, Integer> allWordsPositions) {
        this.words = allWords;
        this.wordsCount = allWordsCount;
        this.wordsPositions = allWordsPositions;
    }
}
