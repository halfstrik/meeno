package live;

import helper.TestData;
import org.junit.Test;
import snowmonkey.meeno.HttpExchangeOperations;
import snowmonkey.meeno.JsonSerialization;
import snowmonkey.meeno.requests.ListMarketBook;
import snowmonkey.meeno.types.*;

import java.time.LocalDate;

import static com.google.common.collect.Iterables.limit;
import static com.google.common.collect.Sets.newHashSet;
import static java.time.ZonedDateTime.now;
import static org.apache.commons.io.FileUtils.readFileToString;
import static snowmonkey.meeno.types.TimeRange.between;

public class ListMarketBookTest extends AbstractLiveTestCase {
    @Test
    public void testRequestForPrices() throws Exception {

        Navigation navigation = navigation();

        Navigation.Markets markets = navigation.findMarkets(
                EventTypeName.SOCCER,
                between(now(), now().plusHours(6)),
                "Match Odds"
        );

        Iterable<MarketId> marketIds = limit(markets.marketsIds(), 5);

        PriceProjection priceProjection = new PriceProjection(
                newHashSet(PriceData.EX_BEST_OFFERS),
                new ExBestOfferOverRides(
                        4,
                        RollupModel.STAKE,
                        null,
                        null,
                        null
                ),
                true,
                false
        );


        ukHttpAccess.listMarketBook(TestData.fileWriter(TestData.generated().listMarketBookPath()),
                new ListMarketBook(
                        marketIds,
                        priceProjection,
                        null,
                        null,
                        null,
                        Locale.EN_US
                ));

        MarketBook[] marketBooks = JsonSerialization.parse(
                readFileToString(TestData.generated().listMarketBookPath().toFile()), MarketBook[].class);

        for (MarketBook marketBook : marketBooks) {
            System.out.println(marketBook);
        }
    }

    @Test
    public void test() throws Exception {
        Navigation navigation = navigation();

        Navigation.Markets markets = navigation.findMarkets(
                EventTypeName.SOCCER,
                between(now(), now().plusHours(6)),
                "Match Odds"
        );

        PriceProjection priceProjection = new PriceProjection(
                newHashSet(PriceData.EX_BEST_OFFERS),
                new ExBestOfferOverRides(
                        4,
                        RollupModel.STAKE,
                        null,
                        null,
                        null
                ),
                true,
                false
        );

        MarketBook marketBook = new HttpExchangeOperations(ukHttpAccess).marketBook(markets.marketsIds().iterator().next(), priceProjection);
        System.out.println("marketBook = " + marketBook);
    }

    @Test
    public void testRequestForResult() throws Exception {

        Navigation navigation = Navigation.parse(TestData.generated().navigationJson(LocalDate.parse("2014-08-08")));

        Navigation.Markets markets = navigation.findMarkets(
                EventTypeName.SOCCER,
                between(now().minusDays(1), now()),
                "Match Odds"
        );

        Iterable<MarketId> marketIds = limit(markets.marketsIds(), 5);

        ukHttpAccess.listMarketBook(TestData.fileWriter(TestData.generated().listMarketBookPath()),
                new ListMarketBook(
                        marketIds,
                        null,
                        null,
                        null,
                        null,
                        Locale.EN_US
                ));

        MarketBook[] marketBooks = JsonSerialization.parse(
                readFileToString(TestData.generated().listMarketBookPath().toFile()), MarketBook[].class);
        for (MarketBook marketBook : marketBooks) {
            System.out.println(marketBook.prettyPrint());
        }
    }
}
