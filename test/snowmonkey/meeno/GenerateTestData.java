package snowmonkey.meeno;

import com.google.common.collect.Iterables;
import com.google.gson.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.StatusLine;
import snowmonkey.meeno.types.*;
import snowmonkey.meeno.types.raw.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.io.FileUtils.readFileToString;
import static snowmonkey.meeno.CountryLookup.Argentina;
import static snowmonkey.meeno.CountryLookup.UnitedKingdom;
import static snowmonkey.meeno.types.TimeGranularity.MINUTES;
import static snowmonkey.meeno.types.raw.TimeRange.between;

public class GenerateTestData {
    public static final Path TEST_DATA_DIR = Paths.get("test-data/generated");
    public static final CountryCode COUNTRY_CODE = Argentina;
    private MeenoConfig config;
    private HttpAccess httpAccess;
    private Path loginFile;

    public GenerateTestData(MeenoConfig config) {
        this.config = config;
    }

    public static void main(String[] args) throws Exception {
//        FileUtils.cleanDirectory(TEST_DATA_DIR);
        MeenoConfig config = MeenoConfig.load();

        GenerateTestData generateTestData = new GenerateTestData(config);
        generateTestData.login();

        try {
            generateTestData.listCountries();

            Navigation navigation = generateTestData.navigation();
            Navigation.Markets markets = navigation.findMarkets(
                    "Soccer",
                    between(ZonedDateTime.now(), ZonedDateTime.now().plusHours(6)),
                    "Match Odds"
            );

            Navigation.Market market = markets.iterator().next();

            EventType soccer = eventType("Soccer");

            generateTestData.listCompetitions(soccer, 1);
            Competitions competitions = Competitions.parse(ListCompetitions.listCompetitionsJson(1));
            Competition competition = competitions.iterator().next();
            generateTestData.listCompetitions(soccer, 2, competition.id);

            generateTestData.listEvents(soccer, markets.marketsIds());

            Markets marketCatalogues = generateTestData.listMarketCatalogue(soccer, markets.marketsIds());

            generateTestData.listMarketBook(markets.marketsIds());

//            generateTestData.placeOrders();
//            generateTestData.listCurrentOrders();
//            generateTestData.cancelOrders();
//        generateTestData.listMarketTypes();
//        generateTestData.listTimeRanges();
//            generateTestData.accountDetails();
//        generateTestData.accountFunds();
//            generateTestData.listClearedOrders();
        } finally {
            generateTestData.cleanup();
        }
    }

    private Navigation navigation() throws IOException, ApiException {
        FileTime lastModifiedTime = Files.getLastModifiedTime(GetNavigation.navigationFile());
        if (lastModifiedTime.toInstant().isBefore(ZonedDateTime.now().minusDays(1).toInstant()))
            httpAccess.nav(fileWriter(GetNavigation.navigationFile()));
        return Navigation.parse(GetNavigation.getNavigationJson());
    }

    private void cancelOrders() throws IOException, ApiException {
        snowmonkey.meeno.types.CurrentOrders currentOrders = snowmonkey.meeno.types.CurrentOrders.parse(CurrentOrders.listCurrentOrdersJson());
        CurrentOrder order = currentOrders.iterator().next();
        MarketId marketId = order.marketId;
        BetId betId = order.betId;
        List<CancelInstruction> cancelInstructions = newArrayList(CancelInstruction.cancel(betId));
        httpAccess.cancelOrders(marketId, cancelInstructions, fileWriter(CancelOrders.cancelOrdersFile()));
    }

    private void listMarketBook(Iterable<MarketId> marketIds) throws IOException, ApiException {
        PriceProjection priceProjection = new PriceProjection(newHashSet(PriceData.EX_BEST_OFFERS), null, false, false);
        httpAccess.listMarketBook(priceProjection, fileWriter(ListMarketBook.listMarketBookFile()), Iterables.limit(marketIds, 5));
    }

    private void cleanup() throws IOException, ApiException {
        try {
            httpAccess.logout();
        } finally {
            Files.delete(loginFile);
        }
    }

