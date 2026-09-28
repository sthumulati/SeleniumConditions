package io.seleniumstudio.conditions.processor;

import io.seleniumstudio.conditions.ExpectedConditionType;
import io.seleniumstudio.conditions.LocatorUse;
import io.seleniumstudio.conditions.Member;
import io.seleniumstudio.conditions.Sentinels;
import io.seleniumstudio.conditions.Tri;
import io.seleniumstudio.conditions.Using;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Compile-time validator for {@code @Condition} declarations.
 *
 * <p>This processor is the answer to the central defect of a flat optional-member bag: it makes
 * arity, applicability and absence checkable before the suite runs. Because
 * {@link ExpectedConditionType} carries required-member metadata as data, adding a new condition
 * constant extends validation coverage with no change to this class.</p>
 *
 * <p>Checks performed:</p>
 * <ol>
 *   <li>Every member required by the condition is set to a non-sentinel value.</li>
 *   <li>No member outside the condition's required set is set (the check the original design could
 *       not perform at all).</li>
 *   <li>{@code LocatorUse.REQUIRED} appears only on {@code By}-typed fields;
 *       {@code LocatorUse.FORBIDDEN} only on types and methods.</li>
 *   <li>{@code regex()} compiles as a {@link Pattern}.</li>
 *   <li>{@code value() == CUSTOM} if and only if {@code factory()} is set.</li>
 *   <li>{@code timeoutMillis} / {@code pollingMillis} are {@code -1} or strictly positive.</li>
 *   <li>{@code childUsing} and {@code childValue} are set together or not at all.</li>
 * </ol>
 */
@SupportedAnnotationTypes({
        "io.seleniumstudio.conditions.Condition",
        "io.seleniumstudio.conditions.Conditions"
})
public final class ConditionProcessor extends AbstractProcessor {

    private static final String CONDITION = "io.seleniumstudio.conditions.Condition";
    private static final String BY = "org.openqa.selenium.By";
    private static final String FACTORY_NONE =
            "io.seleniumstudio.conditions.spi.ConditionFactory.None";

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
        if (round.processingOver()) return false;

