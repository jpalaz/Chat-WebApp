package bsu.fpmi.chat.dao;

import bsu.fpmi.chat.db.ConnectionManager;
import bsu.fpmi.chat.storage.XMLRequestParser;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.*;
import java.util.List;

import static bsu.fpmi.chat.util.ServletUtil.MESSAGES;
import static bsu.fpmi.chat.util.ServletUtil.TOKEN;

public class MessageDaoImpl implements MessageDao {
    private static final String ID = "id";
    private static final String USERNAME = "username";
    private static final String TEXT = "text";
    private static final String TIME = "time";
    private static final String EDITED = "edited";
    private static final String DELETED = "deleted";

    private static Logger logger = Logger.getLogger(MessageDaoImpl.class.getName());

    @Override
    public void add(JSONObject message) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement("INSERT INTO messages " +
                    "(id, text, time, username, edited, deleted) VALUES (?, ?, ?, ?, ?, ?)");
            preparedStatement.setString(1, (String) message.get(ID));
            preparedStatement.setString(2, (String) message.get(TEXT));
            preparedStatement.setString(3, (String) message.get(TIME));
            preparedStatement.setString(4, (String) message.get(USERNAME));
            preparedStatement.setBoolean(5, (Boolean) message.get(EDITED));
            preparedStatement.setBoolean(6, (Boolean) message.get(DELETED));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    @Override
    public void update(JSONObject message) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement("Update messages SET text = ?, edited = ? WHERE id = ?");
            preparedStatement.setString(1, (String) message.get(TEXT));
            preparedStatement.setBoolean(2, (Boolean) message.get(EDITED));
            preparedStatement.setString(3, (String) message.get(ID));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    @Override
    public void remove(String id) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement("Update messages SET text = ?, deleted = ? WHERE id = ?");
            preparedStatement.setString(1, "This message has been deleted.");
            preparedStatement.setBoolean(2, true);
            preparedStatement.setString(3, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    @Override
    public JSONObject selectById(JSONObject task) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject getAll() {
        logger.info("MySQL: get all messages");
        JSONArray messages = new JSONArray();
        JSONObject jsonMessage;
        JSONObject response = new JSONObject();

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = ConnectionManager.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM messages ORDER BY messages.time");
            int size = 0;

            while (resultSet.next()) {
                jsonMessage = new JSONObject();
                jsonMessage.put(ID, resultSet.getString(ID));
                jsonMessage.put(TEXT, resultSet.getString(TEXT));
                jsonMessage.put(EDITED, resultSet.getBoolean(EDITED));
                jsonMessage.put(DELETED, resultSet.getBoolean(DELETED));
                jsonMessage.put(USERNAME, resultSet.getString(USERNAME));
                jsonMessage.put(TIME, resultSet.getString(TIME));
                messages.put(jsonMessage);

                size++;
            }

            response.put(TOKEN, size);
            response.put(MESSAGES, messages);
        } catch (SQLException e) {
            logger.error(e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
        return response;
    }

    @Override
    public JSONObject getMessagesFromRequest(int index) throws SAXException, IOException, ParserConfigurationException {
        logger.info("MySQL: get messages from request index - " + index);
        JSONArray messages = new JSONArray();
        JSONObject message;
        JSONObject response = new JSONObject();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = ConnectionManager.getConnection();
            List<String> ids = XMLRequestParser.getRequests(index);

            for (String id : ids) {
                preparedStatement = connection.prepareStatement("SELECT * FROM chat.messages WHERE id = ?");
                preparedStatement.setString(1, id);
                resultSet = preparedStatement.executeQuery();

                resultSet.next();
                message = new JSONObject();
                message.put(ID, id);
                message.put(TEXT, resultSet.getString(TEXT));
                message.put(EDITED, resultSet.getBoolean(EDITED));
                message.put(DELETED, resultSet.getBoolean(DELETED));
                message.put(USERNAME, resultSet.getString(USERNAME));
                message.put(TIME, resultSet.getString(TIME));
                messages.put(message);
            }

            response.put(TOKEN, XMLRequestParser.getRequestsAmount());
            response.put(MESSAGES, messages);
        }  catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }

        return response;
    }
}
