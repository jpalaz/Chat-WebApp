package bsu.fpmi.chat.util;

import bsu.fpmi.chat.model.Message;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.text.SimpleDateFormat;

public class MessageUtil {
    public static final String TOKEN = "token";
    public static final String MESSAGES = "messages";
    private static final String TN = "TN";
    private static final String EN = "EN";

    private static final String ID = "id";
    private static final String TEXT = "text";
    private static final String USERNAME = "username";
    private static final String TIME = "time";
    private static final String DELETED = "deleted";
    private static final String EDITED = "edited";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat ("dd.MM");
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat ("hh:mm:ss");
    public static final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat ("dd-MM-yyyy hh:mm");

    public static File historyFile = new File( System.getProperty("user.home")
            + System.getProperty("file.separator") + "history.xml");

    private MessageUtil() {}

    public static String getToken(int index) {
        Integer number = index * 8 + 11;
        return TN + number + EN;
    }

    public static int getIndex(String token) {
        return (Integer.valueOf(token.substring(2, token.length() - 2)) - 11) / 8;
    }

    public static JSONObject stringToJson(String data) throws ParseException {
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(data.trim());
    }

    public static Message jsonToMessage(JSONObject json) {
        Object id = json.get(ID);
        Object text = json.get(TEXT);
        Object username = json.get(USERNAME);
        Object time = json.get(TIME);
        Object edited = json.get(EDITED);
        Object deleted = json.get(DELETED);

        if (id != null && text != null && username != null &&
                time != null && edited != null && deleted != null) {
            return new Message((String) text, (String) username, (String)id, (String)time,
                    (Boolean) edited, (Boolean) deleted);
        }
        return null;
    }
}
