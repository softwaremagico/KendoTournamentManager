package com.softwaremagico.kt.html.controller;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.RankingController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.html.lists.BlogExporter;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
public class HtmlController {

    private final MessageSource messageSource;

    private final RoleController roleController;

    private final GroupController groupController;

    private final RankingController rankingController;

    public HtmlController(MessageSource messageSource, RoleController roleController, GroupController groupController, RankingController rankingController) {
        this.messageSource = messageSource;
        this.roleController = roleController;
        this.groupController = groupController;
        this.rankingController = rankingController;
    }

    public BlogExporter generateBlogCode(Locale locale, TournamentDTO tournament) {
        final List<RoleDTO> roleDTOS = roleController.get(tournament);
        return new BlogExporter(messageSource, locale, tournament, roleDTOS, groupController.get(tournament),
                roleDTOS.stream().map(RoleDTO::getParticipant).collect(Collectors.toList()), rankingController.getTeamsScoreRanking(tournament),
                rankingController.getCompetitorsScoreRanking(tournament));

    }
}
