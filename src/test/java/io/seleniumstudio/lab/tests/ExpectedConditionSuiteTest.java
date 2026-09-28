package io.seleniumstudio.lab.tests;

import io.seleniumstudio.conditions.Attributes;
import io.seleniumstudio.conditions.Condition;
import io.seleniumstudio.conditions.ExpectedConditionType;
import io.seleniumstudio.conditions.Tri;
import io.seleniumstudio.conditions.Using;
import io.seleniumstudio.conditions.runtime.AnnotatedPage;
import io.seleniumstudio.conditions.spi.ConditionFactory;
import io.seleniumstudio.lab.fixture.Site;
import io.seleniumstudio.lab.fixture.TestSite;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * One TestNG test per {@link ExpectedConditionType} constant, in a single class.
 *
 * <h2>How pass and fail are decided</h2>
 * <p>Every test drives the solution's own machinery and nothing else. The declared
 * {@code @Condition} is resolved by {@code ConditionResolver} and polled by {@code WaitExecutor}
 * through {@code AnnotatedPage.await(By)} (locator-scoped) or {@code awaitAfter(String)}
 * (document-scoped). The verdict comes from that call:</p>
 * <ul>
 *   <li><strong>Fail</strong> — the wait envelope expires and {@code FluentWait} throws
 *       {@code TimeoutException}, carrying the declared {@code message()}. No assertion is needed
 *       for the condition itself to fail a test.</li>
 *   <li><strong>Pass</strong> — the await returns, and the returned value is asserted to confirm
 *       the condition produced what its {@code Yields} metadata promises: a {@code WebElement},
 *       a {@code List<WebElement>}, {@code Boolean} or {@code Object}.</li>
 * </ul>
 *
 * <p>The {@code @Covers} declarations are read reflectively by {@link #everyConditionHasATest()},
 * which fails the build if any catalogue constant is untested. That makes "one test per condition"
 * an assertion the suite enforces rather than a claim in a comment.</p>
 *
 * <p>The nested {@link Lab} page object exists because {@code PageScanner} scans the annotated
 * type, and the annotated {@code By} fields must therefore live on an {@code AnnotatedPage}. It is
 * a nested class of this file, so the suite remains a single compilation unit.</p>
 */
public class ExpectedConditionSuiteTest {

    // =================================================================================
    // Coverage marker, nested so the suite stays one file
    // =================================================================================

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Covers {
        ExpectedConditionType[] value();
    }

    // =================================================================================
    // Custom condition factory: the open extension point
    // =================================================================================

    /** Must be public with a public no-argument constructor; nested is fine. */
    public static final class SettledCountAtLeast implements ConditionFactory {

        private static final Pattern DIGITS = Pattern.compile("(\\d+)");

        @Override
        public ExpectedCondition<Boolean> create(WebDriver driver, By locator, Attributes a) {
            final int floor = a.number();
            return new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver d) {
                    try {
                        Matcher m = DIGITS.matcher(d.findElement(locator).getText());
                        return m.find() && Integer.parseInt(m.group(1)) >= floor;
                    } catch (NoSuchElementException | StaleElementReferenceException e) {
                        return false;
                    }
                }
                @Override
                public String toString() { return "settled count >= " + floor + " in " + locator; }
            };
        }
    }

    // =================================================================================
    // The page object: every declaration under test
    // =================================================================================

    /** Class-level declarations are the load contract and must be document-scoped. */
    @Condition(value = ExpectedConditionType.TITLE_IS,
               with = @Attributes(title = "Condition Lab - Home"))
    public static final class Lab extends AnnotatedPage {

        // ---- presence and visibility -------------------------------------------------
        @Condition(ExpectedConditionType.PRESENCE_OF_ELEMENT_LOCATED)
        static final By HEADING = By.id("heading");

        @Condition(ExpectedConditionType.PRESENCE_OF_ALL_ELEMENTS)
        static final By ROWS_PRESENT = By.cssSelector("#rows .row");

        @Condition(ExpectedConditionType.VISIBILITY_OF_ELEMENT_LOCATED)
        static final By USERNAME = By.id("username");

        @Condition(ExpectedConditionType.VISIBILITY_OF_ALL_ELEMENTS)
        static final By ROWS_VISIBLE = By.cssSelector("#rows .row");

        @Condition(value = ExpectedConditionType.INVISIBILITY_OF_ELEMENT_LOCATED,
                   timeoutMillis = 5_000)
        static final By SPINNER = By.id("spinner");

        @Condition(value = ExpectedConditionType.INVISIBILITY_OF_ELEMENT_WITH_TEXT,
                   with = @Attributes(text = "Loading, please wait"),
                   timeoutMillis = 5_000)
        static final By SPINNER_WITH_TEXT = By.id("spinner");

        // ---- interaction and selection state ------------------------------------------
        @Condition(value = ExpectedConditionType.ELEMENT_TO_BE_CLICKABLE, timeoutMillis = 5_000)
        static final By SUBMIT = By.id("submit");

        @Condition(value = ExpectedConditionType.ELEMENT_TO_BE_SELECTED, timeoutMillis = 5_000)
        static final By TOS = By.id("tos");

        /** Tri.FALSE is the case a plain boolean member could not express. */
        @Condition(value = ExpectedConditionType.ELEMENT_SELECTION_STATE_TO_BE,
                   with = @Attributes(selected = Tri.FALSE))
        static final By MARKETING = By.id("marketing");

        // ---- text -----------------------------------------------------------------------
        @Condition(value = ExpectedConditionType.TEXT_TO_BE_PRESENT_IN_ELEMENT,
                   with = @Attributes(text = "SETTLED"), timeoutMillis = 5_000)
        static final By STATUS = By.id("status");

        @Condition(value = ExpectedConditionType.TEXT_TO_BE_PRESENT_IN_VALUE,
                   with = @Attributes(text = "qa@example.com"), timeoutMillis = 5_000)
        static final By EMAIL = By.id("email");

        /** Pattern carried as source text: Pattern is not a legal member type (JLS 9.6.1). */
        @Condition(value = ExpectedConditionType.TEXT_MATCHES,
                   with = @Attributes(regex = "^Build v\\d+\\.\\d+\\.\\d+ \\(rc\\d\\)$"))
        static final By VERSION = By.id("version");

        // ---- attributes and DOM properties -------------------------------------------------
        @Condition(value = ExpectedConditionType.ATTRIBUTE_TO_BE,
                   with = @Attributes(attribute = "aria-disabled", expectedValue = "false"),
                   timeoutMillis = 5_000)
        static final By SUBMIT_ARIA = By.id("submit");

        @Condition(value = ExpectedConditionType.ATTRIBUTE_CONTAINS,
                   with = @Attributes(attribute = "data-state", expectedValue = "ready"),
                   timeoutMillis = 5_000)
        static final By SUBMIT_STATE = By.id("submit");

        @Condition(value = ExpectedConditionType.ATTRIBUTE_TO_BE_NOT_EMPTY,
                   with = @Attributes(attribute = "data-state"))
        static final By SUBMIT_STATE_PRESENT = By.id("submit");

        @Condition(value = ExpectedConditionType.DOM_ATTRIBUTE_TO_BE,
                   with = @Attributes(attribute = "name", expectedValue = "username"))
        static final By USERNAME_DOM_ATTR = By.id("username");

        @Condition(value = ExpectedConditionType.DOM_PROPERTY_TO_BE,
                   with = @Attributes(property = "value", expectedValue = "qa@example.com"),
                   timeoutMillis = 5_000)
        static final By EMAIL_DOM_PROPERTY = By.id("email");

        // ---- cardinality ----------------------------------------------------------------------
        @Condition(value = ExpectedConditionType.NUMBER_OF_ELEMENTS_TO_BE,
                   with = @Attributes(number = 3), timeoutMillis = 5_000)
        static final By ROWS_EXACTLY_THREE = By.cssSelector("#rows .row");

        @Condition(value = ExpectedConditionType.NUMBER_OF_ELEMENTS_MORE_THAN,
                   with = @Attributes(number = 2), timeoutMillis = 5_000)
        static final By ROWS_MORE_THAN_TWO = By.cssSelector("#rows .row");

        @Condition(value = ExpectedConditionType.NUMBER_OF_ELEMENTS_LESS_THAN,
                   with = @Attributes(number = 4), timeoutMillis = 5_000)
        static final By ROWS_FEWER_THAN_FOUR = By.cssSelector("#rows .row");

        // ---- nested elements ---------------------------------------------------------------------
        @Condition(value = ExpectedConditionType.PRESENCE_OF_NESTED_ELEMENT,
                   with = @Attributes(childUsing = Using.CSS, childValue = ".child"),
                   timeoutMillis = 5_000)
        static final By PANEL_NESTED_ONE = By.id("panel");

        @Condition(value = ExpectedConditionType.PRESENCE_OF_NESTED_ELEMENTS,
                   with = @Attributes(childUsing = Using.CSS, childValue = ".child"),
                   timeoutMillis = 5_000)
        static final By PANEL_NESTED_ALL = By.id("panel");

        @Condition(value = ExpectedConditionType.VISIBILITY_OF_NESTED_ELEMENTS,
                   with = @Attributes(childUsing = Using.CLASS_NAME, childValue = "child"),
                   timeoutMillis = 5_000)
        static final By PANEL_NESTED_VISIBLE = By.id("panel");

        // ---- lifecycle -----------------------------------------------------------------------------
        @Condition(value = ExpectedConditionType.STALENESS_OF, timeoutMillis = 5_000)
        static final By STALE = By.id("stale");

        // ---- frame by locator -------------------------------------------------------------------------
        @Condition(value = ExpectedConditionType.FRAME_AVAILABLE_BY_LOCATOR, timeoutMillis = 5_000)
        static final By CONTENT_FRAME = By.id("contentFrame");

        // ---- open extension point ----------------------------------------------------------------------
        @Condition(value = ExpectedConditionType.CUSTOM,
                   with = @Attributes(number = 40),
                   factory = SettledCountAtLeast.class,
                   timeoutMillis = 5_000,
                   message = "settled count never reached the declared floor")
        static final By SETTLED_COUNT = By.id("status");

        // ---- plain locators, no contract ------------------------------------------------------------------
        static final By ALERT_BUTTON      = By.id("alertBtn");
        static final By NEW_WINDOW_BUTTON = By.id("newWinBtn");
        static final By GO_SECOND         = By.id("goSecond");

        Lab(WebDriver driver) { super(driver); }

        // ---- document-scoped declarations live on methods ---------------------------------------------------

        @Condition(value = ExpectedConditionType.TITLE_CONTAINS,
                   with = @Attributes(title = "Condition Lab"))
        void titleContains() { awaitAfter("titleContains"); }

        @Condition(value = ExpectedConditionType.URL_CONTAINS,
                   with = @Attributes(fraction = "index.html"))
        void urlContains() { awaitAfter("urlContains"); }

        @Condition(value = ExpectedConditionType.URL_MATCHES,
                   with = @Attributes(regex = "^https?://127\\.0\\.0\\.1:\\d+/index\\.html$"))
        void urlMatches() { awaitAfter("urlMatches"); }

        /** Legal only because {@link Site#SECOND} is a compile-time constant expression. */
        @Condition(value = ExpectedConditionType.URL_TO_BE,
                   with = @Attributes(url = Site.SECOND), timeoutMillis = 5_000)
        void navigateToSecond() {
            driver.findElement(GO_SECOND).click();
            awaitAfter("navigateToSecond");
        }

        @Condition(ExpectedConditionType.ALERT_IS_PRESENT)
        void raiseAlert() {
            driver.findElement(ALERT_BUTTON).click();
            awaitAfter("raiseAlert");
        }

        @Condition(value = ExpectedConditionType.NUMBER_OF_WINDOWS_TO_BE,
                   with = @Attributes(number = 2), timeoutMillis = 5_000)
        void openSecondWindow() {
            driver.findElement(NEW_WINDOW_BUTTON).click();
            awaitAfter("openSecondWindow");
        }

        @Condition(value = ExpectedConditionType.JAVASCRIPT_THROWS_NO_EXCEPTIONS,
                   with = @Attributes(javaScript = "document.getElementById('heading').textContent;"))
        void scriptIsClean() { awaitAfter("scriptIsClean"); }

        @Condition(value = ExpectedConditionType.JS_RETURNS_VALUE,
                   with = @Attributes(javaScript = "return window.appReady;"),
                   timeoutMillis = 5_000)
        void scriptReturnsValue() { awaitAfter("scriptReturnsValue"); }

        @Condition(value = ExpectedConditionType.FRAME_AVAILABLE_BY_NAME_OR_ID,
                   with = @Attributes(frameNameOrId = "content"), timeoutMillis = 5_000)
        void switchFrameByNameOrId() { awaitAfter("switchFrameByNameOrId"); }

        @Condition(value = ExpectedConditionType.FRAME_AVAILABLE_BY_INDEX,
                   with = @Attributes(frameIndex = 0), timeoutMillis = 5_000)
        void switchFrameByIndex() { awaitAfter("switchFrameByIndex"); }
    }

    // =================================================================================
    // Lifecycle
    // =================================================================================

    private TestSite site;
    private WebDriver driver;
    private Lab lab;

    @BeforeClass(alwaysRun = true)
    public void startSite() throws IOException { site = TestSite.start(); }

    @AfterClass(alwaysRun = true)
    public void stopSite() { if (site != null) site.stop(); }

    @BeforeMethod(alwaysRun = true)
    public void openBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--window-size=1280,900",
                "--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
        driver = new ChromeDriver(options);
        driver.get(Site.INDEX);
        lab = new Lab(driver);
    }

    @AfterMethod(alwaysRun = true)
    public void closeBrowser() {
        if (driver == null) return;
        try {
            driver.switchTo().alert().dismiss();      // never leave a modal open before quit
        } catch (RuntimeException ignored) {
            // no alert pending
        }
        driver.quit();
        driver = null;
    }

    // =================================================================================
    // Document scope (9)
    // =================================================================================

    @Test
    @Covers(ExpectedConditionType.TITLE_IS)
    public void titleIs() {
        lab.awaitLoaded();                            // class-level load contract
        assertEquals(driver.getTitle(), "Condition Lab - Home");
    }

    @Test
    @Covers(ExpectedConditionType.TITLE_CONTAINS)
    public void titleContains() {
        lab.titleContains();
        assertTrue(driver.getTitle().contains("Condition Lab"));
    }

    @Test
    @Covers(ExpectedConditionType.URL_TO_BE)
    public void urlToBe() {
        lab.navigateToSecond();
        assertEquals(driver.getCurrentUrl(), Site.SECOND);
    }

    @Test
    @Covers(ExpectedConditionType.URL_CONTAINS)
    public void urlContains() {
        lab.urlContains();
        assertTrue(driver.getCurrentUrl().contains("index.html"));
    }

    @Test
    @Covers(ExpectedConditionType.URL_MATCHES)
    public void urlMatches() {
        lab.urlMatches();
        assertTrue(driver.getCurrentUrl().endsWith("/index.html"));
    }

    @Test
    @Covers(ExpectedConditionType.ALERT_IS_PRESENT)
    public void alertIsPresent() {
        lab.raiseAlert();
        Alert alert = driver.switchTo().alert();
        assertEquals(alert.getText(), "confirm-settlement");
        alert.accept();
    }

    @Test
    @Covers(ExpectedConditionType.NUMBER_OF_WINDOWS_TO_BE)
    public void numberOfWindowsToBe() {
        lab.openSecondWindow();
        assertEquals(driver.getWindowHandles().size(), 2);
    }

    @Test
    @Covers(ExpectedConditionType.JAVASCRIPT_THROWS_NO_EXCEPTIONS)
    public void javaScriptThrowsNoExceptions() {
        lab.scriptIsClean();                          // timeout here is the only failure mode
    }

    @Test
    @Covers(ExpectedConditionType.JS_RETURNS_VALUE)
    public void jsReturnsValue() {
        lab.scriptReturnsValue();
    }

    // =================================================================================
    // Frame switching (3)
    // =================================================================================

    @Test
    @Covers(ExpectedConditionType.FRAME_AVAILABLE_BY_NAME_OR_ID)
    public void frameToBeAvailableByNameOrId() {
        lab.switchFrameByNameOrId();
        assertEquals(driver.findElement(By.id("frameHeading")).getText(), "Inside the frame");
        driver.switchTo().defaultContent();
    }

    @Test
    @Covers(ExpectedConditionType.FRAME_AVAILABLE_BY_INDEX)
    public void frameToBeAvailableByIndex() {
        lab.switchFrameByIndex();
        assertEquals(driver.findElement(By.id("frameHeading")).getText(), "Inside the frame");
        driver.switchTo().defaultContent();
    }

    @Test
    @Covers(ExpectedConditionType.FRAME_AVAILABLE_BY_LOCATOR)
    public void frameToBeAvailableByLocator() {
        WebDriver switched = lab.await(Lab.CONTENT_FRAME);   // yields DRIVER
        assertNotNull(switched);
        assertEquals(driver.findElement(By.id("frameStatus")).getText(), "frame-ready");
        driver.switchTo().defaultContent();
    }

    // =================================================================================
    // Presence and visibility (6)
    // =================================================================================

    @Test
    @Covers(ExpectedConditionType.PRESENCE_OF_ELEMENT_LOCATED)
    public void presenceOfElementLocated() {
        WebElement heading = lab.await(Lab.HEADING);
        assertEquals(heading.getText(), "Condition Lab");
    }

    @Test
    @Covers(ExpectedConditionType.PRESENCE_OF_ALL_ELEMENTS)
    public void presenceOfAllElementsLocatedBy() {
        List<WebElement> rows = lab.await(Lab.ROWS_PRESENT);
        assertFalse(rows.isEmpty());
    }

    @Test
    @Covers(ExpectedConditionType.VISIBILITY_OF_ELEMENT_LOCATED)
    public void visibilityOfElementLocated() {
        WebElement username = lab.await(Lab.USERNAME);
        assertTrue(username.isDisplayed());
    }

    @Test
    @Covers(ExpectedConditionType.VISIBILITY_OF_ALL_ELEMENTS)
    public void visibilityOfAllElementsLocatedBy() {
        List<WebElement> rows = lab.await(Lab.ROWS_VISIBLE);
        assertTrue(rows.get(0).isDisplayed());
    }

    @Test
    @Covers(ExpectedConditionType.INVISIBILITY_OF_ELEMENT_LOCATED)
    public void invisibilityOfElementLocated() {
        Boolean gone = lab.await(Lab.SPINNER);
        assertTrue(gone);
    }

    @Test
    @Covers(ExpectedConditionType.INVISIBILITY_OF_ELEMENT_WITH_TEXT)
    public void invisibilityOfElementWithText() {
        Boolean gone = lab.await(Lab.SPINNER_WITH_TEXT);
        assertTrue(gone);
    }

    // =================================================================================
    // Interaction and selection state (3)
    // =================================================================================

    @Test
    @Covers(ExpectedConditionType.ELEMENT_TO_BE_CLICKABLE)
    public void elementToBeClickable() {
        WebElement submit = lab.await(Lab.SUBMIT);
        assertTrue(submit.isEnabled());
    }

    @Test
    @Covers(ExpectedConditionType.ELEMENT_TO_BE_SELECTED)
    public void elementToBeSelected() {
        Boolean selected = lab.await(Lab.TOS);
        assertTrue(selected);
    }

    @Test
    @Covers(ExpectedConditionType.ELEMENT_SELECTION_STATE_TO_BE)
    public void elementSelectionStateToBe() {
        Boolean matched = lab.await(Lab.MARKETING);
        assertTrue(matched);
        assertFalse(driver.findElement(Lab.MARKETING).isSelected());
    }

    // =================================================================================
    // Text (3)
    // =================================================================================

    @Test
    @Covers(ExpectedConditionType.TEXT_TO_BE_PRESENT_IN_ELEMENT)
    public void textToBePresentInElementLocated() {
        Boolean present = lab.await(Lab.STATUS);
        assertTrue(present);
    }

    @Test
    @Covers(ExpectedConditionType.TEXT_TO_BE_PRESENT_IN_VALUE)
    public void textToBePresentInElementValue() {
        Boolean present = lab.await(Lab.EMAIL);
        assertTrue(present);
    }

    @Test
    @Covers(ExpectedConditionType.TEXT_MATCHES)
    public void textMatches() {
        Boolean matched = lab.await(Lab.VERSION);
        assertTrue(matched);
    }

    // =================================================================================
    // Attributes and DOM properties (5)
    // =================================================================================

    @Test
    @Covers(ExpectedConditionType.ATTRIBUTE_TO_BE)
    public void attributeToBe() {
        Boolean matched = lab.await(Lab.SUBMIT_ARIA);
        assertTrue(matched);
    }

    @Test
    @Covers(ExpectedConditionType.ATTRIBUTE_CONTAINS)
    public void attributeContains() {
        Boolean matched = lab.await(Lab.SUBMIT_STATE);
        assertTrue(matched);
    }

    @Test
    @Covers(ExpectedConditionType.ATTRIBUTE_TO_BE_NOT_EMPTY)
    public void attributeToBeNotEmpty() {
        Boolean matched = lab.await(Lab.SUBMIT_STATE_PRESENT);
        assertTrue(matched);
    }

    @Test
    @Covers(ExpectedConditionType.DOM_ATTRIBUTE_TO_BE)
    public void domAttributeToBe() {
        Boolean matched = lab.await(Lab.USERNAME_DOM_ATTR);
        assertTrue(matched);
    }

    @Test
    @Covers(ExpectedConditionType.DOM_PROPERTY_TO_BE)
    public void domPropertyToBe() {
        Boolean matched = lab.await(Lab.EMAIL_DOM_PROPERTY);
        assertTrue(matched);
    }

    // =================================================================================
    // Cardinality (3)
    // =================================================================================

    @Test
    @Covers(ExpectedConditionType.NUMBER_OF_ELEMENTS_TO_BE)
    public void numberOfElementsToBe() {
        List<WebElement> rows = lab.await(Lab.ROWS_EXACTLY_THREE);
        assertEquals(rows.size(), 3);
    }

    @Test
    @Covers(ExpectedConditionType.NUMBER_OF_ELEMENTS_MORE_THAN)
    public void numberOfElementsToBeMoreThan() {
        List<WebElement> rows = lab.await(Lab.ROWS_MORE_THAN_TWO);
        assertTrue(rows.size() > 2);
    }

    @Test
    @Covers(ExpectedConditionType.NUMBER_OF_ELEMENTS_LESS_THAN)
    public void numberOfElementsToBeLessThan() {
        List<WebElement> rows = lab.await(Lab.ROWS_FEWER_THAN_FOUR);
        assertTrue(rows.size() < 4);
    }

    // =================================================================================
    // Nested elements (3)
    // =================================================================================

    @Test
    @Covers(ExpectedConditionType.PRESENCE_OF_NESTED_ELEMENT)
    public void presenceOfNestedElementLocatedBy() {
        WebElement child = lab.await(Lab.PANEL_NESTED_ONE);
        assertEquals(child.getText(), "alpha");
    }

    @Test
    @Covers(ExpectedConditionType.PRESENCE_OF_NESTED_ELEMENTS)
    public void presenceOfNestedElementsLocatedBy() {
        List<WebElement> children = lab.await(Lab.PANEL_NESTED_ALL);
        assertEquals(children.size(), 2);
    }

    @Test
    @Covers(ExpectedConditionType.VISIBILITY_OF_NESTED_ELEMENTS)
    public void visibilityOfNestedElementsLocatedBy() {
        List<WebElement> children = lab.await(Lab.PANEL_NESTED_VISIBLE);
        assertTrue(children.get(0).isDisplayed());
    }

    // =================================================================================
    // Lifecycle (1)
    // =================================================================================

    @Test
    @Covers(ExpectedConditionType.STALENESS_OF)
    public void stalenessOf() {
        // The element is captured when the wait begins, then asserted stale after replacement.
        Boolean stale = lab.await(Lab.STALE);
        assertTrue(stale);
        assertEquals(driver.findElement(Lab.STALE).getText(), "replaced");
    }

    // =================================================================================
    // Open extension point (1)
    // =================================================================================

    @Test
    @Covers(ExpectedConditionType.CUSTOM)
    public void customConditionViaFactory() {
        Boolean met = lab.await(Lab.SETTLED_COUNT);
        assertTrue(met);
    }

    // =================================================================================
    // Coverage guard
    // =================================================================================

    /**
     * Fails if any catalogue constant has no {@code @Test}. Adding a constant to
     * {@code ExpectedConditionType} without adding a test here is therefore a red build.
     */
    @Test
    public void everyConditionHasATest() {
        Set<ExpectedConditionType> covered = EnumSet.noneOf(ExpectedConditionType.class);
        for (Method m : ExpectedConditionSuiteTest.class.getDeclaredMethods()) {
            Covers c = m.getAnnotation(Covers.class);
            if (c != null && m.isAnnotationPresent(Test.class)) {
                covered.addAll(java.util.Arrays.asList(c.value()));
            }
        }
        Set<ExpectedConditionType> missing = EnumSet.allOf(ExpectedConditionType.class);
        missing.removeAll(covered);
        assertTrue(missing.isEmpty(), "ExpectedConditionType constants with no @Test: " + missing);
        assertEquals(covered.size(), ExpectedConditionType.values().length);
    }
}
