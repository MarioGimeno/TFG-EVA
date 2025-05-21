// com/example/appGrabacion/models/FilesResponse.java
package com.example.appGrabacion.entities;

import java.util.List;

public class FilesResponse {
    private List<FileEntry> files;

    public List<FileEntry> getFiles() {
        return files;
    }

    public void setFiles(List<FileEntry> files) {
        this.files = files;
    }
}
