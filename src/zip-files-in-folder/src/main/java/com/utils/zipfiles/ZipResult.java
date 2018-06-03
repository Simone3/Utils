package com.utils.zipfiles;

import java.util.ArrayList;
import java.util.List;

public class ZipResult {

    private static final String SEPARATOR = ", ";

    private String zipName;
    private List<String> containedFiles;

    public ZipResult(String zipName) {

        this.zipName = zipName;
        containedFiles = new ArrayList<>();
    }

    public String getZipName() {

        return zipName;
    }

    public List<String> getContainedFiles() {

        return containedFiles;
    }

    public String getContainedFilesString() {

        StringBuilder builder = new StringBuilder();
        for(String file: containedFiles) {

            builder.append(file).append(SEPARATOR);
        }

        return builder.length() > 0 ? builder.substring(0, builder.length() - SEPARATOR.length()) : "";
    }

    @Override
    public String toString() {
        return "ZipResult{" +
                "zipName='" + zipName + '\'' +
                ", containedFiles=" + containedFiles +
                '}';
    }
}
