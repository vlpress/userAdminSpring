package com.useradmin.service;

import com.useradmin.entity.Role;
import com.useradmin.entity.User;
import com.useradmin.repository.RoleRepository;
import com.useradmin.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MessageService messageService;

    /**
     * Create user
     * @param user
     * @return
     */
    @Retryable(
            value = { DataAccessException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public User createUser(User user) {
        try {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                Role defaultRole = roleRepository.findByRoleName(ServiceConstants.USER)
                        .orElseThrow(() -> new RuntimeException("Default role not found"));
                user.setRoles(List.of(defaultRole));
            }
            return userRepository.save(user);
        }catch (DataAccessException ex){
            messageService.sendUser(ServiceConstants.CREATE, user);
            return user;
        }
    }

    /**
     * Get user details
     * @param email
     * @return
     */
    @Retryable(
            value = { DataAccessException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public User getUserDetails(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Get all users
     * @return
     */
    @Retryable(
            value = { DataAccessException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public List<User> getAllUsers() {
        try {
            return userRepository.findAll();
        }catch (DataAccessException ex){
            return messageService.receiveMessage(ServiceConstants.OPERATION + " = '"+ServiceConstants.NONE+"' OR "+ServiceConstants.OPERATION+" = '"+ServiceConstants.CREATE+"'",false);
        }
    }

    /**
     * Delete user
     * @param email
     */
    @Retryable(
            value = { DataAccessException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void deleteUser(String email) {
        try {
            userRepository.deleteByEmail(email);
        }catch (DataAccessException ex){
            User user = new User();
            user.setEmail(email);
            messageService.sendUser("DELETE", user);
        }
    }
}
