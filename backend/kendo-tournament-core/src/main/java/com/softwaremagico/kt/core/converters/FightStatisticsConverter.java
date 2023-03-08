package com.softwaremagico.kt.core.converters;

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
