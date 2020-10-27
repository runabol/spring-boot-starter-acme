package com.github.csueiras.acme;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Arik Cohen
 * @since Feb 07, 2018
 */
@Component
public class CertGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ChallengeController.class);
    private static final int KEY_SIZE = 2048;

    private final ChallengeStore challengeStore;
    private final AcmeConfigProperties config;

    @Autowired
    public CertGenerator(final ChallengeStore aChallengeStore, final AcmeConfigProperties aConfig) {
        challengeStore = aChallengeStore;
        config = aConfig;
    }

    /**
     * Generates a certificate for the given domain. Also takes care of the registration
     * process.
     *
     * @param aDomain The name of the domain to get a common certificate for
     * @throws Exception if an error occurs during generation
     */
    public void generate(String aDomain) throws Exception {
        // Load the user key file. If there is no key file, create a new one.
        // Keep this key pair in a safe place! In a production environment, you will not be
        // able to access your account again if you should lose the key pair.
        final KeyPair userKeyPair = loadOrCreateKeyPair(new File(config.getUserKeyFile()));

        // Create a session for Let's Encrypt.
        final Session session = new Session(config.getEndpoint());

        // Get the Account.
        // If there is no account yet, create a new one.
        final Account acct = findOrRegisterAccount(session, userKeyPair);

        // Order the certificate
        final Order order = acct.newOrder().domains(aDomain).create();

        // Perform all required authorizations
        for (final Authorization auth : order.getAuthorizations()) {
            authorize(auth);
        }

        // Load or create a key pair for the domains. This should not be the userKeyPair!
        final KeyPair domainKeyPair = loadOrCreateKeyPair(new File(config.getDomainKeyFile()));

        // Generate a CSR for all of the domains, and sign it with the domain key pair.
        final CSRBuilder csrb = new CSRBuilder();
        csrb.addDomains(List.of(aDomain));
        csrb.sign(domainKeyPair);

        // Write the CSR to a file, for later use.
        try (final Writer out = new FileWriter(new File(config.getDomainCsrFile()))) {
            csrb.write(out);
        }

        // Order the certificate
        order.execute(csrb.getEncoded());

        // Wait for the order to complete
        try {
            int attempts = 10;
            while (order.getStatus() != Status.VALID && attempts-- > 0) {
                // Did the order fail?
                if (order.getStatus() == Status.INVALID) {
                    throw new AcmeException("Order failed... Giving up.");
                }

                // Wait for a few seconds
                Thread.sleep(3000L);

                // Then update the status
                order.update();
            }
        } catch (final InterruptedException ex) {
            logger.error("interrupted", ex);
            Thread.currentThread().interrupt();
            return;
        }

        // Get the certificate
        final Certificate certificate = order.getCertificate();
        if (certificate == null) {
            throw new Exception("No certificate in the order");
        }

        logger.info("Success! The certificate for domain {} has been generated!", aDomain);
        logger.info("Certificate URL: {}", certificate.getLocation());

        // Write a combined file containing the certificate and chain.
        try (final FileWriter fw = new FileWriter(new File(config.getDomainChainFile()))) {
            certificate.writeCertificate(fw);
        }

        // convert the certificate format to PKS
        final ProcessBuilder pbuilder = new ProcessBuilder("openssl", "pkcs12", "-export", "-out", config.getKeyStoreFile(), "-inkey", config.getDomainKeyFile(), "-in", config.getDomainChainFile(), "-password", "pass:" + config.getKeyStorePassword());
        pbuilder.redirectErrorStream(true);

        final Process process = pbuilder.start();
        final int errCode = process.waitFor();

        try (final InputStream in = process.getInputStream(); final StringWriter writer = new StringWriter()) {
            IOUtils.copy(in, writer, "ASCII");
            logger.debug("openssl finished with exit code {} \n{}", errCode, writer.toString());
        }
    }

    /**
     * Loads a key pair from specified file. If the file does not exist,
     * a new key pair is generated and saved.
     *
     * @return {@link KeyPair}.
     * @throws IOException if there's an error while loading or writing the key pair
     */
    private KeyPair loadOrCreateKeyPair(final File file) throws IOException {
        if (file.exists()) {
            try (final FileReader fr = new FileReader(file)) {
                return KeyPairUtils.readKeyPair(fr);
            }
        } else {
            final KeyPair domainKeyPair = KeyPairUtils.createKeyPair(KEY_SIZE);
            try (final FileWriter fw = new FileWriter(file)) {
                KeyPairUtils.writeKeyPair(domainKeyPair, fw);
            }
            return domainKeyPair;
        }
    }

    /**
     * Finds your {@link Account} at the ACME server. It will be found by your user's
     * public key. If your key is not known to the server yet, a new account will be
     * created.
     *
     * @param session {@link Session} to bind with
     * @return {@link Account} that is connected to your account
     * @throws AcmeException if there's an error during registration
     */
    private Account findOrRegisterAccount(final Session session, final KeyPair accountKey) throws AcmeException {
        // Ask the user to accept the TOS, if server provides us with a link.
        final URI tos = session.getMetadata().getTermsOfService();
        if (tos != null) {
            acceptAgreement(tos);
        }

        final Account account = new AccountBuilder()
                .agreeToTermsOfService()
                .useKeyPair(accountKey)
                .create(session);
        logger.info("Registered a new user, URL: {}", account.getLocation());

        return account;
    }

    /**
     * Authorize a domain. It will be associated with your account, so you will be able to
     * retrieve a signed certificate for the domain later.
     *
     * @param auth {@link Authorization} to perform
     * @throws AcmeException if there's an error during authorizatinn
     */
    private void authorize(final Authorization auth) throws AcmeException {
        logger.info("Authorization for domain {}", auth.getIdentifier().getDomain());

        // The authorization is already valid. No need to process a challenge.
        if (auth.getStatus() == Status.VALID) {
            return;
        }

        // Find the desired challenge and prepare it.
        final Challenge challenge = httpChallenge(auth);

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
            Thread.currentThread().interrupt();
            return;
        }

        // All reattempts are used up and there is still no valid authorization?
        if (challenge.getStatus() != Status.VALID) {
            throw new AcmeException("Failed to pass the challenge for domain "
                    + auth.getIdentifier().getDomain() + ", ... Giving up.");
        }

        logger.info("Challenge has been completed. Remember to remove the validation resource.");
    }

    /**
     * Prepares a HTTP challenge.
     * <p>
     * The verification of this challenge expects a file with a certain content to be
     * reachable at a given path under the domain to be tested.
     * </p>
     *
     * @param aAuthorization {@link Authorization} to find the challenge in
     * @return {@link Challenge} to verify
     */
    private Challenge httpChallenge(Authorization aAuthorization) throws AcmeException {
        // Find a single http-01 challenge
        Http01Challenge challenge = aAuthorization.findChallenge(Http01Challenge.TYPE);

        if (challenge == null) {
            throw new AcmeException("Found no " + Http01Challenge.TYPE + " challenge, don't know what to do...");
        }

        challengeStore.put(challenge.getToken(), challenge.getAuthorization());

        return challenge;
    }

    private void acceptAgreement(URI agreement) throws AcmeException {
        if (!config.isAcceptTermsOfService()) {
            throw new AcmeException("User did not accept Terms of Service");
        }
    }

}
