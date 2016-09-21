package com.emc.cf.broker.pg.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
///var/vcap/jobs/pg_broker/conf/pg_broker.properties
///Users/liuc11/Downloads/Workspace/bosh_projects/emc-postgresql-service-bosh-release/src/emc-cf-postgresql-broker/pg-broker-config.properties
/**
 * Created by liuc11 on 8/13/15.
 */
@Configuration
@ComponentScan(basePackageClasses = DataConfig.class)
@PropertySource("file:/var/vcap/jobs/pg_broker/conf/pg_broker.properties")
public class BrokerDataConfig {
    private static final Logger logger = LoggerFactory.getLogger(BrokerDataConfig.class);
    @Autowired
    Environment env;

    private String pg_host = "127.0.0.1";
    private String pg_port = "5432";
    private String pg_user = "psql";
    private String pg_password = "";
    private String with_ssl = "false";

    @Bean
    public BrokerData brokerData() {
        BrokerData brokerData = new BrokerData();
        brokerData.setPgHost(env.getProperty("pg.host", pg_host));
        brokerData.setPgPort(env.getProperty("pg.port", pg_port));
        brokerData.setPgUser(env.getProperty("pg.user", pg_user));
        brokerData.setPgPassword(env.getProperty("pg.password", pg_password));
        brokerData.setWithSSL(env.getProperty("with.ssl", with_ssl));
        logger.debug("pg_broker properties:"
                + "\npg.host=" + brokerData.getPgHost()
                + "\npg.port=" + brokerData.getPgPort()
                + "\npg.user=" + brokerData.getPgUser()
                + "\npg.password=" + brokerData.getPgPassword()
                + "\nwith.ssl=" + brokerData.getWithSSL()
        );
        return brokerData;
    }
}
