package com.example.appGrabacion.models;

public class FileEntry {
    private final String name;
    private final String url;
    private final String created; // ISO date
    private final long   size;    // bytes

    public FileEntry(String name, String url, String created, long size) {
        this.name    = name;
        this.url     = url;
        this.created = created;
        this.size    = size;
    }

    public String getName()    { return name; }
    public String getUrl()     { return url; }
    public String getCreated() { return created; }
    public long   getSize()    { return size; }
}
