package com.duck.owlcctv.model;


import android.graphics.Bitmap;

public class Recorded {
    private Bitmap thumb;
    private String name;
    private String path;
    private String readableFileSize;
    private long fileSize;
    private String lastModified;

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getReadableFileSize() {
        return readableFileSize;
    }

    public void setReadableFileSize(String readableFileSize) {
        this.readableFileSize = readableFileSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public Bitmap getThumb() {
        return thumb;
    }

    public void setThumb(Bitmap thumb) {
        this.thumb = thumb;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
