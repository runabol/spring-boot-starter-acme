package com.creactiviti.spring.boot.starter.acme;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Arik Cohen
 * @since Feb 07, 2018
 */
@Configuration
@ConditionalOnProperty(name="acme.enabled",havingValue="true")
@ComponentScan(basePackages="com.creactiviti.spring.boot.starter.acme")
@EnableConfigurationProperties(AcmeConfigProperties.class)
public class AcmeAutoConfiguration  {
  
  @Bean
  InMemoryChallengeStore inMemoryChallengeStore () {
    return new InMemoryChallengeStore();
  }
  
}
