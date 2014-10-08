package snowmonkey.meeno;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import live.raw.GenerateTestData;
import org.junit.Test;
import snowmonkey.meeno.requests.ListMarketBook;
import snowmonkey.meeno.types.*;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.util.HashSet;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static snowmonkey.meeno.JsonSerialization.gson;

public class JsonSerializationTest {
    @Test
    public void gsonToJson_withArrayList() throws Exception {
        Iterable<MarketId> marketIds = newArrayList(new MarketId("1"), new MarketId("99"));
        assertEquals("[\n  \"1\",\n  \"99\"\n]", gson().toJson(marketIds));
    }

    @Test
    public void gsonFromJson_withJsonArray() {
        MarketId[] marketIds = gson().fromJson("[\n  \"1\",\n  \"99\"\n]", MarketId[].class);
        assertEquals(marketIds[0].asString(), "1");
        assertEquals(marketIds[1].asString(), "99");
    }

    @Test
    public void gsonToJson_withComplexType() throws Exception {
        Iterable<MarketId> marketIds = newArrayList(new MarketId("1"), new MarketId("99"));
        ListMarketBook request = new ListMarketBook(
                marketIds,
                new PriceProjection(
                        new HashSet<>(),
                        new ExBestOfferOverRides(
                                3, RollupModel.STAKE, null, null, null
                        ),
                        true,
                        false
                ),
                OrderProjection.EXECUTION_COMPLETE,
                MatchProjection.ROLLED_UP_BY_PRICE,
                null,
                Locale.EN_US
        );
        assertEquals(gson().toJson(request), "{\n" +
                "  \"marketIds\": [\n" +
                "    \"1\",\n" +
                "    \"99\"\n" +
                "  ],\n" +
                "  \"priceProjection\": {\n" +
                "    \"priceData\": [],\n" +
                "    \"exBestOfferOverRides\": {\n" +
                "      \"bestPricesDepth\": 3,\n" +
                "      \"rollupModel\": \"STAKE\"\n" +
                "    },\n" +
                "    \"virtualise\": true,\n" +
                "    \"rolloverStakes\": false\n" +
                "  },\n" +
                "  \"orderProjection\": \"EXECUTION_COMPLETE\",\n" +
                "  \"matchProjection\": \"ROLLED_UP_BY_PRICE\",\n" +
                "  \"locale\": \"en_US\"\n" +
                "}");
    }

    @Test
    public void gsonFromJson_withComplexObject() throws Exception {
        JsonElement jsonElement = new JsonParser().parse(readFileToString(
                GenerateTestData.LIST_CLEARED_ORDERS_FILE.toFile())).getAsJsonObject().get("clearedOrders");
        ClearedOrderSummaryReport[] clearedOrderSummaryReport = gson().fromJson(
                jsonElement, (Type) ClearedOrderSummaryReport[].class);

        assertThat(clearedOrderSummaryReport.length, equalTo(0));
    }

    @Test
    public void parse_canDeserializeStructuredComplexObjects() throws Exception {
        CurrentOrderSummaryReport currentOrders = JsonSerialization.parse(
                readFileToString(GenerateTestData.LIST_CURRENT_ORDERS_FILE.toFile()), CurrentOrderSummaryReport.class);
        CurrentOrderSummary order = currentOrders.currentOrders.iterator().next();

        assertThat(order.betId, equalTo(new BetId("41475324467")));
        assertThat(order.marketId, equalTo(new MarketId("1.115568466")));
        assertThat(order.selectionId, equalTo(new SelectionId(505717L)));
        assertThat(order.priceSize.price, equalTo(Price.price(1000d)));
        assertThat(order.priceSize.size, equalTo(Size.size(2.0d)));
        assertThat(order.side, equalTo(Side.BACK));
        assertThat(order.status, equalTo(OrderStatus.EXECUTABLE));
        assertThat(order.persistenceType, equalTo(PersistenceType.LAPSE));
        assertThat(order.orderType, equalTo(OrderType.LIMIT));
        assertThat(order.placedDate, equalTo(ZonedDateTime.parse("2014-09-22T15:15:46.000Z")));
        assertThat(order.averagePriceMatched, equalTo(0d));
        assertThat(order.sizeMatched, equalTo(0d));
        assertThat(order.sizeRemaining, equalTo(2d));
        assertThat(order.sizeLapsed, equalTo(0d));
        assertThat(order.sizeCancelled, equalTo(0d));
        assertThat(order.sizeVoided, equalTo(0d));
    }
}