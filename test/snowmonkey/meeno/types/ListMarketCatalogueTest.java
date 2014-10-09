package snowmonkey.meeno.types;

import helper.TestData;
import org.junit.Test;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static snowmonkey.meeno.JsonSerialization.parse;
import static snowmonkey.meeno.types.MarketCatalogues.createMarketCatalogues;

public class ListMarketCatalogueTest {
    @Test
    public void createMarketCatalogues_withProperDataFromFile() throws Exception {
        MarketCatalogues markets = createMarketCatalogues(
                parse(readFileToString(TestData.unitTest().listMarketCataloguePath().toFile()), MarketCatalogue[].class));

        MarketId marketId = new MarketId("1.115568466");
        MarketCatalogue marketCatalogue = markets.get(marketId);

        assertThat(marketCatalogue.marketStartTime, nullValue());
        assertThat(marketCatalogue.marketId, equalTo(marketId));
        assertThat(marketCatalogue.marketName, equalTo("Match Odds"));
        assertThat(marketCatalogue.eventType, nullValue());
        assertThat(marketCatalogue.competition, nullValue());
        assertThat(marketCatalogue.description, nullValue());
    }
}
