package com.creactiviti.spring.boot.starter.letsencrypt;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

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
    
    String domainName = config.getDomainName();
    
    Assert.notNull(domainName,"missing required property: letsencrypt.domain-name");
    
    try {
      generator.generate(domainName);
    } catch (Exception ex) {
      logger.error("Failed to get a certificate for {}: {} ", config.getDomainName(), ex.getMessage());
    }
  }
  
}
