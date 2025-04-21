package com.example.appGrabacion.models;

public class FileEntry {
    private final String name;
    private final String url;

    public FileEntry(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
