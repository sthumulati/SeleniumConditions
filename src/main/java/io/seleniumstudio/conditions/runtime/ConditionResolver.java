package io.seleniumstudio.conditions.runtime;

import io.seleniumstudio.conditions.Condition;
import io.seleniumstudio.conditions.ExpectedConditionType;
import io.seleniumstudio.conditions.build.ConditionBuilders;
import io.seleniumstudio.conditions.spi.ConditionFactory;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Turns one {@link Condition} declaration into a live {@code ExpectedCondition}. */
public final class ConditionResolver {

    private final Map<Class<? extends ConditionFactory>, ConditionFactory> factoryCache =
            new ConcurrentHashMap<Class<? extends ConditionFactory>, ConditionFactory>();

    public ExpectedCondition<?> resolve(WebDriver driver, By locator, Condition c) {
        ExpectedConditionType type = c.value();

        ExpectedCondition<?> base = (type == ExpectedConditionType.CUSTOM)
                ? factory(c.factory()).create(driver, locator, c.with())
                : ConditionBuilders.build(type, driver, locator, c.with());

        if (base == null) {
            throw new IllegalStateException("Resolver produced a null condition for " + type);
        }
        if (c.refreshed()) base = refreshed(base);
        if (c.negate())    base = ExpectedConditions.not(base);
        return base;
    }

    /** Capture helper: {@code refreshed} is generic, so the wildcard must be captured. */
    private static <T> ExpectedCondition<T> refreshed(ExpectedCondition<T> inner) {
        return ExpectedConditions.refreshed(inner);
    }

    private ConditionFactory factory(Class<? extends ConditionFactory> declared) {
        if (declared == null || ConditionFactory.None.class.equals(declared)) {
            throw new IllegalStateException(
                    "value() == CUSTOM requires factory() to name a ConditionFactory implementation");
        }
        ConditionFactory cached = factoryCache.get(declared);
        if (cached != null) return cached;
        try {
            ConditionFactory created = declared.getDeclaredConstructor().newInstance();
            factoryCache.put(declared, created);
            return created;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "ConditionFactory " + declared.getName()
                            + " must be public with a public no-argument constructor", e);
        }
    }
}
