package helper;

import org.apache.commons.io.FileUtils;
import snowmonkey.meeno.DefaultProcessor;
import snowmonkey.meeno.HttpAccess;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import static org.apache.commons.io.FileUtils.readFileToString;

public class TestData {
    private final Path testDataDir;

    private TestData(String basePath) {
        testDataDir = Paths.get(basePath);
    }

    public static TestData generated() {
        return new TestData("test-data/generated");
    }

    public static TestData unitTest() {
        return new TestData("test-data/unit-test");
    }

    public static HttpAccess.Processor fileWriter(final Path file) {
        return (statusLine, in) -> {
            String json = DefaultProcessor.processResponse(statusLine, in);
            FileUtils.writeStringToFile(file.toFile(), json, HttpAccess.UTF_8);
            return json;
        };
    }

    public Path listCountriesPath() {
        return testDataDir.resolve("listCountries.json");
    }

    public Path getAccountDetailsPath() {
        return testDataDir.resolve("getAccountDetails.json");
    }

    public Path getAccountFundsPath() {
        return testDataDir.resolve("getAccountFunds.json");
    }

    public Path transferFundsPath() {
        return testDataDir.resolve("transferFunds.json");
    }

    public Path listMarketBookPath() {
        return testDataDir.resolve("listMarketBook.json");
    }

    public Path listTimeRangesPath() {
        return testDataDir.resolve("listTimeRanges.json");
    }

    public Path listMarketTypesPath() {
        return testDataDir.resolve("listMarketTypes.json");
    }

    public Path listEventTypesPath() {
        return testDataDir.resolve("listEventTypes.json");
    }

    public Path listEventsPath() {
        return testDataDir.resolve("listEvents.json");
    }

    public Path listCompetitionsPath() {
        return testDataDir.resolve("listCompetitions.json");
    }

    public Path listClearedOrdersPath() {
        return testDataDir.resolve("listClearedOrders.json");
    }

    public Path listCurrentOrdersPath() {
        return testDataDir.resolve("listCurrentOrders.json");
    }

    public Path placeOrdersPath() {
        return testDataDir.resolve("placeOrders.json");
    }

    public Path listMarketCataloguePath() {
        return testDataDir.resolve("listMarketCatalogue.json");
    }

    public Path cancelOrdersPath() {
        return testDataDir.resolve("cancelOrders.json");
    }

    public Path loginPath() {
        return testDataDir.resolve("login.json");
    }

    public Path navigationPath(LocalDate localDate) {
        return testDataDir.resolve("navigation" + localDate.toString() + ".json");
    }

    public String navigationJson() throws IOException {
        return readFileToString(testDataDir.resolve("navigation.json").toFile());
    }

    public String navigationJson(LocalDate localDate) throws IOException {
        return readFileToString(navigationPath(localDate).toFile());
    }
}
