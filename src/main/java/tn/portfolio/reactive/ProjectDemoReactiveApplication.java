package tn.portfolio.reactive;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;

@SpringBootApplication
public class ProjectDemoReactiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectDemoReactiveApplication.class, args);
    }

    @Bean
    public R2dbcTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}