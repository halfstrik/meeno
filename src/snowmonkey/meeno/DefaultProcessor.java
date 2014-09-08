package snowmonkey.meeno;

import com.google.gson.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.StatusLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class DefaultProcessor {
    private DefaultProcessor() {
    }

    public static String processResponse(StatusLine statusLine, InputStream in) throws IOException, ApiException {
        try (Reader reader = new InputStreamReader(in, HttpAccess.UTF_8)) {
            JsonElement parsed = new JsonParser().parse(reader);

            try {
                if (statusLine.getStatusCode() != 200) {
                    JsonObject object = parsed.getAsJsonObject();

                    if (object.has("detail")) {
                        JsonObject detail = object.getAsJsonObject("detail");
                        if (detail.has("exceptionname")) {
                            String exceptionName = detail.getAsJsonPrimitive("exceptionname").getAsString();
                            JsonObject exception = detail.getAsJsonObject(exceptionName);
                            throw new ApiException(
                                    exception.getAsJsonPrimitive("errorDetails").getAsString(),
                                    exception.getAsJsonPrimitive("errorCode").getAsString(),
                                    exception.getAsJsonPrimitive("requestUUID").getAsString()
                            );
                        }
                    }

                    if (!object.has("faultstring"))
                        throw new Defect("Bad status code: " + statusLine.getStatusCode() + "\n" + IOUtils.toString(in));

                    String faultString = object.getAsJsonPrimitive("faultstring").getAsString();

                    switch (faultString) {
                        case "DSC-0018": {
                            throw new HttpException("A parameter marked as mandatory was not provided");
                        }
                        default:
                            throw new HttpException("Bad status code " + statusLine.getStatusCode() + ":\n" + IOUtils.toString(in));
                    }
                }
            } catch (HttpException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new Defect("Failed to parse: " + prettyPrintJson(parsed), e);
            }

            return prettyPrintJson(parsed);
        }
    }

    public static class HttpException extends RuntimeException {
        public HttpException(String message) {
            super(message);
        }

        public HttpException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static Gson gson() {
        return new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();
    }

    public static String prettyPrintJson(JsonElement parse) {
        return gson()
                .toJson(parse);
    }
}
