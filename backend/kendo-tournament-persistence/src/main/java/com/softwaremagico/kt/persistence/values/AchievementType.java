package com.softwaremagico.kt.persistence.values;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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

public enum AchievementType {

    BILLY_THE_KID,

    LETHAL_WEAPON,

    TERMINATOR,

    JUGGERNAUT,

    THE_KING,

    LOOKS_GOOD_FROM_FAR_AWAY_BUT,

    I_LOVE_THE_FLAGS,

    THE_TOWER,

    THE_CASTLE,

    ENTRENCHED,

    A_LITTLE_OF_EVERYTHING,

    BONE_BREAKER,

    FLEXIBLE_AS_BAMBOO,

    SWEATY_TENUGUI,

    THE_WINNER,

    THE_WINNER_TEAM,

    WOODCUTTER,

    THE_NEVER_ENDING_STORY,

    LOVE_SHARING,

    MASTER_THE_LOOP,

    TIS_BUT_A_SCRATCH,

    FIRST_BLOOD,

    DARUMA,

    STORMTROOPER_SYNDROME,

    V_FOR_VENDETTA,

    SITH_APPRENTICES_ALWAYS_KILL_THEIR_MASTER,

    DETHRONE_THE_LING;


    public static AchievementType getType(String name) {
        for (final AchievementType type : AchievementType.values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
