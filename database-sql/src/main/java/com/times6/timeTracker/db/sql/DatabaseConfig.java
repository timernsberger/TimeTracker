package com.times6.timeTracker.db.sql;

import lombok.Data;

@Data
public class DatabaseConfig {
    private String hostname;
    private int port;
    private String database;
    private String schema;
    private String username;
    private String password;

    public String getConnectionUrl() {
        return String.format("jdbc:postgresql://%s:%d/%s?currentSchema=%s",
                hostname,
                port,
                database,
                schema);
    }

}
