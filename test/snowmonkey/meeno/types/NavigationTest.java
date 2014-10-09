package snowmonkey.meeno.types;

import helper.TestData;
import org.junit.Test;

import java.time.*;
import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static snowmonkey.meeno.types.EventTypeName.SOCCER;
import static snowmonkey.meeno.types.TimeRange.between;

public class NavigationTest {
    @Test
    public void getEventTypes_withProperDataFromFile() throws Exception {
        Navigation navigation = Navigation.parse(TestData.unitTest().navigationJson());

        List<Navigation> eventTypes = navigation.getEventTypes();
        assertThat(eventTypes.size(), equalTo(31));
    }

    @Test
    public void events_withSoccerAndProperDataFromFile() throws Exception {
        Navigation navigation = Navigation.parse(TestData.unitTest().navigationJson());

        List<Navigation> eventTypes = navigation.events(SOCCER);
        assertThat(eventTypes.size(), equalTo(79));
    }

    @Test
    public void findMarkets_withSoccerCorrectScoreProperDataFromFile() throws Exception {
        Navigation navigation = Navigation.parse(TestData.unitTest().navigationJson());

        Navigation.Markets markets = navigation.findMarkets(
                SOCCER,
                between(ZonedDateTime.of(LocalDate.of(2014, Month.OCTOBER, 9), LocalTime.NOON, ZoneId.of("GMT")),
                        ZonedDateTime.of(LocalDate.of(2014, Month.OCTOBER, 11), LocalTime.NOON, ZoneId.of("GMT"))),
                "Correct Score.*"
        );
        int size = 0;
        for (Navigation.Market ignored : markets) {
            size += 1;
        }
        assertThat(size, equalTo(399));
    }

    @Test
    public void findSiblingMarkets_withProperDataFromFile() throws Exception {
        Navigation navigation = Navigation.parse(TestData.unitTest().navigationJson());
        Navigation.Markets markets = navigation.findMarkets(
                SOCCER,
                between(ZonedDateTime.of(LocalDate.of(2014, Month.OCTOBER, 9), LocalTime.NOON, ZoneId.of("GMT")),
                        ZonedDateTime.of(LocalDate.of(2014, Month.OCTOBER, 11), LocalTime.NOON, ZoneId.of("GMT"))),
                "Match Odds"
        );
        Navigation.Market market = markets.get(new MarketId("1.115732425"));

        Navigation.Markets siblingMarkets = market.findSiblingMarkets("Correct Score.*");
        int size = 0;
        for (Navigation.Market ignored : siblingMarkets) {
            size += 1;
        }
        assertThat(size, equalTo(3));
    }
}