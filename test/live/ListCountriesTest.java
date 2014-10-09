package live;

import helper.TestData;
import org.junit.Test;

import static helper.TestData.fileWriter;
import static org.apache.commons.io.FileUtils.readFileToString;

public class ListCountriesTest extends AbstractLiveTestCase {
    @Test
    public void test() throws Exception {
        ukHttpAccess.listCountries(fileWriter(TestData.generated().listCountriesPath()));

        String s = readFileToString(TestData.generated().listCountriesPath().toFile());

        System.out.println(s);
    }
}
