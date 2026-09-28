package io.seleniumstudio.conditions.runtime;

import io.seleniumstudio.conditions.Condition;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.time.Duration;
import java.util.Map;

/**
 * Base for a Classic Page Object whose {@code By} fields carry their own wait contracts.
 *
 * <p>{@code await(By)} is type-safe rather than string-keyed: callers pass the locator constant
 * itself, and the contract is looked up by identity.</p>
 */
public abstract class AnnotatedPage {

    protected final WebDriver driver;
    protected final WaitExecutor waits;
    private final PageContract contract;

    protected AnnotatedPage(WebDriver driver) {
        this(driver, new WaitExecutor());
    }

    protected AnnotatedPage(WebDriver driver, WaitExecutor waits) {
        this.driver = driver;
        this.waits = waits;
        this.contract = PageScanner.scan(this);
    }

    /** Evaluates the class-level load contract. No-op when the page declares none. */
    public <P extends AnnotatedPage> P awaitLoaded() {
        PageContract.ScopeContract load = contract.loadContract();
        if (load != null) {
            waits.awaitAll(driver, null, load.conditions, load.combine);
        }
        @SuppressWarnings("unchecked") P self = (P) this;
        return self;
    }

    /** Evaluates the conditions declared on the field holding this locator. */
    public <T> T await(By locator) {
        PageContract.FieldContract fc = require(locator);
        if (fc.conditions.length == 1) {
            return waits.<T>await(driver, fc.locator, fc.conditions[0]);
        }
        @SuppressWarnings("unchecked")
        T result = (T) Boolean.valueOf(
                waits.awaitAll(driver, fc.locator, fc.conditions, fc.combine));
        return result;
    }

    /** Boolean form of {@link #await(By)} for fields carrying several combined declarations. */
    public boolean awaitAll(By locator) {
        PageContract.FieldContract fc = require(locator);
        return waits.awaitAll(driver, fc.locator, fc.conditions, fc.combine);
    }

    /** Convenience: await the field contract, then return the element. */
    public WebElement element(By locator) {
        await(locator);
        return driver.findElement(locator);
    }

    /** Evaluates every field contract on the page. Useful as a whole-page readiness gate. */
    public void awaitAllFields() {
        for (Map.Entry<By, PageContract.FieldContract> e : contract.allFields().entrySet()) {
            PageContract.FieldContract fc = e.getValue();
            waits.awaitAll(driver, fc.locator, fc.conditions, fc.combine);
        }
    }

    /** Evaluates the document-scoped post-conditions declared on the named method. */
    public void awaitAfter(String methodName) {
        PageContract.ScopeContract sc = contract.forMethod(methodName);
        if (sc == null) {
            throw new IllegalArgumentException(
                    "No @Condition declared on method " + methodName + " of " + getClass().getName());
        }
        waits.awaitAll(driver, null, sc.conditions, sc.combine);
    }

    /** Runtime-valued arguments bypass the annotation layer but share the same executor. */
    public <T> T awaitDynamic(ExpectedCondition<T> condition, Duration timeout) {
        return waits.awaitDynamic(driver, condition, timeout, null);
    }

    public Condition[] declaredFor(By locator) { return require(locator).conditions; }

    protected PageContract contract() { return contract; }

    private PageContract.FieldContract require(By locator) {
        PageContract.FieldContract fc = contract.forLocator(locator);
        if (fc == null) {
            throw new IllegalArgumentException(
                    "No @Condition declared for this locator on " + getClass().getName()
                            + ". Pass the annotated field constant itself, not an equal By.");
        }
        return fc;
    }
}
