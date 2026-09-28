package io.seleniumstudio.conditions;

/**
 * Absence sentinels for annotation members.
 *
 * <p>Annotation members cannot be {@code null} (JLS 9.7.1), and a default that lies inside the
 * legal value domain makes "not supplied" indistinguishable from "supplied as the default".
 * Every value here is a compile-time constant expression, which is what permits its use as an
 * annotation member default, and every value lies outside the domain of any real Selenium
 * condition argument.</p>
 */
public final class Sentinels {

    /** Contains NUL, which no legal locator, attribute name or expected text may contain. */
    public static final String UNSET_STRING = "\u0000__UNSET__\u0000";

    /** {@code 0} is a legal argument to {@code numberOfElementsToBe}, so the sentinel must not be 0. */
    public static final int UNSET_INT = Integer.MIN_VALUE;

    /** Inherit the framework-wide default rather than override it. */
    public static final long INHERIT_MILLIS = -1L;

    private Sentinels() { }

    public static boolean isSet(String v) { return v != null && !UNSET_STRING.equals(v); }
    public static boolean isSet(int v)    { return v != UNSET_INT; }
}
