package snowmonkey.meeno.types;

import helper.TestData;
import org.junit.Test;
import snowmonkey.meeno.types.experimental.FootballMarket;

import java.time.*;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static snowmonkey.meeno.types.EventTypeName.SOCCER;
import static snowmonkey.meeno.types.TimeRange.between;

public class FootballMarketTest {
    @Test
    public void test() throws Exception {
        Navigation navigation = Navigation.parse(TestData.unitTest().navigationJson());

        Navigation.Markets markets = navigation.findMarkets(
                SOCCER,
                between(ZonedDateTime.of(LocalDate.of(2014, Month.OCTOBER, 9), LocalTime.NOON, ZoneId.of("GMT")),
                        ZonedDateTime.of(LocalDate.of(2014, Month.OCTOBER, 11), LocalTime.NOON, ZoneId.of("GMT"))),
                "Match Odds"
        );

        Iterable<FootballMarket> footballMarkets = markets.asFootballMarkets();
        FootballMarket footballMarket = footballMarkets.iterator().next();
        assertThat(footballMarket.marketName(), equalTo("Match Odds"));
        assertThat(footballMarket.matchName(), equalTo("Belarus v Ukraine"));
        assertThat(footballMarket.competitionName(), equalTo("Fixtures 09 October"));
        assertThat(footballMarket.countryName(), equalTo("Euro 2016 Qualifiers"));
    }
}