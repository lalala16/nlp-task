package cn.edu.pku.sei.sc.nlp.task;

import opennlp.tools.util.model.BaseModel;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Icing on 2017/9/27.
 */
public class Utils {

    public static <T extends BaseModel> T loadModel(Loader<InputStream, T> loader, String modelFileName) {
         try (InputStream modelIn = new FileInputStream(modelFileName)) {
             return loader.load(modelIn);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
    }

    public static final DirectoryFileFilter DIRECTORY_FILE_FILTER = new DirectoryFileFilter();

    public static final TxtFileFilter TXT_FILE_FILTER = new TxtFileFilter();

    public static class ExtensionFileFilter implements FileFilter {
        private String extension;

        public ExtensionFileFilter(String extension) {
            this.extension = extension;
        }

        @Override
        public boolean accept(File file) {
            return file.getName().toUpperCase().endsWith(extension.toUpperCase());
        }
    }

    public static class DirectoryFileFilter implements FileFilter {

        @Override
        public boolean accept(File file) {
            return file.isDirectory();
        }
    }

    public static class TxtFileFilter extends ExtensionFileFilter {
        public TxtFileFilter() {
            super(".txt");
        }
    }

    public static List<File> scanFileRecursively(File rootDirectory, FileFilter fileFilter) {
        File[] dirs = rootDirectory.listFiles(DIRECTORY_FILE_FILTER);
        File[] files = rootDirectory.listFiles(fileFilter);

        List<File> result = new ArrayList<>();
        if (files != null)
            result.addAll(Arrays.asList(files));
        if (dirs != null) {
            for (File dir : dirs) {
                result.addAll(scanFileRecursively(dir, fileFilter));
            }
        }
        return result;
    }


}
