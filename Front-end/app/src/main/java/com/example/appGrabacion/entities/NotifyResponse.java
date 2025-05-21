package com.example.appGrabacion.entities;

public class NotifyResponse {
    private int sentTo;
    private String error;

    public int getSentTo() {
        return sentTo;
    }

    public void setSentTo(int sentTo) {
        this.sentTo = sentTo;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}