package live;

import helper.TestData;
import org.junit.Test;

import static helper.TestData.fileWriter;

public class ListMarketTypesTest extends AbstractLiveTestCase {
    @Test
    public void test() throws Exception {
        ukHttpAccess.listMarketTypes(fileWriter(TestData.generated().listMarketTypesPath()));
    }

}
