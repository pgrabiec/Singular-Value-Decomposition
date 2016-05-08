package pgrabiec.mownit.docsSearch;

import java.io.File;
import java.util.*;

public class Article {
    public final File file;
    public final String[] wordsVector;
    public final int[] wordsOccurrences;

    public Article(File file, String[] wordsVector, int[] wordsOccurrences) {
        this.file = file;
        this.wordsVector = wordsVector;
        this.wordsOccurrences = wordsOccurrences;
    }

    public Article(String[] words) {
        Map<String, Integer> wordsIndexes = new HashMap<String, Integer>();
        List<String> wordsVector = new ArrayList<String>();
        List<Integer> occurrences = new ArrayList<Integer>();

        Integer index;
        for (String word : words) {
            index = wordsIndexes.get(word);
            if (index == null) {
                index = wordsVector.size();

                wordsVector.add(index, word);
                occurrences.add(index, 1);

                wordsIndexes.put(word, index);
            } else {
                occurrences.set(
                        index,
                        occurrences.get(index) + 1
                );
            }
        }

        int size = wordsVector.size();

        this.file = null;
        this.wordsVector = new String[size];
        this.wordsOccurrences = new int[size];

        for (int i=0; i<size; i++) {
            this.wordsVector[i] = wordsVector.get(i);
            this.wordsOccurrences[i] = occurrences.get(i);
        }
    }

    @Override
    public String toString() {
        return "Article {" +
                "\n\tFile=" + file +
                "\n\twords=" + Arrays.toString(wordsVector) +
                "\n\toccurrences=" + Arrays.toString(wordsOccurrences) +
                "\n}";
    }
}
