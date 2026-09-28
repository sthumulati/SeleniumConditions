package io.seleniumstudio.conditions.runtime;

import io.seleniumstudio.conditions.Combine;
import io.seleniumstudio.conditions.Condition;
import io.seleniumstudio.conditions.Sentinels;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes declared conditions against a {@code FluentWait}.
 *
 * <p>Both the declarative path ({@code AnnotatedPage.await}) and the imperative path
 * ({@code awaitDynamic}) run through this class, so the two cannot drift.</p>
 */
public final class WaitExecutor {

    private final ConditionResolver resolver = new ConditionResolver();
    private final Duration defaultTimeout;
    private final Duration defaultPolling;

    public WaitExecutor(Duration defaultTimeout, Duration defaultPolling) {
        this.defaultTimeout = defaultTimeout;
        this.defaultPolling = defaultPolling;
    }

    public WaitExecutor() {
        this(Duration.ofSeconds(10), Duration.ofMillis(200));
    }

    /** Evaluates a single declaration and returns its result. */
    @SuppressWarnings("unchecked")
    public <T> T await(WebDriver driver, By locator, Condition c) {
        ExpectedCondition<?> condition = resolver.resolve(driver, locator, c);
        return (T) wait(driver, c).until(condition);
    }

    /** Evaluates several declarations under one combinator and one envelope. */
    public boolean awaitAll(WebDriver driver, By locator, Condition[] declared, Combine combine) {
        if (declared == null || declared.length == 0) return true;
        if (declared.length == 1) {
            Object r = await(driver, locator, declared[0]);
            return !(r instanceof Boolean) || (Boolean) r;
        }
        List<ExpectedCondition<?>> parts = new ArrayList<ExpectedCondition<?>>(declared.length);
        for (Condition c : declared) {
            parts.add(resolver.resolve(driver, locator, c));
        }
        ExpectedCondition<?>[] arr = parts.toArray(new ExpectedCondition<?>[0]);
        ExpectedCondition<Boolean> composite = (combine == Combine.OR)
                ? ExpectedConditions.or(arr)
                : ExpectedConditions.and(arr);

        // The widest envelope across the group governs the composite.
        return wait(driver, widest(declared)).until(composite);
    }

    /**
     * Imperative escape hatch for runtime-valued arguments, which annotations cannot carry
     * because their members must be compile-time constants.
     */
    public <T> T awaitDynamic(WebDriver driver, ExpectedCondition<T> condition,
                              Duration timeout, Duration polling) {
        FluentWait<WebDriver> fw = new FluentWait<WebDriver>(driver)
                .withTimeout(timeout == null ? defaultTimeout : timeout)
                .pollingEvery(polling == null ? defaultPolling : polling);
        return fw.until(condition);
    }

    private FluentWait<WebDriver> wait(WebDriver driver, Condition c) {
        Duration timeout = c.timeoutMillis() == Sentinels.INHERIT_MILLIS
                ? defaultTimeout : Duration.ofMillis(c.timeoutMillis());
        Duration polling = c.pollingMillis() == Sentinels.INHERIT_MILLIS
                ? defaultPolling : Duration.ofMillis(c.pollingMillis());

        FluentWait<WebDriver> fw = new FluentWait<WebDriver>(driver)
                .withTimeout(timeout)
                .pollingEvery(polling);

        for (Class<? extends Throwable> ex : c.ignoring()) {
            fw = fw.ignoring(ex);
        }
        String msg = c.message();
        if (msg != null && !msg.isEmpty()) {
            fw = fw.withMessage(msg);
        } else {
            fw = fw.withMessage("declared condition " + c.value() + " was not met");
        }
        return fw;
    }

    private Condition widest(Condition[] declared) {
        Condition best = declared[0];
        long bestMs = best.timeoutMillis() == Sentinels.INHERIT_MILLIS
                ? defaultTimeout.toMillis() : best.timeoutMillis();
        for (Condition c : declared) {
            long ms = c.timeoutMillis() == Sentinels.INHERIT_MILLIS
                    ? defaultTimeout.toMillis() : c.timeoutMillis();
            if (ms > bestMs) { bestMs = ms; best = c; }
        }
        return best;
    }
}
