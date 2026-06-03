package io.github.jpa_labs.jpafieldconstraints;

import feign.Param;
import feign.RequestLine;
import feign.Response;

/** Feign client used in tests: existence via HTTP {@code HEAD} (2xx = present, 404 = absent). */
interface HeadResourceClient {

  @RequestLine("HEAD /resources/{id}")
  Response headResource(@Param("id") String id);
}
