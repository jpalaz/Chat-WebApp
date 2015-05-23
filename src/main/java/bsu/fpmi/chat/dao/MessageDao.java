package bsu.fpmi.chat.dao;

import org.json.simple.JSONObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public interface MessageDao {
    void add(JSONObject task);

    void update(JSONObject task);

    void remove(String id);

    JSONObject selectById(JSONObject task);

    JSONObject getAll();

    JSONObject getMessagesFromRequest(int index)  throws SAXException, IOException, ParserConfigurationException;
}

