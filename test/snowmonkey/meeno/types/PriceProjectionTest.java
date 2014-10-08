package snowmonkey.meeno.types;

import org.junit.Test;

public class PriceProjectionTest {
    @Test(expected = IllegalArgumentException.class)
    public void constructor_withNullPriceData() {
        new PriceProjection(null, new ExBestOfferOverRides(3, RollupModel.STAKE, null, null, null), true, false);
    }
}
