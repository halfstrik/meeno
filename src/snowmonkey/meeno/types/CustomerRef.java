package snowmonkey.meeno.types;

import snowmonkey.meeno.Defect;

import java.util.UUID;

/**
 * Optional parameter allowing the client to pass a unique string (up to 32 chars) that is used to
 * de-dupe mistaken re-submissions.   CustomerRef can contain: upper/lower chars, digits, chars : - . _ + * : ; ~ only.
 */
public class CustomerRef extends MicroType<String> {
    public static final CustomerRef NONE = new CustomerRef("");

    protected CustomerRef(String value) {
        super(value);
    }

    public static CustomerRef unique() {
        return safeCustomerRef(UUID.randomUUID().toString());
    }

    public static CustomerRef customerRef(String value) {
        CustomerRef customerRef = safeCustomerRef(value);
        String safe = customerRef.asString();
        if (!safe.equals(value)) {
            throw new Defect("'" + value + "' is no a valid customer ref - see https://api.developer.betfair.com/services/webapps/docs/display/1smk3cen4v3lu3yomq5qye0ni/placeOrders");
        }
        return customerRef;
    }

    public static CustomerRef safeCustomerRef(String value) {
        String cleaned = value.replaceAll("[^a-zA-Z0-9\\._+*;~:-]", "");
        String chopped = cleaned.substring(0, Math.min(cleaned.length(), 32));
        return new CustomerRef(chopped);
    }

    public String asString() {
        return value;
    }
}
