package com.alimaddi.datatypes;

public enum ConnectionContentType
{
    JSON("application/json"),
    TEXT("text/plain");
//    SIT("https://sit.domain.com:2019/"),
//    CIT("https://cit.domain.com:8080/"),
//    DEV("https://dev.domain.com:21323/");

    private String contentType;

    ConnectionContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType()
    {
        return this.contentType;
    }
}
