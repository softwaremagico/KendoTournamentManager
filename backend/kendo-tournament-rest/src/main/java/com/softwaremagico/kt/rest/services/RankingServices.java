package com.softwaremagico.kt.rest.services;

import com.softwaremagico.kt.core.controller.RankingController;
import com.softwaremagico.kt.core.score.ScoreOfCompetitor;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/rankings")
public class RankingServices {

    private final RankingController rankingController;

    public RankingServices(RankingController rankingController) {
        this.rankingController = rankingController;
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets participants' ranking.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/competitors/group/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ScoreOfCompetitor> getCompetitorsScoreRankingGroup(@Parameter(description = "Id of an existing group", required = true)
                                                                   @PathVariable("groupId") Integer groupId,
                                                                   HttpServletRequest request) {
        return rankingController.getCompetitorsScoreRankingFromGroup(groupId);
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets participants' ranking.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/competitors/tournament/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ScoreOfCompetitor> getCompetitorsScoreRankingTournament(@Parameter(description = "Id of an existing tournament", required = true)
                                                                        @PathVariable("tournamentId") Integer tournamentId,
                                                                        HttpServletRequest request) {
        return rankingController.getCompetitorsScoreRankingFromTournament(tournamentId);
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets teams' ranking.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/teams/group/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ScoreOfTeam> getTeamsScoreRankingFromGroup(@Parameter(description = "Id of an existing group", required = true)
                                                           @PathVariable("groupId") Integer groupId,
                                                           HttpServletRequest request) {
        return rankingController.getTeamsScoreRankingFromGroup(groupId);
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @Operation(summary = "Gets teams' ranking.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/teams/tournament/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ScoreOfTeam> getTeamsScoreRankingFromTournament(@Parameter(description = "Id of an existing tournament", required = true)
                                                                @PathVariable("tournamentId") Integer tournamentId,
                                                                HttpServletRequest request) {
        return rankingController.getTeamsScoreRankingFromTournament(tournamentId);
    }
}
