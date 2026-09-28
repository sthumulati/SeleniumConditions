package io.seleniumstudio.conditions;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Container for repeated {@link Condition} declarations. Carries the combinator, which is how
 * {@code ExpectedConditions.and(...)} and {@code or(...)} are expressed.
 *
 * <p>Note the reflection asymmetry mandated by JLS 9.7.5: a <em>single</em> {@code @Condition} is
 * stored directly and is NOT wrapped in this container. {@code PageScanner} therefore reads
 * {@code getAnnotationsByType(Condition.class)} for the elements and separately probes for this
 * container to recover {@link #combine()}, defaulting to {@code AND} when absent.</p>
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, FIELD, METHOD })
public @interface Conditions {

    Condition[] value();

    Combine combine() default Combine.AND;
}