    private void accountFunds() throws IOException, ApiException {
        httpAccess.getAccountFunds(fileWriter(AccountFunds.getAccountFundsFile()));
    }

    private void accountDetails() throws IOException, ApiException {
        httpAccess.getAccountDetails(fileWriter(AccountDetails.getAccountDetailsFile()));
    }

    private void listTimeRanges() throws IOException, ApiException {
        httpAccess.listTimeRanges(fileWriter(TimeRanges.listTimeRangesFile()), MINUTES, new MarketFilterBuilder()
                .withEventTypeIds("1")
                .withEventIds(someEvent().id)
                .withMarketCountries(UnitedKingdom)
                .withMarketStartTime(between(ZonedDateTime.now(), ZonedDateTime.now().plusDays(1)))
                .build());
    }

    private void listMarketTypes() throws IOException, ApiException {
        httpAccess.listMarketTypes(fileWriter(MarketTypes.listMarketTypesFile()));
    }

    private void listEventTypes() throws IOException, ApiException {
        httpAccess.listEventTypes(fileWriter(EventTypes.listEventTypesFile()));
    }

    private void listEvents(EventType eventType, Iterable<MarketId> marketIds) throws IOException, ApiException {
        httpAccess.listEvents(
                fileWriter(Events.listEventsFile()),
                new MarketFilterBuilder()
                        .withEventTypeIds(eventType.id)
                        .withMarketIds(marketIds)
                        .build()
        );
    }

    private static EventType eventType(String eventName) throws IOException, ApiException {
        snowmonkey.meeno.types.EventTypes eventTypes = snowmonkey.meeno.types.EventTypes.parse(EventTypes.listEventTypesJson());
        return eventTypes.lookup(eventName);
    }

    private void listCompetitions(EventType eventType, int level, CompetitionId... competitionIds) throws IOException, ApiException {
        httpAccess.listCompetitions(fileWriter(ListCompetitions.listCompetitionsFile(level)),
                new MarketFilterBuilder()
                        .withEventTypeIds(eventType.id)
                        .withCompetitionIds(competitionIds)
                        .withMarketCountries(COUNTRY_CODE)
                        .withMarketStartTime(between(ZonedDateTime.now(), ZonedDateTime.now().plusDays(1)))
                        .build());
    }

    private void listCurrentOrders() throws IOException, ApiException {
        httpAccess.listCurrentOrders(fileWriter(CurrentOrders.listCurrentOrdersFile()));
    }

    private void listClearedOrders() throws IOException, ApiException {
        httpAccess.listClearedOrders(fileWriter(CurrentOrders.listClearedOrdersFile()));
    }

    private void placeOrders() throws IOException, ApiException {
        snowmonkey.meeno.types.raw.MarketCatalogue marketCatalogue = aMarket();
        LimitOrder limitOrder = new LimitOrder(2.00D, 1000, PersistenceType.LAPSE);
        PlaceInstruction placeLimitOrder = PlaceInstruction.createPlaceLimitOrder(marketCatalogue.runners.get(0).selectionId, Side.BACK, limitOrder);
        httpAccess.placeOrders(marketCatalogue.marketId, newArrayList(placeLimitOrder), CustomerRef.NONE, fileWriter(PlaceOrders.placeOrdersFile()));
    }

    private Markets listMarketCatalogue(EventType eventType, Iterable<MarketId> marketIds) throws IOException, ApiException {
        int maxResults = 5;
        httpAccess.listMarketCatalogue(fileWriter(MarketCatalogue.listMarketCatalogueFile()),
                MarketProjection.all(),
                MarketSort.FIRST_TO_START,
                maxResults,
                new MarketFilterBuilder()
                        .withEventTypeIds(eventType.id)
                        .withMarketIds(Iterables.limit(marketIds, maxResults))
                        .build());

        return Markets.parse(GenerateTestData.MarketCatalogue.listMarketCatalogueJson());
    }

    private void listCountries() throws IOException, ApiException {
        httpAccess.listCountries(fileWriter(Countries.listCountriesFile()));
    }

