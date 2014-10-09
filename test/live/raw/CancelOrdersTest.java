package live.raw;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import live.AbstractLiveTestCase;
import org.junit.Test;
import snowmonkey.meeno.DefaultProcessor;
import snowmonkey.meeno.HttpExchangeOperations;
import snowmonkey.meeno.requests.CancelInstruction;
import snowmonkey.meeno.requests.CancelOrders;
import snowmonkey.meeno.types.*;

import static helper.TestData.fileWriter;
import static helper.TestData.generated;
import static java.time.ZonedDateTime.now;
import static org.apache.commons.io.FileUtils.readFileToString;
import static snowmonkey.meeno.JsonSerialization.parse;
import static snowmonkey.meeno.requests.ListCurrentOrders.Builder;
import static snowmonkey.meeno.types.TimeRange.between;

public class CancelOrdersTest extends AbstractLiveTestCase {
    @Test
    public void cancelOrders() throws Exception {
        HttpExchangeOperations httpExchangeOperations = new HttpExchangeOperations(ukHttpAccess);

        CurrentOrderSummaryReport currentOrders = httpExchangeOperations.listCurrentOrders(new Builder()
                .withOrderProjection(OrderProjection.ALL)
                .withDateRange(between(now().minusDays(1), now().plusMonths(1)))
                .build());

        Iterable<CancelExecutionReport> cancelled = httpExchangeOperations.cancelAllOrders(currentOrders);
        for (CancelExecutionReport cancelExecutionReport : cancelled) {
            System.out.println("cancelExecutionReport = " + cancelExecutionReport);
        }
    }

    @Test
    public void cancelAllOrders() throws Exception {

        ukHttpAccess.listCurrentOrders(fileWriter(generated().listCurrentOrdersPath()), new Builder().build());

        CurrentOrderSummaryReport currentOrders = parse(
                readFileToString(generated().listCurrentOrdersPath().toFile()), CurrentOrderSummaryReport.class);

        Multimap<MarketId, CancelInstruction> cancelInstructions = ArrayListMultimap.create();
        for (CurrentOrderSummary currentOrder : currentOrders.currentOrders) {
            MarketId marketId = currentOrder.marketId;
            CancelInstruction cancel = CancelInstruction.cancel(currentOrder.betId);
            cancelInstructions.put(marketId, cancel);
        }

        for (MarketId marketId : cancelInstructions.keySet()) {
            ukHttpAccess.cancelOrders((statusLine, in) -> {
                String s = DefaultProcessor.processResponse(statusLine, in);
                System.out.println(s);
                return s;
            }, new CancelOrders(marketId, cancelInstructions.get(marketId), null));
        }
    }

}
