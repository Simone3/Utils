package com.utils.renamefiles;

import com.utils.common.files.FileInfos;
import com.utils.common.files.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RenameFilesFromList {

    public List<RenameResult> process(String sourceFileName, String filesFolderName, boolean justPreview) throws IllegalStateException {

        List<RenameResult> result = new ArrayList<>();
        Set<String> newFileNamesSet = new HashSet<>();

        // Source file and folder
        File sourceFile = FileUtils.getValidFile(sourceFileName);
        File filesFolder = FileUtils.getValidFolder(filesFolderName);
        final File[] files = filesFolder.listFiles();
        if(files == null) {

            throw new IllegalStateException(filesFolderName + " has null files list");
        }

        // Order files by name
        Arrays.sort(files);

        // Get new file names
        List<String> newFileNames;
        try {

            newFileNames = FileUtils.readLines(sourceFile);
        }
        catch(IOException e) {

            throw new IllegalStateException("Cannot read " + sourceFileName);
        }

        // Check sizes
        if(newFileNames.size() != files.length) {

            // Stop here if we are not previewing
            if(!justPreview) {

                throw new IllegalStateException("There are " + newFileNames.size() + " new file nemes but " + files.length + " files to rename");
            }
        }
        int maxSize = (newFileNames.size() > files.length ? newFileNames.size() : files.length);

        // Loop both lists
        for(int i = 0; i < maxSize; i++) {

            // Handle missing matches for preview
            if(i >= files.length) {

                result.add(RenameResult.invalidMissingFrom(newFileNames.get(i)));
                continue;
            }
            else if(i >= newFileNames.size()) {

                result.add(RenameResult.invalidMissingTo(files[i].getName()));
                continue;
            }

            // Old file data
            final File file = files[i];
            final String oldFileName = file.getName();
            final FileInfos fileInfos = new FileInfos(file);
            final String filePath = fileInfos.getPath();
            final String fileExtension = fileInfos.getExtension();

            // New file data
            final String newFileName = newFileNames.get(i) + "." + fileExtension;
            final String newFileFullName = filePath + File.separator + newFileName;

            // Check that we do not have special characters in the new file name
            if(!FileUtils.isFilenameValid(newFileFullName)) {

                String error = "Filename " + newFileName + " is not valid";
                result.add(RenameResult.invalid(oldFileName, newFileName, error));

                // Stop if we are not previewing
                if(!justPreview) {

                    throw new IllegalStateException(error);
                }
            }

            // Check that we do not have the same twice
            else if(!newFileNamesSet.add(newFileFullName)) {

                String error = "Filename " + newFileName + " is repeated";
                result.add(RenameResult.invalid(oldFileName, newFileName, error));

                // Stop if we are not previewing
                if(!justPreview) {

                    throw new IllegalStateException(error);
                }
            }

            // Otherwise the new file is OK
            else {

                result.add(RenameResult.valid(oldFileName, newFileName));
            }

            // Rename file if user confirmed
            if(!justPreview) {

                File newFile = new File(newFileFullName);

                boolean success = file.renameTo(newFile);

                if(!success) {

                    throw new IllegalStateException("Renaming for " + oldFileName + " failed");
                }
            }
        }

        return result;
    }
}
