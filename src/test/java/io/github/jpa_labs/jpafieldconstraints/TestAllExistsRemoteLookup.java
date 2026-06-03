package io.github.jpa_labs.jpafieldconstraints;

import feign.FeignException;
import feign.Response;
import java.util.Set;

/**
 * Resolves batch existence via repeated {@code HEAD} calls (same contract as {@link
 * TestExistsRemoteLookup}).
 */
public class TestAllExistsRemoteLookup implements RemoteAllExistsLookup {

  private final HeadResourceClient client;

  public TestAllExistsRemoteLookup(HeadResourceClient client) {
    this.client = client;
  }

  @Override
  public boolean allExist(Set<Object> values, AllExists constraint) {
    for (Object value : values) {
      try (Response response = client.headResource(String.valueOf(value))) {
        int status = response.status();
        if (status < 200 || status >= 300) {
          return false;
        }
      } catch (FeignException e) {
        if (e.status() == 404) {
          return false;
        }
        if (e.status() >= 200 && e.status() < 300) {
          continue;
        }
        throw e;
      }
    }
    return true;
  }
}
