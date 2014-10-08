package live;

import org.junit.Test;
import snowmonkey.meeno.Exchange;
import snowmonkey.meeno.HttpAccess;
import snowmonkey.meeno.HttpExchangeOperations;
import snowmonkey.meeno.MeenoConfig;
import snowmonkey.meeno.types.*;
import snowmonkey.meeno.types.experimental.FootballMarket;

import static java.time.ZonedDateTime.now;
import static snowmonkey.meeno.types.MarketFilter.Builder.marketFilter;
import static snowmonkey.meeno.types.TimeRange.between;

public class HttpExchangeOperationsTest {

    @Test
    public void test() throws Exception {
        MeenoConfig config = MeenoConfig.loadMeenoConfig();
        SessionToken login = HttpAccess.login(config);

        HttpAccess httpAccess = new HttpAccess(login, config.appKey(), Exchange.UK);

        HttpExchangeOperations exchangeOperations = new HttpExchangeOperations(httpAccess);

        Navigation navigation = exchangeOperations.navigation();

        Navigation.Markets markets = navigation.findMarkets(EventTypeName.SOCCER, between(now(), now().plusHours(1)), "Match Odds");

        for (FootballMarket footballMarket : markets.asFootballMarkets()) {
            System.out.println(footballMarket.print());
        }

        EventTypeId eventTypeId = markets.iterator().next().eventTypeId();
        System.out.println("eventTypeId = " + eventTypeId);

        Iterable<MarketId> marketIds = markets.marketsIds();
        System.out.println("marketIds = " + marketIds);

        MarketCatalogues marketCatalogues = exchangeOperations.marketCatalogue(marketFilter(eventTypeId, marketIds));

        for (MarketCatalogue marketCatalogue : marketCatalogues) {
            System.out.println("marketCatalogue = " + marketCatalogue);
        }

        httpAccess.logout();
    }
}