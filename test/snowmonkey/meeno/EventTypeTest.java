package snowmonkey.meeno;

import helper.TestData;
import org.junit.Test;
import snowmonkey.meeno.types.EventTypes;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class EventTypeTest {
    @Test
    public void testRequestForPrices() throws Exception {
        EventTypes eventTypes = EventTypes.parse(readFileToString(TestData.unitTest().listEventTypesPath().toFile()));
        assertThat(eventTypes.lookup("Soccer").prettyPrint(), equalTo("{\n" +
                "  \"id\": {\n" +
                "    \"value\": \"1\"\n" +
                "  },\n" +
                "  \"name\": \"Soccer\"\n" +
                "}"));
    }
}
