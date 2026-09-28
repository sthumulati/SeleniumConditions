package io.seleniumstudio.conditions;

/**
 * Locator strategy for nested-element members.
 *
 * <p>A bare {@code String} cannot say whether it is CSS, XPath or an id, so the strategy is
 * carried explicitly and the {@code By} is constructed in the runtime layer.</p>
 */
public enum Using {
    UNSET,
    ID,
    NAME,
    CLASS_NAME,
    TAG_NAME,
    LINK_TEXT,
    PARTIAL_LINK_TEXT,
    CSS,
    XPATH;

    public boolean isSet() { return this != UNSET; }
}
