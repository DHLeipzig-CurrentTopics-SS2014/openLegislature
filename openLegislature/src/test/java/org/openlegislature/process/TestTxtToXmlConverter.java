package org.openlegislature.process;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestTxtToXmlConverter {

	@Test
	public void testInterjectionIsNotDiscoveredAtEndOfDoc01010() throws Exception {
		File fixture = new File("src/test/resources/protocols-txt/01010.cleaned.txt");
		TxtToXmlConverter testable = new TxtToXmlConverter();
		File outputFile = new File("target/test-classes/01010.xml");
		testable.convertToXml(fixture, outputFile);

		testable.convertToXml(new File("src/test/resources/protocols-txt/02002.cleaned.txt"), new File("target/test-classes/02002.xml"));
		testable.convertToXml(new File("src/test/resources/protocols-txt/03006.cleaned.txt"), new File("target/test-classes/03006.xml"));
		testable.convertToXml(new File("src/test/resources/protocols-txt/04030.cleaned.txt"), new File("target/test-classes/04030.xml"));
		testable.convertToXml(new File("src/test/resources/protocols-txt/05004.cleaned.txt"), new File("target/test-classes/05004.xml"));
		testable.convertToXml(new File("src/test/resources/protocols-txt/06001.cleaned.txt"), new File("target/test-classes/06001.xml"));
		testable.convertToXml(new File("src/test/resources/protocols-txt/07185.cleaned.txt"), new File("target/test-classes/07185.xml"));
		testable.convertToXml(new File("src/test/resources/protocols-txt/08005.cleaned.txt"), new File("target/test-classes/08005.xml"));
		testable.convertToXml(new File("src/test/resources/protocols-txt/09003.cleaned.txt"), new File("target/test-classes/09003.xml"));
		testable.convertToXml(new File("src/test/resources/protocols-txt/06145.cleaned.txt"), new File("target/test-classes/06145.xml"));
		testable.convertToXml(new File("src/test/resources/protocols-txt/06146.cleaned.txt"), new File("target/test-classes/06146.xml"));
		testable.convertToXml(new File("src/test/resources/protocols-txt/06147.cleaned.txt"), new File("target/test-classes/06147.xml"));
		testable.convertToXml(new File("src/test/resources/protocols-txt/06148.cleaned.txt"), new File("target/test-classes/06148.xml"));
		testable.convertToXml(new File("src/test/resources/protocols-txt/06149.cleaned.txt"), new File("target/test-classes/06149.xml"));
		testable.convertToXml(new File("src/test/resources/protocols-txt/06150.cleaned.txt"), new File("target/test-classes/06150.xml"));

		String expecptedString = IOUtils.toString(new FileInputStream(new File("src/test/resources/protocols-txt/01010.xml")));
		String resultingString = IOUtils.toString(new FileInputStream(outputFile));
		assertEquals(expecptedString, resultingString);
		
		assertEquals(IOUtils.toString(new FileInputStream(new File("src/test/resources/protocols-txt/02002.xml"))), IOUtils.toString(new FileInputStream(new File("target/test-classes/02002.xml"))));
		assertEquals(IOUtils.toString(new FileInputStream(new File("src/test/resources/protocols-txt/03006.xml"))), IOUtils.toString(new FileInputStream(new File("target/test-classes/03006.xml"))));
		assertEquals(IOUtils.toString(new FileInputStream(new File("src/test/resources/protocols-txt/04030.xml"))), IOUtils.toString(new FileInputStream(new File("target/test-classes/04030.xml"))));
		assertEquals(IOUtils.toString(new FileInputStream(new File("src/test/resources/protocols-txt/05004.xml"))), IOUtils.toString(new FileInputStream(new File("target/test-classes/05004.xml"))));

	}
}
