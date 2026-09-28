package io.seleniumstudio.conditions;

/**
 * The type argument of the produced {@code ExpectedCondition<T>}.
 *
 * <p>Erasure destroys this at runtime, so it is recorded as metadata. Carrying it as an enum
 * rather than a {@code Class} keeps this package free of any Selenium dependency, which is what
 * allows the annotation processor to run without Selenium on the processor path.</p>
 */
public enum Yields { ELEMENT, ELEMENTS, BOOLEAN, ALERT, DRIVER, OBJECT }
