package org.vaadin.addons.asyntai;

import com.vaadin.flow.server.VaadinService;

import java.util.function.UnaryOperator;

/**
 * Reads the app-wide settings. The first value found wins: Spring properties,
 * a servlet init parameter, a system property, then an environment variable.
 */
final class AsyntaiSettings {

    static final String WIDGET_ID_PROPERTY = "asyntai.widget-id";
    static final String WIDGET_ID_ENV = "ASYNTAI_WIDGET_ID";
    static final String SCRIPT_URL_PROPERTY = "asyntai.script-url";
    static final String SCRIPT_URL_ENV = "ASYNTAI_SCRIPT_URL";

    private AsyntaiSettings() {
    }

    static String widgetId(VaadinService service, UnaryOperator<String> environment) {
        return read(service, environment, WIDGET_ID_PROPERTY, WIDGET_ID_ENV);
    }

    static String scriptUrl(VaadinService service, UnaryOperator<String> environment) {
        String url = read(service, environment, SCRIPT_URL_PROPERTY, SCRIPT_URL_ENV);
        return url != null ? url : WidgetScript.DEFAULT_SCRIPT_URL;
    }

    private static String read(VaadinService service, UnaryOperator<String> environment, String property,
            String variable) {
        String value = clean(fromSpring(service, property));
        if (value == null && service != null && service.getDeploymentConfiguration() != null) {
            value = clean(service.getDeploymentConfiguration().getStringProperty(property, null));
        }
        if (value == null) {
            value = clean(System.getProperty(property));
        }
        if (value == null) {
            value = clean(environment.apply(variable));
        }
        return value;
    }

    /**
     * Spring is optional for a Vaadin app, so the add-on cannot depend on it.
     * In a Spring app the instantiator returns the Environment bean, which also
     * reads application.properties. In any other app this returns null.
     */
    private static String fromSpring(VaadinService service, String property) {
        if (service == null || service.getInstantiator() == null) {
            return null;
        }
        try {
            ClassLoader loader = service.getClassLoader();
            Class<?> type = Class.forName("org.springframework.core.env.Environment", false, loader);
            Object environment = service.getInstantiator().getOrCreate(type);
            Object value = type.getMethod("getProperty", String.class).invoke(environment, property);
            return value == null ? null : value.toString();
        } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
            return null;
        }
    }

    private static String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
