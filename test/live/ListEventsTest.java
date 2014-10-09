package live;

import org.junit.Test;
import snowmonkey.meeno.JsonSerialization;
import snowmonkey.meeno.types.EventResult;
import snowmonkey.meeno.types.EventType;
import snowmonkey.meeno.types.EventTypes;
import snowmonkey.meeno.types.MarketFilter;

import static helper.TestData.fileWriter;
import static helper.TestData.generated;
import static org.apache.commons.io.FileUtils.readFileToString;

public class ListEventsTest extends AbstractLiveTestCase {
    @Test
    public void test() throws Exception {
        EventTypes eventTypes = EventTypes.parse(readFileToString(generated().listEventTypesPath().toFile()));
        EventType soccer = eventTypes.lookup("Soccer");

        ukHttpAccess.listEvents(
                fileWriter(generated().listEventsPath()),
                new MarketFilter.Builder()
                        .withEventTypeIds(soccer.id)
                        .build()
        );

        EventResult[] eventResults = JsonSerialization.parse(
                readFileToString(generated().listEventsPath().toFile()), EventResult[].class);
        for (EventResult eventResult : eventResults) {
            System.out.println("eventResult = " + eventResult);
        }
    }

}
