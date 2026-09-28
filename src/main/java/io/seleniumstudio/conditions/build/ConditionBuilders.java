package io.seleniumstudio.conditions.build;

import io.seleniumstudio.conditions.Attributes;
import io.seleniumstudio.conditions.ExpectedConditionType;
import io.seleniumstudio.conditions.LocatorUse;
import io.seleniumstudio.conditions.Member;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * The one place that depends on {@code ExpectedConditions}. Keeping construction out of
 * {@link ExpectedConditionType} keeps the metadata enum Selenium-free, which is what lets the
 * annotation processor consume it without Selenium on the processor path.
 *
 * <p>Four constants resolve a {@code WebElement} from the locator at build time, because their
 * Selenium overloads accept only a {@code WebElement}:
 * {@code ATTRIBUTE_TO_BE_NOT_EMPTY}, {@code DOM_ATTRIBUTE_TO_BE}, {@code DOM_PROPERTY_TO_BE} and
 * {@code STALENESS_OF}. For {@code STALENESS_OF} this is the correct semantic — the element is
 * captured when the wait begins, and staleness is asserted against that capture.</p>
 */
public final class ConditionBuilders {

    private ConditionBuilders() { }

    public static ExpectedCondition<?> build(ExpectedConditionType type,
                                             WebDriver driver,
                                             By locator,
                                             Attributes a) {

        if (type.locatorUse() == LocatorUse.REQUIRED && locator == null) {
            throw new IllegalArgumentException(type + " requires a locator, but none was supplied");
        }
        if (type.locatorUse() == LocatorUse.FORBIDDEN && locator != null) {
            throw new IllegalArgumentException(
                    type + " is document-scoped and must not be declared on a locator field");
        }

        switch (type) {

            // ---------- Document scope ----------
            case TITLE_IS:
                return ExpectedConditions.titleIs(Attr.required(type, Member.TITLE, a.title()));
            case TITLE_CONTAINS:
                return ExpectedConditions.titleContains(Attr.required(type, Member.TITLE, a.title()));
            case URL_TO_BE:
                return ExpectedConditions.urlToBe(Attr.required(type, Member.URL, a.url()));
            case URL_CONTAINS:
                return ExpectedConditions.urlContains(Attr.required(type, Member.FRACTION, a.fraction()));
            case URL_MATCHES:
                return ExpectedConditions.urlMatches(Attr.requiredPattern(type, a).pattern());
            case ALERT_IS_PRESENT:
                return ExpectedConditions.alertIsPresent();
            case NUMBER_OF_WINDOWS_TO_BE:
                return ExpectedConditions.numberOfWindowsToBe(
                        Attr.required(type, Member.NUMBER, a.number()));
            case JAVASCRIPT_THROWS_NO_EXCEPTIONS:
                return ExpectedConditions.javaScriptThrowsNoExceptions(
                        Attr.required(type, Member.JAVASCRIPT, a.javaScript()));
            case JS_RETURNS_VALUE:
                return ExpectedConditions.jsReturnsValue(
                        Attr.required(type, Member.JAVASCRIPT, a.javaScript()));

            // ---------- Frames ----------
            case FRAME_AVAILABLE_BY_NAME_OR_ID:
                return ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                        Attr.required(type, Member.FRAME_NAME_OR_ID, a.frameNameOrId()));
            case FRAME_AVAILABLE_BY_INDEX:
                return ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                        Attr.required(type, Member.FRAME_INDEX, a.frameIndex()));
            case FRAME_AVAILABLE_BY_LOCATOR:
                return ExpectedConditions.frameToBeAvailableAndSwitchToIt(locator);

            // ---------- Presence and visibility ----------
            case PRESENCE_OF_ELEMENT_LOCATED:
                return ExpectedConditions.presenceOfElementLocated(locator);
            case PRESENCE_OF_ALL_ELEMENTS:
                return ExpectedConditions.presenceOfAllElementsLocatedBy(locator);
            case VISIBILITY_OF_ELEMENT_LOCATED:
                return ExpectedConditions.visibilityOfElementLocated(locator);
            case VISIBILITY_OF_ALL_ELEMENTS:
                return ExpectedConditions.visibilityOfAllElementsLocatedBy(locator);
            case INVISIBILITY_OF_ELEMENT_LOCATED:
                return ExpectedConditions.invisibilityOfElementLocated(locator);
            case INVISIBILITY_OF_ELEMENT_WITH_TEXT:
                return ExpectedConditions.invisibilityOfElementWithText(
                        locator, Attr.required(type, Member.TEXT, a.text()));

