package io.github.jpa_labs.jpafieldconstraints;

import java.util.Set;

/**
 * Spring bean contract for {@linkplain AllExists#lookup() remote} {@link AllExists} validation (for
 * example batch checks via OpenFeign).
 *
 * <p>Register a bean whose type matches {@link AllExists#lookup()}, typically a {@code @Component}
 * that injects a {@code @FeignClient}. You may issue one HTTP {@code HEAD} per distinct value (or a
 * batch API if the remote service supports it).
 */
@FunctionalInterface
public interface RemoteAllExistsLookup {

  /**
   * @param values distinct non-null elements to check (empty collections are not passed)
   * @param constraint annotation instance for this validation
   * @return {@code true} when every value exists remotely
   */
  boolean allExist(Set<Object> values, AllExists constraint);
}
