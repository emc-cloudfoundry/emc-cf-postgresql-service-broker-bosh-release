package com.emc.cf.broker.pg.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.emc.cf.broker.pg.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

/**
 * Created by liuc11 on 9/2/15.
 */
@Repository
public class ServiceInstanceCredentialsRepository {

    private static final Logger logger = LoggerFactory.getLogger(ServiceInstanceCredentialsRepository.class);

    private static final String SERVICE_INSTANCE_CREDENTIALS_TABLE = "t_service_instance_credentials";
    private static final String SELECT_SERVICE_INSTANCE_CREDENTIAL_BY_ROLE = "SELECT * FROM t_service_instance_credentials WHERE role = ?";
    private static final String SELECT_SERVICE_INSTANCE_CREDENTIALS_BY_DATABASE = "SELECT * FROM t_service_instance_credentials WHERE database = ?";
    private static final String DELETE_SERVICE_INSTANCE_CREDENTIALS_BY_ROLE = "DELETE FROM t_service_instance_credentials WHERE role = ?";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ServiceInstanceCredentialsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int saveCredential(BindingCredentials credential) {
        logger.info("Try to save binding credentials.");
        int r = -1;
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName(SERVICE_INSTANCE_CREDENTIALS_TABLE);
            logger.info("Insert binding credential information:"
                            + "\ndbType=" + credential.getDbType()
                            + "\nhost=" + credential.getHostname()
                            + "\nport=" + credential.getName()
                            + "\ndatabase=" + credential.getName()
                            + "\nrole=" + credential.getUsername()
                            + "\npassword=" + credential.getPassword()
            );
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("dbType", credential.getDbType());
            args.put("host", credential.getHostname());
            args.put("port", credential.getPort());
            args.put("database", credential.getName());
            args.put("role", credential.getUsername());
            args.put("password", credential.getPassword());
            r = jdbcInsert.execute(args);
        } catch (Exception e) {
            logger.debug("Failed to save binding credential.", e);
            throw e;
        } finally {
            if(conn != null) {
                logger.debug("Try to release the connection.");
                DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
            }
        }
        return r;
    }

    public BindingCredentials getCredentialsByRole(String role) {
        logger.info("Try to get credentials by role: " + role);
        BindingCredentials credentials = null;
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            List<BindingCredentials> list = jdbcTemplate.query(SELECT_SERVICE_INSTANCE_CREDENTIAL_BY_ROLE, new BindingCredentialsRowMapper(), role);
            if(list != null && list.size() > 0) {
                credentials = list.get(0);
            }
        } catch (DataAccessException e) {
            logger.debug("", e);
        } catch (Exception e) {
            logger.debug("Failed to get credentials by role " + role, e);
            throw e;
        } finally {
            if(conn != null) {
                logger.debug("Try to release the connection.");
                DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
            }
        }
        return credentials;
    }

    public List<BindingCredentials> getAllCredentialsByDatabase(String database) {
        logger.info("Try to get all credentials by database_name: " + database);
        List<BindingCredentials> list = null;
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            list = jdbcTemplate.query(SELECT_SERVICE_INSTANCE_CREDENTIALS_BY_DATABASE, new BindingCredentialsRowMapper(), database);
        } catch (DataAccessException e) {
            logger.debug("", e);
        } catch (Exception e) {
            logger.debug("Failed to get all credentials by database_name: " + database, e);
            throw e;
        } finally {
            if(conn != null) {
                logger.debug("Try to release the connection.");
                DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
            }
        }
        return list;
    }

    public void deleteCredential(BindingCredentials credential) {
        logger.info("Try to delete credential from database.");
        if(credential != null) {
            Connection conn = null;
            try {
                conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
                jdbcTemplate.update(DELETE_SERVICE_INSTANCE_CREDENTIALS_BY_ROLE, credential.getUsername());
            } catch (Exception e) {
                logger.debug("Failed to delete credentials from database.", e);
                throw e;
            } finally {
                if(conn != null) {
                    logger.debug("Try to release the connection.");
                    DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
                }
            }
        } else {
            logger.info("The credential is null, cannot delete.");
        }
    }

    private static final class BindingCredentialsRowMapper implements RowMapper<BindingCredentials> {
        public BindingCredentials mapRow(ResultSet rs, int rowNum) throws SQLException {
            String dbType = rs.getString("dbType");
            String host = rs.getString("host");
            String port = rs.getString("port");
            String database = rs.getString("database");
            String role = rs.getString("role");
            String password = rs.getString("password");
            logger.debug("Get binding credential information from database:"
                            + "\ndbType=" + dbType
                            + "\nhost=" + host
                            + "\nport=" + port
                            + "\ndatabase=" + database
                            + "\nrole=" + role
                            + "\npassword=" + password
            );
            return new BindingCredentials(dbType, host, port, database, role, password);
        }
    }
}
