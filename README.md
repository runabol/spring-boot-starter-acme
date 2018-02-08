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

# Configuration

| Name                         | Description                         | Default Value  | 
|------------------------------|-------------------------------------|----------------|
| acme.accept-terms-of-service |                                     |                |


# License

Version 2.0 of the Apache License.