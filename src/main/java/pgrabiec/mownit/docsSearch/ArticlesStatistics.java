package pgrabiec.mownit.docsSearch;

import org.jblas.DoubleMatrix;
import org.jblas.Singular;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ArticlesStatistics {
    private final List<Article> articles;
    private final ArticlesSummary summary;


    private DoubleMatrix matrix;

    private final int rows;
    private final int columns;

    public ArticlesStatistics(List<Article> articles, ArticlesSummary summary, boolean idf, boolean svdlra) {
        this.articles = articles;
        this.summary = summary;
        this.rows = summary.words.size();
        this.columns = articles.size();

        matrix = DoubleMatrix.zeros(rows, columns);

        System.out.println("Initializing matrix");
        initMatrix();
        System.out.println("Matrix: " + matrix.rows + " X " + matrix.columns);

        if (idf) {
            System.out.println("Executing inverse document frequency");
            executeInverseDocumentFrequency();
        }

        if (svdlra) {
            System.out.println("Reducing sough");
            reduceSough();
        }
    }

    private void reduceSough() {
        System.out.println("Normalizing vectors");
        normalizePropertiesVectors();

        System.out.println("Executing SVD");
        DoubleMatrix[] svd = Singular.sparseSVD(matrix);
        DoubleMatrix u, eig, v;
        u = svd[0];
        eig = svd[1];
        v = svd[2];

        DoubleMatrix d = new DoubleMatrix(eig.length, eig.length);
        d.fill(0);
        for (int i=0; i<eig.length; i++) {
            d.put(i, i, eig.get(i));
        }


        System.out.println("Processing SVD results");
        matrix = u.mmul(d).mmul(v.transpose());
    }

    private void normalizePropertiesVectors() {
        DoubleMatrix row;
        for (int i=0; i<rows; i++) {
            row = matrix.getRow(i);

            double rowSum = row.sum();

            row = row.mul(1.0/rowSum);

            matrix.putRow(i, row);
        }
    }

    private void initMatrix() {
        Article article;
        for (int i=0; i<columns; i++) {
            article = articles.get(i);

            String word;
            int count;
            Integer index;
            for (int p=0; p<article.wordsVector.length; p++) {
                word = article.wordsVector[p];
                count = article.wordsOccurrences[p];
                index = summary.wordsPositions.get(word);
                if (index == null) {
                    throw new RuntimeException("An article " +
                            article.file.getAbsolutePath() +
                            " contains unmapped word"
                    );
                }

                matrix.put(index, i, (double) count);
            }
        }
    }

    private void executeInverseDocumentFrequency() {
        int n = columns;
        int count;
        double coefficient;
        for (int row=0; row<rows; row++) {
            count = 0;

            for (int i=0; i<columns; i++) {
                if (matrix.get(row, i) > 0) {
                    count++;
                }
            }

            if (count == 0) {
                throw new IllegalStateException("No articles with the mapped word: " + summary.words.get(row));
            }

            coefficient = Math.log(n/count);

            if (coefficient == 0.0) {
                coefficient = 1.0;
            }

            for (int i=0; i<columns; i++) {
                matrix.put(
                        row,
                        i,
                        matrix.get(row, i) * coefficient
                );
            }
        }
    }

    public List<Article> search(String[] words, int resultCount) {
        System.out.println("Searching for " + resultCount + " best matches");

        if (resultCount >= columns) {
            return articles;
        }

        System.out.println("Getting search vector");
        DoubleMatrix searchVector = getSearchVector(words);

        System.out.println("Resolving matching rates");
        double[] matchRates = resolveMatchingRates(searchVector);

        System.out.println("Resolving best documents indexes");
        int[] indexes = getIndexesWithGreatestValue(matchRates, resultCount);

        List<Article> result = new ArrayList<Article>(indexes.length);

        for (int i=0; i<resultCount; i++) {
            result.add(articles.get(indexes[i]));
        }

        return result;
    }

    private int[] getIndexesWithGreatestValue(double[] matchRates, int resultCount) {
        System.out.println(Arrays.toString(matchRates));

        int size = matchRates.length;

        Element[] elements = new Element[size];
        for (int i=0; i<size; i++) {
            elements[i] = new Element(i, matchRates[i]);
        }
        Arrays.sort(elements, new Comparator<Element>() {
            public int compare(Element o1, Element o2) {
                if (o1.value < o2.value) {
                    return -1;
                }

                if (o1.value == o2.value) {
                    return 0;
                }

                return 1;
            }
        });



        int[] indexes = new int[resultCount];

        for (int i=0; i<resultCount; i++) {
            indexes[i] = elements[size-1-i].index;
        }

        return indexes;
    }

    private double[] resolveMatchingRates(DoubleMatrix searchVector) {
        double[] matchRates = new double[columns];

        double searchVectorNorm = searchVector.norm2();
        if (searchVectorNorm == 0.0) {
            System.out.println("Empty query results");
            Arrays.fill(matchRates, 1.0);
            return matchRates;
        }

        DoubleMatrix searchVectorTransposed = searchVector.transpose();
        DoubleMatrix columnVector;
        DoubleMatrix numerator;
        for (int column=0; column<columns; column++) {
            columnVector = matrix.getColumn(column);

            numerator = searchVectorTransposed.mmul(columnVector);


            if (numerator.rows != 1 || numerator.columns != 1) {
                throw new IllegalStateException("Numerator invalid multiplication result" +
                        "(length=" + numerator.getLength() + ")");
            }

            matchRates[column] = (numerator.get(0, 0));
        }

        return matchRates;
    }

    private DoubleMatrix getSearchVector(String[] words) {
        Article searchPattern = new Article(words);

        DoubleMatrix result = DoubleMatrix.zeros(rows, 1);

        Integer index;
        int count;
        String word;
        for (int i=0; i<searchPattern.wordsVector.length; i++) {
            word = searchPattern.wordsVector[i];
            count = searchPattern.wordsOccurrences[i];

            index = summary.wordsPositions.get(word);
            if (index != null) {
                result.put(index, 0, count);
            }
        }

        return result;
    }

    private class Element {
        public int index;
        public double value;

        public Element(int index, double value) {
            this.index = index;
            this.value = value;
        }
    }
}
