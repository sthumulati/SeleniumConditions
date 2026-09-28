package io.seleniumstudio.lab.tests;

import io.seleniumstudio.lab.pages.HomePage;

import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * One test per locator-scoped condition. Each asserts the declared contract and, where the
 * condition yields a value, the shape of that value.
 */
public class ElementConditionTest extends BaseTest {

    // ---- presence and visibility ----------------------------------------------------

    @Test
    public void presenceOfElementLocated() {
        WebElement heading = home.await(HomePage.HEADING);
        assertEquals(heading.getText(), "Condition Lab");
    }

    @Test
    public void visibilityOfElementLocated() {
        WebElement username = home.await(HomePage.USERNAME);
        assertTrue(username.isDisplayed());
    }

    @Test
    public void presenceOfAllElementsLocatedBy() {
        List<WebElement> rows = home.await(HomePage.ALL_ROWS_PRESENT);
        assertFalse(rows.isEmpty());
    }

    @Test
    public void visibilityOfAllElementsLocatedBy() {
        List<WebElement> rows = home.await(HomePage.ALL_ROWS_VISIBLE);
        assertTrue(rows.get(0).isDisplayed());
    }

    @Test
    public void invisibilityOfElementLocated() {
        Boolean gone = home.await(HomePage.SPINNER);
        assertTrue(gone);
    }

    @Test
    public void invisibilityOfElementWithText() {
        Boolean gone = home.await(HomePage.SPINNER_TEXT);
        assertTrue(gone);
    }

    // ---- interaction and selection state --------------------------------------------

    @Test
    public void elementToBeClickableUnderConjunction() {
        // Three repeated declarations combined with AND; the composite yields Boolean.
        Boolean ready = home.await(HomePage.SUBMIT);
        assertTrue(ready);
        assertTrue(home.element(HomePage.SUBMIT).isEnabled());
    }

    @Test
    public void elementToBeSelected() {
        Boolean selected = home.await(HomePage.TOS);
        assertTrue(selected);
    }

    @Test
    public void elementSelectionStateToBe() {
        Boolean matches = home.await(HomePage.MARKETING);
        assertTrue(matches);
    }

    @Test
    public void notElementToBeSelected() {
        Boolean negated = home.await(HomePage.MARKETING_NOT_SELECTED);
        assertTrue(negated);
    }

    // ---- text -------------------------------------------------------------------------

    @Test
    public void textToBePresentInElementLocated() {
        assertTrue((Boolean) home.await(HomePage.STATUS));
    }

    @Test
    public void textToBePresentInElementValue() {
        assertTrue((Boolean) home.await(HomePage.EMAIL));
    }

    @Test
    public void textMatches() {
        assertTrue((Boolean) home.await(HomePage.VERSION));
    }

    // ---- attributes and DOM properties -------------------------------------------------

    @Test
    public void attributeToBe() {
        assertTrue((Boolean) home.await(HomePage.SUBMIT_ARIA));
    }

    @Test
    public void attributeContains() {
        assertTrue((Boolean) home.await(HomePage.SUBMIT_STATE));
    }

    @Test
    public void attributeToBeNotEmpty() {
        assertTrue((Boolean) home.await(HomePage.SUBMIT_STATE_PRESENT));
    }

    @Test
    public void domAttributeToBe() {
        assertTrue((Boolean) home.await(HomePage.USERNAME_DOM_ATTR));
    }

    @Test
    public void domPropertyToBe() {
        assertTrue((Boolean) home.await(HomePage.EMAIL_DOM_PROPERTY));
    }

    // ---- cardinality ----------------------------------------------------------------------

    @Test
    public void numberOfElementsToBe() {
        List<WebElement> rows = home.await(HomePage.ROWS_EXACTLY_THREE);
        assertEquals(rows.size(), 3);
    }

    @Test
    public void numberOfElementsToBeMoreThan() {
        List<WebElement> rows = home.await(HomePage.ROWS_MORE_THAN_TWO);
        assertTrue(rows.size() > 2);
    }

    @Test
    public void numberOfElementsToBeLessThan() {
        List<WebElement> rows = home.await(HomePage.ROWS_FEWER_THAN_FOUR);
        assertTrue(rows.size() < 4);
    }

    // ---- nested elements --------------------------------------------------------------------

    @Test
    public void presenceOfNestedElementLocatedBy() {
        WebElement child = home.await(HomePage.PANEL_NESTED_ONE);
        assertEquals(child.getText(), "alpha");
    }

    @Test
    public void presenceOfNestedElementsLocatedBy() {
        List<WebElement> children = home.await(HomePage.PANEL_NESTED_ALL);
        assertEquals(children.size(), 2);
    }

    @Test
    public void visibilityOfNestedElementsLocatedBy() {
        List<WebElement> children = home.await(HomePage.PANEL_NESTED_VISIBLE);
        assertTrue(children.get(0).isDisplayed());
    }

    // ---- lifecycle ------------------------------------------------------------------------------

    @Test
    public void stalenessOf() {
        // The element is captured when the wait begins and asserted stale after replacement.
        Boolean stale = home.await(HomePage.STALE);
        assertTrue(stale);
        assertEquals(driver.findElement(HomePage.STALE).getText(), "replaced");
    }

    // ---- open extension point --------------------------------------------------------------------

    @Test
    public void customFactoryCondition() {
        // CUSTOM: a predicate with no ExpectedConditions counterpart, still reading @Attributes.
        Boolean met = home.await(HomePage.SETTLED_COUNT);
        assertNotNull(met);
        assertTrue(met);
    }
}
