package io.seleniumstudio.conditions.build;

import io.seleniumstudio.conditions.Attributes;
import io.seleniumstudio.conditions.ExpectedConditionType;
import io.seleniumstudio.conditions.Member;
import io.seleniumstudio.conditions.Sentinels;
import io.seleniumstudio.conditions.Using;

import org.openqa.selenium.By;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Sentinel-aware accessors. Every read of an {@link Attributes} member goes through here so that
 * the single point where "unset" becomes a failure is one method with one diagnostic shape.
 *
 * <p>The processor should have rejected an unset required member at compile time; these throws are
 * the defence-in-depth path for reflection-constructed or dynamically proxied annotations.</p>
 */
public final class Attr {

    private Attr() { }

    public static String required(ExpectedConditionType type, Member member, String raw) {
        if (!Sentinels.isSet(raw)) throw missing(type, member);
        return raw;
    }

    public static int required(ExpectedConditionType type, Member member, int raw) {
        if (!Sentinels.isSet(raw)) throw missing(type, member);
        return raw;
    }

    public static boolean requiredSelected(ExpectedConditionType type, Attributes a) {
        if (!a.selected().isSet()) throw missing(type, Member.SELECTED);
        return a.selected().asBoolean();
    }

    public static Pattern requiredPattern(ExpectedConditionType type, Attributes a) {
        String raw = required(type, Member.REGEX, a.regex());
        try {
            return Pattern.compile(raw);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException(
                    type + ": regex() does not compile: " + raw, e);
        }
    }

    /** Builds the child {@code By} for the nested-element conditions. */
    public static By requiredChildBy(ExpectedConditionType type, Attributes a) {
        Using using = a.childUsing();
        if (!using.isSet()) throw missing(type, Member.CHILD_USING);
        String value = required(type, Member.CHILD_VALUE, a.childValue());
        return by(using, value);
    }

    public static By by(Using using, String value) {
        switch (using) {
            case ID:                return By.id(value);
            case NAME:              return By.name(value);
            case CLASS_NAME:        return By.className(value);
            case TAG_NAME:          return By.tagName(value);
            case LINK_TEXT:         return By.linkText(value);
            case PARTIAL_LINK_TEXT: return By.partialLinkText(value);
            case CSS:               return By.cssSelector(value);
            case XPATH:             return By.xpath(value);
            default:
                throw new IllegalArgumentException("Using.UNSET cannot be converted to a By");
        }
    }

    private static IllegalArgumentException missing(ExpectedConditionType type, Member member) {
        return new IllegalArgumentException(
                type + " requires @Attributes(" + member.memberName() + " = ...), which is unset. "
                        + "Required members: " + type.requiredMembers());
    }
}
