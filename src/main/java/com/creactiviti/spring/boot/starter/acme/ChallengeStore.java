package com.creactiviti.spring.boot.starter.acme;

/**
 * @author Arik Cohen
 * @since Feb 07, 2018
 */
public interface ChallengeStore {
  
  String get (String aToken);
  
  void put (String aToken, String aAuthorization);
  
}
