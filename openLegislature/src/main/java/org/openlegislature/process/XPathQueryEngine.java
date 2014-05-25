package org.openlegislature.process;

import static javax.xml.parsers.DocumentBuilderFactory.newInstance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

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

import org.openlegislature.util.OpenLegislatureConstants;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class XPathQueryEngine {

    private static final DocumentBuilderFactory FACTORY = newInstance();

    private volatile List<File> fileList = new Vector<File>();
    private volatile Map<File, Document> docChache = new ConcurrentHashMap<File, Document>();
    private volatile OpenLegislatureConstants constants;
    
    private XPathFactory xPathfactory = XPathFactory.newInstance();
    private Transformer nodePrinter;

    @Inject
    public XPathQueryEngine(OpenLegislatureConstants constants) throws ParserConfigurationException, TransformerConfigurationException {
        this.constants = constants;
		nodePrinter = TransformerFactory.newInstance().newTransformer();
    }

    public void add(File xmlFile) throws Exception {
        try {
        	fileList.add(xmlFile);
        	if(constants.isXpathInMemory()){
        		DocumentBuilder db = FACTORY.newDocumentBuilder();
        		docChache.put(xmlFile, db.parse(xmlFile));
        	} 
            System.err.println(fileList.size());
        } catch (Exception e) {
            PrintWriter pw = new PrintWriter(new FileOutputStream(new File("fail-files.txt"), true));
            pw.println(xmlFile.toString());
            pw.println(e.toString());
            pw.close();
            throw e;
        }
    }

    public void query(String xpath) throws XPathExpressionException, TransformerException, SAXException, IOException, ParserConfigurationException {
        System.out.println(fileList.size());
        XPath xpathExpr = xPathfactory.newXPath();
        XPathExpression expr = xpathExpr.compile(xpath);
        for(File file: fileList){
        	try {
        		Document doc;
        		if(constants.isXpathInMemory()){
					doc = FACTORY.newDocumentBuilder().parse(file);
        		} else {
        			doc = docChache.get(file);
        		}
				applyXPathExpression(expr, doc);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }

	private void applyXPathExpression(XPathExpression expr, Document doc) throws XPathExpressionException, TransformerException {
		NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for(int i = 0; i < nodeList.getLength(); i++){
		    nodePrinter.transform(new DOMSource(nodeList.item(i)), new StreamResult(new BufferedWriter(new PrintWriter(System.out))));
		}
	}

}