    private void login() throws Exception {
        loginFile = Login.loginFile();

        AppKey apiKey = config.appKey();

        HttpAccess.login(
                config.certificateFile(),
                config.certificatePassword(),
                config.username(),
                config.password(),
                apiKey,
                fileWriter(loginFile)
        );

        SessionToken sessionToken = SessionToken.parseJson(readFileToString(loginFile.toFile()));

        httpAccess = new HttpAccess(sessionToken, apiKey, Exchange.UK);
    }

    private static snowmonkey.meeno.types.raw.MarketCatalogue aMarket() throws IOException, ApiException {
        Markets markets = Markets.parse(GenerateTestData.MarketCatalogue.listMarketCatalogueJson());
        Iterator<snowmonkey.meeno.types.raw.MarketCatalogue> iterator = markets.iterator();
        iterator.next();
        return iterator.next();
    }

    private static Event someEvent() throws IOException, ApiException {
        return snowmonkey.meeno.types.Events.parse(Events.listEventsJson()).iterator().next();
    }

    public static HttpAccess.Processor fileWriter(final Path file) {
        return new HttpAccess.Processor() {
            @Override
            public void process(StatusLine statusLine, InputStream in) throws IOException, ApiException {

//                if (file.exists()) {
//                    System.out.println(file + " already exists - not overwriting");
//                    return;
//                }

                try (Reader reader = new InputStreamReader(in, HttpAccess.UTF_8)) {
                    JsonElement parsed = new JsonParser().parse(reader);

                    if (statusLine.getStatusCode() != 200) {
                        JsonObject object = parsed.getAsJsonObject();

                        System.out.println(prettyPrintJson(parsed));

                        if (object.has("detail")) {
                            JsonObject detail = object.getAsJsonObject("detail");
                            String exceptionName = detail.getAsJsonPrimitive("exceptionname").getAsString();
                            JsonObject exception = detail.getAsJsonObject(exceptionName);
                            throw new ApiException(
                                    exception.getAsJsonPrimitive("errorDetails").getAsString(),
                                    exception.getAsJsonPrimitive("errorCode").getAsString(),
                                    exception.getAsJsonPrimitive("requestUUID").getAsString()
                            );
                        }

                        if (!object.has("faultstring"))
                            throw new Defect("Bad status code: " + statusLine.getStatusCode() + "\n" + IOUtils.toString(in));

                        String faultstring = object.getAsJsonPrimitive("faultstring").getAsString();

                        switch (faultstring) {
                            case "DSC-0018": {
                                throw new IllegalStateException("A parameter marked as mandatory was not provided");
                            }
                            default:
                                throw new Defect("Bad status code: " + statusLine.getStatusCode() + "\n" + IOUtils.toString(in));
                        }
                    }

                    FileUtils.writeStringToFile(file.toFile(), prettyPrintJson(parsed), HttpAccess.UTF_8);
                }
            }

            private String prettyPrintJson(JsonElement parse) {
                return gson()
                        .toJson(parse);
            }
        };
    }

    private static Gson gson() {
        return new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();
    }

    public static class MarketCatalogue {

        public static Path listMarketCatalogueFile() {
            return TEST_DATA_DIR.resolve("listMarketCatalogue.json");
        }

        public static String listMarketCatalogueJson() throws IOException, ApiException {
            return readFileToString(listMarketCatalogueFile().toFile());
        }
    }

    private static class CancelOrders {
        public static Path cancelOrdersFile() {
            return TEST_DATA_DIR.resolve("cancelOrders.json");
        }
    }

    private static class PlaceOrders {
        public static Path placeOrdersFile() {
            return TEST_DATA_DIR.resolve("placeOrders.json");
        }
    }

    public static class CurrentOrders {
        public static Path listCurrentOrdersFile() {
            return TEST_DATA_DIR.resolve("listCurrentOrders.json");
        }

        public static Path listClearedOrdersFile() {
            return TEST_DATA_DIR.resolve("listClearedOrders.json");
        }

