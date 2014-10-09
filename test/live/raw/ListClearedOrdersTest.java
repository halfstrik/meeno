package live.raw;

import live.AbstractLiveTestCase;
import org.junit.Test;
import snowmonkey.meeno.HttpExchangeOperations;
import snowmonkey.meeno.JsonSerialization;
import snowmonkey.meeno.types.BetStatus;
import snowmonkey.meeno.types.ClearedOrderSummary;
import snowmonkey.meeno.types.ClearedOrderSummaryReport;

import static helper.TestData.fileWriter;
import static helper.TestData.generated;
import static java.time.ZonedDateTime.now;
import static org.apache.commons.io.FileUtils.readFileToString;
import static snowmonkey.meeno.types.TimeRange.between;

public class ListClearedOrdersTest extends AbstractLiveTestCase {
    @Test
    public void canGetSettledOrders() throws Exception {

        ukHttpAccess.listClearedOrders(fileWriter(generated().listClearedOrdersPath()),
                BetStatus.SETTLED,
                between(now().minusMonths(3), now()), 0
        );

        ClearedOrderSummary clearedOrderSummaryReport = JsonSerialization.parse(
                readFileToString(generated().listClearedOrdersPath().toFile()), ClearedOrderSummary.class);
        for (ClearedOrderSummaryReport orderSummaryReport : clearedOrderSummaryReport.clearedOrders) {
            System.out.println("clearedOrderSummaryReport = " + orderSummaryReport);
        }
    }

    @Test
    public void canGetSettledOrders2() throws Exception {

        ukHttpAccess.listClearedOrders(fileWriter(generated().listClearedOrdersPath()),
                BetStatus.SETTLED,
                between(now().minusMonths(3), now()),
                0
        );

        HttpExchangeOperations httpExchangeOperations = new HttpExchangeOperations(ukHttpAccess);
        ClearedOrderSummary clearedOrderSummaryReport = httpExchangeOperations.listClearedOrders(
                BetStatus.SETTLED,
                between(now().minusMonths(3), now()),
                0
        );

        for (ClearedOrderSummaryReport orderSummaryReport : clearedOrderSummaryReport.clearedOrders) {
            System.out.println("clearedOrderSummaryReport = " + orderSummaryReport);
        }
    }

    @Test
    public void canGetCancelledOrders() throws Exception {

        ukHttpAccess.listClearedOrders(fileWriter(generated().listClearedOrdersPath()),
                BetStatus.LAPSED,
                between(now().minusMonths(3), now()), 0
        );

        ClearedOrderSummary clearedOrderSummaryReport = JsonSerialization.parse(
                readFileToString(generated().listClearedOrdersPath().toFile()), ClearedOrderSummary.class);
        for (ClearedOrderSummaryReport orderSummaryReport : clearedOrderSummaryReport.clearedOrders) {
            System.out.println("clearedOrderSummaryReport = " + orderSummaryReport);
        }
    }
}
