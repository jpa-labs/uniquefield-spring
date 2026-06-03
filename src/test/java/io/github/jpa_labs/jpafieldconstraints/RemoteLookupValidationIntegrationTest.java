package io.github.jpa_labs.jpafieldconstraints;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = TestApplication.class)
@Import(FeignHeadRemoteLookupConfiguration.class)
@TestPropertySource(
    properties = {
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.sql.init.mode=never",
    })
class RemoteLookupValidationIntegrationTest {

  @Autowired private Validator validator;

  @Test
  void existsDelegatesToRemoteLookupBeanUsingHead() {
    Set<ConstraintViolation<RemoteExistsDto>> ok =
        validator.validate(new RemoteExistsDto(FeignHeadRemoteLookupConfiguration.EXISTING_RESOURCE_ID));
    assertThat(ok).isEmpty();

    Set<ConstraintViolation<RemoteExistsDto>> bad =
        validator.validate(new RemoteExistsDto("missing"));
    assertThat(bad).hasSize(1);
    assertThat(bad.iterator().next().getPropertyPath()).hasToString("code");
  }

  @Test
  void allExistsDelegatesToRemoteLookupBeanUsingHeadPerElement() {
    Set<ConstraintViolation<RemoteAllExistsDto>> ok =
        validator.validate(
            new RemoteAllExistsDto(List.of(FeignHeadRemoteLookupConfiguration.EXISTING_RESOURCE_ID)));
    assertThat(ok).isEmpty();

    Set<ConstraintViolation<RemoteAllExistsDto>> bad =
        validator.validate(new RemoteAllExistsDto(List.of("other")));
    assertThat(bad).hasSize(1);
    assertThat(bad.iterator().next().getPropertyPath()).hasToString("codes");
  }

  record RemoteExistsDto(
      @Exists(
              lookup = TestExistsRemoteLookup.class,
              entity = Object.class,
              column = "id")
          String code) {}

  record RemoteAllExistsDto(
      @AllExists(
              lookup = TestAllExistsRemoteLookup.class,
              entity = Object.class,
              column = "id")
          List<String> codes) {}
}
