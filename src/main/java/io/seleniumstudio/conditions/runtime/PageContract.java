package io.seleniumstudio.conditions.runtime;

import io.seleniumstudio.conditions.Combine;
import io.seleniumstudio.conditions.Condition;

import org.openqa.selenium.By;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** The reflective scan of one page class, cached per class. */
public final class PageContract {

    /** A field-level declaration: the field's By, its conditions and their combinator. */
    public static final class FieldContract {
        public final String fieldName;
        public final By locator;
        public final Condition[] conditions;
        public final Combine combine;

        FieldContract(String fieldName, By locator, Condition[] conditions, Combine combine) {
            this.fieldName = fieldName;
            this.locator = locator;
            this.conditions = conditions;
            this.combine = combine;
        }
    }

    /** A type- or method-level declaration; document-scoped, so no locator. */
    public static final class ScopeContract {
        public final String name;
        public final Condition[] conditions;
        public final Combine combine;

        ScopeContract(String name, Condition[] conditions, Combine combine) {
            this.name = name;
            this.conditions = conditions;
            this.combine = combine;
        }
    }

    private final ScopeContract loadContract;
    private final Map<By, FieldContract> byLocator;
    private final Map<String, ScopeContract> byMethod;

    PageContract(ScopeContract loadContract,
                 IdentityHashMap<By, FieldContract> byLocator,
                 LinkedHashMap<String, ScopeContract> byMethod) {
        this.loadContract = loadContract;
        this.byLocator = Collections.unmodifiableMap(byLocator);
        this.byMethod = Collections.unmodifiableMap(byMethod);
    }

    public ScopeContract loadContract() { return loadContract; }

    /**
     * Keyed by identity, not equality: two distinct fields may hold equal {@code By} values with
     * different declared conditions, and identity keying keeps them apart.
     */
    public FieldContract forLocator(By locator) { return byLocator.get(locator); }

    public ScopeContract forMethod(String methodName) { return byMethod.get(methodName); }

    public Map<By, FieldContract> allFields() { return byLocator; }
}
