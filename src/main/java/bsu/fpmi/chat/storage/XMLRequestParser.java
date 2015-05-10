package bsu.fpmi.chat.storage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class XMLRequestParser {
    private static final String REQUESTS_LOCATION = System.getProperty("user.home") +  File.separator + "requests.xml";

    private static final String REQUEST = "request";
    private static final String ID = "id";
    private static final String ACTION = "action";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat ("dd.MM");
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat ("hh:mm:ss");
    public static final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat ("dd-MM-yyyy hh:mm");

    private XMLRequestParser() {
    }

    public static synchronized boolean doesStorageExist() {
        File file = new File(REQUESTS_LOCATION);
        return file.exists();
    }

    public static synchronized void createRequestsStorage() throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("requests");
        doc.appendChild(rootElement);

        Transformer transformer = getTransformer();

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(REQUESTS_LOCATION));
        transformer.transform(source, result);
    }

    public static synchronized void addRequest(String id) throws ParserConfigurationException, SAXException, IOException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document document = docBuilder.parse(REQUESTS_LOCATION);
        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();

        Element idElement = document.createElement(ID);
        idElement.appendChild(document.createTextNode(id));
        root.appendChild(idElement);

        Transformer transformer = getTransformer();
        DOMSource source = new DOMSource(document);

        StreamResult result = new StreamResult(REQUESTS_LOCATION);
        transformer.transform(source, result);
    }

    public static synchronized List<String> getRequests(int start) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(REQUESTS_LOCATION);
        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();
        NodeList requestList = root.getElementsByTagName(ID);

        List<String> ids = new ArrayList<>();
        for (int i = start; i < requestList.getLength(); i++) {
            Element requestTag = (Element) requestList.item(i);

            String id = requestTag.getTextContent();
            if (!ids.contains(id))
                ids.add(id);
        }

        return ids;
    }

    private static Transformer getTransformer() throws TransformerConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // Formatting XML properly
        return transformer;
    }

    public static int getRequestsAmount() throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(REQUESTS_LOCATION);
        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();
        return root.getElementsByTagName(ID).getLength();
    }
}
