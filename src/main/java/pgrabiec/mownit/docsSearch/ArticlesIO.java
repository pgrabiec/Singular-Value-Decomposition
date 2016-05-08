package pgrabiec.mownit.docsSearch;

import java.io.*;
import java.util.*;

public class ArticlesIO {
    private List<String> allWords = new ArrayList<String>();
    private List<Integer> allWordsCount = new ArrayList<Integer>();
    private Map<String, Integer> allWordsPositions = new HashMap<String, Integer>();

    private final List<Article> articles = new LinkedList<Article>();


    public List<Article> loadArticles(File articlesDirectory) {
        if (!articlesDirectory.isDirectory()) {
            throw new IllegalArgumentException("File " + articlesDirectory.getAbsolutePath() + " is not a directory");
        }

        File[] files = articlesDirectory.listFiles();
        if (files == null) {
            throw new IllegalArgumentException("Unable to get files in directory " + articlesDirectory.getAbsolutePath());
        }

        try {
            for (File file : files) {
                if (!file.isDirectory()) {
                    articles.add(
                            processFile(file)
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("An error occurred while processing articles files");
        }

        return articles;
    }

    private Article processFile(File file) throws IOException {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(
                    new FileReader(
                            file
                    )
            );

            List<String> words = new ArrayList<String>();
            List<Integer> wordsCount = new ArrayList<Integer>();
            Map<String, Integer> wordsPositions = new HashMap<String, Integer>();

            while (reader.ready()) {
                processLine(reader.readLine(), words, wordsCount, wordsPositions);
            }

            updateOverallWordsEntries(words, wordsCount);

            return parseArticleProcessingResults(words, wordsCount, file);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processLine(String line, List<String> words, List<Integer> wordsCount, Map<String, Integer> wordsPositions) {
        line = filter(line);

        String[] lineWords = line.split(" ");

        for (String word : lineWords) {
            processWord(word, words, wordsCount, wordsPositions);
        }
    }

    private void processWord(String word, List<String> words, List<Integer> wordsCount, Map<String, Integer> wordsPositions) {
        Integer index = wordsPositions.get(word);

        if (index == null) {
            index = words.size();
            words.add(index, word);
            wordsPositions.put(word, index);
            wordsCount.add(index, 1);
        } else {
            wordsCount.set(index, wordsCount.get(index) + 1);
        }
    }

    private String filter(String text) {
        char[] buf = new char[text.length()];

        int count = 0;
        char c;
        for (int i=0; i<text.length(); i++) {
            c = text.charAt(i);
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c <= '9' && c >= '0') || (c == ' ')) {
                buf[count] = c;
                count++;
            }
        }

        return new String(buf, 0, count);
    }

    private Article parseArticleProcessingResults(List<String> words, List<Integer> wordsCount, File file) {
        int size = words.size();

        String[] wordsVector = new String[size];
        int[] occurranceCount = new int[size];

        for (int i=0; i<size; i++) {
            wordsVector[i] = words.get(i);
            occurranceCount[i] = wordsCount.get(i);
        }

        return new Article(file, wordsVector, occurranceCount);
    }

    private void updateOverallWordsEntries(List<String> words, List<Integer> wordsCount) {
        int size = words.size();

        Integer index;
        String word;
        int count;
        for (int i=0; i<size; i++) {
            word = words.get(i);
            count = wordsCount.get(i);

            index = allWordsPositions.get(word);
            if (index == null) {
                index = allWords.size();
                allWords.add(index, word);
                allWordsCount.add(index, count);
                allWordsPositions.put(word, index);
            } else {
                allWordsCount.set(
                        index,
                        allWordsCount.get(index) + count
                );
            }

        }
    }

    public ArticlesSummary getSummary() {
        return new ArticlesSummary(
                allWords,
                allWordsCount,
                allWordsPositions
        );
    }
}
