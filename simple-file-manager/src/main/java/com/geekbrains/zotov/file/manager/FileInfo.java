package com.geekbrains.zotov.file.manager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInfo {
    public static final String UP_TOKEN="..";
    private String fileName;
    public long length;

    public FileInfo(String fileName, long length) {
        this.fileName = fileName;
        this.length = length;
    }

    public FileInfo(Path path) {

        try {
            this.fileName = path.getFileName().toString();
            if (Files.isDirectory(path)) {
                this.length = -1L;
            } else {
                this.length = Files.size(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("×עמ עמ םו עמ ס פאיכמל" + path.toAbsolutePath().toString());
        }
    }

    public boolean isDirectory() {
        return length == -1L;
    }
    public boolean isUpElement(){
        return length==-2L;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
}
