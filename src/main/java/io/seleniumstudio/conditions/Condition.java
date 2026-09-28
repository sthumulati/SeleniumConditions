package io.seleniumstudio.conditions;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import io.seleniumstudio.conditions.spi.ConditionFactory;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Declares an expected condition, its arguments and its wait envelope.
 *
 * <h2>Target semantics</h2>
 * <ul>
 *   <li>{@code TYPE} — the page's load contract. Every condition must be
 *       {@link LocatorUse#FORBIDDEN}; evaluated by {@code AnnotatedPage.awaitLoaded()}.</li>
 *   <li>{@code FIELD} — applies to the {@code By} held in that field. Every condition must be
 *       {@link LocatorUse#REQUIRED}; evaluated by {@code AnnotatedPage.await(By)}.</li>
 *   <li>{@code METHOD} — document-scoped post-condition for that method. Every condition must be
 *       {@link LocatorUse#FORBIDDEN}; evaluated by {@code AnnotatedPage.awaitAfter(String)}.</li>
 * </ul>
 * All three rules are enforced by the annotation processor, not merely documented.
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, FIELD, METHOD })
@Repeatable(Conditions.class)
public @interface Condition {

    /** The condition to evaluate. Enum rather than String: overloads in {@code ExpectedConditions}
     *  make a bare method name insufficient to select a target. */
    ExpectedConditionType value();

    /** Named arguments. Which members are required is declared by {@link ExpectedConditionType}. */
    Attributes with() default @Attributes;

    /** Overrides the framework default. {@code -1} inherits. */
    long timeoutMillis() default Sentinels.INHERIT_MILLIS;

    /** Overrides the framework default polling interval. {@code -1} inherits. */
    long pollingMillis() default Sentinels.INHERIT_MILLIS;

    /** Wraps the condition in {@code ExpectedConditions.not(...)}. */
    boolean negate() default false;

    /** Wraps the condition in {@code ExpectedConditions.refreshed(...)} to survive DOM replacement. */
    boolean refreshed() default false;

    /** Additional throwables to swallow during polling, beyond the FluentWait defaults. */
    Class<? extends Throwable>[] ignoring() default {};

    /** Required if and only if {@code value() == CUSTOM}. Enforced by the processor. */
    Class<? extends ConditionFactory> factory() default ConditionFactory.None.class;

    /** Diagnostic text surfaced in the timeout message. */
    String message() default "";
}
