package com.github.csueiras.acme;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller that provides the ACME Challenge endpoint
 *
 * @author Arik Cohen
 * @since Feb 07, 2018
 */
@RestController
public class ChallengeController {
    private static final Logger logger = LoggerFactory.getLogger(ChallengeController.class);

    private final ChallengeStore challengeStore;

    @Autowired
    public ChallengeController(final ChallengeStore aChallengeStore) {
        challengeStore = aChallengeStore;
    }

    @GetMapping("/.well-known/acme-challenge/{token}")
    public String challenge(@PathVariable("token") String aToken) {
        logger.info("Received challenge for {}", aToken);
        return challengeStore.get(aToken);
    }

}
