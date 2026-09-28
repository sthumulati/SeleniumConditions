package io.seleniumstudio.lab.tests;

import io.seleniumstudio.conditions.Attributes;
import io.seleniumstudio.conditions.Combine;
import io.seleniumstudio.conditions.Condition;
import io.seleniumstudio.conditions.Conditions;
import io.seleniumstudio.conditions.ExpectedConditionType;
import io.seleniumstudio.conditions.runtime.AnnotatedPage;
import io.seleniumstudio.lab.pages.HomePage;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Composition, negation, refresh and the failure path.
 *
 * <p>{@code and} / {@code or} / {@code not} / {@code refreshed} are modifiers rather than catalogue
 * entries, expressed by {@code Conditions.combine()}, {@code Condition.negate()} and
 * {@code Condition.refreshed()}. Depth-1 composition is the ceiling: JLS 9.6.1 forbids an
 * annotation type from containing itself, so arbitrary boolean trees are inexpressible and are
 * routed to {@code CUSTOM}.</p>
 */
public class CompositionTest extends BaseTest {

    static final class CompositionPage extends AnnotatedPage {

        @Conditions(combine = Combine.OR, value = {
                @Condition(value = ExpectedConditionType.TEXT_TO_BE_PRESENT_IN_ELEMENT,
                           with = @Attributes(text = "NEVER-APPEARS"), timeoutMillis = 4_000),
                @Condition(value = ExpectedConditionType.TEXT_TO_BE_PRESENT_IN_ELEMENT,
                           with = @Attributes(text = "SETTLED"), timeoutMillis = 4_000)
        })
        static final By STATUS_EITHER = By.id("status");

        @Condition(value = ExpectedConditionType.VISIBILITY_OF_ELEMENT_LOCATED,
                   refreshed = true, timeoutMillis = 5_000)
        static final By STALE_REFRESHED = By.id("stale");

        @Condition(value = ExpectedConditionType.TEXT_TO_BE_PRESENT_IN_ELEMENT,
                   with = @Attributes(text = "WILL-NEVER-APPEAR"),
                   timeoutMillis = 1_200,
                   pollingMillis = 100,
                   message = "status never reached the impossible value")
        static final By NEVER = By.id("status");

        CompositionPage(WebDriver driver) { super(driver); }
    }

    @Test
    public void conjunctionAcrossRepeatedDeclarations() {
        assertTrue((Boolean) home.await(HomePage.SUBMIT));
    }

    @Test
    public void disjunctionShortCircuitsToTheSatisfiableBranch() {
        CompositionPage page = new CompositionPage(driver);
        assertTrue(page.awaitAll(CompositionPage.STATUS_EITHER));
    }

    @Test
    public void refreshedSurvivesDomReplacement() {
        CompositionPage page = new CompositionPage(driver);
        assertTrue(page.<org.openqa.selenium.WebElement>await(
                CompositionPage.STALE_REFRESHED).isDisplayed());
    }

    @Test(expectedExceptions = TimeoutException.class)
    public void unsatisfiableConditionTimesOutWithinItsOwnEnvelope() {
        long started = System.currentTimeMillis();
        try {
            new CompositionPage(driver).await(CompositionPage.NEVER);
        } finally {
            long elapsed = System.currentTimeMillis() - started;
            // Proves the per-declaration timeout overrode the 10 s framework default.
            assertTrue(elapsed < 5_000, "per-declaration timeout was not honoured: " + elapsed + " ms");
        }
    }

    @Test
    public void declarationsAreIntrospectable() {
        Condition[] declared = home.declaredFor(HomePage.SUBMIT);
        assertEquals(declared.length, 3);
        assertEquals(declared[2].value(), ExpectedConditionType.ELEMENT_TO_BE_CLICKABLE);
        assertEquals(declared[2].timeoutMillis(), 5_000L);
    }
}
