package io.github.jpa_labs.jpafieldconstraints;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
class AllExistsValidator implements ConstraintValidator<AllExists, Object> {

  @PersistenceContext private EntityManager entityManager;

  private final ApplicationContext applicationContext;

  private Class<?> entityClass;
  private String attributePath;
  private boolean ignoreNullOrEmpty;
  private boolean ignoreCase;
  private String dtoField;
  private boolean typeLevel;
  private boolean remote;
  private Class<?> lookupType;
  private AllExists allExistsAnnotation;

  AllExistsValidator(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  public void initialize(AllExists constraintAnnotation) {
    this.allExistsAnnotation = constraintAnnotation;
    this.lookupType = constraintAnnotation.lookup();
    this.remote = lookupType != void.class;
    this.entityClass = constraintAnnotation.entity();
    if (remote) {
      if (!RemoteAllExistsLookup.class.isAssignableFrom(lookupType)) {
        throw new IllegalArgumentException(
            "AllExists.lookup() must be assignable to RemoteAllExistsLookup: "
                + lookupType.getName());
      }
      if (entityClass.isPrimitive() || entityClass.isArray()) {
        throw new IllegalArgumentException(
            "entity must be a non-array reference type when using lookup: " + entityClass);
      }
    } else {
      UniqueConstraintPathSecurity.assertJpaEntityClass(this.entityClass);
    }
    this.attributePath = constraintAnnotation.column();
    this.ignoreNullOrEmpty = constraintAnnotation.ignoreNullOrEmpty();
    this.ignoreCase = constraintAnnotation.ignoreCase();
    this.dtoField = nullToEmpty(constraintAnnotation.dtoField());
    UniqueConstraintPathSecurity.assertJpaAttributePath(this.attributePath, "column");
    this.typeLevel = !this.dtoField.isBlank();
    if (this.typeLevel) {
      UniqueConstraintPathSecurity.assertDtoPropertyPath(this.dtoField, "dtoField");
    }
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    Object source = value;
    if (typeLevel) {
      if (value == null) {
        return true;
      }
      source = new BeanWrapperImpl(value).getPropertyValue(dtoField);
    }
    if (source == null) {
      return ignoreNullOrEmpty || fail(context);
    }
    if (!(source instanceof Iterable<?> iterable)) {
      return fail(context);
    }
    Set<Object> normalized = normalizeValues(iterable);
    if (normalized.isEmpty()) {
      return true;
    }
    if (remote) {
      RemoteAllExistsLookup lookup =
          applicationContext.getBean(lookupType.asSubclass(RemoteAllExistsLookup.class));
      if (lookup.allExist(normalized, allExistsAnnotation)) {
        return true;
      }
      return fail(context);
    }
    long matched =
        JpaUniqueConstraintSupport.countRowsIn(
            entityManager, entityClass, attributePath, normalized, ignoreCase);
    if (matched == normalized.size()) {
      return true;
    }
    return fail(context);
  }

  private Set<Object> normalizeValues(Iterable<?> iterable) {
    Set<Object> values = new LinkedHashSet<>();
    for (Object item : iterable) {
      if (item == null || (item instanceof String s && s.isBlank())) {
        if (!ignoreNullOrEmpty) {
          values.add(item);
        }
      } else {
        values.add(item);
      }
    }
    return values;
  }

  private static String nullToEmpty(String s) {
    return java.util.Objects.requireNonNullElse(s, "");
  }

  private boolean fail(ConstraintValidatorContext context) {
    if (typeLevel) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
          .addPropertyNode(dtoField)
          .addConstraintViolation();
    }
    return false;
  }
}
