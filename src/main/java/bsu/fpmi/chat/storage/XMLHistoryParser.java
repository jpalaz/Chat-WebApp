package bsu.fpmi.chat.storage;

import org.json.JSONArray;
import org.json.XML;
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
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public static synchronized void addToStorage(JSONObject message, boolean existed) throws ParserConfigurationException, SAXException, IOException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document document = docBuilder.parse(STORAGE_LOCATION);
        document.getDocumentElement().normalize();

        //logger.info(message.toString());
        Element root = document.getDocumentElement();

        Element messageTag = document.createElement(MESSAGE);
        root.appendChild(messageTag);

        Element username = document.createElement(USERNAME);
        username.appendChild(document.createTextNode((String) message.get(USERNAME)));
        messageTag.appendChild(username);

        Element text = document.createElement(TEXT);
        text.appendChild(document.createTextNode((String) message.get(TEXT)));
        messageTag.appendChild(text);

        Element time = document.createElement(TIME);
        Element logTime = document.createElement(LOG_TIME);

        if(existed) {
            messageTag.setAttribute(ID, (String) message.get(ID));
            time.appendChild(document.createTextNode((String) message.get(TIME)));
            logTime.appendChild(document.createTextNode((String) message.get(LOG_TIME)));
        } else {
            messageTag.setAttribute(ID, UUID.randomUUID().toString());

            Date date = new Date();
            StringBuilder timeString = new StringBuilder(DATE_FORMAT.format(date))
                    .append("<br>").append(TIME_FORMAT.format(date));

            time.appendChild(document.createTextNode(timeString.toString()));
            logTime.appendChild(document.createTextNode(LOG_DATE_FORMAT.format(date)));
        }

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
        Node messageToUpdate = getNodeById(document, (String) message.get(ID));

        if (messageToUpdate != null) {
            addToStorage(message, true);

            /*NodeList childNodes = messageToUpdate.getChildNodes();

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
            transformer.transform(source, result);*/
        } else {
            return false;
        }

        return true;
    }

    public static boolean remove(String id) throws ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(STORAGE_LOCATION);
        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();
        Node messageToDelete = getNodeById(document, id);

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

            root.appendChild(messageToDelete.cloneNode(true));
            Transformer transformer = getTransformer();

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
            transformer.transform(source, result);
        } else {
            return false;
        }

        return true;
    }

    public static synchronized String restoreMessages(int start) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(STORAGE_LOCATION);
        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();
        NodeList messageList = root.getElementsByTagName(MESSAGE);

        JSONArray messages = new JSONArray();
        JSONObject message;
        message = new JSONObject();

        for (int i = start; i < messageList.getLength(); i++) {
            Element messageTag = (Element) messageList.item(i);

            message.put(ID, messageTag.getAttribute(ID));
            message.put(TEXT, messageTag.getElementsByTagName(TEXT).item(0).getTextContent());
            message.put(USERNAME, messageTag.getElementsByTagName(USERNAME).item(0).getTextContent());
            message.put(TIME, messageTag.getElementsByTagName(TIME).item(0).getTextContent());
            message.put(LOG_TIME, messageTag.getElementsByTagName(LOG_TIME).item(0).getTextContent());
            message.put(EDITED, Boolean.valueOf(messageTag.getElementsByTagName(EDITED).item(0).getTextContent()));
            message.put(DELETED, Boolean.valueOf(messageTag.getElementsByTagName(DELETED).item(0).getTextContent()));

            messages.put(message);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(MESSAGES, messages);
        jsonObject.put(TOKEN, getToken(root.getElementsByTagName(MESSAGE).getLength()));
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
