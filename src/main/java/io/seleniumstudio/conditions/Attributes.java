package io.seleniumstudio.conditions;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Named argument bag for a declared condition.
 *
 * <p>{@code @Target({})} is deliberate: an empty target set makes this type usable <em>only</em>
 * as a member of another annotation. That structurally enforces the coupling to
 * {@link Condition} — an orphan {@code @Attributes} will not compile.</p>
 *
 * <p>Every default is a sentinel outside the legal value domain (see {@link Sentinels}), so the
 * resolver and the annotation processor can distinguish "not supplied" from "supplied as the
 * default". No member is overloaded to serve two unrelated Selenium parameters; frame name/id and
 * frame index have their own members rather than sharing {@code property} and {@code number}.</p>
 *
 * <p>{@code value()} is intentionally left undeclared, so the single-element shorthand
 * {@code @Attributes("x")} is not available and cannot be mistaken for a condition name.</p>
 */
@Documented
@Retention(RUNTIME)
@Target({})
public @interface Attributes {

    /** {@code attributeToBe / attributeContains / attributeToBeNotEmpty / domAttributeToBe}. */
    String attribute() default Sentinels.UNSET_STRING;

    /** Expected value for the attribute or DOM property comparisons. */
    String expectedValue() default Sentinels.UNSET_STRING;

    /** {@code domPropertyToBe(WebElement, String property, String value)}. */
    String property() default Sentinels.UNSET_STRING;

    /** {@code elementSelectionStateToBe(By, boolean)}. Three-valued: {@code boolean} has no sentinel. */
    Tri selected() default Tri.UNSET;

    /** {@code numberOfElementsToBe* } and {@code numberOfWindowsToBe}. */
    int number() default Sentinels.UNSET_INT;

    /** {@code textToBePresentInElementLocated / textToBePresentInElementValue / invisibilityOfElementWithText}. */
    String text() default Sentinels.UNSET_STRING;

    /** {@code javaScriptThrowsNoExceptions / jsReturnsValue}. */
    String javaScript() default Sentinels.UNSET_STRING;

    /**
     * {@code urlMatches(String)} and {@code textMatches(By, Pattern)}.
     * Carried as source text because {@code java.util.regex.Pattern} is not a legal annotation
     * member type (JLS 9.6.1); compiled by the resolver and validated by the processor.
     */
    String regex() default Sentinels.UNSET_STRING;

    /** {@code titleIs / titleContains}. */
    String title() default Sentinels.UNSET_STRING;

    /** {@code urlContains(String fraction)} — Selenium's own parameter name. */
    String fraction() default Sentinels.UNSET_STRING;

    /** {@code urlToBe(String)}. */
    String url() default Sentinels.UNSET_STRING;

    /** Strategy half of the nested-element child locator. */
    Using childUsing() default Using.UNSET;

    /** Value half of the nested-element child locator. */
    String childValue() default Sentinels.UNSET_STRING;

    /** {@code frameToBeAvailableAndSwitchToIt(String nameOrId)} — its own member, not shared. */
    String frameNameOrId() default Sentinels.UNSET_STRING;

    /** {@code frameToBeAvailableAndSwitchToIt(int index)} — its own member, not shared. */
    int frameIndex() default Sentinels.UNSET_INT;
}
