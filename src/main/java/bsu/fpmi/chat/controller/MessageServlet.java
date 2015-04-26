package bsu.fpmi.chat.controller;

import bsu.fpmi.chat.model.Message;
import bsu.fpmi.chat.model.MessageStorage;
import static bsu.fpmi.chat.util.MessageUtil.*;

import bsu.fpmi.chat.util.SAXParsing;
import bsu.fpmi.chat.util.ServletUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Date;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.*;

@WebServlet("/messages")
public class MessageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    //private static Logger logger = Logger.getLogger(MessageServlet.class.getName());


    @Override
    public void init() throws ServletException {
        addStubData();

        try {
            

            if ( historyFile.createNewFile() ){
                createXML();
            } else {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                SAXParsing parsing = new SAXParsing();
                parser.parse(historyFile, parsing);
            }

        } catch (SAXException e) {
            //logger.error(e);
            e.printStackTrace();
        } catch (Exception e) {
            //logger.error(e);
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //logger.info("doPost");
        System.out.println("doPost");
        String data = ServletUtil.getMessageBody(request);
        //logger.info(data);
        System.out.println(data);

        try {
            JSONObject json = stringToJson(data);
            Message message = jsonToMessage(json);

            Date date = new Date();
            StringBuilder time = new StringBuilder(DATE_FORMAT.format(date));
            time.append("<br>").append(TIME_FORMAT.format(date));
            message.setTime(time.toString());
            message.setLogTime(LOG_DATE_FORMAT.format(date));
            message.setId(MessageStorage.getSize() + "");
            System.out.println(message.toString());

            MessageStorage.addMessage(message);
            writeMessageToXML(message);
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (ParseException e) {
            e.printStackTrace();
            //logger.error(e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //logger.info("doGet");
        System.out.println("doGet");
        String token = request.getParameter(TOKEN);
        //logger.info("Token " + token);
        System.out.println("Token " + token);

        if (token != null && !"".equals(token)) {
            int index = getIndex(token);
            //logger.info("Index " + index);
            System.out.println("Index " + index);
            String messages = formResponse(index);
            response.setContentType(ServletUtil.APPLICATION_JSON);

            PrintWriter out = response.getWriter();
            out.print(messages);
            out.flush();
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "'token' parameter needed");
        }
    }

    @SuppressWarnings("unchecked")
    private String formResponse(int index) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(MESSAGES, MessageStorage.getSubMessages(index));
        jsonObject.put(TOKEN, getToken(MessageStorage.getSize()));
        return jsonObject.toJSONString();
    }

    private void addStubData() {
        Message[] stubTasks = {
                new Message("Hello, World!", "User1", "0", "04.04<br>15:09:49"),
                new Message("Hello!", "World", "1", "04.04<br>15:11:20") };
        MessageStorage.addAll(stubTasks);
    }

    private void createXML() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("messages");
            doc.appendChild(rootElement);

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(historyFile);

            transformer.transform(source, result);
        } catch (ParserConfigurationException e) {
            //logger.error(e);
            e.printStackTrace();
        } catch (TransformerException e) {
            //logger.error(e);
            e.printStackTrace();
        }
    }

    public void writeMessageToXML(Message message) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(historyFile);
            Node messageTag = doc.getFirstChild();

            Element username = doc.createElement("username");
            username.appendChild(doc.createTextNode(message.getUsername()));
            messageTag.appendChild(username);

            Element text = doc.createElement("text");
            text.appendChild(doc.createTextNode(message.getText()));
            messageTag.appendChild(text);

            Element time = doc.createElement("time");
            time.appendChild(doc.createTextNode(message.getTime()));
            messageTag.appendChild(time);

            Element logTime = doc.createElement("log-time");
            logTime.appendChild(doc.createTextNode(message.getLogTime()));
            messageTag.appendChild(logTime);

            Element edited = doc.createElement("edited");
            edited.appendChild(doc.createTextNode(message.isEdited() + ""));
            messageTag.appendChild(edited);

            Element deleted = doc.createElement("deleted");
            deleted.appendChild(doc.createTextNode(message.isDeleted() + ""));
            messageTag.appendChild(deleted);

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(historyFile);

            transformer.transform(source, result);
        } catch (ParserConfigurationException e) {
            //logger.error(e);
            e.printStackTrace();
        } catch (TransformerException e) {
            //logger.error(e);
            e.printStackTrace();
        } catch (Exception e) {
            //logger.error(e);
            e.printStackTrace();
        }
    }
}
