package com.amor.api.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

//@Configuration
public class H2config {

    public static final String DATASOURCE_PROPERTIES = "spring.datasource.h2";

    public static final String DATASOURCE = "h2DataSource";

    public static final String TRANSACTION_MANAGER = "h2TransactionManager";

    public static final String ENTITY_MANAGE_FACTORY = "h2EntityManagerFactory";

    @Bean
    @ConfigurationProperties(prefix = DATASOURCE_PROPERTIES)
    public HikariConfig h2DataSource() {
        return new HikariConfig();
    }

    @Bean(DATASOURCE)
    public DataSource h2DateSource() {
        return new LazyConnectionDataSourceProxy(new HikariDataSource(h2DataSource()));
    }

    @Bean(ENTITY_MANAGE_FACTORY)
    public LocalContainerEntityManagerFactoryBean h2EntityManagerFactory(EntityManagerFactoryBuilder builder, JpaProperties jpaProperties, HibernateProperties hibernateProperties) {
        return builder
                .dataSource(h2DateSource())
                .packages("com.amor.api.domain")
                .persistenceUnit(ENTITY_MANAGE_FACTORY)
                .properties(hibernateProperties.determineHibernateProperties(jpaProperties.getProperties(), new HibernateSettings()))
                .build();
    }

    @Bean(TRANSACTION_MANAGER)
    public PlatformTransactionManager h2TransactionManager(@Qualifier(ENTITY_MANAGE_FACTORY)EntityManagerFactory managerFactory){
        return new JpaTransactionManager(managerFactory);
    }

}
