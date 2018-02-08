package com.creactiviti.spring.boot.starter.letsencrypt;

import java.security.Security;
import java.util.Arrays;
import java.util.Collection;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Arik Cohen
 * @since Feb 07, 2018
 */
@RestController
public class LetsEncryptController {

  private static final Logger LOG = LoggerFactory.getLogger(LetsEncryptController.class);

  private final CertGenerator certGenerator;
  
  private final ChallengeStore challengeStore;
  
  public LetsEncryptController (ChallengeStore aChallengeStore, CertGenerator aCertGenerator) {
    challengeStore = aChallengeStore;
    certGenerator = aCertGenerator;
  }

  @GetMapping("/letsencrypt/generate")
  public String generate () {

    LOG.info("Starting up...");

    Security.addProvider(new BouncyCastleProvider());

    Collection<String> domains = Arrays.asList("readingtheclassics.org");
    try {
      certGenerator.fetchCertificate(domains);
    } catch (Exception ex) {
      LOG.error("Failed to get a certificate for domains " + domains, ex);
    }

    return "OK";
  }

  @GetMapping("/.well-known/acme-challenge/{token}")
  public String challenge (@PathVariable("token") String aToken) {
    return challengeStore.get(aToken);
  }

}
