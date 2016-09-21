package com.emc.cf.broker.pg.service;

import java.util.List;

import com.emc.cf.broker.pg.data.BindingCredentials;
import com.emc.cf.broker.pg.data.ServiceInstanceCredentialsRepository;
import com.emc.cf.broker.pg.data.ServiceInstanceRepository;
import com.emc.cf.broker.pg.exception.TransactionException;
import com.emc.cf.broker.pg.utils.Utils;
import com.pivotal.cf.broker.exception.ServiceBrokerException;
import com.pivotal.cf.broker.exception.ServiceInstanceExistsException;
import com.pivotal.cf.broker.model.Plan;
import com.pivotal.cf.broker.model.ServiceDefinition;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.service.BeanCatalogService;
import com.pivotal.cf.broker.service.ServiceInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Created by liuc11 on 8/13/15.
 */
@Service
public class PGInstanceService implements ServiceInstanceService {

    private static final Logger logger = LoggerFactory.getLogger(PGInstanceService.class);

    private PGAdmin pgAdmin;
    private ServiceInstanceRepository serviceInstanceRepository;
    private ServiceInstanceCredentialsRepository serviceInstanceCredentialsRepository;

    @Autowired
    private BeanCatalogService catalogService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    public PGInstanceService(PGAdmin pgAdmin, ServiceInstanceRepository serviceInstanceRepository,
                             ServiceInstanceCredentialsRepository serviceInstanceCredentialsRepository) {
        this.pgAdmin = pgAdmin;
        this.serviceInstanceRepository = serviceInstanceRepository;
        this.serviceInstanceCredentialsRepository = serviceInstanceCredentialsRepository;
    }

    @Override
    public List<ServiceInstance> getAllServiceInstances() {
        return serviceInstanceRepository.getAllServiceInstances();
    }

    @Override
    public ServiceInstance createServiceInstance(ServiceDefinition service, String serviceInstanceId,
                                                 String planId, String organizationGuid,
                                                 String spaceGuid)
            throws ServiceInstanceExistsException, ServiceBrokerException {
        logger.info("Start to method: createServiceInstance");
        ServiceInstance instance = serviceInstanceRepository.getServiceInstance(serviceInstanceId);
        if (instance != null) {
            logger.info("The service instance already exists.");
            throw new ServiceInstanceExistsException(instance);
        }
        try {
            pgAdmin.createDatabase(Utils.convertToDatabaseIdentifier(serviceInstanceId));
            return transactionTemplate.execute(new TransactionCallback<ServiceInstance>() {
                @Override
                public ServiceInstance doInTransaction(TransactionStatus status) {
                    try {
                        ServiceInstance instance = new ServiceInstance(serviceInstanceId, service.getId(), planId, organizationGuid, spaceGuid, null);
                        serviceInstanceRepository.createServiceInstance(instance);

                        BindingCredentials bindingCredentials = pgAdmin.createRoleForDatabase(
                                Utils.convertToDatabaseIdentifier(serviceInstanceId),
                                Utils.convertToDatabaseIdentifier(serviceInstanceId));
                        serviceInstanceCredentialsRepository.saveCredential(bindingCredentials);
                        return instance;
                    } catch (Exception e) {
                        throw new TransactionException(e);
                    }
                }
            });
        } catch (Exception e) {
            if(e instanceof TransactionException) {
                logger.debug("Failed to create service instance in transaction block, so delete the database: " + Utils.convertToDatabaseIdentifier(serviceInstanceId));
                pgAdmin.deleteDatabase(Utils.convertToDatabaseIdentifier(serviceInstanceId));
            }
            throw new ServiceBrokerException(e.getMessage());
        }
    }

    @Override
    public ServiceInstance getServiceInstance(String id) {
        logger.info("Start to method: getServiceInstance");
        return serviceInstanceRepository.getServiceInstance(id);
    }


    @Override
    public ServiceInstance deleteServiceInstance(String id) throws ServiceBrokerException {
        logger.info("Start to method: deleteServiceInstance");
        ServiceInstance instance = serviceInstanceRepository.getServiceInstance(id);
        if(instance == null) {
            logger.info("Cannot find the service instance for id: " + id);
            logger.info("Instance not found we still return empty instance " +
                    "so that Cloud Controller can delete the service meta data from its database and deletion succeeds");
            instance = new ServiceInstance(id, null, null, null, null, null);
        } else {
            try {
                ServiceDefinition serviceDefinition = catalogService.getServiceDefinition(instance.getServiceDefinitionId());
                Plan plan = searchPlanInServiceDefinition(instance.getPlanId(), serviceDefinition);
                if(plan.getName().startsWith("dev")) {
                    pgAdmin.revokeRoleFromDatabase(Utils.convertToDatabaseIdentifier(id), Utils.convertToDatabaseIdentifier(id));
                    try {
                        pgAdmin.deleteDatabase(Utils.convertToDatabaseIdentifier(id));
                    } catch (Exception e) {
                        logger.info("Failed to delete database: " + Utils.convertToDatabaseIdentifier(id) +
                                ". But this failure would not affect to delete the service instance " +
                                "because the meta data of this service instance has already been deleted.", e);
                    }
                    List<BindingCredentials> credentials = serviceInstanceCredentialsRepository.getAllCredentialsByDatabase(Utils.convertToDatabaseIdentifier(id));
                    if(credentials != null && !credentials.isEmpty()) {
                        for(BindingCredentials cre : credentials) {
                            try {
                                pgAdmin.deleteRole(cre.getUsername());
                                serviceInstanceCredentialsRepository.deleteCredential(cre);
                            } catch (Exception e) {
                                logger.error("Failed to delete role: " + cre.getUsername(), e);
                                continue;
                            }
                        }
                    }
                } else if (plan.getName().startsWith("prod")) {
                    //do nothing
                } else {
                    //other service plan implementation
                }
                serviceInstanceRepository.deleteServiceInstance(id);
            } catch (Exception e) {
                throw new ServiceBrokerException(e.getMessage());
            }
        }
        return instance;
    }

    private Plan searchPlanInServiceDefinition(String planId, ServiceDefinition serviceDefinition) {
        List<Plan> plans = serviceDefinition.getPlans();
        for (Plan p : plans) {
            if(p.getId().equals(planId)) {
                return p;
            } else {
                continue;
            }
        }
        return null;
    }
}
