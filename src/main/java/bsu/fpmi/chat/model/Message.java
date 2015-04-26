package bsu.fpmi.chat.model;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class Message implements JSONAware {
    private String text;
    private String username;
    private String id;
    private String time;
    private String logTime;
    private boolean edited;
    private boolean deleted;

    public Message(String text, String username, String id) {
        this.text = text;
        this.username = username;
        this.id = id;
        this.edited = this.deleted = false;
    }

    public Message(String text, String username, String id, String time) {
        this.text = text;
        this.username = username;
        this.id = id;
        this.time = time;
        this.edited = this.deleted = false;
    }

    public Message(String text, String username, String id, String time, boolean edited, boolean deleted) {
        this.text = text;
        this.username = username;
        this.id = id;
        this.time = time;
        this.edited = edited;
        this.deleted = deleted;
    }

    public Message(Message message, boolean deleted) {
        this.username = message.username;
        this.id = message.id;
        this.time = message.time;
        this.edited = message.edited;

        if (deleted)
            this.deleteMessage();
        else {
            this.text = message.text;
            this.deleted = message.deleted;
        }
    }

    public Message() {}

    public String getUsername() {
        return username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public boolean isEdited() {
        return edited;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void deleteMessage() {
        deleted = true;
        text = "This message has been deleted";
    }

    public void editMessage(String text) {
        if(!deleted) {
            edited = true;
            this.text = text;
        }
        else
            System.out.println("Message has been deleted. You can't edit it.");
    }

    public String toJSONString(){
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("username", username);
        obj.put("text", text);
        obj.put("time", time);
        obj.put("deleted", deleted);
        obj.put("edited", edited);
        return obj.toString();
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setLogTime(String logTime) {
        this.logTime = logTime;
    }

    public String getTime() {
        return time;
    }

    public String getLogTime() {
        return logTime;
    }



    @Override
    public String toString() {
        return this.logTime + " " + this.username + " : " +
                this.text;

        /*return "{\"id\":\"" + this.id +
                "\",\"username\":\"" + this.username +
                "\",\"text\":" + this.text +
                "\",\"time\":" + this.time +
                "\",\"deleted\":" + this.deleted +
                "\",\"edited\":" + this.edited + "}";*/
    }
}
