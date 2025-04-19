package com.example.appGrabacion.models;

import com.google.gson.annotations.SerializedName;

public class FileEntry {
    @SerializedName("name")
    private String name;

    @SerializedName("url")
    private String signedUrl;

    // getters
    public String getName() { return name; }
    public String getSignedUrl() { return signedUrl; }
}
