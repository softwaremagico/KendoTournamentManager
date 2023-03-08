package com.softwaremagico.kt.core.converters;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.core.controller.models.FightStatisticsDTO;
import com.softwaremagico.kt.core.converters.models.FightStatisticsConverterRequest;
import com.softwaremagico.kt.core.statistics.FightStatistics;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class FightStatisticsConverter extends ElementConverter<FightStatistics, FightStatisticsDTO, FightStatisticsConverterRequest> {
    @Override
    protected FightStatisticsDTO convertElement(FightStatisticsConverterRequest from) {
        final FightStatisticsDTO fightStatisticsDTO = new FightStatisticsDTO();
        BeanUtils.copyProperties(from.getEntity(), fightStatisticsDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));
        return fightStatisticsDTO;
    }

    @Override
    public FightStatistics reverse(FightStatisticsDTO to) {
        if (to == null) {
            return null;
        }
        final FightStatistics fightStatistics = new FightStatistics();
        BeanUtils.copyProperties(to, fightStatistics, ConverterUtils.getNullPropertyNames(to));
        return fightStatistics;
    }
}
