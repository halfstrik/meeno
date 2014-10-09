package live;

import org.junit.Test;
import snowmonkey.meeno.requests.ListCurrentOrders;
import snowmonkey.meeno.types.CurrentOrderSummaryReport;

import static helper.TestData.fileWriter;
import static helper.TestData.generated;
import static org.apache.commons.io.FileUtils.readFileToString;
import static snowmonkey.meeno.JsonSerialization.parse;

public class ListCurrentOrdersTest extends AbstractLiveTestCase {
    @Test
    public void test() throws Exception {
        ukHttpAccess.listCurrentOrders(fileWriter(generated().listCurrentOrdersPath()), new ListCurrentOrders.Builder().build());

        CurrentOrderSummaryReport currentOrders = parse(
                readFileToString(generated().listCurrentOrdersPath().toFile()), CurrentOrderSummaryReport.class);

        currentOrders.currentOrders.forEach(System.out::println);
    }

}
