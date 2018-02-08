package com.creactiviti.spring.boot.starter.letsencrypt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="letsencrypt")
public class LetsEncryptProperties {

  private boolean acceptTermsOfService = false;
  
  private String domainName;
  
  private boolean staging = true;
  
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

  public boolean isStaging() {
    return staging;
  }
  
  public void setStaging(boolean staging) {
    this.staging = staging;
  }
  
}
