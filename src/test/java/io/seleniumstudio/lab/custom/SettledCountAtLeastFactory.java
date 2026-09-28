package io.seleniumstudio.lab.custom;

import io.seleniumstudio.conditions.Attributes;
import io.seleniumstudio.conditions.spi.ConditionFactory;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The open extension point: a predicate with no counterpart in {@code ExpectedConditions}.
 *
 * <p>Note that a factory may still read the {@link Attributes} bag, so a custom condition keeps the
 * same declarative argument surface as a catalogue entry. Boolean structure too deep for annotation
 * grammar belongs here, expressed in Java.</p>
 */
public final class SettledCountAtLeastFactory implements ConditionFactory {

    private static final Pattern DIGITS = Pattern.compile("(\\d+)");

    @Override
    public ExpectedCondition<Boolean> create(WebDriver driver, By locator, Attributes attributes) {
        final int floor = attributes.number();
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
            public String toString() {
                return "a settled count of at least " + floor + " in " + locator;
            }
        };
    }
}
