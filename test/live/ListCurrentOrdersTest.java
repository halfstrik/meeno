package live;

import org.junit.Test;
import snowmonkey.meeno.requests.ListCurrentOrders;
import snowmonkey.meeno.types.CurrentOrderSummary;
import snowmonkey.meeno.types.CurrentOrderSummaryReport;

import static live.GenerateTestData.*;
import static org.apache.commons.io.FileUtils.*;
import static snowmonkey.meeno.JsonSerialization.*;

public class ListCurrentOrdersTest extends AbstractLiveTestCase {
    @Test
    public void test() throws Exception {
        httpAccess.listCurrentOrders(fileWriter(LIST_CURRENT_ORDERS_FILE), new ListCurrentOrders.Builder().build());

        CurrentOrderSummaryReport currentOrders = parse(readFileToString(LIST_CURRENT_ORDERS_FILE.toFile()), CurrentOrderSummaryReport.class);

        for (CurrentOrderSummary currentOrder : currentOrders.currentOrders) {
            System.out.println(currentOrder);
        }
    }

}
