package com.creactiviti.spring.boot.starter.letsencrypt;

import java.security.Security;

import javax.annotation.PostConstruct;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages="com.creactiviti.spring.boot.starter.letsencrypt")
public class LetsEncryptAutoConfiguration {
  
  @PostConstruct
  public void init () {
    Security.addProvider(new BouncyCastleProvider());
  }
  
}
