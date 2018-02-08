package com.creactiviti.spring.boot.starter.letsencrypt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="letsencrypt")
public class LetsEncryptProperties {

  private boolean enabled = false;
  
  private boolean acceptTermsOfService = false;
  
  private String domainName;
  
  public boolean isAcceptTermsOfService() {
    return acceptTermsOfService;
  }
  
  public void setAcceptTermsOfService(boolean aAcceptsTos) {
    acceptTermsOfService = aAcceptsTos;
  }
  
  public String getDomainName() {
    return domainName;
  }
  
  public void setDomainName(String aDomainName) {
    domainName = aDomainName;
  }
  
  public boolean isEnabled() {
    return enabled;
  }
  
  public void setEnabled(boolean aEnabled) {
    enabled = aEnabled;
  }
  
}
