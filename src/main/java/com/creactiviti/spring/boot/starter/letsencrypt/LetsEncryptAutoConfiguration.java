package com.creactiviti.spring.boot.starter.letsencrypt;

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
@ConditionalOnProperty(name="letsencrypt.enabled",havingValue="true")
@ComponentScan(basePackages="com.creactiviti.spring.boot.starter.letsencrypt")
@EnableConfigurationProperties(LetsEncryptConfigProperties.class)
public class LetsEncryptAutoConfiguration  {
  
  @Bean
  InMemoryChallengeStore inMemoryChallengeStore () {
    return new InMemoryChallengeStore();
  }
  
}
