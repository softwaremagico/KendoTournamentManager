package com.softwaremagico.kt.selenium;

/*-
 * #%L
 * Kendo Tournament Manager (End-to-End Tests)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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

import com.softwaremagico.kt.selenium.pages.FightPage;
import com.softwaremagico.kt.selenium.pages.TournamentPage;
import com.softwaremagico.kt.selenium.pages.UserPage;
import com.softwaremagico.kt.selenium.pages.LoginPage;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WebSocketReconnectTest extends AdvancedBrowserTest {
    @Test
    public void reconnectRestoresCurrentStateAndContinuesReceivingLiveScores() {
        seed("League",2,1); fights.generateLeagueFights();
        final String editor="reconnect."+System.currentTimeMillis()+"@test.local";
        final UserPage users=new UserPage(client); users.open(URL);users.create(editor,PASSWORD);users.setEditorRole(editor);
        tournaments.open(URL);tournaments.select(tournament);tournaments.openFights();
        other=browser();
        final LoginPage login=new LoginPage(new WebDriverClient(other)); login.open(URL);
        ((JavascriptExecutor)other).executeScript("""
            window.e2eSockets=[];
            const NativeSocket=window.WebSocket;
            window.WebSocket=class extends NativeSocket {
              constructor(...args) {super(...args);window.e2eSockets.push(this)}
            };
            """);
        // LoginPage refreshes to apply language; install monitoring again on the authenticated fight document.
        login.login(editor,PASSWORD);
        final WebDriverClient remote=new WebDriverClient(other);
        final TournamentPage remoteTournaments=new TournamentPage(remote);
        remoteTournaments.open(URL);remoteTournaments.select(tournament);remoteTournaments.openFights();
        fights.scoreFirstDuelFor("Team 1");new FightPage(remote).waitUntilContainsScore("M");
        ((JavascriptExecutor)other).executeScript("window.e2eOldSocketCount=window.e2eSockets.length;window.e2eSockets.forEach(s=>s.close());");
        remote.getWebDriverWait().until(browser -> Boolean.TRUE.equals(((JavascriptExecutor)browser).executeScript(
                "return window.e2eSockets.length>window.e2eOldSocketCount && window.e2eSockets.at(-1).readyState===1;")));
        score(duel(0),0,1,"Kote");
        remote.getWebDriverWait().until(browser -> new FightPage(remote).containsScore("K"));
        Assert.assertEquals(number(request(other,"GET","/auth/jwt/renew",null),"status"),200);
    }
}
