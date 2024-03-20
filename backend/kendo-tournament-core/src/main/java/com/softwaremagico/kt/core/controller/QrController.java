package com.softwaremagico.kt.core.controller;

import com.softwaremagico.kt.core.providers.QrProvider;
import org.springframework.stereotype.Controller;

@Controller
public class QrController {

    private final QrProvider qrProvider;

    public QrController(QrProvider qrProvider) {
        this.qrProvider = qrProvider;
    }


}
