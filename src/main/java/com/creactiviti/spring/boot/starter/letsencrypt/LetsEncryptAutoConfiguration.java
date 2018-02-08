package com.creactiviti.spring.boot.starter.letsencrypt;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages="com.creactiviti.spring.boot.starter.letsencrypt")
@EnableConfigurationProperties(LetsEncryptConfigProperties.class)
public class LetsEncryptAutoConfiguration  {
  
  @Bean
  InMemoryChallengeStore inMemoryChallengeStore () {
    return new InMemoryChallengeStore();
  }
  
}
