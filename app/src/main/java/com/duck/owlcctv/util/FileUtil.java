package com.duck.owlcctv.util;


import java.io.File;

public class FileUtil {
    private static final String TAG = "[FileUtil]";

    public static long getFileSize(File file) {
        try {
            if (file == null)
                throw new NullPointerException("file is null");
            long fileSize = 0;
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    fileSize += getFileSize(f);
                }
            } else {
                fileSize = file.length();
            }
            return fileSize;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getReadableFileSize(File file) {
        long fileSize = getFileSize(file) / 1024;
        String size;
        if (fileSize >= 1024) {
            size = ((fileSize / 1024) + " MB");
        } else {
            size = fileSize + " KB";
        }
        return size;
    }
}
