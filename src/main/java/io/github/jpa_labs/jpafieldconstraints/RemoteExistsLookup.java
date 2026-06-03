package io.github.jpa_labs.jpafieldconstraints;

/**
 * Spring bean contract for {@linkplain Exists#lookup() remote} {@link Exists} validation (for example
 * delegating to an OpenFeign client or another service).
 *
 * <p>Register a bean whose type matches {@link Exists#lookup()}, typically a {@code @Component}
 * that injects a {@code @FeignClient}. For HTTP services, a {@code HEAD} request to the resource
 * URL is a common pattern (2xx means present, 404 means absent). If your Feign client turns
 * non-success responses into exceptions, catch those and return {@code false} for “not found”.
 */
@FunctionalInterface
public interface RemoteExistsLookup {

  /**
   * @param value value under validation (never skipped by ignore-null handling; the validator
   *     applies {@link Exists#ignoreNullOrEmpty()} before calling this)
   * @param constraint annotation instance for this validation (use {@link Exists#column()},
   *     {@link Exists#where()}, etc. if the lookup needs them)
   * @return {@code true} when the value exists remotely
   */
  boolean exists(Object value, Exists constraint);
}
