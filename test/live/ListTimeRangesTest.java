package live;

import helper.TestData;
import org.junit.Test;
import snowmonkey.meeno.types.EventId;
import snowmonkey.meeno.types.EventTypeId;
import snowmonkey.meeno.types.MarketFilter;
import snowmonkey.meeno.types.Navigation;

import java.time.ZonedDateTime;

import static snowmonkey.meeno.CountryLookup.UnitedKingdom;
import static snowmonkey.meeno.types.EventTypeName.SOCCER;
import static snowmonkey.meeno.types.TimeGranularity.MINUTES;
import static snowmonkey.meeno.types.TimeRange.between;

public class ListTimeRangesTest extends AbstractLiveTestCase {
    @Test
    public void test() throws Exception {
        Navigation navigation = navigation().events(SOCCER).get(0);

        EventId eventId = navigation.eventId();
        EventTypeId eventTypeId = navigation.parent().eventTypeId();

        ukHttpAccess.listTimeRanges(TestData.fileWriter(TestData.generated().listTimeRangesPath()), MINUTES,
                new MarketFilter.Builder()
                        .withEventTypeIds(eventTypeId)
                        .withEventIds(eventId)
                        .withMarketCountries(UnitedKingdom)
                        .withMarketStartTime(between(ZonedDateTime.now(), ZonedDateTime.now().plusDays(1)))
                        .build());
    }
}
