package com.softwaremagico.kt.websockets;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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

import com.softwaremagico.kt.core.controller.DuelController;
import com.softwaremagico.kt.core.controller.FightController;
import org.springframework.stereotype.Component;

@Component
public class WebsocketFights {

    public WebsocketFights(DuelController duelController, FightController fightController, WebSocketController webSocketController) {

        //Refresh screens when a duel is updated.
        duelController.addFightUpdatedListener(((tournament, fight, duel, actor) -> webSocketController.fightUpdated(fight, actor)));
        fightController.addFightsAddedListeners((webSocketController::fightsCreated));

    }

}
