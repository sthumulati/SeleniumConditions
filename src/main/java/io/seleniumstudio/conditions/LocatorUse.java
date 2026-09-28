package io.seleniumstudio.conditions;

/**
 * Whether a condition consumes the locator supplied by its declaration site.
 *
 * <p>This is the discriminator that lets the annotation processor reject, at compile time,
 * a document-scoped condition placed on a locator field (or the reverse).</p>
 */
public enum LocatorUse {
    /** Condition takes a {@code By}; must be declared on a {@code By}-typed field. */
    REQUIRED,
    /** Condition is document-scoped; must be declared on a type or method, never on a locator field. */
    FORBIDDEN,
    /** Locator is used only if the declaration site provides one (CUSTOM factories). */
    OPTIONAL
}