            // ---------- Interaction and selection state ----------
            case ELEMENT_TO_BE_CLICKABLE:
                return ExpectedConditions.elementToBeClickable(locator);
            case ELEMENT_TO_BE_SELECTED:
                return ExpectedConditions.elementToBeSelected(locator);
            case ELEMENT_SELECTION_STATE_TO_BE:
                return ExpectedConditions.elementSelectionStateToBe(
                        locator, Attr.requiredSelected(type, a));

            // ---------- Text ----------
            case TEXT_TO_BE_PRESENT_IN_ELEMENT:
                return ExpectedConditions.textToBePresentInElementLocated(
                        locator, Attr.required(type, Member.TEXT, a.text()));
            case TEXT_TO_BE_PRESENT_IN_VALUE:
                return ExpectedConditions.textToBePresentInElementValue(
                        locator, Attr.required(type, Member.TEXT, a.text()));
            case TEXT_MATCHES:
                return ExpectedConditions.textMatches(locator, Attr.requiredPattern(type, a));

            // ---------- Attributes and DOM properties ----------
            case ATTRIBUTE_TO_BE:
                return ExpectedConditions.attributeToBe(
                        locator,
                        Attr.required(type, Member.ATTRIBUTE, a.attribute()),
                        Attr.required(type, Member.EXPECTED_VALUE, a.expectedValue()));
            case ATTRIBUTE_CONTAINS:
                return ExpectedConditions.attributeContains(
                        locator,
                        Attr.required(type, Member.ATTRIBUTE, a.attribute()),
                        Attr.required(type, Member.EXPECTED_VALUE, a.expectedValue()));
            case ATTRIBUTE_TO_BE_NOT_EMPTY:
                return ExpectedConditions.attributeToBeNotEmpty(
                        element(driver, locator),
                        Attr.required(type, Member.ATTRIBUTE, a.attribute()));
            case DOM_ATTRIBUTE_TO_BE:
                return ExpectedConditions.domAttributeToBe(
                        element(driver, locator),
                        Attr.required(type, Member.ATTRIBUTE, a.attribute()),
                        Attr.required(type, Member.EXPECTED_VALUE, a.expectedValue()));
            case DOM_PROPERTY_TO_BE:
                return ExpectedConditions.domPropertyToBe(
                        element(driver, locator),
                        Attr.required(type, Member.PROPERTY, a.property()),
                        Attr.required(type, Member.EXPECTED_VALUE, a.expectedValue()));

            // ---------- Cardinality ----------
            case NUMBER_OF_ELEMENTS_TO_BE:
                return ExpectedConditions.numberOfElementsToBe(
                        locator, Attr.required(type, Member.NUMBER, a.number()));
            case NUMBER_OF_ELEMENTS_MORE_THAN:
                return ExpectedConditions.numberOfElementsToBeMoreThan(
                        locator, Attr.required(type, Member.NUMBER, a.number()));
            case NUMBER_OF_ELEMENTS_LESS_THAN:
                return ExpectedConditions.numberOfElementsToBeLessThan(
                        locator, Attr.required(type, Member.NUMBER, a.number()));

            // ---------- Nested ----------
            case PRESENCE_OF_NESTED_ELEMENT:
                return ExpectedConditions.presenceOfNestedElementLocatedBy(
                        locator, Attr.requiredChildBy(type, a));
            case PRESENCE_OF_NESTED_ELEMENTS:
                return ExpectedConditions.presenceOfNestedElementsLocatedBy(
                        locator, Attr.requiredChildBy(type, a));
            case VISIBILITY_OF_NESTED_ELEMENTS:
                return ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                        locator, Attr.requiredChildBy(type, a));

            // ---------- Lifecycle ----------
            case STALENESS_OF:
                return ExpectedConditions.stalenessOf(element(driver, locator));

            case CUSTOM:
                throw new UnsupportedOperationException(
                        "CUSTOM is resolved via Condition.factory(), not by ConditionBuilders");

            default:
                throw new UnsupportedOperationException("Unmapped condition: " + type);
        }
    }

    private static WebElement element(WebDriver driver, By locator) {
        if (driver == null) {
            throw new IllegalStateException(
                    "A WebDriver is required to resolve the element for this condition");
        }
        return driver.findElement(locator);
    }
}
