# Spring Boot Starter ACME

A Spring Boot module that is meant to ease the pain of generating a valid SSL Certificate using the Automatic Certificate Management Environment (ACME) protocol.

This project depends on the [acme4j](https://github.com/shred/acme4j) library.

# Dependencies

This module depends on having `openssl` on the `PATH` to convert the certificate to PKCS12 format.

# Maven

```
<dependency>
  <groupId>com.creactiviti</groupId>
  <artifactId>spring-boot-starter-acme</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>

<repositories>
   <repository>
      <id>maven-snapshots</id>
      <url>http://oss.sonatype.org/content/repositories/snapshots</url>
      <layout>default</layout>
      <releases>
         <enabled>false</enabled>
      </releases>
      <snapshots>
         <enabled>true</enabled>
      </snapshots>
   </repository>
</repositories>
```

# Usage

1. Build your Spring Boot project. 

2. Deploy it to a target machine and point your domain name to the IP address of that machine. LetsEncrypt validates your ownership of the domain by maaking a callback to the `http://your-domain/.well-known/acme-challenge/{token}` exposed by this module.

# Configuration

| Name                         | Description                                  | Type           | Default Value                   | 
|------------------------------|----------------------------------------------|----------------|---------------------------------|
| acme.accept-terms-of-service | Accepts the CA's terms of service            | boolean        | false                           |
| acme.domain-name             | The domain name to register the SSL cert for | string         |                                 |
| acme.user-key-file           | The location of the user private key file    | string         | user.key                        |
| acme.domain-key-file         | The location of the domain private key file  | string         | domain.key                      |
| acme.domain-csr-file         | The location of the domain csr file          | string         | domain.csr                      |
| acme.domain-chain-file       | The location of the domain chain file        | string         | domain-chain.crt                |
| acme.key-store-file          | The location of the keystore file            | string         | keystore.p12                    |
| acme.key-store-password      | The keystore password                        | string         | password                        |
| acme.endpoint                | The acme endpoint to generate the cert with  | string         | acme://letsencrypt.org/staging  |

# Endpoints

By default `acme.endpoint` points to LetsEncrypt's staging endpoint. To generate a valid SSL certificate you will have to point it to the production endpoint listed below.

| CA          | Env        | URL                             |
|-------------|------------|---------------------------------|
| LetsEncrypt | Staging    | acme://letsencrypt.org/staging  |
| LetsEncrypt | Prod       | acme://letsencrypt.org          |


# License

Version 2.0 of the Apache License.