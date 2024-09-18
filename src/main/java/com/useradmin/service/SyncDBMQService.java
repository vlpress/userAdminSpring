package com.useradmin.service;

import com.useradmin.entity.User;
import com.useradmin.repository.UserRepository;
import com.useradmin.utils.DBUtils;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@Service
public class SyncDBMQService {

    private final UserRepository userRepository;
    private final MessageService messageService;
    private final JdbcTemplate jdbcTemplate;
    private DBUtils dbUtils;

    /**
     * Sync users from DB to MQ
     */
    @Scheduled(fixedRate = 5000)
    public void scheduleSyncUsers() {
        if (dbUtils.isDatabaseAlive())
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

        // 2. get CREATED messages from MQ and save it to DB and delete it from MQ
        usersFromMq = receiveUsersFromMQ(ServiceConstants.OPERATION+" = '"+ServiceConstants.CREATE+"'", true);
        userRepository.saveAll(usersFromMq);

        // 3. get all users from DB and check if it is in MQ, if not send it to MQ by setting operation as NONE
        List<User> usersFromDb = userRepository.findAll();
        List<User> mqUserList;
        for (User user : usersFromDb) {
            mqUserList = messageService.receiveMessage("userEmail = '" + user.getEmail() + "'", false);
            if (mqUserList.isEmpty()) {
                messageService.sendUser(ServiceConstants.NONE, user);
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
