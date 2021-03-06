package snowmonkey.meeno.requests;

import snowmonkey.meeno.types.ImmutbleType;
import snowmonkey.meeno.types.Locale;
import snowmonkey.meeno.types.MarketFilter;

public class ListEvents extends ImmutbleType {
    public final MarketFilter marketFilter;
    public final Locale locale;

    public ListEvents(MarketFilter marketFilter, Locale locale) {
        this.marketFilter = marketFilter;
        this.locale = locale;
    }
}
