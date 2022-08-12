package com.softwaremagico.kt.pdf;

public class InvalidXmlElementException extends Exception {
    private static final long serialVersionUID = -6961127287958139460L;

    public InvalidXmlElementException(String message) {
        super(message);
    }

    public InvalidXmlElementException(String message, Exception e) {
        super(message, e);
    }
}
