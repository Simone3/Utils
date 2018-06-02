package com.utils.common.files;

import java.io.File;

/**
 * A descriptor that contains data about a file path and name
 */
public class FileInfos {

    private String fullFileName;
    private String path;
    private String fileName;
    private String baseName;
    private String extension;

    /**
     * Constructor
     * @param file the non-null file
     */
    public FileInfos(final File file) {

        this.fullFileName = file.getAbsolutePath();

        int pathPos = this.fullFileName.lastIndexOf(File.separator);
        if(pathPos > 0) {

            this.path = this.fullFileName.substring(0, pathPos);
            this.fileName = this.fullFileName.substring(pathPos + 1, this.fullFileName.length());

            int extensionPos = this.fileName.lastIndexOf(".");
            if(extensionPos > 0) {

                this.baseName = this.fileName.substring(0, extensionPos);
                this.extension = this.fileName.substring(extensionPos + 1, this.fileName.length());
            }
        }
    }

    /**
     * Getter
     * @return e.g. File("C:\Programs\MyProgram\program.exe") ---> C:\Programs\MyProgram\program.exe
     */
    public String getFullFileName() {

        return fullFileName;
    }

    /**
     * Getter
     * @return e.g. File("C:\Programs\MyProgram\program.exe") ---> C:\Programs\MyProgram\
     */
    public String getPath() {

        return path;
    }

    /**
     * Getter
     * @return e.g. File("C:\Programs\MyProgram\program.exe") ---> program.exe
     */
    public String getFileName() {

        return fileName;
    }

    /**
     * Getter
     * @return e.g. File("C:\Programs\MyProgram\program.exe") ---> program
     */
    public String getBaseName() {

        return baseName;
    }

    /**
     * Getter
     * @return e.g. File("C:\Programs\MyProgram\program.exe") ---> exe
     */
    public String getExtension() {

        return extension;
    }
}