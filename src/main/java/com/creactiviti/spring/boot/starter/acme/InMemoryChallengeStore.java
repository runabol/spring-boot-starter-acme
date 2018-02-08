package com.creactiviti.spring.boot.starter.acme;

import java.util.Map;

/**
 * @author Arik Cohen
 * @since Feb 07, 2018
 */
public class InMemoryChallengeStore implements ChallengeStore {
  
  private static final long FIFTEEN_MINS = 15 * 60 * 1000;
  
  private Map<String, String> challenges = new SelfExpiringHashMap<>(FIFTEEN_MINS);
  
  public String get (String aToken) {
    return challenges.get(aToken);
  }
  
  public void put (String aToken, String aAuthorization) {
    challenges.put(aToken, aAuthorization);
  }
  
}
