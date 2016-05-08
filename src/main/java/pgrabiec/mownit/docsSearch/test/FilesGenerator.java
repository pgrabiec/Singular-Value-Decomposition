package pgrabiec.mownit.docsSearch.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FilesGenerator {
    private static final int FILES_COUNT = 1000;
    private static final File DIRECTORY = new File("articles");
    private static final int WORDS_COUNT = 7;
    private static final int WORDS_SET_SIZE = 1000;

    public static void main(String[] args) {
        try {
            generateFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(FILES_COUNT + " files successfully generated");
    }

    private static void generateFiles() throws IOException {
        initDirectory();

        File file;
        BufferedWriter writer;

        for (int i = 0; i< FILES_COUNT; i++) {
            file = getFile(i);

            writer = new BufferedWriter(
                    new FileWriter(
                            file,
                            false
                    )
            );

            writer.write(getRandomDocument());

            writer.flush();
        }
    }

    private static void initDirectory() throws IOException {
        if (!DIRECTORY.exists()) {
            DIRECTORY.mkdir();
        } else if (!DIRECTORY.isDirectory()) {
            throw new IllegalStateException("Default storage DIRECTORY name conflicts with a file");
        } else {
            File[] files = DIRECTORY.listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                if (!f.isDirectory()) {
                    f.delete();
                }
            }
        }
    }

    private static File getFile(int i) {
        File file = new File(DIRECTORY.getName() + File.separator + "article" + i + ".txt");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    private static String getRandomDocument() {
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<WORDS_COUNT; i++) {
            int randomNumber = (int) (Math.random() * (WORDS_SET_SIZE + 1));

            builder.append(randomNumber);

            if (i < WORDS_COUNT-1) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }
}
