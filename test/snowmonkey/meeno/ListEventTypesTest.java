package snowmonkey.meeno;

import helper.TestData;
import org.junit.Test;
import snowmonkey.meeno.types.EventType;
import snowmonkey.meeno.types.EventTypes;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ListEventTypesTest {
    @Test
    public void canParse() throws Exception {
        EventTypes eventTypes = EventTypes.parse(readFileToString(TestData.unitTest().listEventTypesPath().toFile()));

        EventType soccer = eventTypes.lookup("Soccer");

        assertThat(soccer.id.asString(), equalTo("1"));
        assertThat(soccer.name, equalTo("Soccer"));

        EventType horseRacing = eventTypes.lookup("Horse Racing");

        assertThat(horseRacing.id.asString(), equalTo("7"));
        assertThat(horseRacing.name, equalTo("Horse Racing"));
    }
}
