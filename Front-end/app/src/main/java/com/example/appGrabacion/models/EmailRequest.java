package com.example.appGrabacion.models;

public class EmailRequest {
    public String to, subject, text, html;
    public EmailRequest(String to, String subject, String text, String html){
        this.to = to; this.subject = subject; this.text = text; this.html = html;
    }
}