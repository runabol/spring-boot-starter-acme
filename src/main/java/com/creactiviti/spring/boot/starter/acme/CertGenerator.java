package com.creactiviti.spring.boot.starter.acme;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
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

/**
 * @author Arik Cohen
 * @since Feb 07, 2018
 */
@Component
public class CertGenerator {

  private final ChallengeStore challengeStore;
  
  private final AcmeConfigProperties config;
  
  private static final int KEY_SIZE = 2048;

  private static final Logger logger = LoggerFactory.getLogger(ChallengeController.class);

  public CertGenerator (ChallengeStore aChallengeStore, AcmeConfigProperties aConfig) {
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
    KeyPair userKeyPair = loadOrCreateKeyPair(new File(config.getUserKeyFile()));

    // Create a session for Let's Encrypt.
    Session session = new Session(config.getEndpoint(), userKeyPair);

    // Get the Registration to the account.
    // If there is no account yet, create a new one.
    Registration reg = getOrCreateAccount(session);

    authorize(reg, aDomain);

    // Load or create a key pair for the domains. This should not be the userKeyPair!
    KeyPair domainKeyPair = loadOrCreateKeyPair(new File(config.getDomainKeyFile()));

    // Generate a CSR for all of the domains, and sign it with the domain key pair.
    CSRBuilder csrb = new CSRBuilder();
    csrb.addDomains(Arrays.asList(aDomain));
    csrb.sign(domainKeyPair);

    // Write the CSR to a file, for later use.
    try (Writer out = new FileWriter(new File(config.getDomainCsrFile()))) {
      csrb.write(out);
    }

    // Now request a signed certificate.
    Certificate certificate = reg.requestCertificate(csrb.getEncoded());

    logger.info("Success! The certificate for domain {} has been generated!", aDomain);
    logger.info("Certificate URL: {}", certificate.getLocation());

    // Download the leaf certificate and certificate chain.
    X509Certificate cert = certificate.download();
    X509Certificate[] chain = certificate.downloadChain();

    // Write a combined file containing the certificate and chain.
    try (FileWriter fw = new FileWriter(new File (config.getDomainChainFile()))) {
      CertificateUtils.writeX509CertificateChain(fw, cert, chain);
    }

    // convert the certificate format to PKS
    ProcessBuilder pbuilder = new ProcessBuilder("openssl","pkcs12","-export","-out",config.getKeyStoreFile(),"-inkey",config.getDomainKeyFile(),"-in",config.getDomainChainFile(),"-password","pass:" + config.getKeyStorePassword());
    pbuilder.redirectErrorStream(true);

    Process process = pbuilder.start();
    int errCode = process.waitFor();
    
    try(InputStream in = process.getInputStream(); StringWriter writer = new StringWriter()) {
      IOUtils.copy(in, writer, "ASCII");
      logger.debug("openssl finished with exit code {} \n{}",errCode, writer.toString());
    }
    
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
    Assert.isTrue(config.isAcceptTermsOfService(),"You must accept the TOS: " + aAgreement + " by setting the property acme.accept-terms-of-service to true");
    // Motify the Registration and accept the agreement
    aRegistration.modify().setAgreement(aAgreement).commit();
    logger.info("Updated user's ToS");
  }

}
