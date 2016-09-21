package com.emc.cf.broker.pg.service;

import com.emc.cf.broker.pg.data.BindingCredentials;
import com.emc.cf.broker.pg.data.BrokerData;
import com.emc.cf.broker.pg.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;

/**
 * Created by liuc11 on 8/13/15.
 */
@Component
public class PGAdmin {

    private static final Logger logger = LoggerFactory.getLogger(PGAdmin.class);

    private static final String CREATE_DATABASE = "CREATE DATABASE %s ENCODING 'UTF8'";
    private static final String PRIVATIZE_DATABASE = "REVOKE ALL ON DATABASE %s FROM PUBLIC";
    private static final String DELETE_DATABASE = "DROP DATABASE IF EXISTS %s";

    private static final String CREATE_ROLE = "CREATE ROLE %s LOGIN PASSWORD '%s'";
    private static final String BIND_ROLE_TO_DATABASE = "GRANT ALL ON DATABASE %s TO %s";
    private static final String UNBIND_ROLE_FROM_DATABASE = "REVOKE ALL ON DATABASE %s FROM %s";
    private static final String DROP_ROLE = "DROP ROLE IF EXISTS %s";


    private static final String DATA_CONN = "update pg_database set datallowconn = 'false' where datname = '%s'";
    private static final String DROP_ALL_CONNECTIONS = "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '%s'";
    @Autowired
    private BrokerData brokerData;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public PGAdmin(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createDatabase(String database) {
        jdbc_update(jdbcTemplate, String.format(CREATE_DATABASE, database));
        jdbc_update(jdbcTemplate, String.format(PRIVATIZE_DATABASE, database));
    }

    public void deleteDatabase(String database) {
        /**
        try {
            jdbcTemplate.update(String.format(DATA_CONN, database));
            jdbcTemplate.update(String.format(DROP_ALL_CONNECTIONS, database));
        }catch (Exception e) {
            logger.error("Failed to drop all connections.", e);
            //throw e;
        }
         **/
        jdbc_update(jdbcTemplate, String.format(DELETE_DATABASE, database));
    }

    public BindingCredentials createRoleForDatabase(String role, String database) {
        String passwd = Utils.generatePassword();
        jdbc_update(jdbcTemplate, String.format(CREATE_ROLE, role, passwd));
        jdbc_update(jdbcTemplate, String.format(BIND_ROLE_TO_DATABASE, database, role));
        BindingCredentials bindingCredentials = new BindingCredentials(
                brokerData.getPgHost(), brokerData.getPgPort(),
                database, role, passwd);
        return bindingCredentials;
    }


    public void revokeRoleFromDatabase(String role, String database) {
        jdbc_update(jdbcTemplate, String.format(UNBIND_ROLE_FROM_DATABASE, database, role));
    }

    public void deleteRole(String role) {
        jdbc_update(jdbcTemplate, String.format(DROP_ROLE, role));
    }

    private static void jdbc_update(JdbcTemplate jdbcTemplate, String sql) {
        Connection conn = null;
        try {
            logger.info("Try to execute sql: " + sql);
            conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            jdbcTemplate.update(sql);
        } catch (Exception e) {
            logger.debug("Failed to execute sql: " + sql, e);
            throw e;
        } finally {
            if(conn != null) {
                logger.debug("Try to release the connection.");
                DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
            }
        }
    }
}
