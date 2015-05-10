package bsu.fpmi.chat.controller;

import bsu.fpmi.chat.storage.XMLHistoryParser;
import bsu.fpmi.chat.storage.XMLRequestParser;
import bsu.fpmi.chat.util.ServletUtil;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

import static bsu.fpmi.chat.util.ServletUtil.*;

@WebServlet(urlPatterns = {"/messages"}, asyncSupported = true)
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
        final AsyncContext asyncContext = request.startAsync();
        logger.info("doGet");
        String token = asyncContext.getRequest().getParameter(TOKEN);
        logger.info("Token " + token);

        if (token != null && !"".equals(token)) {
            int index = getIndex(token);
            logger.info("Index " + index);

            try {
                if (XMLRequestParser.getRequestsAmount() > index) {
                    AsyncProcessor.getMessages(asyncContext);
                    asyncContext.complete();
                }
                else
                    AsyncProcessor.addAsyncContext(asyncContext, logger);
            } catch (SAXException | IOException | ParserConfigurationException e) {
                logger.error(e);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("doPost");
        String data = ServletUtil.getMessageBody(request);
        String username = request.getParameter("username");
        logger.info(data);

        try {
            if (username != null && "true".equals(username)) {
                JSONObject usernames = stringToJson(data);
                XMLHistoryParser.changeUsername(usernames);
            } else {
                JSONObject message = stringToJson(data);
                XMLHistoryParser.addToStorage(message);
            }

            AsyncProcessor.notifyAllClients();
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (ParseException | XPathExpressionException | ParserConfigurationException | SAXException | TransformerException  e) {
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
                AsyncProcessor.notifyAllClients();
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
                AsyncProcessor.notifyAllClients();
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