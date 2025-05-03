package com.example.appGrabacion.models;

// com/example/appGrabacion/models/LocationUpdateRequest.java

import java.util.List;

// com/example/appGrabacion/models/LocationUpdateRequest.java
public class LocationUpdateRequest {
    // renómbralo de tokens a recipientIds
    private List<Integer> recipientIds;
    private double latitude;
    private double longitude;

    public LocationUpdateRequest(List<Integer> recipientIds,
                                 double latitude,
                                 double longitude) {
        this.recipientIds = recipientIds;
        this.latitude     = latitude;
        this.longitude    = longitude;
    }
    // getters / setters si usas Gson o Moshi
}
