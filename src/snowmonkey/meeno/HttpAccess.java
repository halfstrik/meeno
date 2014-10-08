package snowmonkey.meeno;

import com.google.gson.JsonIOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicNameValuePair;
import snowmonkey.meeno.requests.*;
import snowmonkey.meeno.types.*;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.*;

import static snowmonkey.meeno.DefaultProcessor.defaultProcessor;
import static snowmonkey.meeno.types.Locale.EN_US;
import static snowmonkey.meeno.types.MarketFilter.Builder.noFilter;

public class HttpAccess {

    public static final String UTF_8 = "UTF-8";
    public static final String X_APPLICATION = "X-Application";
    private final List<Auditor> auditors = new ArrayList<>();
    private final SessionToken sessionToken;
    private final AppKey appKey;
    private final Exchange exchange;
    private final RequestConfig conf;
    private final HttpClientBuilder httpClientBuilder;

    public HttpAccess(SessionToken sessionToken, AppKey appKey, Exchange exchange) {
        this(sessionToken, appKey, exchange, conf(), HttpClientBuilder.create());
    }

    public HttpAccess(SessionToken sessionToken, AppKey appKey, Exchange exchange, RequestConfig conf, HttpClientBuilder httpClientBuilder) {
        this.sessionToken = sessionToken;
        this.appKey = appKey;
        this.exchange = exchange;
        this.conf = conf;
        this.httpClientBuilder = httpClientBuilder;
    }

    private static HttpPost httpPost(URI uri, RequestConfig requestConfig) {
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setConfig(requestConfig);
        return httpPost;
    }

    private static HttpGet httpGet(URI uri, RequestConfig requestConfig) {
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setConfig(requestConfig);
        return httpGet;
    }

