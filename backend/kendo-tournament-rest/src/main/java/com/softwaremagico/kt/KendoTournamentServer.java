package com.softwaremagico.kt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

@SpringBootApplication
@Service
public class KendoTournamentServer {

    public static void main(String[] args) {
        SpringApplication.run(KendoTournamentServer.class, args);
    }

}
