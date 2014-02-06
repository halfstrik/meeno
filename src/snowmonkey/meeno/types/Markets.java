package snowmonkey.meeno.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.joda.time.DateTime;
import snowmonkey.meeno.Defect;
import snowmonkey.meeno.HttpAccess;
import snowmonkey.meeno.types.raw.*;

import java.util.*;

import static snowmonkey.meeno.types.Events.printElement;

public class Markets implements Iterable<MarketCatalogue> {
    private final Map<MarketId, MarketCatalogue> markets = new LinkedHashMap<>();

    public static Markets parse(String json) {
        Markets markets = new Markets();

        JsonElement parsed = new JsonParser().parse(json);

        for (JsonElement jsonElement : parsed.getAsJsonArray()) {
            try {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                MarketId marketId = new MarketId(jsonObject.getAsJsonPrimitive("marketId").getAsString());
                String marketName = jsonObject.getAsJsonPrimitive("marketName").getAsString();

                JsonObject description = jsonObject.get("description").getAsJsonObject();

                MarketDescription marketDescription = new MarketDescription(
                        description.getAsJsonPrimitive("persistenceEnabled").getAsBoolean(),
                        description.getAsJsonPrimitive("bspMarket").getAsBoolean(),
                        HttpAccess.DATE_TIME_FORMATTER.parseDateTime(description.getAsJsonPrimitive("marketTime").getAsString()),
                        HttpAccess.DATE_TIME_FORMATTER.parseDateTime(description.getAsJsonPrimitive("suspendTime").getAsString()),
                        optionalDateTime(description, "settleTime"),
                        MarketBettingType.valueOf(description.getAsJsonPrimitive("bettingType").getAsString()),
                        description.getAsJsonPrimitive("turnInPlayEnabled").getAsBoolean(),
                        description.getAsJsonPrimitive("marketType").getAsString(),
                        optionalString(description, "regulator"),
                        description.getAsJsonPrimitive("marketBaseRate").getAsDouble(),
                        description.getAsJsonPrimitive("discountAllowed").getAsBoolean(),
                        optionalString(description, "wallet"),
                        optionalString(description, "rules"),
                        optionalBool(description, "rulesHasDate"),
                        optionalString(description, "clarifications")
                );

                List<RunnerCatalog> runners = new ArrayList<>();
                for (JsonElement runnerElement : jsonObject.get("runners").getAsJsonArray()) {
                    JsonObject obj = runnerElement.getAsJsonObject();
                    RunnerCatalog runnerCatalog = new RunnerCatalog(
                            obj.getAsJsonPrimitive("selectionId").getAsLong(),
                            obj.getAsJsonPrimitive("runnerName").getAsString(),
                            obj.getAsJsonPrimitive("handicap").getAsDouble()
                    );
                    runners.add(runnerCatalog);
                }

                JsonObject eventTypeObj = jsonObject.get("eventType").getAsJsonObject();
                EventType eventType = new EventType(
                        eventTypeObj.getAsJsonPrimitive("id").getAsString(),
                        eventTypeObj.getAsJsonPrimitive("name").getAsString()
                );

                JsonObject competitionObj = jsonObject.get("competition").getAsJsonObject();
                Competition competition = new Competition(
                        competitionObj.getAsJsonPrimitive("id").getAsString(),
                        competitionObj.getAsJsonPrimitive("name").getAsString()
                );

                JsonObject eventObj = jsonObject.get("event").getAsJsonObject();
                Event event = new Event(
                        eventObj.getAsJsonPrimitive("id").getAsString(),
                        eventObj.getAsJsonPrimitive("name").getAsString(),
                        eventObj.getAsJsonPrimitive("countryCode").getAsString(),
                        eventObj.getAsJsonPrimitive("timezone").getAsString(),
                        optionalString(eventObj, "venue"),
                        optionalDateTime(eventObj, "openDate")
                );

                MarketCatalogue marketCatalogue = new MarketCatalogue(marketId, marketName, marketDescription, runners, eventType, competition, event);
                markets.markets.put(marketId, marketCatalogue);
            } catch (RuntimeException e) {
                throw new Defect("Cannot parse:\n" + printElement(jsonElement), e);
            }
        }
        return markets;
    }

    private static DateTime optionalDateTime(JsonObject description, String name) {
        return description.has(name) ? HttpAccess.DATE_TIME_FORMATTER.parseDateTime(description.getAsJsonPrimitive(name).getAsString()) : null;
    }

    private static String optionalString(JsonObject description, String name) {
        return description.has(name) ? description.getAsJsonPrimitive(name).getAsString() : null;
    }

    private static Boolean optionalBool(JsonObject description, String name) {
        return description.has(name) ? description.getAsJsonPrimitive(name).getAsBoolean() : null;
    }

    @Override
    public Iterator<MarketCatalogue> iterator() {
        return markets.values().iterator();
    }

    public MarketCatalogue get(MarketId marketId) {
        MarketCatalogue marketCatalogue = markets.get(marketId);
        if (marketCatalogue == null) {
            throw new Defect("There is no market for id '" + marketId + "'");
        }
        return marketCatalogue;
    }
}