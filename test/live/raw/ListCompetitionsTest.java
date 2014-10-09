package live.raw;

import live.AbstractLiveTestCase;
import org.junit.Test;
import snowmonkey.meeno.types.EventType;
import snowmonkey.meeno.types.EventTypes;
import snowmonkey.meeno.types.MarketFilter;

import static helper.TestData.fileWriter;
import static helper.TestData.generated;
import static java.time.ZonedDateTime.now;
import static org.apache.commons.io.FileUtils.readFileToString;
import static snowmonkey.meeno.CountryLookup.Argentina;
import static snowmonkey.meeno.types.TimeRange.between;

public class ListCompetitionsTest extends AbstractLiveTestCase {

    @Test
    public void test() throws Exception {
        EventTypes eventTypes = EventTypes.parse(readFileToString(generated().listEventTypesPath().toFile()));
        EventType soccer = eventTypes.lookup("Soccer");

        ukHttpAccess.listCompetitions(fileWriter(generated().listCompetitionsPath()),
                new MarketFilter.Builder()
                        .withEventTypeIds(soccer.id)
                        .withMarketCountries(Argentina)
                        .withMarketStartTime(between(now(), now().plusDays(1)))
                        .build());
    }
}
