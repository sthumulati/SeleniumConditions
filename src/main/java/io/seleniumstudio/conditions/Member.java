package io.seleniumstudio.conditions;

/**
 * Names the members of {@link Attributes}, so each {@link ExpectedConditionType} can declare
 * exactly which members it requires. This is the metadata that makes both arity checking and
 * extraneous-member detection possible at compile time.
 */
public enum Member {

    ATTRIBUTE("attribute"),
    EXPECTED_VALUE("expectedValue"),
    PROPERTY("property"),
    SELECTED("selected"),
    NUMBER("number"),
    TEXT("text"),
    JAVASCRIPT("javaScript"),
    REGEX("regex"),
    TITLE("title"),
    FRACTION("fraction"),
    URL("url"),
    CHILD_USING("childUsing"),
    CHILD_VALUE("childValue"),
    FRAME_NAME_OR_ID("frameNameOrId"),
    FRAME_INDEX("frameIndex");

    private final String memberName;

    Member(String memberName) { this.memberName = memberName; }

    /** The declared method name on {@link Attributes}. Used by the annotation processor. */
    public String memberName() { return memberName; }

    public static Member byMemberName(String name) {
        for (Member m : values()) {
            if (m.memberName.equals(name)) return m;
        }
        return null;
    }
}
