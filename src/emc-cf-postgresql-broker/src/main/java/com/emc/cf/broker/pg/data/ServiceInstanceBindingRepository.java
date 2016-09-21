package com.emc.cf.broker.pg.data;

import com.emc.cf.broker.pg.utils.Utils;
import com.pivotal.cf.broker.model.ServiceInstanceBinding;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuc11 on 8/17/15.
 */
@Repository
public class ServiceInstanceBindingRepository {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ServiceInstanceBindingRepository.class);

    private static final String SERVICE_INSTANCE_BINDING_TABLE = "t_service_instance_binding";
    private static final String SELECT_SERVICE_INSTANCE_BINDING_BY_ID = "SELECT * FROM t_service_instance_binding WHERE bindingId = ?";
    private static final String DELETE_SERVICE_INSTANCE_BINDING_BY_ID = "DELETE FROM t_service_instance_binding WHERE bindingId = ?";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ServiceInstanceBindingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int createServiceInstanceBinding(ServiceInstanceBinding serviceInstanceBinding) {
        logger.info("Try to create service instance binding.");
        int r = -1;
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName(SERVICE_INSTANCE_BINDING_TABLE);
            logger.info("Insert service instance binding information:"
                            + "\nbindingId=" + serviceInstanceBinding.getId()
                            + "\nbindingId=" + serviceInstanceBinding.getId()
                            + "\nserviceInstanceId=" + serviceInstanceBinding.getServiceInstanceId()
                            + "\nappGuid=" + serviceInstanceBinding.getAppGuid()
                            + "\ncredentials=" + serviceInstanceBinding.getCredentials()
                            + "\nsyslogDrainUrl=" + serviceInstanceBinding.getSyslogDrainUrl()
            );
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("bindingId", serviceInstanceBinding.getId());
            args.put("serviceInstanceId", serviceInstanceBinding.getServiceInstanceId());
            args.put("appGuid", serviceInstanceBinding.getAppGuid());
            args.put("credentials", Utils.convertMapToJson(serviceInstanceBinding.getCredentials()));
            args.put("syslogDrainUrl", serviceInstanceBinding.getSyslogDrainUrl());
            r = jdbcInsert.execute(args);
        } catch (Exception e) {
            logger.debug("Failed to create service instance binding.", e);
            throw e;
        } finally {
            if(conn != null) {
                logger.debug("Try to release the connection.");
                DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
            }
        }
        return r;
    }

    public ServiceInstanceBinding getServiceInstanceBinding(String bindingId) {
        logger.info("Try to get service instance binding, binding id is " + bindingId);
        ServiceInstanceBinding serviceInstanceBinding = null;
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            serviceInstanceBinding = jdbcTemplate.queryForObject(SELECT_SERVICE_INSTANCE_BINDING_BY_ID, new ServiceInstanceBindingRowMapper(), bindingId);
        } catch (EmptyResultDataAccessException e) {
            logger.info("There is no service binding instance for bindingId: \"" + bindingId + "\".");
            logger.debug("There is no service binding instance for bindingId: \"" + bindingId + "\".", e);
        } catch (Exception e) {
            logger.debug("Failed to get service instance binding for binding id: " + bindingId, e);
            throw e;
        } finally {
            if(conn != null) {
                logger.debug("Try to release the connection.");
                DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
            }
        }
        return serviceInstanceBinding;
    }

    public ServiceInstanceBinding deleteServiceInstanceBinding(String bindingId) {
        logger.info("Try to delete service instance binding for bindId: " + bindingId);
        ServiceInstanceBinding binding = getServiceInstanceBinding(bindingId);
        if(binding != null) {
            Connection conn = null;
            try {
                conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
                jdbcTemplate.update(DELETE_SERVICE_INSTANCE_BINDING_BY_ID, bindingId);
            } catch (Exception e) {
                logger.debug("Failed to delete service instance binding for binding id: " + bindingId, e);
                throw e;
            } finally {
                if(conn != null) {
                    logger.debug("Try to release the connection.");
                    DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
                }
            }
        }
        return binding;
    }

    private static final class ServiceInstanceBindingRowMapper implements RowMapper<ServiceInstanceBinding> {
        public ServiceInstanceBinding mapRow(ResultSet rs, int rowNum) throws SQLException {
            String bindingId = rs.getString("bindingId");
            String serviceInstanceId = rs.getString("serviceInstanceId");
            String appGuid = rs.getString("appGuid");
            String syslogDrainUrl = rs.getString("syslogDrainUrl");
            String credentials = rs.getString("credentials");
            logger.debug("Get service instance binding information from database:"
                    + "\nbindingId=" + bindingId
                    + "\nserviceInstanceId=" + serviceInstanceId
                    + "\nappGuid=" + appGuid
                    + "\nsyslogDrainUrl=" + syslogDrainUrl
                    + "\ncredentials=" + credentials
            );
            return new ServiceInstanceBinding(bindingId, serviceInstanceId, Utils.convertJsonToMap(credentials), syslogDrainUrl, appGuid);
        }
    }
}
