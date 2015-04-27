package bsu.fpmi.chat.controller;

import bsu.fpmi.chat.model.Message;
import bsu.fpmi.chat.model.MessageStorage;
import static bsu.fpmi.chat.util.MessageUtil.*;

import bsu.fpmi.chat.storage.XMLHistoryParser;
import bsu.fpmi.chat.util.ServletUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.Date;

import org.xml.sax.*;

@WebServlet("/messages")
public class MessageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    //private static Logger logger = Logger.getLogger(MessageServlet.class.getName());

    @Override
    public void init() throws ServletException {
        try {
            readHistory();
        } catch (SAXException e) {
            //logger.error(e);
            e.printStackTrace();
        } catch (Exception e) {
            //logger.error(e);
            e.printStackTrace();
        }
    }

    private void readHistory() throws SAXException, IOException, ParserConfigurationException, TransformerException {
        if (XMLHistoryParser.doesStorageExist()) {
            MessageStorage.addAll(XMLHistoryParser.restoreMessages());
        } else {
            XMLHistoryParser.createStorage();
            addStubData();
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
            XMLHistoryParser.addToStorage(message);
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (ParseException | ParserConfigurationException | SAXException | TransformerException  e) {
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

    private void addStubData() throws ParserConfigurationException, TransformerException {
        Message[] stubMessages = {
                new Message("Hello, World!", "User1", "0", "04.04<br>15:09:49", "04-04-2015 15:09"),
                new Message("Hello!", "World", "1", "04.04<br>15:11:20", "04-04-2015 15:11") };
        MessageStorage.addAll(stubMessages);

        for (Message message : stubMessages) {
            try {
                XMLHistoryParser.addToStorage(message);
            } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
                //logger.error(e);
                e.printStackTrace();
            }
        }
    }
}
