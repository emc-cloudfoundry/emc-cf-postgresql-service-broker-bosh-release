package com.emc.cf.broker.pg.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuc11 on 8/18/15.
 */
public class BindingCredentials {

    private String dbType = "postgresql";
    private String hostname = "127.0.0.1";
    private String port = "5432";
    private String name = "";
    private String username = "";
    private String password = "";

    public BindingCredentials(String dbType, String host, String port, String database, String role, String password) {
        this.dbType = dbType;
        this.hostname = host;
        this.port = port;
        this.name = database;
        this.username = role;
        this.password = password;
    }

    public BindingCredentials(String host, String port, String database, String role, String password) {
        this.hostname = host;
        this.port = port;
        this.name = database;
        this.username = role;
        this.password = password;
    }

    public String getDbType() {
        return dbType;
    }

    public String getHostname() {
        return hostname;
    }

    public String getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Map<String, Object> toMap() {
			String uri = dbType + "://" + username + ":" + password + "@" +
					hostname + ":" + port + "/" + name;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("dbType", dbType);
        map.put("hostname", hostname);
        map.put("port", port);
        map.put("name", name);
        map.put("username", username);
        map.put("password", password);
        map.put("uri", uri);
        return map;
    }
}