        for (TypeElement annotation : annotations) {
            for (Element element : round.getElementsAnnotatedWith(annotation)) {
                for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
                    collectConditions(mirror, element);
                }
            }
        }
        return false;
    }

    /** Handles both the direct form and the repeatable container form. */
    private void collectConditions(AnnotationMirror mirror, Element site) {
        String name = mirror.getAnnotationType().toString();
        if (CONDITION.equals(name)) {
            validate(mirror, site);
            return;
        }
        if (!name.endsWith(".Conditions")) return;
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e
                : mirror.getElementValues().entrySet()) {
            if (!"value".equals(e.getKey().getSimpleName().toString())) continue;
            Object v = e.getValue().getValue();
            if (!(v instanceof List)) continue;
            for (Object item : (List<?>) v) {
                Object inner = ((AnnotationValue) item).getValue();
                if (inner instanceof AnnotationMirror) {
                    validate((AnnotationMirror) inner, site);
                }
            }
        }
    }

    private void validate(AnnotationMirror condition, Element site) {
        Map<String, AnnotationValue> values = explicitValues(condition);

        ExpectedConditionType type = readType(values.get("value"));
        if (type == null) {
            error(site, "@Condition requires an ExpectedConditionType value()");
            return;
        }

        // --- 3. Applicability of the declaration site -------------------------------------
        if (type.locatorUse() == LocatorUse.REQUIRED) {
            if (site.getKind() != ElementKind.FIELD) {
                error(site, type + " requires a locator and must be declared on a By field, not on "
                        + site.getKind());
            } else if (!BY.equals(((VariableElement) site).asType().toString())) {
                error(site, type + " requires a locator, but " + site.getSimpleName()
                        + " is of type " + ((VariableElement) site).asType() + ", not " + BY);
            }
        } else if (type.locatorUse() == LocatorUse.FORBIDDEN
                && site.getKind() == ElementKind.FIELD) {
            error(site, type + " is document-scoped (LocatorUse.FORBIDDEN) and must not be declared"
                    + " on the locator field " + site.getSimpleName()
                    + ". Declare it on the page class or on a method.");
        }

        // --- 5. CUSTOM <=> factory -------------------------------------------------------
        AnnotationValue factory = values.get("factory");
        boolean factorySet = factory != null && !factory.getValue().toString().equals(FACTORY_NONE);
        if (type == ExpectedConditionType.CUSTOM && !factorySet) {
            error(site, "value() == CUSTOM requires factory() to name a ConditionFactory");
        }
        if (type != ExpectedConditionType.CUSTOM && factorySet) {
            error(site, "factory() is only meaningful with value() == CUSTOM, but value() is " + type);
        }

        // --- 6. Wait envelope ------------------------------------------------------------
        checkMillis(site, values.get("timeoutMillis"), "timeoutMillis");
        checkMillis(site, values.get("pollingMillis"), "pollingMillis");

        // --- 1, 2, 4, 7. Member set ------------------------------------------------------
        AnnotationMirror attributes = readAttributes(values.get("with"));
        Set<Member> supplied = suppliedMembers(attributes);

        if (type != ExpectedConditionType.CUSTOM) {
            Set<Member> missing = EnumSet.noneOf(Member.class);
            for (Member required : type.requiredMembers()) {
                if (!supplied.contains(required)) missing.add(required);
            }
            if (!missing.isEmpty()) {
                error(site, type + " requires @Attributes members " + memberNames(missing)
                        + " to be set. Declared required set: " + memberNames(type.requiredMembers()));
            }
            Set<Member> extraneous = new LinkedHashSet<Member>(supplied);
            extraneous.removeAll(type.requiredMembers());
            if (!extraneous.isEmpty()) {
                error(site, type + " does not accept @Attributes members " + memberNames(extraneous)
                        + ". It accepts only " + memberNames(type.requiredMembers()) + ".");
            }
        }

        boolean childUsing = supplied.contains(Member.CHILD_USING);
        boolean childValue = supplied.contains(Member.CHILD_VALUE);
        if (childUsing != childValue) {
            error(site, "childUsing() and childValue() must be set together; only "
                    + (childUsing ? "childUsing()" : "childValue()") + " was set");
        }

        if (attributes != null) {
            AnnotationValue regex = explicitValues(attributes).get("regex");
            if (regex != null && regex.getValue() instanceof String) {
                String raw = (String) regex.getValue();
                if (Sentinels.isSet(raw)) {
                    try {
                        Pattern.compile(raw);
                    } catch (PatternSyntaxException ex) {
                        error(site, "regex() does not compile: " + ex.getMessage());
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------

    private Set<Member> suppliedMembers(AnnotationMirror attributes) {
        Set<Member> supplied = EnumSet.noneOf(Member.class);
        if (attributes == null) return supplied;

        for (Map.Entry<String, AnnotationValue> e : explicitValues(attributes).entrySet()) {
            Member member = Member.byMemberName(e.getKey());
            if (member == null) continue;
            if (isSentinel(e.getValue().getValue())) continue;
            supplied.add(member);
        }
        return supplied;
    }

    /**
     * A member explicitly written as its own sentinel counts as unset. This keeps the check
     * honest under code generation, which often emits every member.
     */
    private boolean isSentinel(Object value) {
        if (value instanceof String)  return !Sentinels.isSet((String) value);
        if (value instanceof Integer) return !Sentinels.isSet((Integer) value);
        String s = String.valueOf(value);
        return s.endsWith("." + Tri.UNSET.name()) || s.endsWith("." + Using.UNSET.name())
                || Tri.UNSET.name().equals(s) || Using.UNSET.name().equals(s);
    }

    private void checkMillis(Element site, AnnotationValue value, String member) {
        if (value == null) return;
        Object raw = value.getValue();
        if (!(raw instanceof Long)) return;
        long ms = (Long) raw;
        if (ms != Sentinels.INHERIT_MILLIS && ms <= 0L) {
            error(site, member + " must be -1 (inherit) or strictly positive, but was " + ms);
        }
    }

    private ExpectedConditionType readType(AnnotationValue value) {
        if (value == null) return null;
        Object raw = value.getValue();
        String name = (raw instanceof VariableElement)
                ? ((VariableElement) raw).getSimpleName().toString()
                : String.valueOf(raw);
        int dot = name.lastIndexOf('.');
        if (dot >= 0) name = name.substring(dot + 1);
        try {
            return ExpectedConditionType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private AnnotationMirror readAttributes(AnnotationValue value) {
        if (value == null) return null;
        Object raw = value.getValue();
        return (raw instanceof AnnotationMirror) ? (AnnotationMirror) raw : null;
    }

    /** Only members written at the declaration site; defaults are deliberately not expanded. */
    private Map<String, AnnotationValue> explicitValues(AnnotationMirror mirror) {
        java.util.LinkedHashMap<String, AnnotationValue> out =
                new java.util.LinkedHashMap<String, AnnotationValue>();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e
                : mirror.getElementValues().entrySet()) {
            out.put(e.getKey().getSimpleName().toString(), e.getValue());
        }
        return out;
    }

    private String memberNames(Set<Member> members) {
        StringBuilder sb = new StringBuilder("[");
        for (Member m : members) {
            if (sb.length() > 1) sb.append(", ");
            sb.append(m.memberName()).append("()");
        }
        return sb.append(']').toString();
    }

    private void error(Element site, String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, site);
    }
}
