package bsu.fpmi.chat.controller;

import bsu.fpmi.chat.storage.XMLHistoryParser;
import bsu.fpmi.chat.storage.XMLRequestParser;
import bsu.fpmi.chat.util.ServletUtil;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.PrintWriter;

import static bsu.fpmi.chat.util.ServletUtil.*;

@WebServlet("/messages")
public class MessageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(MessageServlet.class.getName());

    @Override
    public void init() throws ServletException {
        try {
            readHistory();
        } catch (SAXException e) {
            logger.error(e);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void readHistory() throws SAXException, IOException, ParserConfigurationException, TransformerException {
        if (!XMLHistoryParser.doesStorageExist()) {
            XMLHistoryParser.createStorage();
        }

        if (!XMLRequestParser.doesStorageExist()) {
            XMLRequestParser.createRequestsStorage();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("doGet");
        String token = request.getParameter(TOKEN);
        logger.info("Token " + token);

        if (token != null && !"".equals(token)) {
            int index = getIndex(token);
            logger.info("Index " + index);

            try {
                String messages = XMLHistoryParser.getMessagesFrom(index);
                response.setContentType(ServletUtil.APPLICATION_JSON);
                response.setCharacterEncoding("utf-8");
                PrintWriter out = response.getWriter();
                out.print(messages);
                out.flush();
            } catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
                logger.error(e);
            }

        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "'token' parameter needed");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("doPost");
        String data = ServletUtil.getMessageBody(request);
//        String token = request.getParameter(USERNAME);
        logger.info(data);

        try {
            JSONObject message = stringToJson(data);
            XMLHistoryParser.addToStorage(message);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (ParseException | ParserConfigurationException | SAXException | TransformerException  e) {
            logger.error(e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("doPut");
        String data = ServletUtil.getMessageBody(request);
        logger.info(data);

        try {
            JSONObject message = stringToJson(data);
            if(XMLHistoryParser.update(message)) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Message does not exist");
            }
        } catch (ParseException | ParserConfigurationException | SAXException | TransformerException | XPathExpressionException e) {
            logger.error(e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("doDelete");
        String id = request.getParameter("id");
        logger.info(id);

        try {
            if(XMLHistoryParser.remove(id)) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Message does not exist");
            }
        } catch (ParserConfigurationException | SAXException | TransformerException | XPathExpressionException e) {
            logger.error(e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
//