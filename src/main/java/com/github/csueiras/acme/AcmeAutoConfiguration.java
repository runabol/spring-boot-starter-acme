package com.github.csueiras.acme;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Auto configuration for ACME, note that this auto configuration has to be explicitly enabled with "acme.enabled"
 *
 * @author Arik Cohen
 * @since Feb 07, 2018
 */
@Configuration
@ConditionalOnProperty(name = "acme.enabled", havingValue = "true")
@ComponentScan(basePackages = "com.github.csueiras.acme")
@EnableConfigurationProperties(AcmeConfigProperties.class)
public class AcmeAutoConfiguration {

    @Bean
    ChallengeStore inMemoryChallengeStore() {
        return new InMemoryChallengeStore();
    }
}
