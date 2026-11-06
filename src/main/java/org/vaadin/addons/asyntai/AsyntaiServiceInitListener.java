package org.vaadin.addons.asyntai;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Element;
import org.slf4j.LoggerFactory;

import java.util.function.UnaryOperator;

/**
 * Adds the widget to the first page of the app when a widget ID is configured.
 * Vaadin finds this class through META-INF/services, so the app needs no code.
 * Without a widget ID this listener does nothing, and the app can use
 * {@link AsyntaiChat} instead.
 */
public class AsyntaiServiceInitListener implements VaadinServiceInitListener {

    private static final long serialVersionUID = 1L;

    private final transient UnaryOperator<String> environment;

    /** Vaadin calls this constructor. */
    public AsyntaiServiceInitListener() {
        this(System::getenv);
    }

    AsyntaiServiceInitListener(UnaryOperator<String> environment) {
        this.environment = environment;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        String widgetId = AsyntaiSettings.widgetId(event.getSource(), environment);
        if (widgetId == null) {
            return;
        }
        if (!WidgetScript.isValidWidgetId(widgetId)) {
            // A wrong ID must never stop the app from starting.
            LoggerFactory.getLogger(AsyntaiServiceInitListener.class).warn(
                    "Asyntai AI chatbot: \"{}\" does not look like a widget ID. It starts with asyntai_, "
                            + "for example asyntai_1a2b3c4d5e6f.",
                    widgetId);
            return;
        }
        String script = WidgetScript.loader(widgetId, AsyntaiSettings.scriptUrl(event.getSource(), environment));
        event.addIndexHtmlRequestListener(response -> {
            Element tag = response.getDocument().body().appendElement("script");
            tag.attr("data-asyntai-loader", "");
            tag.appendChild(new DataNode(script));
        });
    }
}
