package snowmonkey.meeno;

import org.apache.http.StatusLine;
import snowmonkey.meeno.types.MarketCatalogues;
import snowmonkey.meeno.types.MarketId;
import snowmonkey.meeno.types.Navigation;
import snowmonkey.meeno.types.raw.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;
import static snowmonkey.meeno.JsonSerialization.parse;
import static snowmonkey.meeno.types.raw.MarketProjection.allMarketProjections;

public class HttpExchangeOperations implements ExchangeOperations {

    private final HttpAccess httpAccess;

    public HttpExchangeOperations(HttpAccess httpAccess) {
        this.httpAccess = httpAccess;
    }

    public MarketCatalogues marketCatalogue(MarketFilter marketFilter) throws ApiException {
        MarketSort marketSort = MarketSort.FIRST_TO_START;
        return marketCatalogue(allMarketProjections(), marketSort, marketFilter);
    }

    public MarketCatalogues marketCatalogue(Iterable<MarketProjection> marketProjections, MarketSort marketSort, MarketFilter marketFilter) throws ApiException {
        try {
            JsonProcessor processor = new JsonProcessor();
            httpAccess.listMarketCatalogue(processor, marketProjections, marketSort, marketFilter);
            MarketCatalogue[] catalogues = JsonSerialization.parse(processor.json, MarketCatalogue[].class);
            return MarketCatalogues.createMarketCatalogues(catalogues);
        } catch (IOException e) {
            throw new RuntimeEnvironmentException("listMarketCatalogue call failed", e);
        }
    }

    public Navigation navigation() throws ApiException {
        try {
            JsonProcessor processor = new JsonProcessor();
            httpAccess.nav(processor);
            return Navigation.parse(processor.json);
        } catch (IOException e) {
            throw new RuntimeEnvironmentException("navigation call failed", e);
        }
    }

    public MarketBook marketBook(MarketId marketId) throws ApiException, NotFoundException {
        MarketBooks marketBooks = marketBooks(newArrayList(marketId));
        return marketBooks.get(marketId);
    }

    public MarketBooks marketBooks(MarketId marketIds) throws ApiException {
        return marketBooks(newArrayList(marketIds));
    }

    public MarketBooks marketBooks(Iterable<MarketId> marketIds) throws ApiException {
        try {
            JsonProcessor processor = new JsonProcessor();

            httpAccess.listMarketBook(
                    processor,
                    new PriceProjection(
                            new ArrayList<>(),
                            null,
                            false,
                            false
                    ),
                    marketIds,
                    null,
                    null
            );

            return MarketBooks.parseMarketBooks(parse(processor.json, MarketBook[].class));
        } catch (IOException e) {
            throw new RuntimeEnvironmentException("listMarketBook call failed", e);
        }
    }

    public static class RuntimeEnvironmentException extends RuntimeException {
        public RuntimeEnvironmentException(String message, Exception cause) {
            super(message, cause);
        }
    }

    private static class JsonProcessor implements HttpAccess.Processor {
        public String json;

        @Override
        public String process(StatusLine statusLine, InputStream in) throws IOException, ApiException {
            json = DefaultProcessor.processResponse(statusLine, in);
            return json;
        }
    }
}
