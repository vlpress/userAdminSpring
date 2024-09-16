package com.useradmin.service;

import com.useradmin.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.useradmin.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@Service
public class SyncDBMQService {

    private final UserRepository userRepository;
    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Check if database is alive
     */
    public boolean isDatabaseAlive() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sync users from DB to MQ
     */
    @Scheduled(fixedRate = 3000)
    public void scheduleSyncUsers() {
        if (isDatabaseAlive())
            syncUsers();
    }

    /**
     *  Sync users from DB to MQ
     */
    @Async
    public void syncUsers() {

        // 1. get DELETED messages from MQ and delete it from DB and MQ
        List<User> usersFromMq = receiveUsersFromMQ(ServiceConstants.OPERATION+" = '"+ServiceConstants.DELETE+"'", true);
        for (User user : usersFromMq) {
            userRepository.deleteByEmail(user.getEmail());
        }

        usersFromMq = receiveUsersFromMQ(ServiceConstants.OPERATION+" = '"+ServiceConstants.CREATE+"'", true);
        userRepository.saveAll(usersFromMq);

        List<User> usersFromDb = userRepository.findAll();
        List<User> mqUserList;
        for (User user : usersFromDb) {
            mqUserList = messageService.receiveMessage("userEmail = '" + user.getEmail() + "'", false);
            if (mqUserList.isEmpty()) {
                messageService.sendUser("NONE", user);
            }
        }
        System.out.println("Sync completed");
    }

    /**
     * Receive users from MQ
     */
    private List<User> receiveUsersFromMQ(String selector, boolean isAcknowledge) {
        try {
            return messageService.receiveMessage(selector, isAcknowledge);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