    private static RequestConfig conf() {
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setExpectContinueEnabled(true)
                .setStaleConnectionCheckEnabled(true)
                .build();

        return RequestConfig.copy(defaultRequestConfig)
                .setSocketTimeout(10 * 1000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(4000)
                .build();
    }

    public static SessionToken login(MeenoConfig config) {
        return login(
                config.certificateFile(),
                config.certificatePassword(),
                config.username(),
                config.password(),
                config.appKey()
        );
    }

    public static SessionToken login(File certFile, String certPassword, String betfairUsername, String betfairPassword, AppKey apiKey) {

        try {
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", socketFactory(certFile, certPassword))
                    .build();

            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

            connManager.setDefaultSocketConfig(SocketConfig.custom().build());
            connManager.setDefaultConnectionConfig(ConnectionConfig.custom().build());
            try (CloseableHttpClient client = HttpClients.custom()
                    .setConnectionManager(connManager)
                    .disableRedirectHandling()
                    .build()) {

                HttpPost httpPost = new HttpPost(Exchange.LOGIN_URI);
                List<NameValuePair> postFormData = new ArrayList<>();
                postFormData.add(new BasicNameValuePair("username", betfairUsername));
                postFormData.add(new BasicNameValuePair("password", betfairPassword));

                httpPost.setEntity(new UrlEncodedFormEntity(postFormData));

                httpPost.setHeader(X_APPLICATION, apiKey.asString());

                HttpResponse response = client.execute(httpPost);
                HttpEntity entity = response.getEntity();
                try (InputStream content = entity.getContent()) {
                    String json = DefaultProcessor.processResponse(response.getStatusLine(), content);
                    return SessionToken.parseJson(json);
                }
            } finally {
                connManager.close();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Cannot log in", e);
        }
    }

    private static SSLConnectionSocketFactory socketFactory(File certFile, String certPassword) throws Exception {
        SSLContext ctx = SSLContext.getInstance("TLS");
        KeyStore keyStore = KeyStore.getInstance("pkcs12");
        keyStore.load(new FileInputStream(certFile), certPassword.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, certPassword.toCharArray());
        KeyManager[] keyManagers = kmf.getKeyManagers();
        ctx.init(keyManagers, null, new SecureRandom());
        return new SSLConnectionSocketFactory(ctx, new StrictHostnameVerifier());
    }

    public void addAuditor(Auditor auditor) {
        this.auditors.add(auditor);
    }

    public void transferFunds(Processor processor, TransferFunds request) throws IOException, ApiException {
        String body = JsonSerialization.gson().toJson(request);
        sendPostRequest(processor, exchange.accountUris.jsonRestUri(Exchange.MethodName.TRANSFER_FUNDS), body);
    }

    public void cancelOrders(Processor processor, CancelOrders request) throws IOException, ApiException {
        sendPostRequest(processor, exchange.bettingUris.jsonRestUri(Exchange.MethodName.CANCEL_ORDERS), JsonSerialization.gson().toJson(request));
    }

    public void placeOrders(Processor processor, MarketId marketId, List<PlaceInstruction> instructions, CustomerRef customerRef) throws IOException, ApiException {
        PlaceOrders request = new PlaceOrders(marketId, instructions, customerRef);
        placeOrders(processor, request);
    }

    public void placeOrders(Processor processor, final PlaceOrders request) throws IOException, ApiException {
        sendPostRequest(processor, exchange.bettingUris.jsonRestUri(Exchange.MethodName.PLACE_ORDERS), JsonSerialization.gson().toJson(request));
    }

    public void listMarketBook(Processor processor, PriceProjection priceProjection, MarketId... marketId) throws IOException, ApiException {
        listMarketBook(processor, priceProjection, Arrays.asList(marketId), null, null);
    }

    public void listMarketBook(Processor processor, PriceProjection priceProjection, Iterable<MarketId> marketIds, OrderProjection orderProjection, MatchProjection matchProjection) throws IOException, ApiException {
        ListMarketBook request = new ListMarketBook(
                marketIds,
                priceProjection,
                orderProjection,
                matchProjection,
                null,
                EN_US
        );

        listMarketBook(processor, request);
    }

    public void listMarketBook(Processor processor, final ListMarketBook request) throws IOException, ApiException {
        sendPostRequest(processor, exchange.bettingUris.jsonRestUri(Exchange.MethodName.LIST_MARKET_BOOK), JsonSerialization.gson().toJson(request));
    }

    public void listMarketCatalogue(Processor processor, Collection<MarketProjection> marketProjection, MarketSort sort, MarketFilter marketFilter) throws IOException, ApiException {
        ListMarketCatalogue listMarketCatalogue = new ListMarketCatalogue(marketFilter, marketProjection, sort, 1000, EN_US);
        listMarketCatalogue(processor, listMarketCatalogue);
    }

    public void listMarketCatalogue(Processor processor, ListMarketCatalogue listMarketCatalogue) throws IOException, ApiException {
        sendPostRequest(processor, exchange.bettingUris.jsonRestUri(Exchange.MethodName.LIST_MARKET_CATALOGUE), JsonSerialization.gson().toJson(listMarketCatalogue));
    }

    public void listCountries(Processor processor) throws IOException, ApiException {
        listCountries(processor, noFilter());
    }

    public void listCountries(Processor processor, MarketFilter marketFilter) throws IOException, ApiException {
        ListCountries listCountries = new ListCountries(marketFilter, EN_US);
        listCountries(processor, listCountries);
    }

    public void listCountries(Processor processor, ListCountries listCountries) throws IOException, ApiException {
        sendPostRequest(processor, exchange.bettingUris.jsonRestUri(Exchange.MethodName.LIST_COUNTRIES), JsonSerialization.gson().toJson(listCountries));
    }

    public void listCurrentOrders(Processor processor, Set<BetId> betIds, Set<MarketId> marketIds, OrderProjection orderProjection, TimeRange dateRange, OrderBy orderBy, SortDir sortDir, int fromRecord) throws IOException, ApiException {
        TimeRange placedDateRange = null;

        ListCurrentOrders request = new ListCurrentOrders(
                betIds,
                marketIds,
                orderProjection,
                placedDateRange,
                dateRange,
                orderBy,
                sortDir,
                fromRecord,
                0
        );

        listCurrentOrders(processor, request);
    }

    public void listCurrentOrders(Processor processor, final ListCurrentOrders request) throws IOException, ApiException {
        sendPostRequest(processor, exchange.bettingUris.jsonRestUri(Exchange.MethodName.LIST_CURRENT_ORDERS), JsonSerialization.gson().toJson(request));
    }

    public void listClearedOrders(Processor processor, BetStatus betStatus, TimeRange between, int fromRecord) throws IOException, ApiException {
        ListClearedOrders request = new ListClearedOrders(
                betStatus,
                null,
                null,
                null,
                null,
                null,
                null,
                between,
                null,
                null,
                EN_US,
                fromRecord,
                0
        );

        listClearedOrders(processor, request);
    }

    public void listClearedOrders(Processor processor, final ListClearedOrders request) throws IOException, ApiException {
        sendPostRequest(processor, exchange.bettingUris.jsonRestUri(Exchange.MethodName.LIST_CLEARED_ORDERS), JsonSerialization.gson().toJson(request));
    }

    public void listCompetitions(Processor processor, MarketFilter marketFilter) throws IOException, ApiException {
        ListCompetitions listCompetitions = new ListCompetitions(marketFilter, EN_US);
        listCompetitions(processor, listCompetitions);
    }

    public void listCompetitions(Processor processor, ListCompetitions listCompetitions) throws IOException, ApiException {
        sendPostRequest(processor, exchange.bettingUris.jsonRestUri(Exchange.MethodName.LIST_COMPETITIONS), JsonSerialization.gson().toJson(listCompetitions));
    }

    public void listEventTypes(Processor processor, ListEventTypes request) throws IOException, ApiException {
        sendPostRequest(processor, exchange.bettingUris.jsonRestUri(Exchange.MethodName.LIST_EVENT_TYPES), JsonSerialization.gson().toJson(request));
    }

    public void listMarketTypes(Processor processor) throws IOException, ApiException {
        listMarketTypes(processor, noFilter());
    }

    public void listMarketTypes(Processor processor, MarketFilter marketFilter) throws IOException, ApiException {
        ListMarketTypes listMarketTypes = new ListMarketTypes(marketFilter, EN_US);
        listMarketTypes(processor, listMarketTypes);
    }

    public void listMarketTypes(Processor processor, ListMarketTypes listMarketTypes) throws IOException, ApiException {
        sendPostRequest(processor, exchange.bettingUris.jsonRestUri(Exchange.MethodName.LIST_MARKET_TYPES), JsonSerialization.gson().toJson(listMarketTypes));
    }

    public void listTimeRanges(Processor processor, TimeGranularity timeGranularity, MarketFilter marketFilter) throws IOException, ApiException {
        ListTimeRanges listTimeRanges = new ListTimeRanges(marketFilter, timeGranularity);
        listTimeRanges(processor, listTimeRanges);
    }

    public void listTimeRanges(Processor processor, ListTimeRanges listTimeRanges) throws IOException, ApiException {
        sendPostRequest(processor, exchange.bettingUris.jsonRestUri(Exchange.MethodName.LIST_TIME_RANGES), JsonSerialization.gson().toJson(listTimeRanges));
    }

    public void listEvents(Processor processor, MarketFilter marketFilter) throws IOException, ApiException {
        ListEvents listEvents = new ListEvents(marketFilter, EN_US);
        listEvents(processor, listEvents);
    }

    public void listEvents(Processor processor, ListEvents listEvents) throws IOException, ApiException {
        sendPostRequest(processor, exchange.bettingUris.jsonRestUri(Exchange.MethodName.LIST_EVENTS), JsonSerialization.gson().toJson(listEvents));
    }

    public void getAccountDetails(Processor processor) throws IOException, ApiException {
        sendPostRequest(processor, exchange.accountUris.jsonRestUri(Exchange.MethodName.GET_ACCOUNT_DETAILS), "");
    }

    public void getAccountFunds(Processor processor) throws IOException, ApiException {
        sendPostRequest(processor, exchange.accountUris.jsonRestUri(Exchange.MethodName.GET_ACCOUNT_FUNDS), "");
    }

    public void nav(Processor processor) throws IOException, ApiException {
        sendGetRequest(processor, Exchange.NAVIGATION, httpGet(Exchange.NAVIGATION, conf));
    }

    private void sendPostRequest(Processor processor, URI uri, String body) throws IOException, ApiException {
        HttpPost httpPost = httpPost(uri, conf);

        try (CloseableHttpClient httpClient = httpClientBuilder.build()) {

            applyHeaders(httpPost);

            httpPost.setEntity(new StringEntity(body, UTF_8));

            try {
                String responseBody = processResponse(processor, httpClient, httpPost);

                if (responseBody == null)
                    throw new IllegalStateException("There was no response body from POST to " + uri);

                for (Auditor auditor : auditors) {
                    auditor.auditPost(uri, body, responseBody);
                }
            } catch (DefaultProcessor.HttpException | IOException | JsonIOException | ApiException e) {
                for (Auditor auditor : auditors) {
                    auditor.auditPostFailure(uri, body, e);
                }
                throw e;
            }
        }
    }

    private void sendGetRequest(Processor processor, URI uri, HttpGet httpGet) throws IOException, ApiException {
        try (CloseableHttpClient httpClient = httpClientBuilder.build()) {

            applyHeaders(httpGet);

            String body = processResponse(processor, httpClient, httpGet);

            for (Auditor auditor : auditors) {
                auditor.auditGet(uri, body);
            }
        }
    }

    private void applyHeaders(AbstractHttpMessage abstractHttpMessage) {
        abstractHttpMessage.setHeader("Content-Type", "application/json");
        abstractHttpMessage.setHeader("Accept", "application/json");
        abstractHttpMessage.setHeader("Accept-Charset", UTF_8);
        abstractHttpMessage.setHeader(X_APPLICATION, appKey.asString());
        abstractHttpMessage.setHeader("X-Authentication", sessionToken.asString());
    }

    public void logout() throws IOException, ApiException {
        sendPostRequest(defaultProcessor(), Exchange.LOGOUT_URI, JsonSerialization.gson().toJson(noFilter()));
    }

    private String processResponse(Processor processor, CloseableHttpClient httpClient, HttpUriRequest httpPost) throws IOException, ApiException {
        long start = System.currentTimeMillis();

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            try (InputStream inputStream = entity.getContent()) {
                return processor.process(response.getStatusLine(), inputStream);
            }
        } catch (ConnectTimeoutException e) {
            long time = (System.currentTimeMillis() - start);
            throw new TimeoutException("Connection timed out after ~" + time + "ms", e);
        } catch (SocketTimeoutException e) {
            long time = (System.currentTimeMillis() - start);
            throw new TimeoutException("Socket timed out after ~" + time + "ms", e);
        } catch (JsonIOException e) {
            long time = (System.currentTimeMillis() - start);
            throw new TimeoutException("Read timed out after ~" + time + "ms", e);
        }
    }

    public static interface Auditor {
        default void auditPostFailure(URI uri, String body, Exception whatWentWrong) {
            System.out.println("[post " + uri + "]");
            System.out.println("--> " + body);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            whatWentWrong.printStackTrace(new PrintStream(out));
            System.out.println("<-- " + new String(out.toByteArray()));
        }

        default void auditPost(URI uri, String body, String response) {
            System.out.println("[post " + uri + "]");
            System.out.println("--> " + body);
            System.out.println("<-- " + response);
        }

        default void auditGet(URI uri, String response) {
            System.out.println("[get " + uri + "]");
            System.out.println("<-- " + response);
        }
    }

    public interface Processor {
        String process(StatusLine statusLine, InputStream in) throws IOException, ApiException;
    }

    private static class TimeoutException extends IOException {
        public TimeoutException(String message, Exception cause) {
            super(message, cause);
        }
    }
}
