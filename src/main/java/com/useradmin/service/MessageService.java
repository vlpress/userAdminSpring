package com.useradmin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.useradmin.entity.User;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.JmsUtils;
import org.springframework.stereotype.Service;

import jakarta.jms.*;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Service
public class MessageService {


    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    /**
     * @param operation: CREATE or DELETE or NONE
     * @param user
     */
    public void sendUser(String operation, User user) {
        if(ServiceConstants.DELETE.equals(operation) || ServiceConstants.CREATE.equals(operation))
          receiveMessage("userEmail = '" + user.getEmail() + "'", true);

        Connection connection = null;
        Session session = null;
        try {
            connection = Objects.requireNonNull(jmsTemplate.getConnectionFactory()).createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            TextMessage message = session.createTextMessage();
            message.setStringProperty("userId", String.valueOf(user.getId()));
            message.setStringProperty("userEmail", user.getEmail());
            message.setStringProperty(ServiceConstants.OPERATION, operation);
            message.setText(serializeUser(user));

            MessageProducer producer = session.createProducer(session.createQueue(ServiceConstants.QUEUE_NAME));
            producer.send(message);

        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            JmsUtils.closeSession(session);
            JmsUtils.closeConnection(connection);
        }
    }

    /**
     *
     * @param user
     * @return
     * @throws JMSException
     */
    private String serializeUser(User user) throws JMSException {
        try {
            return objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new JMSException("Error serializing User object to JSON");
        }
    }


    /**
     * @param selector : "operation = 'DELETE' OR operation = 'CREATE'"; (operation = 'DELETE' OR operation = 'CREATE') AND email = 'user@example.com'"
     * @param isAcknowledge : true if the message should be acknowledged
     * @return List<User>
     */

    public List<User> receiveMessage(String selector, boolean isAcknowledge) {
        List<User> users = new ArrayList<>();
        Connection connection = null;
        Session session = null;

        try  {
            connection = Objects.requireNonNull(jmsTemplate.getConnectionFactory()).createConnection();
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

            MessageConsumer consumer = session.createConsumer(session.createQueue(ServiceConstants.QUEUE_NAME), selector);
            connection.start();

            while (true) {
                Message message = consumer.receive(200);
                if (message instanceof TextMessage) {
                    String userJson = ((TextMessage) message).getText();

                    if (isAcknowledge) {
                       message.acknowledge();
                    }

                    User user =  objectMapper.readValue(userJson, User.class);
                    users.add(user);
                } else if (message == null) {
                    break;
                }
            }

        } catch (JMSException | JsonProcessingException e) {
            e.printStackTrace();
        } finally {
            JmsUtils.closeSession(session);
            JmsUtils.closeConnection(connection);
        }
        return users;
    }

}
