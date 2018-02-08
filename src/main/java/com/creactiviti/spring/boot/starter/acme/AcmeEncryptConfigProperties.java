package com.creactiviti.spring.boot.starter.acme;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Arik Cohen
 * @since Feb 07, 2018
 */
@ConfigurationProperties(prefix="acme")
public class AcmeEncryptConfigProperties {

  private boolean acceptTermsOfService = false;
  private String domainName;
  private boolean staging = true;
  private String userKeyFile = "user.key";
  private String domainKeyFile = "domain.key";
  private String domainCsrFile = "domain.csr";
  private String domainChainFile = "domain-chain.crt";
  private String keyStoreFile = "keystore.p12";
  private String keyStorePassword = "password";
  
  private static final String STAGING_ENDPOINT = "acme://letsencrypt.org/staging";
  
  private static final String PRODUCTION_ENDPOINT = "acme://letsencrypt.org";
  
  public String getUserKeyFile() {
    return userKeyFile;
  }
  
  public void setUserKeyFile(String aUserKeyFile) {
    userKeyFile = aUserKeyFile;
  }
  
  public String getDomainKeyFile() {
    return domainKeyFile;
  }
  
  public void setDomainKeyFile(String aDomainKeyFile) {
    domainKeyFile = aDomainKeyFile;
  }
  
  public String getDomainChainFile() {
    return domainChainFile;
  }
  
  public void setDomainChainFile(String aDomainChainFile) {
    domainChainFile = aDomainChainFile;
  }
  
  public String getDomainCsrFile() {
    return domainCsrFile;
  }
  
  public void setDomainCsrFile(String aDomainCsrFile) {
    domainCsrFile = aDomainCsrFile;
  }
  
  public boolean isAcceptTermsOfService() {
    return acceptTermsOfService;
  }
  
  public void setAcceptTermsOfService (boolean aAcceptsTos) {
    acceptTermsOfService = aAcceptsTos;
  }
  
  public String getDomainName() {
    return domainName;
  }
  
  public void setDomainName(String aDomainName) {
    domainName = aDomainName;
  }

  public String getKeyStoreFile() {
    return keyStoreFile;
  }
  
  public void setKeyStoreFile(String aKeyStoreFile) {
    keyStoreFile = aKeyStoreFile;
  }
  
  public String getKeyStorePassword() {
    return keyStorePassword;
  }
  
  public void setKeyStorePassword(String aKeyStorePassword) {
    keyStorePassword = aKeyStorePassword;
  }
  
  public boolean isStaging() {
    return staging;
  }
  
  public void setStaging(boolean staging) {
    this.staging = staging;
  }
  
  public String getEndpoint () {
    return staging ? STAGING_ENDPOINT : PRODUCTION_ENDPOINT;
  }
  
}
