package bsu.fpmi.chat.storage;

import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static bsu.fpmi.chat.util.ServletUtil.*;

public class XMLHistoryParser {
    private static final String STORAGE_LOCATION = System.getProperty("user.home") +  File.separator + "history.xml";

    private static final String MESSAGE = "message";
    private static final String ID = "id";
    private static final String USERNAME = "username";
    private static final String TEXT = "text";
    private static final String TIME = "time";
    private static final String LOG_TIME = "log-time";
    private static final String EDITED = "edited";
    private static final String DELETED = "deleted";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat ("dd.MM");
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat ("hh:mm:ss");
    public static final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat ("dd-MM-yyyy hh:mm");

    private XMLHistoryParser() {
    }

    public static synchronized boolean doesStorageExist() {
        File file = new File(STORAGE_LOCATION);
        return file.exists();
    }

    public static synchronized void createStorage() throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("messages");
        doc.appendChild(rootElement);

        Transformer transformer = getTransformer();

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
        transformer.transform(source, result);
    }

    public static synchronized void addToStorage(JSONObject message) throws ParserConfigurationException, SAXException, IOException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document document = docBuilder.parse(STORAGE_LOCATION);
        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();
        Element messageTag = document.createElement(MESSAGE);
        root.appendChild(messageTag);

        String id = UUID.randomUUID().toString();
        messageTag.setAttribute(ID, id);
        XMLRequestParser.addRequest(id);

        Element username = document.createElement(USERNAME);
        username.appendChild(document.createTextNode((String) message.get(USERNAME)));
        messageTag.appendChild(username);

        Element text = document.createElement(TEXT);
        text.appendChild(document.createTextNode((String) message.get(TEXT)));
        messageTag.appendChild(text);

        Element time = document.createElement(TIME);
        Element logTime = document.createElement(LOG_TIME);

        Date date = new Date();
        StringBuilder timeString = new StringBuilder(DATE_FORMAT.format(date))
                .append("<br>").append(TIME_FORMAT.format(date));

        time.appendChild(document.createTextNode(timeString.toString()));
        logTime.appendChild(document.createTextNode(LOG_DATE_FORMAT.format(date)));

        messageTag.appendChild(time);
        messageTag.appendChild(logTime);

        Element edited = document.createElement(EDITED);
        edited.appendChild(document.createTextNode(message.get(EDITED).toString()));
        messageTag.appendChild(edited);

        Element deleted = document.createElement(DELETED);
        deleted.appendChild(document.createTextNode(message.get(DELETED).toString()));
        messageTag.appendChild(deleted);

        Transformer transformer = getTransformer();
        DOMSource source = new DOMSource(document);

        StreamResult result = new StreamResult(STORAGE_LOCATION);
        transformer.transform(source, result);
    }

    public static synchronized boolean update(JSONObject message) throws ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(STORAGE_LOCATION);
        document.getDocumentElement().normalize();

        String id = (String) message.get(ID);
        Node messageToUpdate = getNodeById(document, id);
        XMLRequestParser.addRequest(id);

        if (messageToUpdate != null) {
            NodeList childNodes = messageToUpdate.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);

                if (TEXT.equals(node.getNodeName())) {
                    node.setTextContent((String) message.get(TEXT));
                } else if (EDITED.equals(node.getNodeName())) {
                    node.setTextContent(message.get(EDITED).toString());
                } else if (DELETED.equals(node.getNodeName())) {
                    node.setTextContent(message.get(DELETED).toString());
                }
            }

            Transformer transformer = getTransformer();

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
            transformer.transform(source, result);
        } else {
            return false;
        }

        return true;
    }

    public static synchronized boolean remove(String id) throws ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(STORAGE_LOCATION);
        document.getDocumentElement().normalize();

        Node messageToDelete = getNodeById(document, id);
        XMLRequestParser.addRequest(id);

        if (messageToDelete != null) {
            NodeList childNodes = messageToDelete.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);

                if (TEXT.equals(node.getNodeName())) {
                    node.setTextContent("This message has been deleted.");
                } else if (DELETED.equals(node.getNodeName())) {
                    node.setTextContent("true");
                }
            }

            Transformer transformer = getTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
            transformer.transform(source, result);
        } else {
            return false;
        }

        return true;
    }

    public static synchronized String getMessagesFrom(int start) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(STORAGE_LOCATION);
        document.getDocumentElement().normalize();

        JSONArray messages = new JSONArray();
        JSONObject jsonMessage;
        jsonMessage = new JSONObject();

        List<String> ids = XMLRequestParser.getRequests(start);

        for (String id : ids) {
            Node message = getNodeById(document, id);
            NodeList childNodes = message.getChildNodes();

            jsonMessage.put(ID, id);
            String nodeValue;

            for (int j = 0; j < childNodes.getLength(); j++) {
                Node node = childNodes.item(j);
                nodeValue = node.getTextContent();

                if (TEXT.equals(node.getNodeName())) {
                    jsonMessage.put(TEXT, nodeValue);
                } else if (EDITED.equals(node.getNodeName())) {
                    jsonMessage.put(EDITED, Boolean.valueOf(nodeValue));
                } else if (DELETED.equals(node.getNodeName())) {
                    jsonMessage.put(DELETED, Boolean.valueOf(nodeValue));
                } else if (USERNAME.equals(node.getNodeName())) {
                    jsonMessage.put(USERNAME, nodeValue);
                } else if (TIME.equals(node.getNodeName())) {
                    jsonMessage.put(TIME, nodeValue);
                } else if (LOG_TIME.equals(node.getNodeName())) {
                    jsonMessage.put(LOG_TIME, nodeValue);
                }
            }

            messages.put(jsonMessage);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(MESSAGES, messages);
        jsonObject.put(TOKEN, getToken(XMLRequestParser.getRequestsAmount()));
        return jsonObject.toJSONString();
    }

    private static Node getNodeById(Document doc, String id) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile("//" + MESSAGE + "[@id='" + id + "']");
        return (Node) expr.evaluate(doc, XPathConstants.NODE);
    }

    private static Transformer getTransformer() throws TransformerConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // Formatting XML properly
        return transformer;
    }
}
