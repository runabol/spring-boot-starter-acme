package com.creactiviti.spring.boot.starter.letsencrypt;

import java.security.Security;
import java.util.Arrays;
import java.util.Collection;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages="com.creactiviti.spring.boot.starter.letsencrypt")
@EnableConfigurationProperties(LetsEncryptProperties.class)
@ConditionalOnProperty(name="letsencrypt.enabled",havingValue="true")
public class LetsEncryptAutoConfiguration implements CommandLineRunner {
  
  @Autowired private CertGenerator generator;
  
  @Autowired private LetsEncryptProperties config;
  
  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  @Override
  public void run (String... args) throws Exception {
    
    Security.addProvider(new BouncyCastleProvider());
    
    Collection<String> domains = Arrays.asList(config.getDomainName());
    
    try {
      generator.fetchCertificate(domains);
    } catch (Exception ex) {
      logger.error("Failed to get a certificate for {}: {} ", domains, ex.getMessage());
    }
    
  }
  
}
