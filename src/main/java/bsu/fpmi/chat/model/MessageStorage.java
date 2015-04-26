package bsu.fpmi.chat.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MessageStorage {
    private static final List<Message> INSTANCE = Collections.synchronizedList(new ArrayList<Message>());

    private MessageStorage() {
    }

    public static List<Message> getStorage() {
        return INSTANCE;
    }

    public static void addMessage(Message message) {
        INSTANCE.add(message);
    }

    public static void addAll(Message[] messages) {
        INSTANCE.addAll(Arrays.asList(messages));
    }

    public static int getSize() {
        return INSTANCE.size();
    }

    public static List<Message> getSubMessages(int index) {
        return INSTANCE.subList(index, INSTANCE.size());
    }

    public static Message getMessageById(String id) {
        for (Message message : INSTANCE) {
            if (message.getId().equals(id)) {
                return message;
            }
        }

        return null;
    }
}
