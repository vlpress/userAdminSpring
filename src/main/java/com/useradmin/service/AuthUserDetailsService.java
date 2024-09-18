package com.useradmin.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.useradmin.dto.CachedUserDetails;
import com.useradmin.entity.User;
import com.useradmin.repository.UserRepository;
import com.useradmin.utils.DBUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//@AllArgsConstructor
@Service
public class AuthUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;
    private final Cache<String, UserDetails> userCache;
    private DBUtils dbUtils;

    public AuthUserDetailsService(UserRepository userRepository, DBUtils dbUtils) {
        this.userRepository = userRepository;
        this.userCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(1000)
                .build();
        this.dbUtils = dbUtils;
    }

    /**
     * Load user by username
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if(dbUtils.isDatabaseAlive()){
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            System.out.println("PASSWORD:"+user.getPasswordHash());

            //UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            CachedUserDetails userDetails = new CachedUserDetails(
                    user.getEmail(),
                    user.getPasswordHash(),
                    user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                            .collect(Collectors.toList())
            );

            userCache.put(username, userDetails);

            return userDetails;
        }

        UserDetails cachedUser = userCache.getIfPresent(username);
        if (cachedUser != null) {
            return cachedUser;
        }

        throw new UsernameNotFoundException("User not found");
    }
}
