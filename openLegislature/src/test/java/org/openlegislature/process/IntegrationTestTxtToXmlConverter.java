package org.openlegislature.process;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openlegislature.util.OpenLegislatureConstants;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import static javax.xml.parsers.DocumentBuilderFactory.newInstance;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class IntegrationTestTxtToXmlConverter {

    @Parameterized.Parameters(name = "test for protocol: {0}")
    public static Iterable<Object[]> initParams() {
        return Arrays.asList(new Object[][]{
                {"src/test/resources/protocols-txt/01095", "target/test-classes/01095.xml"},
                {"src/test/resources/protocols-txt/01139", "target/test-classes/01139.xml"},

                {"src/test/resources/protocols-txt/18023", "target/test-classes/18023.xml"},
                {"src/test/resources/protocols-txt/01010", "target/test-classes/01010.xml"},
                {"src/test/resources/protocols-txt/02002", "target/test-classes/02002.xml"},
                {"src/test/resources/protocols-txt/03006", "target/test-classes/03006.xml"},
                {"src/test/resources/protocols-txt/04030", "target/test-classes/04030.xml"},
                {"src/test/resources/protocols-txt/05004", "target/test-classes/05004.xml"},
                {"src/test/resources/protocols-txt/06001", "target/test-classes/06001.xml"},
                {"src/test/resources/protocols-txt/07185", "target/test-classes/07185.xml"},
                {"src/test/resources/protocols-txt/08005", "target/test-classes/08005.xml"},
                {"src/test/resources/protocols-txt/09003", "target/test-classes/09003.xml"},
                {"src/test/resources/protocols-txt/06145", "target/test-classes/06145.xml"},
                {"src/test/resources/protocols-txt/06146", "target/test-classes/06146.xml"},
                {"src/test/resources/protocols-txt/06147", "target/test-classes/06147.xml"},
                {"src/test/resources/protocols-txt/06148", "target/test-classes/06148.xml"},
                {"src/test/resources/protocols-txt/06149", "target/test-classes/06149.xml"},
                {"src/test/resources/protocols-txt/06150", "target/test-classes/06150.xml"},
                {"src/test/resources/protocols-txt/01002", "target/test-classes/01002.xml"},
                {"src/test/resources/protocols-txt/01008", "target/test-classes/01008.xml"},
                {"src/test/resources/protocols-txt/01012", "target/test-classes/01012.xml"},
                {"src/test/resources/protocols-txt/01018", "target/test-classes/01018.xml"}
        });
    }

    @Parameterized.Parameter
    public String pathPartToResources;
    @Parameterized.Parameter(1)
    public String pathToOutputFile;

    private File expectedFile;
    private File resultFile;

    private DocumentBuilderFactory factory = newInstance();

    @Before
    public void setUp() throws Exception {
        TxtToXmlConverter testable = new TxtToXmlConverter(new OpenLegislatureConstants());
        expectedFile = new File(pathPartToResources + ".xml");
        File inputFile = new File(pathPartToResources + ".cleaned.txt");
        resultFile = new File(pathToOutputFile);
        testable.convertToXml(inputFile, resultFile);
    }

    @Test
	public void testExpectedXmlHolds() throws Exception {
        assertEquals(fileToText(expectedFile), fileToText(resultFile));
	}

    String fileToText(File obj) throws IOException {
        return IOUtils.toString(new FileReader(obj));
    }

    @Test
    public void testParseXml() throws Exception {
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.parse(new FileInputStream(resultFile));
    }

}
