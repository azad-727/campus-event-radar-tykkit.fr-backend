package com.tykkit.fr.main.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Mongoconfig {

    @Bean
    public MongoClient mongoClient() {
        String uri = System.getenv("SPRING_DATA_MONGODB_URI");
        if (uri == null || uri.isBlank()) {
            throw new IllegalStateException("SPRING_DATA_MONGODB_URI environment variable is not set!");
        }
        ConnectionString connectionString = new ConnectionString(uri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(settings);
    }
}