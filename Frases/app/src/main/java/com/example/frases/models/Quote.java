package com.example.frases.models;

public class Quote {
    private String author;
    private String quote;

    public Quote() {
    }

    public Quote(String author, String quote) {
        this.author = author;
        this.quote = quote;
    }

    public String getAuthor() { return author; }
    public String getQuote() { return quote; }
}
