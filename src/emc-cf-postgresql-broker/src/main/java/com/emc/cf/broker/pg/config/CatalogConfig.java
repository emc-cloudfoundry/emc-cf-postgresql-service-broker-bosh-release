package com.emc.cf.broker.pg.config;

import com.emc.cf.broker.pg.data.BrokerData;
import com.pivotal.cf.broker.model.Catalog;
import com.pivotal.cf.broker.model.Plan;
import com.pivotal.cf.broker.model.ServiceDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuc11 on 8/14/15.
 */
@Configuration
@ComponentScan(basePackageClasses = {Catalog.class})
public class CatalogConfig {

    private static final Logger logger = LoggerFactory.getLogger(CatalogConfig.class);

    @Autowired
    Environment env;

    private String service_definition_id = "e9f22964-3eeb-4ac4-8c39-0ccafdce5375";
    private String service_definition_name = "postgresql";
    private String service_definition_description = "postgresql service broker";
    private boolean service_definition_bindable = true;
    private String plan1_id = "0a61b858-8422-484f-aff1-c32a1c702dbb";
    private String plan1_name = "developer";
    private String plan1_description = "Developer plan will delete postgresql data when service is deleted";
    private String plan2_id = "bbf38a8d-4754-4b5f-a0db-b75cfb6766ca";
    private String plan2_name = "production";
    private String plan2_description = "Production plan will never delete postgresql data even if service is deleted";

    @Bean
    public Catalog catalog() {
        logger.debug("Start to construct CatalogConfig.");
        List<ServiceDefinition> serviceDefinitions = serviceDefinitions();
        Catalog catalog = new Catalog(serviceDefinitions);
        return catalog;
    }

    private List<ServiceDefinition> serviceDefinitions() {
        List<ServiceDefinition> list = new ArrayList<ServiceDefinition>();
        List<Plan> plans = plans();
        service_definition_id = env.getProperty("service.definition.id", service_definition_id);
        service_definition_name = env.getProperty("service.definition.name", service_definition_name);
        ServiceDefinition serviceDefinition = new ServiceDefinition(
                service_definition_id,
                service_definition_name,
                service_definition_description,
                service_definition_bindable,
                plans
        );
        logger.debug("pgbroker cataglog properties:"
                        + "\nservice_definition_id=" + service_definition_id
                        + "\nservice_definition_name=" + service_definition_name
                        + "\nservice_definition_description=" + service_definition_description
                        + "\nservice_definition_bindable=" + service_definition_bindable
        );
        serviceDefinition.setTags(tags());
        list.add(serviceDefinition);
        return list;
    }

    private List<Plan> plans() {
        List<Plan> list = new ArrayList<Plan>(2);
        plan1_id = env.getProperty("service.dev.plan.id", plan1_id);
        plan2_id = env.getProperty("service.prod.plan.id", plan2_id);
        Plan plan1 = new Plan(plan1_id, plan1_name, plan1_description);
        Plan plan2 = new Plan(plan2_id, plan2_name, plan2_description);
        list.add(plan1);
        list.add(plan2);
        return list;
    }

    private List<String> tags() {
        List<String> list = new ArrayList<String>();
        list.add("postgresql");
        list.add("cache");
        return list;
    }
}
