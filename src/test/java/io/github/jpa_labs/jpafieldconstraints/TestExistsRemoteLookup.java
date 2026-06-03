package io.github.jpa_labs.jpafieldconstraints;

import feign.FeignException;
import feign.Response;

/** Resolves existence via OpenFeign {@link HeadResourceClient}: {@code HEAD} → 2xx means exists. */
public class TestExistsRemoteLookup implements RemoteExistsLookup {

  private final HeadResourceClient client;

  public TestExistsRemoteLookup(HeadResourceClient client) {
    this.client = client;
  }

  @Override
  public boolean exists(Object value, Exists constraint) {
    try (Response response = client.headResource(String.valueOf(value))) {
      int status = response.status();
      return status >= 200 && status < 300;
    } catch (FeignException e) {
      if (e.status() == 404) {
        return false;
      }
      if (e.status() >= 200 && e.status() < 300) {
        return true;
      }
      throw e;
    }
  }
}
