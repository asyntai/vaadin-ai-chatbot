package org.vaadin.addons.asyntai;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Builds the JavaScript that loads the Asyntai widget.
 *
 * The app-wide listener puts this script into the first page, and
 * {@link AsyntaiChat} runs it when the component is attached, so both load the
 * widget in the same way.
 */
final class WidgetScript {

    static final String DEFAULT_SCRIPT_URL = "https://widget.asyntai.com/static/js/chat-widget.js";

    private static final Pattern WIDGET_ID = Pattern.compile("^asyntai_[A-Za-z0-9]+$");

    private WidgetScript() {
    }

    static boolean isValidWidgetId(String widgetId) {
        return widgetId != null && WIDGET_ID.matcher(widgetId).matches();
    }

    /**
     * The loader waits for the page's load event before it fetches the widget,
     * so an unreachable server can never delay the app. It adds the script only
     * once, so the app-wide listener and the component can be used together.
     */
    static String loader(String widgetId, String scriptUrl) {
        return "(function(){var l=function(){"
                + "if(document.querySelector(\"script[data-asyntai-id]\"))return;"
                + "var s=document.createElement(\"script\");"
                + "s.src=" + toJson(scriptUrl) + ";s.async=true;"
                + "s.setAttribute(\"data-asyntai-id\"," + toJson(widgetId) + ");"
                + "document.head.appendChild(s)};"
                + "if(document.readyState===\"complete\"){l()}"
                + "else{window.addEventListener(\"load\",l)}})();";
    }

    /** The widget reads window.Asyntai.userContext when it sends a message. */
    static String userContext(Map<String, ?> context) {
        return "window.Asyntai=window.Asyntai||{};window.Asyntai.userContext=" + toJson(context) + ";";
    }

    /** JSON that is also safe inside an HTML script element. */
    static String toJson(Object value) {
        StringBuilder out = new StringBuilder();
        appendJson(out, value);
        return out.toString();
    }

    private static void appendJson(StringBuilder out, Object value) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof Boolean) {
            out.append(value);
        } else if (value instanceof Number number) {
            double asDouble = number.doubleValue();
            if (Double.isNaN(asDouble) || Double.isInfinite(asDouble)) {
                out.append("null");
            } else {
                out.append(number);
            }
        } else if (value instanceof Map<?, ?> map) {
            out.append('{');
            Iterator<? extends Map.Entry<?, ?>> entries = map.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<?, ?> entry = entries.next();
                appendString(out, String.valueOf(entry.getKey()));
                out.append(':');
                appendJson(out, entry.getValue());
                if (entries.hasNext()) {
                    out.append(',');
                }
            }
            out.append('}');
        } else if (value instanceof Iterable<?> items) {
            out.append('[');
            Iterator<?> iterator = items.iterator();
            while (iterator.hasNext()) {
                appendJson(out, iterator.next());
                if (iterator.hasNext()) {
                    out.append(',');
                }
            }
            out.append(']');
        } else {
            appendString(out, value.toString());
        }
    }

    private static void appendString(StringBuilder out, String text) {
        out.append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    // < > & ' and the JavaScript line separators are escaped so
                    // that the text cannot end the script element it sits in.
                    if (c < 0x20 || c == '<' || c == '>' || c == '&' || c == '\'' || c == 0x2028 || c == 0x2029) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }
}
