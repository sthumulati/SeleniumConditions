package io.seleniumstudio.lab.pages;

import io.seleniumstudio.conditions.Attributes;
import io.seleniumstudio.conditions.Combine;
import io.seleniumstudio.conditions.Condition;
import io.seleniumstudio.conditions.Conditions;
import io.seleniumstudio.conditions.ExpectedConditionType;
import io.seleniumstudio.conditions.Tri;
import io.seleniumstudio.conditions.Using;
import io.seleniumstudio.conditions.runtime.AnnotatedPage;
import io.seleniumstudio.lab.custom.SettledCountAtLeastFactory;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Classic Page Object: locators are {@code By} fields, and each field carries its own wait
 * contract as field-level annotations. Nothing here is a PageFactory proxy.
 *
 * <p>The class-level declaration is the page's load contract and is restricted by the processor to
 * document-scoped conditions.</p>
 */
@Condition(value = ExpectedConditionType.TITLE_IS,
           with = @Attributes(title = "Condition Lab - Home"))
@Condition(value = ExpectedConditionType.URL_CONTAINS,
           with = @Attributes(fraction = "index.html"))
public final class HomePage extends AnnotatedPage {

    // ---- presence / visibility -------------------------------------------------------
    @Condition(ExpectedConditionType.PRESENCE_OF_ELEMENT_LOCATED)
    public static final By HEADING = By.id("heading");

    @Condition(ExpectedConditionType.VISIBILITY_OF_ELEMENT_LOCATED)
    public static final By USERNAME = By.id("username");

    @Condition(ExpectedConditionType.PRESENCE_OF_ALL_ELEMENTS)
    public static final By ALL_ROWS_PRESENT = By.cssSelector("#rows .row");

    @Condition(ExpectedConditionType.VISIBILITY_OF_ALL_ELEMENTS)
    public static final By ALL_ROWS_VISIBLE = By.cssSelector("#rows .row");

    @Condition(value = ExpectedConditionType.INVISIBILITY_OF_ELEMENT_LOCATED,
               timeoutMillis = 5_000)
    public static final By SPINNER = By.id("spinner");

    @Condition(value = ExpectedConditionType.INVISIBILITY_OF_ELEMENT_WITH_TEXT,
               with = @Attributes(text = "Loading, please wait"),
               timeoutMillis = 5_000)
    public static final By SPINNER_TEXT = By.id("spinner");

    // ---- interaction and selection state ---------------------------------------------
    /**
     * Repeated declarations compose via {@code ExpectedConditions.and(...)}. This is the depth-1
     * composition the design supports without violating the JLS 9.6.1 cyclic-member prohibition.
     */
    @Conditions(combine = Combine.AND, value = {
            @Condition(ExpectedConditionType.PRESENCE_OF_ELEMENT_LOCATED),
            @Condition(ExpectedConditionType.VISIBILITY_OF_ELEMENT_LOCATED),
            @Condition(value = ExpectedConditionType.ELEMENT_TO_BE_CLICKABLE, timeoutMillis = 5_000)
    })
    public static final By SUBMIT = By.id("submit");

    @Condition(value = ExpectedConditionType.ELEMENT_TO_BE_SELECTED, timeoutMillis = 5_000)
    public static final By TOS = By.id("tos");

    @Condition(value = ExpectedConditionType.ELEMENT_SELECTION_STATE_TO_BE,
               with = @Attributes(selected = Tri.FALSE))
    public static final By MARKETING = By.id("marketing");

    // ---- text -------------------------------------------------------------------------
    @Condition(value = ExpectedConditionType.TEXT_TO_BE_PRESENT_IN_ELEMENT,
               with = @Attributes(text = "SETTLED"),
               timeoutMillis = 5_000)
    public static final By STATUS = By.id("status");

    @Condition(value = ExpectedConditionType.TEXT_TO_BE_PRESENT_IN_VALUE,
               with = @Attributes(text = "qa@example.com"),
               timeoutMillis = 5_000)
    public static final By EMAIL = By.id("email");

    @Condition(value = ExpectedConditionType.TEXT_MATCHES,
               with = @Attributes(regex = "^Build v\\d+\\.\\d+\\.\\d+ \\(rc\\d\\)$"))
    public static final By VERSION = By.id("version");

    // ---- attributes and DOM properties -------------------------------------------------
    @Condition(value = ExpectedConditionType.ATTRIBUTE_TO_BE,
               with = @Attributes(attribute = "aria-disabled", expectedValue = "false"),
               timeoutMillis = 5_000)
    public static final By SUBMIT_ARIA = By.id("submit");

    @Condition(value = ExpectedConditionType.ATTRIBUTE_CONTAINS,
               with = @Attributes(attribute = "data-state", expectedValue = "ready"),
               timeoutMillis = 5_000)
    public static final By SUBMIT_STATE = By.id("submit");

    @Condition(value = ExpectedConditionType.ATTRIBUTE_TO_BE_NOT_EMPTY,
               with = @Attributes(attribute = "data-state"))
    public static final By SUBMIT_STATE_PRESENT = By.id("submit");

    @Condition(value = ExpectedConditionType.DOM_ATTRIBUTE_TO_BE,
               with = @Attributes(attribute = "name", expectedValue = "username"))
    public static final By USERNAME_DOM_ATTR = By.id("username");

