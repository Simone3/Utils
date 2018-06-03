package com.utils.renamefiles;

public class RenameResult {

    private static final String MISSING = "-";
    private static final String MISSING_MESSAGE = "Match not found";

    private String fromFile;
    private String toFile;
    private boolean isValid;
    private String message;

    private RenameResult(boolean isValid, String fromFile, String toFile, String message) {

        this.isValid = isValid;
        this.fromFile = fromFile;
        this.toFile = toFile;
        this.message = message;
    }

    static RenameResult valid(String fromFile, String toFile) {

        return new RenameResult(true, fromFile, toFile, null);
    }

    static RenameResult invalidMissingTo(String fromFile) {

        return new RenameResult(false, fromFile, MISSING, MISSING_MESSAGE);
    }

    static RenameResult invalidMissingFrom(String toFile) {

        return new RenameResult(false, MISSING, toFile, MISSING_MESSAGE);
    }

    static RenameResult invalid(String fromFile, String toFile, String error) {

        return new RenameResult(false, fromFile, toFile, error);
    }

    public boolean isValid() {

        return isValid;
    }

    public String getFromFile() {

        return fromFile;
    }

    public String getToFile() {

        return toFile;
    }

    public String getMessage() {

        return message;
    }

    @Override
    public String toString() {

        return "RenameResult{" +
                "fromFile='" + fromFile + '\'' +
                ", toFile='" + toFile + '\'' +
                ", isValid=" + isValid +
                ", message='" + message + '\'' +
                '}';
    }
}
