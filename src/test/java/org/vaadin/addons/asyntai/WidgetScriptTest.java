package org.vaadin.addons.asyntai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class WidgetScriptTest {

    @Test
    void acceptsRealWidgetIdsOnly() {
        assertTrue(WidgetScript.isValidWidgetId("asyntai_1a2b3c4d5e6f"));
        assertFalse(WidgetScript.isValidWidgetId(null));
        assertFalse(WidgetScript.isValidWidgetId(""));
        assertFalse(WidgetScript.isValidWidgetId("asyntai_"));
        assertFalse(WidgetScript.isValidWidgetId("1a2b3c4d5e6f"));
        assertFalse(WidgetScript.isValidWidgetId("asyntai_1a2b\"><script>"));
        assertFalse(WidgetScript.isValidWidgetId("asyntai_1a2b\n"));
    }

    @Test
    void loaderCarriesTheIdAndUrlAndWaitsForLoad() {
        String script = WidgetScript.loader("asyntai_abc", "https://example.com/w.js");
        assertTrue(script.contains("s.src=\"https://example.com/w.js\""));
        assertTrue(script.contains("s.setAttribute(\"data-asyntai-id\",\"asyntai_abc\")"));
        assertTrue(script.contains("window.addEventListener(\"load\",l)"));
        assertTrue(script.contains("document.querySelector(\"script[data-asyntai-id]\")"));
    }

    @Test
    void loaderUrlCannotEndTheScriptElement() {
        String script = WidgetScript.loader("asyntai_abc", "https://example.com/</script><b>");
        assertFalse(script.contains("</script>"));
        assertFalse(script.contains("<b>"));
    }

    @Test
    void userContextKeepsTypesAndOrder() {
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("seats", 3);
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("name", "Ana");
        context.put("active", true);
        context.put("balance", 12.5);
        context.put("missing", null);
        context.put("tags", List.of("a", "b"));
        context.put("company", nested);
        assertEquals(
                "window.Asyntai=window.Asyntai||{};window.Asyntai.userContext="
                        + "{\"name\":\"Ana\",\"active\":true,\"balance\":12.5,\"missing\":null,"
                        + "\"tags\":[\"a\",\"b\"],\"company\":{\"seats\":3}};",
                WidgetScript.userContext(context));
    }

    @Test
    void jsonEscapesEverythingThatIsDangerousInAScript() {
        assertEquals("\"\\u003c/script\\u003e\"", WidgetScript.toJson("</script>"));
        assertEquals("\"a\\\"b\\\\c\\nd\"", WidgetScript.toJson("a\"b\\c\nd"));
        assertEquals("\"\\u0026\\u0027\\u2028\\u2029\\u0001\"", WidgetScript.toJson("&'\u2028\u2029\u0001"));
        assertEquals("\"Šárka 你好\"", WidgetScript.toJson("Šárka 你好"));
    }

    @Test
    void jsonHandlesValuesThatJsonCannotHold() {
        assertEquals("[null,null,1]", WidgetScript.toJson(Arrays.asList(Double.NaN, Double.POSITIVE_INFINITY, 1)));
    }
}