    @Condition(value = ExpectedConditionType.DOM_PROPERTY_TO_BE,
               with = @Attributes(property = "value", expectedValue = "qa@example.com"),
               timeoutMillis = 5_000)
    public static final By EMAIL_DOM_PROPERTY = By.id("email");

    // ---- cardinality --------------------------------------------------------------------
    @Condition(value = ExpectedConditionType.NUMBER_OF_ELEMENTS_TO_BE,
               with = @Attributes(number = 3),
               timeoutMillis = 5_000)
    public static final By ROWS_EXACTLY_THREE = By.cssSelector("#rows .row");

    @Condition(value = ExpectedConditionType.NUMBER_OF_ELEMENTS_MORE_THAN,
               with = @Attributes(number = 2),
               timeoutMillis = 5_000)
    public static final By ROWS_MORE_THAN_TWO = By.cssSelector("#rows .row");

    @Condition(value = ExpectedConditionType.NUMBER_OF_ELEMENTS_LESS_THAN,
               with = @Attributes(number = 4),
               timeoutMillis = 5_000)
    public static final By ROWS_FEWER_THAN_FOUR = By.cssSelector("#rows .row");

    // ---- nested elements -----------------------------------------------------------------
    @Condition(value = ExpectedConditionType.PRESENCE_OF_NESTED_ELEMENT,
               with = @Attributes(childUsing = Using.CSS, childValue = ".child"),
               timeoutMillis = 5_000)
    public static final By PANEL_NESTED_ONE = By.id("panel");

    @Condition(value = ExpectedConditionType.PRESENCE_OF_NESTED_ELEMENTS,
               with = @Attributes(childUsing = Using.CSS, childValue = ".child"),
               timeoutMillis = 5_000)
    public static final By PANEL_NESTED_ALL = By.id("panel");

    @Condition(value = ExpectedConditionType.VISIBILITY_OF_NESTED_ELEMENTS,
               with = @Attributes(childUsing = Using.CLASS_NAME, childValue = "child"),
               timeoutMillis = 5_000)
    public static final By PANEL_NESTED_VISIBLE = By.id("panel");

    // ---- lifecycle -------------------------------------------------------------------------
    @Condition(value = ExpectedConditionType.STALENESS_OF, timeoutMillis = 5_000)
    public static final By STALE = By.id("stale");

    // ---- frame by locator --------------------------------------------------------------------
    @Condition(ExpectedConditionType.FRAME_AVAILABLE_BY_LOCATOR)
    public static final By CONTENT_FRAME = By.id("contentFrame");

    // ---- negation --------------------------------------------------------------------------
    /** {@code not(...)} is a modifier, not a catalogue entry. */
    @Condition(value = ExpectedConditionType.ELEMENT_TO_BE_SELECTED, negate = true)
    public static final By MARKETING_NOT_SELECTED = By.id("marketing");

    // ---- open extension point ------------------------------------------------------------------
    @Condition(value = ExpectedConditionType.CUSTOM,
               with = @Attributes(number = 40),
               factory = SettledCountAtLeastFactory.class,
               timeoutMillis = 5_000,
               message = "settled count never reached the floor")
    public static final By SETTLED_COUNT = By.id("status");

    // ---- plain locators (no contract) ------------------------------------------------------------
    public static final By ALERT_BUTTON = By.id("alertBtn");
    public static final By NEW_WINDOW_BUTTON = By.id("newWinBtn");
    public static final By GO_SECOND = By.id("goSecond");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    // ---- method-level post-conditions ------------------------------------------------------------
    /** Document-scoped post-condition; evaluated via {@code awaitAfter("raiseAlert")}. */
    @Condition(ExpectedConditionType.ALERT_IS_PRESENT)
    public HomePage raiseAlert() {
        driver.findElement(ALERT_BUTTON).click();
        awaitAfter("raiseAlert");
        return this;
    }

    @Condition(value = ExpectedConditionType.NUMBER_OF_WINDOWS_TO_BE,
               with = @Attributes(number = 2),
               timeoutMillis = 5_000)
    public HomePage openSecondWindow() {
        driver.findElement(NEW_WINDOW_BUTTON).click();
        awaitAfter("openSecondWindow");
        return this;
    }

    @Condition(value = ExpectedConditionType.JS_RETURNS_VALUE,
               with = @Attributes(javaScript = "return window.appReady;"),
               timeoutMillis = 5_000)
    @Condition(value = ExpectedConditionType.JAVASCRIPT_THROWS_NO_EXCEPTIONS,
               with = @Attributes(javaScript = "document.getElementById('heading').textContent;"))
    public HomePage awaitScriptState() {
        awaitAfter("awaitScriptState");
        return this;
    }

    @Condition(value = ExpectedConditionType.TITLE_CONTAINS,
               with = @Attributes(title = "Second"))
    @Condition(value = ExpectedConditionType.URL_MATCHES,
               with = @Attributes(regex = ".*/second\\.html$"))
    public SecondPage goSecond() {
        driver.findElement(GO_SECOND).click();
        awaitAfter("goSecond");
        return new SecondPage(driver);
    }
}
