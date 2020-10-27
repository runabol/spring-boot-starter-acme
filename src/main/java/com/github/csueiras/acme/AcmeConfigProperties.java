package com.github.csueiras.acme;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;

/**
 * Configuration properties for ACME
 *
 * @author Arik Cohen
 * @since Feb 07, 2018
 */
@ConfigurationProperties(prefix = "acme")
public class AcmeConfigProperties {

    private static final String LETS_ENCRYPT_PROD_ENDPOINT = "acme://letsencrypt.org";

    private boolean acceptTermsOfService = false;
    @NotBlank
    private String domainName;
    @NotBlank
    private String userKeyFile = "user.key";
    @NotBlank
    private String domainKeyFile = "domain.key";
    @NotBlank
    private String domainCsrFile = "domain.csr";
    @NotBlank
    private String domainChainFile = "domain-chain.crt";
    @NotBlank
    private String keyStoreFile = "keystore.p12";
    @NotBlank
    private String keyStorePassword = "password";
    @NotBlank
    private String endpoint = LETS_ENCRYPT_PROD_ENDPOINT;

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

    public void setAcceptTermsOfService(boolean aAcceptsTos) {
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

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

}
