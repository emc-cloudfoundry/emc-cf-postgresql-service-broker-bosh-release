package com.emc.cf.broker.pg.data;

/**
 * Created by liuc11 on 8/13/15.
 */
public class BrokerData {
    private String pgHost;

    private String pgPort;

    private String pgUser;

    private String pgPassword;

    private String withSSL;

    public String getPgHost() {
        return pgHost;
    }

    public String getPgPort() {
        return pgPort;
    }

    public String getPgUser() {
        return pgUser;
    }

    public String getPgPassword() {
        return pgPassword;
    }

    public String getWithSSL() {
        return withSSL;
    }

    public void setPgHost(String pgHost) {
        this.pgHost = pgHost;
    }

    public void setPgPort(String pgPort) {
        this.pgPort = pgPort;
    }

    public void setPgUser(String pgUser) {
        this.pgUser = pgUser;
    }

    public void setPgPassword(String pgPassword) {
        this.pgPassword = pgPassword;
    }

    public void setWithSSL(String withSSL) {
        this.withSSL = withSSL;
    }
}
