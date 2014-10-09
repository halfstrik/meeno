package snowmonkey.meeno;

import helper.TestData;
import org.junit.Test;
import snowmonkey.meeno.types.SessionToken;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.assertEquals;

public class SessionTokenTest {
    @Test
    public void testParseJson() throws Exception {
        String json = readFileToString(TestData.unitTest().loginPath().toFile());
        SessionToken sessionToken = SessionToken.parseJson(json);
        assertEquals("arf/eWGaEy3hifh5erGaeT7c/sDjfDhR5IFJf01hGCa=", sessionToken.asString());
    }
}
