package live.raw;

import helper.TestData;
import live.AbstractLiveTestCase;
import org.junit.Test;
import snowmonkey.meeno.requests.ListEventTypes;
import snowmonkey.meeno.types.Locale;

import static helper.TestData.fileWriter;
import static snowmonkey.meeno.types.MarketFilter.Builder.noFilter;

public class ListEventTypesTest extends AbstractLiveTestCase {

    @Test
    public void test() throws Exception {
        ukHttpAccess.listEventTypes(fileWriter(
                TestData.generated().listEventTypesPath()), new ListEventTypes(noFilter(), Locale.EN_US));
    }

}
