package com.emc.cf.broker.pg.service;

import com.emc.cf.broker.pg.data.*;
import com.emc.cf.broker.pg.utils.Utils;
import com.pivotal.cf.broker.exception.ServiceBrokerException;
import com.pivotal.cf.broker.exception.ServiceInstanceBindingExistsException;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.model.ServiceInstanceBinding;
import com.pivotal.cf.broker.service.ServiceInstanceBindingService;
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
public class PGInstanceBindingService implements ServiceInstanceBindingService{

    private static final Logger logger = LoggerFactory.getLogger(PGInstanceService.class);

    private PGAdmin pgAdmin;
    private ServiceInstanceBindingRepository serviceInstanceBindingRepository;
    private ServiceInstanceCredentialsRepository serviceInstanceCredentialsRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    public PGInstanceBindingService(PGAdmin pgAdmin, ServiceInstanceBindingRepository serviceInstanceBindingRepository,
                                    ServiceInstanceCredentialsRepository serviceInstanceCredentialsRepository) {
        this.pgAdmin = pgAdmin;
        this.serviceInstanceBindingRepository = serviceInstanceBindingRepository;
        this.serviceInstanceCredentialsRepository = serviceInstanceCredentialsRepository;
    }

    @Override
    public ServiceInstanceBinding createServiceInstanceBinding(String bindingId, ServiceInstance serviceInstance,
                                                               String serviceId, String planId,
                                                               String appGuid)
            throws ServiceInstanceBindingExistsException, ServiceBrokerException {
        logger.info("Start to method: createServiceInstanceBinding");
        ServiceInstanceBinding binding = serviceInstanceBindingRepository.getServiceInstanceBinding(bindingId);
        if(binding != null) {
            logger.info("The service instance binding already exists.");
            throw new ServiceInstanceBindingExistsException(binding);
        }
        try {
            return transactionTemplate.execute(new TransactionCallback<ServiceInstanceBinding>() {
                @Override
                public ServiceInstanceBinding doInTransaction(TransactionStatus status) {
                    BindingCredentials bindingCredentials = serviceInstanceCredentialsRepository
                            .getCredentialsByRole(Utils.convertToDatabaseIdentifier(serviceInstance.getId()));
                    ServiceInstanceBinding binding = new ServiceInstanceBinding(bindingId, serviceInstance.getId(), bindingCredentials.toMap(), null, appGuid);
                    serviceInstanceBindingRepository.createServiceInstanceBinding(binding);
                    return binding;
                }
            });

        } catch (Exception e) {
            throw new ServiceBrokerException(e.getMessage());
        }
    }

    @Override
    public ServiceInstanceBinding getServiceInstanceBinding(String id) {
        logger.info("Start to method: getServiceInstanceBinding");
        return serviceInstanceBindingRepository.getServiceInstanceBinding(id);
    }

    @Override
    public ServiceInstanceBinding deleteServiceInstanceBinding(String id) throws ServiceBrokerException {
        logger.info("Start to method: deleteServiceInstanceBinding");
        ServiceInstanceBinding binding = serviceInstanceBindingRepository.getServiceInstanceBinding(id);
        if(binding == null) {
            logger.info("The service instance binding does not exist.");
            binding = new ServiceInstanceBinding(id, null, null, null, null);
        } else {
            try {
                transactionTemplate.execute(new TransactionCallback<ServiceInstanceBinding>() {
                    @Override
                    public ServiceInstanceBinding doInTransaction(TransactionStatus status) {
                        serviceInstanceBindingRepository.deleteServiceInstanceBinding(id);
                        return null;
                    }
                });
            } catch (Exception e) {
                throw new ServiceBrokerException(e.getMessage());
            }
        }
        return binding;
    }
}
