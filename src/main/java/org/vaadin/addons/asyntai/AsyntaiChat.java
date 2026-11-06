package org.vaadin.addons.asyntai;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.server.VaadinService;

import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shows the Asyntai chat assistant. Add this component to a layout or a view.
 * It has no visible element of its own: the chat bubble sits in the corner of
 * the page and stays open when the user moves between views.
 *
 * <pre>
 * add(new AsyntaiChat("asyntai_1a2b3c4d5e6f"));
 * </pre>
 */
@Tag("asyntai-chat")
public class AsyntaiChat extends Component {

    private static final long serialVersionUID = 1L;

    private final String widgetId;
    private String scriptUrl;
    private Map<String, Object> userContext;

    /** Uses the app-wide widget ID, for example from application.properties. */
    public AsyntaiChat() {
        this(null);
    }

    /**
     * Uses this widget ID.
     *
     * @param widgetId the widget ID from the Install page of the Asyntai
     *                 dashboard. It starts with asyntai_.
     */
    public AsyntaiChat(String widgetId) {
        this.widgetId = widgetId;
        getElement().getStyle().set("display", "none");
    }

    /**
     * Sets data about the signed-in user that the assistant can use in its
     * answers, for example the user's name and plan.
     *
     * @param userContext names and values: text, numbers, booleans, lists or
     *                    nested maps
     * @return this component
     */
    public AsyntaiChat setUserContext(Map<String, ?> userContext) {
        this.userContext = userContext == null ? null : new LinkedHashMap<>(userContext);
        return this;
    }

    /**
     * Advanced. Loads the widget from another URL.
     *
     * @param scriptUrl the URL of the widget script
     * @return this component
     */
    public AsyntaiChat setScriptUrl(String scriptUrl) {
        this.scriptUrl = scriptUrl;
        return this;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        VaadinService service = VaadinService.getCurrent();
        String id = widgetId != null ? widgetId.trim() : AsyntaiSettings.widgetId(service, System::getenv);
        if (!WidgetScript.isValidWidgetId(id)) {
            // A wrong ID must never break the view this component sits in.
            LoggerFactory.getLogger(AsyntaiChat.class).warn(
                    "Asyntai AI chatbot: \"{}\" is not a widget ID. Copy it from the Install page of your "
                            + "Asyntai dashboard. It starts with asyntai_, for example asyntai_1a2b3c4d5e6f.",
                    id);
            return;
        }
        String url = scriptUrl != null && !scriptUrl.isBlank() ? scriptUrl.trim()
                : AsyntaiSettings.scriptUrl(service, System::getenv);
        if (userContext != null && !userContext.isEmpty()) {
            attachEvent.getUI().getPage().executeJs(WidgetScript.userContext(userContext));
        }
        attachEvent.getUI().getPage().executeJs(WidgetScript.loader(id, url));
    }
}
