package com.utils.common.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Some utils to process files
 */
public class FileUtils {

    private FileUtils() {

    }

    /**
     * Gets a file from a file name performing all checks (fileName is not empty, file exists, file is not a directory, etc.)
     * @param fileName the file name
     * @return the valid file
     * @throws IllegalStateException if something is wrong with the file
     */
    public static File getValidFile(String fileName) throws IllegalStateException {

        if(fileName == null || fileName.isEmpty()) {

           throw new IllegalStateException("Empty file name");
        }

        final File file = new File(fileName);
        if(!file.exists()) {

            throw new IllegalStateException("Cannot find " + fileName);
        }

        if(file.isDirectory()) {

            throw new IllegalStateException(fileName + " is not a file");
        }

        return file;
    }

    /**
     * Gets a folder from a folder name performing all checks (folderName is not empty, folder exists, folder is actually a folder, etc.)
     * @param folderName the folder name
     * @return the valid folder
     * @throws IllegalStateException if something is wrong with the folderName
     */
    public static File getValidFolder(String folderName) throws IllegalStateException {

        if(folderName == null || folderName.isEmpty()) {

            throw new IllegalStateException("Empty folder name");
        }

        final File directory = new File(folderName);
        if(!directory.exists()) {

            throw new IllegalStateException("Cannot find " + folderName);
        }

        if(!directory.isDirectory()) {

            throw new IllegalStateException(folderName + " is not a folder");
        }

        return directory;
    }

    /**
     * Reads a file into a list (each element is a file row(
     * @param file the file to read
     * @return the list of lines
     * @throws IOException if a read error occurs
     */
    public static List<String> readLines(File file) throws IOException {

        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = new ArrayList<>();
        String line;
        while((line = bufferedReader.readLine()) != null) {

            lines.add(line);
        }
        bufferedReader.close();
        return lines;
    }

    /**
     * Checks if a file name is valid
     * @param filename the file name
     * @return true if it's valid
     */
    public static boolean isFilenameValid(String filename) {

        try {

            File file = new File(filename);
            file.getCanonicalPath();
            return true;
        }
        catch(IOException e) {

            return false;
        }
    }
}
