package model;

public class Match {
    private String fileName;
    private long timeIndex;

    public Match(String fileName, long timeIndex) {
        this.fileName = fileName;
        this.timeIndex = timeIndex;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getTimeIndex() {
        return timeIndex;
    }

    public void setTimeIndex(long timeIndex) {
        this.timeIndex = timeIndex;
    }
}
