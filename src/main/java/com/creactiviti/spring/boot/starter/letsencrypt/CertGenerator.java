package com.creactiviti.spring.boot.starter.letsencrypt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URI;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Registration;
import org.shredzone.acme4j.RegistrationBuilder;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeConflictException;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.CertificateUtils;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class CertGenerator {

  private static final File USER_KEY_FILE = new File("ssl/user.key");
  private static final File DOMAIN_KEY_FILE = new File("ssl/domain.key");
  private static final File DOMAIN_CSR_FILE = new File("ssl/domain.csr");
  private static final File DOMAIN_CHAIN_FILE = new File("ssl/domain-chain.crt");

  private static final int KEY_SIZE = 2048;

  private static final Logger logger = LoggerFactory.getLogger(ChallengeController.class);

  private final ChallengeStore challengeStore;
  
  private final LetsEncryptProperties config;

  public CertGenerator (ChallengeStore aChallengeStore, LetsEncryptProperties aConfig) {
    challengeStore = aChallengeStore;
    config = aConfig;
  }

  /**
   * Generates a certificate for the given domain. Also takes care of the registration
   * process.
   *
   * @param aDomain
   *            The name of the daomain to get a common certificate for
   */
  public void generate (String aDomain) throws Exception {
    // Load the user key file. If there is no key file, create a new one.
    // Keep this key pair in a safe place! In a production environment, you will not be
    // able to access your account again if you should lose the key pair.
    KeyPair userKeyPair = loadOrCreateKeyPair(USER_KEY_FILE);

    // Create a session for Let's Encrypt.
    // Use "acme://letsencrypt.org" for production server
    //Session session = new Session("acme://letsencrypt.org/staging", userKeyPair);
    Session session = new Session("acme://letsencrypt.org", userKeyPair);

    // Get the Registration to the account.
    // If there is no account yet, create a new one.
    Registration reg = getOrCreateAccount(session);

    authorize(reg, aDomain);

    // Load or create a key pair for the domains. This should not be the userKeyPair!
    KeyPair domainKeyPair = loadOrCreateKeyPair(DOMAIN_KEY_FILE);

    // Generate a CSR for all of the domains, and sign it with the domain key pair.
    CSRBuilder csrb = new CSRBuilder();
    csrb.addDomains(Arrays.asList(aDomain));
    csrb.sign(domainKeyPair);

    // Write the CSR to a file, for later use.
    try (Writer out = new FileWriter(DOMAIN_CSR_FILE)) {
      csrb.write(out);
    }

    // Now request a signed certificate.
    Certificate certificate = reg.requestCertificate(csrb.getEncoded());

    logger.info("Success! The certificate for domain " + aDomain + " has been generated!");
    logger.info("Certificate URL: " + certificate.getLocation());

    // Download the leaf certificate and certificate chain.
    X509Certificate cert = certificate.download();
    X509Certificate[] chain = certificate.downloadChain();

    // Write a combined file containing the certificate and chain.
    try (FileWriter fw = new FileWriter(DOMAIN_CHAIN_FILE)) {
      CertificateUtils.writeX509CertificateChain(fw, cert, chain);
    }


    ProcessBuilder pbuilder = new ProcessBuilder("openssl","pkcs12","-export","-out","ssl/server.p12","-inkey","ssl/domain.key","-in","ssl/domain-chain.crt","-password","pass:password");
    pbuilder.redirectErrorStream(true);

    Process process = pbuilder.start();
    int errCode = process.waitFor();
    System.out.println("Echo Output: " + errCode +"\n" + output(process.getInputStream()));

    // That's all! Configure your web server to use the DOMAIN_KEY_FILE and
    // DOMAIN_CHAIN_FILE for the requested domans.
  }

  private String output(InputStream inputStream) throws IOException {
    StringBuilder sb = new StringBuilder();
    BufferedReader br = null;
    try {
      br = new BufferedReader(new InputStreamReader(inputStream));
      String line = null;
      while ((line = br.readLine()) != null) {
        sb.append(line + System.getProperty("line.separator"));
      }
    } finally {
      br.close();
    }
    return sb.toString();
  }

  /**
   * Loads a key pair from specified file. If the file does not exist,
   * a new key pair is generated and saved.
   *
   * @return {@link KeyPair}.
   */
  private KeyPair loadOrCreateKeyPair(File file) throws IOException {
    if (file.exists()) {
      try (FileReader fr = new FileReader(file)) {
        return KeyPairUtils.readKeyPair(fr);
      }
    } else {
      KeyPair domainKeyPair = KeyPairUtils.createKeyPair(KEY_SIZE);
      try (FileWriter fw = new FileWriter(file)) {
        KeyPairUtils.writeKeyPair(domainKeyPair, fw);
      }
      return domainKeyPair;
    }
  }

  /**
   * Finds your {@link Registration} at the ACME server. It will be found by your user's
   * public key. If your key is not known to the server yet, a new registration will be
   * created.
   * <p>
   * This is a simple way of finding your {@link Registration}. A better way is to get
   * the URL of your new registration with {@link Registration#getLocation()} and store
   * it somewhere. If you need to get access to your account later, reconnect to it via
   * {@link Registration#bind(Session, URL)} by using the stored location.
   *
   * @param session
   *            {@link Session} to bind with
   * @return {@link Registration} connected to your account
   */
  private Registration getOrCreateAccount(Session session) throws AcmeException {

    Registration reg;

    try {
      // Try to create a new Registration.
      reg = new RegistrationBuilder().create(session);
      logger.info("Registered a new user, URL: " + reg.getLocation());

      // This is a new account. Let the user accept the Terms of Service.
      // We won't be able to authorize domains until the ToS is accepted.
      URI agreement = reg.getAgreement();
      logger.info("Terms of Service: " + agreement);
      acceptAgreement(reg, agreement);

    } catch (AcmeConflictException ex) {
      // The Key Pair is already registered. getLocation() contains the
      // URL of the existing registration's location. Bind it to the session.
      reg = Registration.bind(session, ex.getLocation());
      logger.info("Account does already exist, URL: " + reg.getLocation(), ex);
    }

    return reg;
  }

  /**
   * Authorize a domain. It will be associated with your account, so you will be able to
   * retrieve a signed certificate for the domain later.
   * <p>
   * You need separate authorizations for subdomains (e.g. "www" subdomain). Wildcard
   * certificates are currently not supported.
   *
   * @param aRegistration
   *            {@link Registration} of your account
   * @param aDomain
   *            Name of the domain to authorize
   */
  private void authorize (Registration aRegistration, String aDomain) throws AcmeException {
    // Authorize the domain.
    Authorization auth = aRegistration.authorizeDomain(aDomain);
    logger.info("Authorization for domain " + aDomain);

    // Find the desired challenge and prepare it.
    Challenge challenge = httpChallenge(auth, aDomain);

    // If the challenge is already verified, there's no need to execute it again.
    if (challenge.getStatus() == Status.VALID) {
      return;
    }

    // Now trigger the challenge.
    challenge.trigger();

    // Poll for the challenge to complete.
    try {
      int attempts = 10;
      while (challenge.getStatus() != Status.VALID && attempts-- > 0) {
        // Did the authorization fail?
        if (challenge.getStatus() == Status.INVALID) {
          throw new AcmeException("Challenge failed... Giving up.");
        }

        // Wait for a few seconds
        Thread.sleep(3000L);

        // Then update the status
        challenge.update();
      }
    } catch (InterruptedException ex) {
      logger.error("interrupted", ex);
    }

    // All reattempts are used up and there is still no valid authorization?
    if (challenge.getStatus() != Status.VALID) {
      throw new AcmeException("Failed to pass the challenge for domain " + aDomain + ", ... Giving up.");
    }
    
  }

  /**
   * Prepares a HTTP challenge.
   * <p>
   * The verification of this challenge expects a file with a certain content to be
   * reachable at a given path under the domain to be tested.
   * </p>
   *
   * @param aAuthorization
   *            {@link Authorization} to find the challenge in
   * @param aDomainName
   *            Domain name to be authorized
   * @return {@link Challenge} to verify
   */
  private Challenge httpChallenge(Authorization aAuthorization, String aDomainName) throws AcmeException {
    // Find a single http-01 challenge
    Http01Challenge challenge = aAuthorization.findChallenge(Http01Challenge.TYPE);
    
    if (challenge == null) {
      throw new AcmeException("Found no " + Http01Challenge.TYPE + " challenge, don't know what to do...");
    }
    
    challengeStore.put(challenge.getToken(), challenge.getAuthorization());

    return challenge;
  }

  private void acceptAgreement(Registration aRegistration, URI aAgreement) throws AcmeException {
    Assert.isTrue(config.isAcceptTermsOfService(),"You must accept the TOS: " + aAgreement);
    // Motify the Registration and accept the agreement
    aRegistration.modify().setAgreement(aAgreement).commit();
    logger.info("Updated user's ToS");
  }

}
