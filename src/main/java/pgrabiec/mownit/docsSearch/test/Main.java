package pgrabiec.mownit.docsSearch.test;


import pgrabiec.mownit.docsSearch.Article;
import pgrabiec.mownit.docsSearch.ArticlesIO;
import pgrabiec.mownit.docsSearch.ArticlesStatistics;

import java.io.File;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        ArticlesIO articlesIO = new ArticlesIO();

        ArticlesStatistics statistics = new ArticlesStatistics(
                articlesIO.loadArticles(new File("articles")),
                articlesIO.getSummary(),
                true,
                true
        );

        List<Article> searchResults = statistics.search(
                new String[] {
                        "0",
                        "1",
                        "2",
                        "3",
                        "999999999"
                },
                100
        );

        for (int i=0; i<searchResults.size(); i++) {
            System.out.println("(" + i + ") " + searchResults.get(i));
        }
    }

}
