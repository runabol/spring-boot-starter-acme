package com.creactiviti.spring.boot.starter.letsencrypt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Arik Cohen
 * @since Feb 07, 2018
 */
@ConfigurationProperties(prefix="letsencrypt")
public class LetsEncryptConfigProperties {

  private boolean acceptTermsOfService = false;
  
  private String domainName;
  
  private boolean staging = true;
  
  private static final String STAGING_ENDPOINT = "acme://letsencrypt.org/staging";
  
  private static final String PRODUCTION_ENDPOINT = "acme://letsencrypt.org";
  
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
  
  public String getEndpoint () {
    return staging ? STAGING_ENDPOINT : PRODUCTION_ENDPOINT;
  }
  
}
