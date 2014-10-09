package live.raw;

import helper.TestData;
import live.AbstractLiveTestCase;
import org.junit.Test;
import snowmonkey.meeno.JsonSerialization;
import snowmonkey.meeno.types.AccountDetailsResponse;
import snowmonkey.meeno.types.AccountFundsResponse;

import static helper.TestData.fileWriter;
import static org.apache.commons.io.FileUtils.readFileToString;

public class AccountOperationsTest extends AbstractLiveTestCase {
    @Test
    public void testGetAccountFunds() throws Exception {
        ukHttpAccess.getAccountFunds(fileWriter(TestData.generated().getAccountFundsPath()));

        AccountFundsResponse response = JsonSerialization.parse(readFileToString(TestData.generated().getAccountFundsPath().toFile()), AccountFundsResponse.class);

        System.out.println("response = " + response);
    }

    @Test
    public void testGetAccountDetails() throws Exception {
        ukHttpAccess.getAccountDetails(fileWriter(TestData.generated().getAccountDetailsPath()));

        AccountDetailsResponse accountDetailsResponse = JsonSerialization.parse(readFileToString(TestData.generated().getAccountDetailsPath().toFile()), AccountDetailsResponse.class);

        System.out.println("accountDetailsResponse = " + accountDetailsResponse);
    }
}
