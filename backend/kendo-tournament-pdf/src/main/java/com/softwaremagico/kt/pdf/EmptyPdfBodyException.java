package com.softwaremagico.kt.pdf;

public class EmptyPdfBodyException extends Exception {
    private static final long serialVersionUID = -8207546492378482320L;

    public EmptyPdfBodyException(String text) {
        super(text);
    }
}
