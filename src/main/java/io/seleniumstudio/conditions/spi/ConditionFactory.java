package io.seleniumstudio.conditions.spi;

import io.seleniumstudio.conditions.Attributes;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

/**
 * Open extension point. Any condition not in the closed catalogue — project-specific predicates,
 * conditions added by a future Selenium release, or boolean structure too deep to express in
 * annotations — is supplied by an implementation of this interface referenced from
 * {@code Condition.factory()}.
 *
 * <p>Implementations must be public with a public no-argument constructor.</p>
 */
public interface ConditionFactory {

    ExpectedCondition<?> create(WebDriver driver, By locator, Attributes attributes);

    /**
     * Sentinel default. Annotation members cannot be {@code null}, so absence of a factory is
     * encoded by this type rather than by {@code null}.
     */
    final class None implements ConditionFactory {
        @Override
        public ExpectedCondition<?> create(WebDriver driver, By locator, Attributes attributes) {
            throw new IllegalStateException(
                    "ConditionFactory.None is a sentinel and must never be invoked");
        }
    }
}
