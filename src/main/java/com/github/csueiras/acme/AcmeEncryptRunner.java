package com.github.csueiras.acme;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * {@link CommandLineRunner} that will generate the certificates
 *
 * @author Arik Cohen
 * @since Feb 07, 2018
 */
@Component
public class AcmeEncryptRunner implements CommandLineRunner {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CertGenerator generator;
    private final AcmeConfigProperties config;

    @Autowired
    public AcmeEncryptRunner(final CertGenerator aCertGenerator, final AcmeConfigProperties aConfig) {
        config = aConfig;
        generator = aCertGenerator;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Generating a LetsEncrypt certificate...");

        Security.addProvider(new BouncyCastleProvider());

        final String domainName = config.getDomainName();

        generator.generate(domainName);
    }

}
