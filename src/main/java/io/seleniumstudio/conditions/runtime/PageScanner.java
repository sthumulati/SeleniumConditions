package io.seleniumstudio.conditions.runtime;

import io.seleniumstudio.conditions.Combine;
import io.seleniumstudio.conditions.Condition;
import io.seleniumstudio.conditions.Conditions;
import io.seleniumstudio.conditions.LocatorUse;

import org.openqa.selenium.By;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reflectively builds a {@link PageContract}.
 *
 * <p>Two behaviours are non-obvious and deliberate:</p>
 * <ul>
 *   <li>Field annotations are not inherited ({@code @Inherited} applies to types only), so the
 *       superclass chain is walked explicitly.</li>
 *   <li>Under JLS 9.7.5, a single {@code @Condition} is stored directly rather than wrapped in
 *       {@code @Conditions}, so the combinator is read from the container only when present.</li>
 * </ul>
 *
 * <p>The contract is keyed on the page <em>instance</em>, not the class, because instance fields
 * hold instance-specific {@code By} objects. Static-only page classes still work; the cache simply
 * never hits across instances.</p>
 */
public final class PageScanner {

    private static final Map<Class<?>, Boolean> VALIDATED =
            new ConcurrentHashMap<Class<?>, Boolean>();

    private PageScanner() { }

    public static PageContract scan(Object page) {
        Class<?> type = page.getClass();

        PageContract.ScopeContract load = scopeOf(type, type.getSimpleName(), LocatorUse.FORBIDDEN);

        IdentityHashMap<By, PageContract.FieldContract> fields =
                new IdentityHashMap<By, PageContract.FieldContract>();
        LinkedHashMap<String, PageContract.ScopeContract> methods =
                new LinkedHashMap<String, PageContract.ScopeContract>();

        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {

            for (Field f : c.getDeclaredFields()) {
                Condition[] declared = f.getAnnotationsByType(Condition.class);
                if (declared.length == 0) continue;

                if (!By.class.isAssignableFrom(f.getType())) {
                    throw new IllegalStateException(
                            c.getName() + "." + f.getName()
                                    + " is annotated with @Condition but is not a By field");
                }
                assertLocatorUse(declared, LocatorUse.REQUIRED,
                        c.getName() + "." + f.getName());

                f.setAccessible(true);
                By locator;
                try {
                    locator = (By) f.get(java.lang.reflect.Modifier.isStatic(f.getModifiers())
                            ? null : page);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Unreadable locator field " + f, e);
                }
                if (locator == null) {
                    throw new IllegalStateException(
                            "Locator field " + c.getName() + "." + f.getName() + " is null");
                }
                fields.put(locator, new PageContract.FieldContract(
                        f.getName(), locator, declared, combineOf(f)));
            }

            for (Method m : c.getDeclaredMethods()) {
                Condition[] declared = m.getAnnotationsByType(Condition.class);
                if (declared.length == 0) continue;
                assertLocatorUse(declared, LocatorUse.FORBIDDEN,
                        c.getName() + "#" + m.getName());
                if (!methods.containsKey(m.getName())) {
                    methods.put(m.getName(), new PageContract.ScopeContract(
                            m.getName(), declared, combineOf(m)));
                }
            }
        }
        VALIDATED.put(type, Boolean.TRUE);
        return new PageContract(load, fields, methods);
    }

    private static PageContract.ScopeContract scopeOf(Class<?> type, String name, LocatorUse rule) {
        Condition[] declared = type.getAnnotationsByType(Condition.class);
        if (declared.length == 0) return null;
        assertLocatorUse(declared, rule, type.getName());
        return new PageContract.ScopeContract(name, declared, combineOf(type));
    }

    private static Combine combineOf(AnnotatedElement element) {
        Conditions container = element.getAnnotation(Conditions.class);
        return container == null ? Combine.AND : container.combine();
    }

    private static void assertLocatorUse(Condition[] declared, LocatorUse expected, String where) {
        for (Condition c : declared) {
            LocatorUse actual = c.value().locatorUse();
            if (actual == LocatorUse.OPTIONAL) continue;
            if (actual != expected) {
                throw new IllegalStateException(
                        where + ": " + c.value() + " has LocatorUse." + actual
                                + " and cannot be declared here (expected " + expected + ")");
            }
        }
    }
}
