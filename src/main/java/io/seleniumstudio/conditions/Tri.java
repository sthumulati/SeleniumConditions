package io.seleniumstudio.conditions;

/**
 * Three-valued boolean.
 *
 * <p>{@code boolean} has no spare value to reserve as a sentinel, so a member such as
 * {@code selected()} — whose argument to {@code elementSelectionStateToBe(By, boolean)} may
 * legitimately be either {@code true} or {@code false} — cannot express absence as a
 * {@code boolean}. This enum supplies the missing third state.</p>
 */
public enum Tri {
    TRUE, FALSE, UNSET;

    public boolean isSet() { return this != UNSET; }

    public boolean asBoolean() {
        if (this == UNSET) {
            throw new IllegalStateException("Tri.UNSET has no boolean value");
        }
        return this == TRUE;
    }
}
