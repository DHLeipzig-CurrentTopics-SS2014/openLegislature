package org.openlegislature.process;

import static javax.xml.parsers.DocumentBuilderFactory.newInstance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openlegislature.util.Logger;
import org.openlegislature.util.OpenLegislatureConstants;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class XPathQueryEngine {

    private static final DocumentBuilderFactory FACTORY = newInstance();

    static {
        FACTORY.setNamespaceAware(false); // never forget this!
        FACTORY.setCoalescing(false);
        FACTORY.setValidating(false);
    }

    private List<File> fileList = new Vector<File>();
    private List<Document> docList = new Vector<Document>();

    private volatile OpenLegislatureConstants constants;

    private XPathFactory xPathfactory = XPathFactory.newInstance();
    private Transformer nodePrinter;

    private Writer stream;
    
    @Inject
    public XPathQueryEngine(OpenLegislatureConstants constants) throws ParserConfigurationException, TransformerConfigurationException {
        this.constants = constants;
        nodePrinter = TransformerFactory.newInstance().newTransformer();
    }

    public void add(File xmlFile) throws Exception {
        try {
            fileList.add(xmlFile);
            if (constants.isXpathInMemory()) {
                DocumentBuilder db = FACTORY.newDocumentBuilder();
                Document doc = parseDocument(db, xmlFile);
                docList.add(doc);
            }
        } catch (Exception e) {
            PrintWriter pw = new PrintWriter(new FileOutputStream(new File("fail-files2.txt"), true));
            pw.println(xmlFile.toString());
            pw.println(e.toString());
            pw.close();
            throw e;
        }
    }

    private Document parseDocument(DocumentBuilder db, File xmlFile) throws SAXException, IOException {
        InputSource source = new InputSource(new BufferedReader(new InputStreamReader(new FileInputStream(xmlFile), constants.getEncoding())));
        return db.parse(source);
    }

    public void query(String xpath) throws Exception {
        Logger.getInstance().info("count of available files: " + fileList.size());
        XPath xpathExpr = xPathfactory.newXPath();
        XPathExpression expr = xpathExpr.compile(xpath);
        stream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("query-results" + new Date() + ".txt", true), constants.getEncoding()));
        if (constants.isXpathInMemory()) {
            for (Document doc : docList) {
                try {
                    applyXPathExpression(expr, doc);
                } catch (Exception e) {
                    Logger.getInstance().error(ExceptionUtils.getStackTrace(e));
                }
            }
        } else {
            for (File file : fileList) {
                try {
                	String curFilename = file.getName();
					stream.append(curFilename + "\n");
                    Document doc;
                    doc = parseDocument(FACTORY.newDocumentBuilder(), file);
                    applyXPathExpression(expr, doc);
                } catch (Exception e) {
                	PrintWriter pw = new PrintWriter(new FileOutputStream(new File("fail-files-xpath.txt"), true));
                	pw.println(file.toString());
                	pw.println(ExceptionUtils.getStackTrace(e));
                	pw.close();
                	Logger.getInstance().error(String.format("File %s is not wellformed:", file.getName()));
                	Logger.getInstance().error(ExceptionUtils.getStackTrace(e));
                }
            }
        }
        IOUtils.closeQuietly(stream);
        System.out.println("processing finished");
    }

    private void applyXPathExpression(XPathExpression expr, Document doc) throws XPathExpressionException, TransformerException, IOException {
        NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
			nodePrinter.transform(new DOMSource(nodeList.item(i)), new StreamResult(stream));
        }
        stream.append("\n\n");
    }

}
