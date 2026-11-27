package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.softwaremagico.kt.core.statistics.AbstractFightStatistics;

import java.io.Serial;
import java.util.Objects;

public class ParticipantFightStatisticsDTO extends AbstractFightStatistics {

    @Serial
    private static final long serialVersionUID = 7868206856267549959L;

    @Override
    public Long getFusenGachiNumber() {
        return fusenGachiNumberValue();
    }

    @Override
    public Long getReceivedFusenGachiNumber() {
        return receivedFusenGachiNumberValue();
    }

    @Override
    public Long getReceivedFaults() {
        return receivedFaultsValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ParticipantFightStatisticsDTO that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return Objects.equals(getMenNumber(), that.getMenNumber())
                && Objects.equals(getKoteNumber(), that.getKoteNumber())
                && Objects.equals(getDoNumber(), that.getDoNumber())
                && Objects.equals(getTsukiNumber(), that.getTsukiNumber())
                && Objects.equals(getHansokuNumber(), that.getHansokuNumber())
                && Objects.equals(getIpponNumber(), that.getIpponNumber())
                && Objects.equals(getFusenGachiNumber(), that.getFusenGachiNumber())
                && Objects.equals(getReceivedMenNumber(), that.getReceivedMenNumber())
                && Objects.equals(getReceivedKoteNumber(), that.getReceivedKoteNumber())
                && Objects.equals(getReceivedDoNumber(), that.getReceivedDoNumber())
                && Objects.equals(getReceivedTsukiNumber(), that.getReceivedTsukiNumber())
                && Objects.equals(getReceivedHansokuNumber(), that.getReceivedHansokuNumber())
                && Objects.equals(getReceivedIpponNumber(), that.getReceivedIpponNumber())
                && Objects.equals(getReceivedFusenGachiNumber(), that.getReceivedFusenGachiNumber())
                && Objects.equals(getDuelsNumber(), that.getDuelsNumber())
                && Objects.equals(getAverageTime(), that.getAverageTime())
                && Objects.equals(getAverageWinTime(), that.getAverageWinTime())
                && Objects.equals(getAverageLostTime(), that.getAverageLostTime())
                && Objects.equals(getTotalDuelsTime(), that.getTotalDuelsTime())
                && Objects.equals(getFaults(), that.getFaults())
                && Objects.equals(getReceivedFaults(), that.getReceivedFaults())
                && Objects.equals(getQuickestHit(), that.getQuickestHit())
                && Objects.equals(getQuickestReceivedHit(), that.getQuickestReceivedHit())
                && Objects.equals(getWonDuels(), that.getWonDuels())
                && Objects.equals(getLostDuels(), that.getLostDuels())
                && Objects.equals(getDrawDuels(), that.getDrawDuels());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getMenNumber(), getKoteNumber(), getDoNumber(), getTsukiNumber(),
                getHansokuNumber(), getIpponNumber(), getFusenGachiNumber(), getReceivedMenNumber(),
                getReceivedKoteNumber(), getReceivedDoNumber(), getReceivedTsukiNumber(), getReceivedHansokuNumber(),
                getReceivedIpponNumber(), getReceivedFusenGachiNumber(), getDuelsNumber(), getAverageTime(),
                getAverageWinTime(), getAverageLostTime(), getTotalDuelsTime(), getFaults(), getReceivedFaults(),
                getQuickestHit(), getQuickestReceivedHit(), getWonDuels(), getLostDuels(), getDrawDuels());
    }
}
