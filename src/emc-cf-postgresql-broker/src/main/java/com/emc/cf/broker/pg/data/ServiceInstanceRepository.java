package com.emc.cf.broker.pg.data;

import com.emc.cf.broker.pg.utils.Utils;
import com.pivotal.cf.broker.model.ServiceInstance;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by liuc11 on 8/17/15.
 */
@Repository
public class ServiceInstanceRepository {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ServiceInstanceRepository.class);

    private JdbcTemplate jdbcTemplate;

    private static final String SERVICE_INSTANCE_TABLE = "t_service_instance";
    private static final String SELECT_SERVICE_INSTANCE_BY_ID = "SELECT * FROM t_service_instance WHERE serviceInstanceId = ?";
    private static final String SELECT_ALL_SERVICE_INSTANCES = "SELECT * FROM t_service_instance";
    private static final String DELETE_SERVICE_INSTANCE_BY_ID = "DELETE FROM t_service_instance WHERE serviceInstanceId = ?";

    @Autowired
    public ServiceInstanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int createServiceInstance(ServiceInstance serviceInstance) {
        logger.info("Try to create service instance.");
        int r = -1;
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName(SERVICE_INSTANCE_TABLE);
            logger.info("Insert service instance information:"
                            + "\nserviceInstanceId=" + serviceInstance.getId()
                            + "\nserviceDefinitionId=" + serviceInstance.getServiceDefinitionId()
                            + "\nplanId=" + serviceInstance.getPlanId()
                            + "\norganizationGuid=" + serviceInstance.getOrganizationGuid()
                            + "\nspaceGuid=" + serviceInstance.getSpaceGuid()
            );
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("serviceInstanceId", serviceInstance.getId());
            args.put("serviceDefinitionId", serviceInstance.getServiceDefinitionId());
            args.put("planId", serviceInstance.getPlanId());
            args.put("organizationGuid", serviceInstance.getOrganizationGuid());
            args.put("spaceGuid", serviceInstance.getSpaceGuid());
            r = jdbcInsert.execute(args);
        } catch (Exception e) {
            logger.debug("Failed to create service instance.", e);
            throw e;
        } finally {
            if(conn != null) {
                logger.debug("Try to release the connection.");
                DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
            }
        }
        return r;
    }

    public ServiceInstance getServiceInstance(String serviceInstanceId) {
        logger.info("Try to get service instance, instance id is " + serviceInstanceId);
        ServiceInstance instance = null;
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            instance = jdbcTemplate.queryForObject(SELECT_SERVICE_INSTANCE_BY_ID, new ServiceInstanceRowMapper(), serviceInstanceId);
        } catch (EmptyResultDataAccessException e) {
            logger.info("There is no service instance for serviceInstanceId: \"" + serviceInstanceId + "\".");
            logger.debug("There is no service instance for serviceInstanceId: \"" + serviceInstanceId + "\".", e);
        } catch (Exception e) {
            logger.debug("Failed to get service instance.", e);
            throw e;
        } finally {
                if(conn != null) {
                    logger.debug("Try to release the connection.");
                    DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
                }
        }
        return instance;
    }

    public List<ServiceInstance> getAllServiceInstances() {
        logger.info("Try to get all service instances.");
        List<ServiceInstance> list = null;
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            list = jdbcTemplate.query(SELECT_ALL_SERVICE_INSTANCES, new ServiceInstanceRowMapper());
        } catch (DataAccessException e) {
            logger.debug("", e);
        } catch (Exception e) {
            logger.debug("Failed to get all service instance from database.", e);
            throw e;
        } finally {
            if(conn != null) {
                logger.debug("Try to release the connection.");
                DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
            }
        }
        return list;
    }

    public ServiceInstance deleteServiceInstance(String serviceInstanceId) {
        logger.info("Try to delete service instance for instance id: " + serviceInstanceId);
        ServiceInstance serviceinstance = null;
        Connection conn = null;
        try {
            logger.info("Try to get the service instance first, this instance object will return to client.");
            conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            serviceinstance = jdbcTemplate.queryForObject(SELECT_SERVICE_INSTANCE_BY_ID, new ServiceInstanceRowMapper(), serviceInstanceId);

        } catch (Exception e) {
            logger.debug("Failed to get service instance for instance id: " + serviceInstanceId, e);
            throw e;
        } finally {
            if(conn != null) {
                logger.debug("Try to release the connection.");
                DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
            }
        }
        try {
            logger.info("Then, delete the service instance.");
            conn = DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
            jdbcTemplate.update(DELETE_SERVICE_INSTANCE_BY_ID, serviceInstanceId);
        } catch (Exception e) {
            logger.debug("Failed to delete service instance for instance id: " + serviceInstanceId, e);
            throw e;
        } finally {
            if(conn != null) {
                logger.debug("Try to release the connection.");
                DataSourceUtils.releaseConnection(conn, jdbcTemplate.getDataSource());
            }
        }
        return serviceinstance;
    }

    private static final class ServiceInstanceRowMapper implements RowMapper<ServiceInstance> {
        public ServiceInstance mapRow(ResultSet rs, int rowNum) throws SQLException {
            String serviceDefinitionId = rs.getString("serviceDefinitionId");
            String serviceInstanceId = rs.getString("serviceInstanceId");
            String planId = rs.getString("planId");
            String organizationGuid = rs.getString("organizationGuid");
            String spaceGuid = rs.getString("spaceGuid");
            logger.debug("Get service instance information from database:"
                    + "\nserviceDefinitionId=" + serviceDefinitionId
                    + "\nserviceInstanceId=" + serviceInstanceId
                    + "\nplanId=" + planId
                    + "\norganizationGuid=" + organizationGuid
                    + "\nspaceGuid=" + spaceGuid
            );
            return new ServiceInstance(serviceInstanceId, serviceDefinitionId, planId, organizationGuid, spaceGuid, null);
        }
    }
}
