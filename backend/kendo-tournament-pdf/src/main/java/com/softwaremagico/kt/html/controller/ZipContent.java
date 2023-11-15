package com.softwaremagico.kt.html.controller;

public class ZipContent {
    private final String name;
    private final String extension;

    private final byte[] content;

    ZipContent(String name, String extension, byte[] content) {
        this.name = name;
        this.extension = extension;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }

    public byte[] getContent() {
        return content;
    }
}
