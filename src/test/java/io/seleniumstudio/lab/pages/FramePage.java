package io.seleniumstudio.lab.pages;

import io.seleniumstudio.conditions.Attributes;
import io.seleniumstudio.conditions.Condition;
import io.seleniumstudio.conditions.ExpectedConditionType;
import io.seleniumstudio.conditions.runtime.AnnotatedPage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Contents of the iframe. Instantiated only after a frame switch has been awaited. */
public final class FramePage extends AnnotatedPage {

    @Condition(ExpectedConditionType.VISIBILITY_OF_ELEMENT_LOCATED)
    public static final By HEADING = By.id("frameHeading");

    @Condition(value = ExpectedConditionType.TEXT_TO_BE_PRESENT_IN_ELEMENT,
               with = @Attributes(text = "frame-ready"))
    public static final By STATUS = By.id("frameStatus");

    public FramePage(WebDriver driver) {
        super(driver);
    }
}
