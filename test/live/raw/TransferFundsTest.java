package live.raw;

import helper.TestData;
import live.AbstractLiveTestCase;
import org.junit.Test;
import snowmonkey.meeno.HttpAccess;
import snowmonkey.meeno.JsonSerialization;
import snowmonkey.meeno.requests.TransferFunds;
import snowmonkey.meeno.types.TransferResponse;

import static org.apache.commons.io.FileUtils.readFileToString;
import static snowmonkey.meeno.types.Wallet.AUSTRALIAN;
import static snowmonkey.meeno.types.Wallet.UK;

public class TransferFundsTest extends AbstractLiveTestCase {
    @Test
    public void testTransferFunds() throws Exception {
        ukHttpAccess.addAuditor(new HttpAccess.Auditor() {
        });
        ukHttpAccess.transferFunds(TestData.fileWriter(TestData.generated().transferFundsPath()),
                new TransferFunds(UK, AUSTRALIAN, 2d));

        TransferResponse response = JsonSerialization.parse(
                readFileToString(TestData.generated().transferFundsPath().toFile()), TransferResponse.class);

        System.out.println("response = " + response);
    }
}
