package com.creactiviti.spring.boot.starter.acme;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author Arik Cohen
 * @since Feb 07, 2018
 */
@Component
public class AcmeEncryptRunner implements CommandLineRunner {
  
  private final CertGenerator generator;
  
  private final AcmeConfigProperties config;
   
  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  public AcmeEncryptRunner(CertGenerator aCertGenerator, AcmeConfigProperties aConfig) {
    config = aConfig;
    generator = aCertGenerator;
  }
  
  @Override
  public void run (String... args) throws Exception {
    logger.info("Generating a LetsEncrypt certificate...");
    
    Security.addProvider(new BouncyCastleProvider());
    
    String domainName = config.getDomainName();
    
    Assert.notNull(domainName,"missing required property: acme.domain-name");
    
    generator.generate(domainName);
  }
  
}
