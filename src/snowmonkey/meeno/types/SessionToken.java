package snowmonkey.meeno.types;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class SessionToken extends MicroType<String> {
    public SessionToken(String token) {
        super(token);
    }

    public static SessionToken parseJson(String json) {
        JsonObject parsed = new JsonParser().parse(json).getAsJsonObject();
        if (parsed.has("sessionToken")) {
            return new SessionToken(parsed.getAsJsonPrimitive("sessionToken").getAsString());
        } else {
            JsonPrimitive status = parsed.getAsJsonPrimitive("loginStatus");
            throw new IllegalStateException("Login failed: " + status.getAsString() + "\n" + json);
        }
    }

    public String asString() {
        return value;
    }
}
