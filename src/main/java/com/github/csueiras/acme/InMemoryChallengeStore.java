package com.github.csueiras.acme;

import java.util.Map;

/**
 * {@link ChallengeStore} backed by an in-memory store
 *
 * @author Arik Cohen
 * @since Feb 07, 2018
 */
public class InMemoryChallengeStore implements ChallengeStore {
    private static final long FIFTEEN_MINS = 15 * 60 * 1000;

    private final Map<String, String> challenges;

    public InMemoryChallengeStore() {
        challenges = new SelfExpiringHashMap<>(FIFTEEN_MINS);
    }

    public String get(final String aToken) {
        return challenges.get(aToken);
    }

    public void put(final String aToken, final String aAuthorization) {
        challenges.put(aToken, aAuthorization);
    }

}
