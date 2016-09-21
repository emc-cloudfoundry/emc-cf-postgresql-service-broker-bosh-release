package com.emc.cf.broker.pg.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by liuc11 on 8/13/15.
 */
@Configuration
public class DataConfig {
    private static final Logger logger = LoggerFactory.getLogger(DataConfig.class);

    @Autowired
    private BrokerData brokerData;

    @Bean
    public JdbcTemplate jdbcTemplate() {
        JdbcTemplate jdbcTemplate = getJdbcTemplate("postgres");
        if(!checkServiceBrokerDatabaseExist(jdbcTemplate, "service_broker")) {
            jdbcTemplate.update("CREATE DATABASE service_broker ENCODING 'UTF8'");
            jdbcTemplate.update("REVOKE ALL ON DATABASE service_broker FROM PUBLIC");
        }
        jdbcTemplate = getJdbcTemplate("service_broker");
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS t_service_instance ("
                        + " serviceInstanceId varchar(200) not null default '',"
                        + " serviceDefinitionId varchar(200) not null default '',"
                        + " planId varchar(200) not null default '',"
                        + " organizationGuid varchar(200) not null default '',"
                        + " spaceGuid varchar(200) not null default '')"
        );
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS t_service_instance_binding ("
                        + " bindingId varchar(200) not null default '',"
                        + " serviceInstanceId varchar(200) not null default '',"
                        + " appGuid varchar(200) not null default '',"
                        + " syslogDrainUrl varchar(200),"
                        + " credentials varchar(1024) not null default '')"
        );
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS t_service_instance_credentials ("
                        + " role varchar(200) not null default '',"
                        + " dbType varchar(100) not null default '',"
                        + " host varchar(20) not null default '',"
                        + " port varchar(10) not null default '',"
                        + " database varchar(200) not null default '',"
                        + " password varchar(200))"
        );
        return jdbcTemplate;
    }

    @Bean
    @Autowired
    public PlatformTransactionManager platformTransactionManager(JdbcTemplate jdbcTemplate) {
        logger.debug("Try to create PlatformTransactionManager Bean.");
        logger.debug("data source: " + jdbcTemplate.getDataSource().toString());
        return new DataSourceTransactionManager(jdbcTemplate.getDataSource());
    }

    @Bean
    @Autowired
    public TransactionTemplate transactionTemplate(PlatformTransactionManager platformTransactionManager) {
        logger.debug("Try to create TransactionTemplate Bean.");
        return new TransactionTemplate(platformTransactionManager);
    }

    private boolean checkServiceBrokerDatabaseExist(JdbcTemplate jdbcTemplate, String database) {
        logger.info("Check if the service_broker database exists.");
        int result = -1;
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            String sql = String.format("SELECT 1 FROM pg_database WHERE datname = '%s'", database);
            result = jdbcTemplate.queryForObject(sql, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            logger.info("Can not find the database service_broker, then create it.");
            logger.debug("Can not find the database service_broker, then create it.", e);
        } catch (Exception e) {
            logger.debug("Failed to find the database service_broker.", e);
            throw e;
        } finally {
            if(conn != null) {
                logger.debug("Try to release the connection.");
                DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
            }
        }
        if(result == 1) {
            return true;
        }
        return false;
    }

    private JdbcTemplate getJdbcTemplate(String database) {
        logger.info("Start to construct JdbcTemplate from datasource. database=\"" + database + "\"");
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        String pg_url = String.format("jdbc:postgresql://%s:%s/%s", brokerData.getPgHost(), brokerData.getPgPort(), database);
        logger.debug("Try to create datasource for url \"" + pg_url + "\"");
        ds.setUrl(pg_url);
        ds.setUsername(brokerData.getPgUser());
        ds.setPassword(brokerData.getPgPassword());

        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        logger.debug("JdbcTemplate object has been constructed.");
        return jdbcTemplate;
    }
}
