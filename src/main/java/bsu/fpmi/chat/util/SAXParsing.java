package bsu.fpmi.chat.util;

import static bsu.fpmi.chat.model.MessageStorage.*;

import bsu.fpmi.chat.model.Message;
import bsu.fpmi.chat.model.MessageStorage;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXParsing extends DefaultHandler {
    private String thisElement  = "";
    private Message message;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        thisElement = qName;

        if (qName.equalsIgnoreCase("message")) {
            message = new Message();
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        thisElement = "";

        if (qName.equalsIgnoreCase("message")) {
            MessageStorage.addMessage(message);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String content = new String(ch, start, length);

        if (thisElement.equals("id")) {
            message.setId(content);
        } else if (thisElement.equals("text")) {
            message.setText(content);
        } else if (thisElement.equals("username")) {
            message.setUsername(content);
        } else if (thisElement.equals("time")) {
            message.setTime(content);
        } else if (thisElement.equals("log-time")) {
            message.setLogTime(content);
        } else if (thisElement.equals("edited")) {
            message.setEdited(new Boolean(content));
        } else if (thisElement.equals("deleted")) {
            message.setDeleted(new Boolean(content));
        }
    }
}
