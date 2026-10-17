package com.softwaremagico.kt.selenium;

import com.softwaremagico.kt.selenium.pages.ClubPage;
import com.softwaremagico.kt.selenium.pages.LoginPage;
import com.softwaremagico.kt.selenium.pages.TenantPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.List;
import java.util.Map;

public class TenantDirectAccessTest extends AdvancedBrowserTest {
    @Test
    public void knownForeignIdsCannotBeReadChangedDeletedOrExportedAndLogoutChangesTenantContext() {
        final String id=Long.toString(System.currentTimeMillis());
        final String adminA="direct.a."+id+"@test.local", adminB="direct.b."+id+"@test.local";
        final TenantPage tenants=new TenantPage(client);
        tenants.open(URL); tenants.create("Direct A "+id,adminA,PASSWORD); tenants.create("Direct B "+id,adminB,PASSWORD);
        new LoginPage(client).logoutAndOpen(URL); login(driver,adminA);
        seed("League",2,1); fights.generateLeagueFights(); fights.resolveAllFightsFor("Team 1");
        final Map<String,Object> ownTournament=map(api("GET","/tournaments/"+tournamentId,null));
        final Map<String,Object> brief=map(list(persistedFights().getFirst().get("duels")).getFirst().get("competitor1"));
        final Map<String,Object> participant=map(api("GET","/participants/"+number(brief,"id"),null));
        final int participantId=number(participant,"id");
        final Map<String,Object> ownClub=map(map(api("GET","/participants/"+participantId,null)).get("club"));
        final int clubId=number(ownClub,"id");
        final String ownName=tournament;
        other=browser(); login(other,adminB);
        final Map<String,Object> before=request(other,"GET","/tournaments",null);
        for(String path:List.of("/participants/"+participantId,"/clubs/"+clubId,"/tournaments/"+tournamentId,
                "/statistics/participants/"+participantId,"/statistics/tournaments/"+tournamentId,
                "/rankings/summary/"+tournamentId+"/html","/rankings/teams/tournaments/"+tournamentId+"/pdf")) {
            final Map<String,Object> response=request(other,"GET",path,null);
            Assert.assertTrue(List.of(403,404).contains(number(response,"status")),"Foreign resource must be rejected: "+path+" "+response);
            Assert.assertFalse(response.toString().contains(ownName));
        }
        for(Map<String,Object> entity:List.of(participant,ownClub,ownTournament)) {
            final String path=entity==participant?"/participants":entity==ownClub?"/clubs":"/tournaments";
            final Map<String,Object> response=request(other,"PUT",path,entity);
            Assert.assertTrue(List.of(403,404).contains(number(response,"status")),"Cross-tenant update must be rejected: "+response);
        }
        Assert.assertTrue(List.of(403,404).contains(number(request(other,"DELETE","/participants/"+participantId,null),"status")));
        Assert.assertEquals(request(other,"GET","/tournaments",null).get("body"),before.get("body"));
        Assert.assertEquals(map(api("GET","/tournaments/"+tournamentId,null)).get("name"),ownName);
        Assert.assertEquals(map(api("GET","/participants/"+participantId,null)).get("name"),participant.get("name"));
        new LoginPage(client).logoutAndOpen(URL); login(driver,adminB);
        Assert.assertTrue(list(api("GET","/tournaments",null)).isEmpty(),"New login must not retain previous tenant data.");
        Assert.assertTrue(List.of(403,404).contains(number(request(driver,"GET","/tournaments/"+tournamentId,null),"status")));
        Assert.assertEquals(number(request(driver,"GET","/auth/jwt/renew",null),"status"),200,"Token renewal must remain valid in current tenant.");
    }
}
