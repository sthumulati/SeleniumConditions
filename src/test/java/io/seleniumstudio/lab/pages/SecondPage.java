package io.seleniumstudio.lab.pages;

import io.seleniumstudio.conditions.Attributes;
import io.seleniumstudio.conditions.Condition;
import io.seleniumstudio.conditions.ExpectedConditionType;
import io.seleniumstudio.conditions.runtime.AnnotatedPage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Destination page; its load contract proves that class-level conditions are document-scoped. */
@Condition(value = ExpectedConditionType.TITLE_IS,
           with = @Attributes(title = "Condition Lab - Second"))
public final class SecondPage extends AnnotatedPage {

    @Condition(ExpectedConditionType.VISIBILITY_OF_ELEMENT_LOCATED)
    public static final By HEADING = By.id("secondHeading");

    public SecondPage(WebDriver driver) {
        super(driver);
    }
}
