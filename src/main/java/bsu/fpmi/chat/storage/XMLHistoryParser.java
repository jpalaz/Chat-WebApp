package bsu.fpmi.chat.storage;

import bsu.fpmi.chat.model.Message;
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
import java.util.ArrayList;
import java.util.List;

public class XMLHistoryParser {
    private static final String STORAGE_LOCATION = System.getProperty("user.home") +  File.separator + "history.xml";
    private static File historyFile = new File(STORAGE_LOCATION);

    private static final String MESSAGE = "message";
    private static final String ID = "id";
    private static final String USERNAME = "username";
    private static final String TEXT = "text";
    private static final String TIME = "time";
    private static final String LOG_TIME = "log-time";
    private static final String EDITED = "edited";
    private static final String DELETED = "deleted";

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
        //StreamResult result = new StreamResult(historyFile); TODO: field or new instance?
        transformer.transform(source, result);
    }

    public static synchronized void addToStorage(Message message) throws ParserConfigurationException, SAXException, IOException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document document = docBuilder.parse(STORAGE_LOCATION);
        //Document document = docBuilder.parse(historyFile);
        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();

        Element messageTag = document.createElement(MESSAGE);
        root.appendChild(messageTag);

        Element username = document.createElement(USERNAME);
        username.appendChild(document.createTextNode(message.getUsername()));
        messageTag.appendChild(username);

        Element id = document.createElement(ID);
        id.appendChild(document.createTextNode(message.getId()));
        messageTag.appendChild(id);

        Element text = document.createElement(TEXT);
        text.appendChild(document.createTextNode(message.getText()));
        messageTag.appendChild(text);

        Element time = document.createElement(TIME);
        time.appendChild(document.createTextNode(message.getTime()));
        messageTag.appendChild(time);

        Element logTime = document.createElement(LOG_TIME);
        logTime.appendChild(document.createTextNode(message.getLogTime()));
        messageTag.appendChild(logTime);

        Element edited = document.createElement(EDITED);
        edited.appendChild(document.createTextNode(message.isEdited() + ""));
        messageTag.appendChild(edited);

        Element deleted = document.createElement(DELETED);
        deleted.appendChild(document.createTextNode(message.isDeleted() + ""));
        messageTag.appendChild(deleted);

        Transformer transformer = getTransformer();
        DOMSource source = new DOMSource(document);

        StreamResult result = new StreamResult(STORAGE_LOCATION);
        //StreamResult result = new StreamResult(historyFile);
        transformer.transform(source, result);
    }

    public static synchronized void updateStorage(Message message) throws ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(STORAGE_LOCATION);
        document.getDocumentElement().normalize();
        Node messageToUpdate = getNodeById(document, message.getId());

        if (messageToUpdate != null) {

            NodeList childNodes = messageToUpdate.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);

                if (TEXT.equals(node.getNodeName())) {
                    node.setTextContent(message.getText());
                } else if (EDITED.equals(node.getNodeName())) {
                    node.setTextContent(Boolean.toString(message.isEdited()));
                } else if (DELETED.equals(node.getNodeName())) {
                    node.setTextContent(Boolean.toString(message.isDeleted()));
                }
            }

            Transformer transformer = getTransformer();

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
            transformer.transform(source, result);
        } else {
            throw new NullPointerException();
        }
    }

    public static synchronized List<Message> restoreMessages() throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(STORAGE_LOCATION);
        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();
        NodeList messageList = root.getElementsByTagName(MESSAGE);
        List<Message> messages = new ArrayList<>();

        for (int i = 0; i < messageList.getLength(); i++) {
            Element messageTag = (Element) messageList.item(i);

            String id = messageTag.getElementsByTagName(ID).item(0).getTextContent();
            String text = messageTag.getElementsByTagName(TEXT).item(0).getTextContent();
            String username = messageTag.getElementsByTagName(USERNAME).item(0).getTextContent();
            String time = messageTag.getElementsByTagName(TIME).item(0).getTextContent();
            String logTime = messageTag.getElementsByTagName(LOG_TIME).item(0).getTextContent();
            boolean edited = Boolean.valueOf(messageTag.getElementsByTagName(EDITED).item(0).getTextContent());
            boolean deleted = Boolean.valueOf(messageTag.getElementsByTagName(DELETED).item(0).getTextContent());

            messages.add(new Message(text, username, id, time, logTime, edited, deleted));
        }
        return messages;
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
