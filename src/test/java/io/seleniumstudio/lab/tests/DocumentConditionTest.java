package io.seleniumstudio.lab.tests;

import io.seleniumstudio.conditions.Attributes;
import io.seleniumstudio.conditions.Condition;
import io.seleniumstudio.conditions.ExpectedConditionType;
import io.seleniumstudio.lab.pages.FramePage;
import io.seleniumstudio.lab.pages.HomePage;
import io.seleniumstudio.lab.pages.SecondPage;

import org.openqa.selenium.Alert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Document-scoped conditions. These are {@link
 * io.seleniumstudio.conditions.LocatorUse#FORBIDDEN} and therefore appear on the page class or on a
 * method, never on a locator field — a rule the annotation processor enforces at compile time.
 */
public class DocumentConditionTest extends BaseTest {

    @Test
    public void titleIsAndUrlContainsViaLoadContract() {
        // BaseTest already called awaitLoaded(); calling it again re-asserts the contract.
        home.awaitLoaded();
        assertEquals(driver.getTitle(), "Condition Lab - Home");
    }

    @Test
    public void alertIsPresent() {
        home.raiseAlert();
        Alert alert = driver.switchTo().alert();
        assertEquals(alert.getText(), "confirm-settlement");
        alert.accept();
    }

    @Test
    public void numberOfWindowsToBe() {
        home.openSecondWindow();
        assertEquals(driver.getWindowHandles().size(), 2);
    }

    @Test
    public void jsReturnsValueAndJavaScriptThrowsNoExceptions() {
        home.awaitScriptState();
    }

    @Test
    public void titleContainsAndUrlMatchesOnNavigation() {
        SecondPage second = home.goSecond().awaitLoaded();
        assertTrue(driver.getCurrentUrl().endsWith("/second.html"));
        assertTrue(second.<org.openqa.selenium.WebElement>await(SecondPage.HEADING).isDisplayed());
    }

    @Test
    public void urlToBeExactly() {
        // urlToBe needs the runtime origin, which an annotation member cannot carry: it must be a
        // compile-time constant. This is the documented boundary — use the imperative path.
        driver.get(site.url("second.html"));
        home.awaitDynamic(
                org.openqa.selenium.support.ui.ExpectedConditions.urlToBe(site.url("second.html")),
                java.time.Duration.ofSeconds(5));
        assertEquals(driver.getCurrentUrl(), site.url("second.html"));
    }

    @Test
    public void frameToBeAvailableByLocator() {
        home.await(HomePage.CONTENT_FRAME);   // switches into the frame
        FramePage frame = new FramePage(driver);
        assertTrue(frame.<org.openqa.selenium.WebElement>await(FramePage.HEADING).isDisplayed());
        assertTrue((Boolean) frame.await(FramePage.STATUS));
        driver.switchTo().defaultContent();
    }

    @Test
    public void frameToBeAvailableByNameOrIdAndByIndex() {
        FrameSwitches switches = new FrameSwitches(driver);

        switches.awaitAfter("byNameOrId");
        assertEquals(driver.findElement(FramePage.HEADING).getText(), "Inside the frame");
        driver.switchTo().defaultContent();

        switches.awaitAfter("byIndex");
        assertEquals(driver.findElement(FramePage.HEADING).getText(), "Inside the frame");
        driver.switchTo().defaultContent();
    }

    /** Local holder for the two document-scoped frame variants. */
    static final class FrameSwitches extends io.seleniumstudio.conditions.runtime.AnnotatedPage {

        FrameSwitches(org.openqa.selenium.WebDriver driver) { super(driver); }

        @Condition(value = ExpectedConditionType.FRAME_AVAILABLE_BY_NAME_OR_ID,
                   with = @Attributes(frameNameOrId = "content"))
        void byNameOrId() { }

        @Condition(value = ExpectedConditionType.FRAME_AVAILABLE_BY_INDEX,
                   with = @Attributes(frameIndex = 0))
        void byIndex() { }
    }
}