        public static String getCurrentOrderJson() throws IOException, ApiException {
            JsonElement parse = new JsonParser().parse(listCurrentOrdersJson());
            JsonElement currentOrders = parse.getAsJsonObject().get("currentOrders");
            return array(currentOrders);
        }

        public static String listCurrentOrdersJson() throws IOException, ApiException {
            return readFileToString(listCurrentOrdersFile().toFile());
        }
    }

    private static class ListCompetitions {

        public static Path listCompetitionsFile(int level) {
            return TEST_DATA_DIR.resolve("listCompetitions_" + level + ".json");
        }

        public static String getCompetitionJson(int level) throws IOException, ApiException {
            return jsonForFirstElementInArray(listCompetitionsJson(level));
        }

        public static String listCompetitionsJson(int level) throws IOException, ApiException {
            return readFileToString(listCompetitionsFile(level).toFile());
        }
    }

    public static class Events {

        private static Path listEventsFile() {
            return TEST_DATA_DIR.resolve("listEvents.json");
        }

        public static String listEventsJson() throws IOException, ApiException {
            return readFileToString(listEventsFile().toFile());
        }
    }

    public static class EventTypes {

        public static Path listEventTypesFile() {
            return TEST_DATA_DIR.resolve("listEventTypes.json");
        }

        public static String getEventTypeJson() throws IOException, ApiException {
            return jsonForFirstElementInArray(listEventTypesJson());
        }

        public static String listEventTypesJson() throws IOException, ApiException {
            return readFileToString(listEventTypesFile().toFile());
        }
    }

    private static class MarketTypes {

        public static Path listMarketTypesFile() {
            return TEST_DATA_DIR.resolve("listMarketTypes.json");
        }

        public static String getMarketTypeJson() throws IOException, ApiException {
            return jsonForFirstElementInArray(listMarketTypesJson());
        }

        public static String listMarketTypesJson() throws IOException, ApiException {
            return readFileToString(listMarketTypesFile().toFile());
        }
    }

    public static class GetNavigation {

        public static Path navigationFile() {
            return TEST_DATA_DIR.resolve("navigation.json");
        }

        public static String getNavigationJson() throws IOException, ApiException {
            return readFileToString(navigationFile().toFile());
        }
    }

    private static class TimeRanges {
        public static Path listTimeRangesFile() {
            return TEST_DATA_DIR.resolve("listTimeRanges.json");
        }

        public static String getTimeRangeJson() throws IOException, ApiException {
            return jsonForFirstElementInArray(listTimeRangesJson());
        }

        public static String listTimeRangesJson() throws IOException, ApiException {
            return readFileToString(listTimeRangesFile().toFile());
        }
    }

    private static class ListMarketBook {
        public static Path listMarketBookFile() {
            return TEST_DATA_DIR.resolve("listMarketBook.json");
        }
    }

    private static class AccountFunds {
        public static Path getAccountFundsFile() {
            return TEST_DATA_DIR.resolve("getAccountFunds.json");
        }

        public static String getAccountFundsJson() throws IOException, ApiException {
            return readFileToString(getAccountFundsFile().toFile());
        }
    }

    private static class AccountDetails {

        public static Path getAccountDetailsFile() {
            return TEST_DATA_DIR.resolve("getAccountDetails.json");
        }

        public static String getAccountDetailsJson() throws IOException, ApiException {
            return readFileToString(getAccountDetailsFile().toFile());
        }
    }

    public static class Login {

        public static String loginJson() throws IOException, ApiException {
            return readFileToString(loginFile().toFile());
        }

        public static Path loginFile() {
            return Paths.get("private").resolve(UUID.randomUUID() + ".login.json");
        }
    }

    private static class Countries {
        public static Path listCountriesFile() {
            return TEST_DATA_DIR.resolve("listCountries.json");
        }
    }

    private static String jsonForFirstElementInArray(String json) {
        JsonElement parse = new JsonParser().parse(json);
        return array(parse);
    }

    private static String array(JsonElement currentOrders) {
        JsonArray jsonArray = currentOrders.getAsJsonArray();
        JsonElement jsonElement = jsonArray.get(0);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonElement);
    }
}
