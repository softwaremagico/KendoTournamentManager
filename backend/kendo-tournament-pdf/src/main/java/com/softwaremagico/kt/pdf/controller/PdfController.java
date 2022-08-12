package com.softwaremagico.kt.pdf.controller;

import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.score.ScoreOfCompetitor;
import com.softwaremagico.kt.pdf.CompetitorsScoreList;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Locale;

@Controller
public class PdfController {
    private final MessageSource messageSource;

    public PdfController(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public CompetitorsScoreList generateCompetitorsScoreList(Locale locale, TournamentDTO tournament, List<ScoreOfCompetitor> competitorTopTen) {
        return new CompetitorsScoreList(messageSource, Locale.getDefault(), tournament, competitorTopTen);
    }
}
