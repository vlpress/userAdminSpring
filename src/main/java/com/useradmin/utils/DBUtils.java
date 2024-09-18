package com.useradmin.utils;

import lombok.AllArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


@AllArgsConstructor
@Component
public class DBUtils {

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
}
