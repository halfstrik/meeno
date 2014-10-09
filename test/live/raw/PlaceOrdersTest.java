package live.raw;

import com.google.common.collect.ImmutableList;
import live.AbstractLiveTestCase;
import org.junit.Test;
import snowmonkey.meeno.ApiException;
import snowmonkey.meeno.JsonSerialization;
import snowmonkey.meeno.requests.CancelInstruction;
import snowmonkey.meeno.requests.CancelOrders;
import snowmonkey.meeno.requests.ListCurrentOrders;
import snowmonkey.meeno.types.*;

import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static helper.TestData.fileWriter;
import static helper.TestData.generated;
import static java.time.ZonedDateTime.now;
import static org.apache.commons.io.FileUtils.readFileToString;
import static snowmonkey.meeno.JsonSerialization.parse;
import static snowmonkey.meeno.types.MarketProjection.RUNNER_METADATA;
import static snowmonkey.meeno.types.PlaceInstruction.createPlaceLimitOrder;
import static snowmonkey.meeno.types.TimeRange.between;

public class PlaceOrdersTest extends AbstractLiveTestCase {
    @Test
    public void test() throws Exception {
        Navigation.Markets markets = navigation().findMarkets(EventTypeName.SOCCER, between(now().plusDays(6), now().plusDays(7)), "Match Odds");
        Navigation.Market market = markets.iterator().next();

        ukHttpAccess.listMarketCatalogue(fileWriter(generated().listMarketCataloguePath()),
                newHashSet(RUNNER_METADATA),
                MarketSort.FIRST_TO_START,
                new MarketFilter.Builder().withMarketIds(market.id).build()
        );

        MarketCatalogue marketCatalogue = JsonSerialization.parse(readFileToString(generated().listMarketCataloguePath().toFile()), MarketCatalogue[].class)[0];

        LimitOrder limitOrder = new LimitOrder(2.00D, 1000, PersistenceType.LAPSE);
        PlaceInstruction placeLimitOrder = createPlaceLimitOrder(marketCatalogue.runners.get(0).selectionId, Side.BACK, limitOrder);
        ukHttpAccess.placeOrders(fileWriter(generated().placeOrdersPath()), marketCatalogue.marketId, newArrayList(placeLimitOrder), CustomerRef.uniqueCustomerRef());

        PlaceExecutionReport placeInstructionReport = parse(readFileToString(generated().placeOrdersPath().toFile()), PlaceExecutionReport.class);

        System.out.println("placeInstructionReport = " + placeInstructionReport);

        ImmutableList<PlaceInstructionReport> instructionReports = placeInstructionReport.instructionReports;
        BetId betId = instructionReports.get(0).betId;

        ukHttpAccess.listCurrentOrders(
                fileWriter(generated().listCurrentOrdersPath()), new ListCurrentOrders.Builder().build());

        CurrentOrderSummaryReport currentOrders = parse(
                readFileToString(generated().listCurrentOrdersPath().toFile()), CurrentOrderSummaryReport.class);

        currentOrders.currentOrders.stream().filter(currentOrder -> {
            assert betId != null;
            return betId.equals(betId);
        }).forEach(currentOrder -> {
            MarketId marketId = currentOrder.marketId;

            List<CancelInstruction> cancelInstructions = newArrayList(CancelInstruction.cancel(betId));

            try {
                ukHttpAccess.cancelOrders(fileWriter(generated().cancelOrdersPath()), new CancelOrders(marketId, cancelInstructions, null));
            } catch (IOException | ApiException e) {
                e.printStackTrace();
            }
        });
    }

}
