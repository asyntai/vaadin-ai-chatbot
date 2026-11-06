package org.vaadin.addons.asyntai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.VaadinService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

class AsyntaiSettingsTest {

    private static final String PROPERTY = AsyntaiSettings.WIDGET_ID_PROPERTY;

    @AfterEach
    void clearSystemProperty() {
        System.clearProperty(PROPERTY);
    }

    @Test
    void noValueAnywhereGivesNull() {
        assertNull(AsyntaiSettings.widgetId(service(null, null), name -> null));
    }

    @Test
    void readsTheEnvironmentVariable() {
        assertEquals("asyntai_env",
                AsyntaiSettings.widgetId(service(null, null), name -> "ASYNTAI_WIDGET_ID".equals(name) ? "asyntai_env" : null));
    }

    @Test
    void systemPropertyWinsOverTheEnvironmentVariable() {
        System.setProperty(PROPERTY, "asyntai_system");
        assertEquals("asyntai_system", AsyntaiSettings.widgetId(service(null, null), name -> "asyntai_env"));
    }

    @Test
    void initParameterWinsOverTheSystemProperty() {
        System.setProperty(PROPERTY, "asyntai_system");
        assertEquals("asyntai_init", AsyntaiSettings.widgetId(service("asyntai_init", null), name -> null));
    }

    @Test
    void springPropertyWinsOverEverythingElse() {
        StandardEnvironment spring = new StandardEnvironment();
        spring.getPropertySources().addFirst(new MapPropertySource("test", Map.of(PROPERTY, "asyntai_spring")));
        assertEquals("asyntai_spring", AsyntaiSettings.widgetId(service("asyntai_init", spring), name -> "asyntai_env"));
    }

    @Test
    void instantiatorThatCannotCreateTheEnvironmentIsIgnored() {
        VaadinService service = service(null, null);
        when(service.getInstantiator().getOrCreate(any())).thenThrow(new IllegalStateException("no such bean"));
        assertEquals("asyntai_env", AsyntaiSettings.widgetId(service, name -> "asyntai_env"));
    }

    @Test
    void blankValuesCountAsMissingAndValuesAreTrimmed() {
        System.setProperty(PROPERTY, "   ");
        assertEquals("asyntai_env", AsyntaiSettings.widgetId(service(" ", null), name -> "  asyntai_env \n"));
    }

    @Test
    void scriptUrlHasADefaultAndCanBeChanged() {
        assertEquals(WidgetScript.DEFAULT_SCRIPT_URL, AsyntaiSettings.scriptUrl(service(null, null), name -> null));
        assertEquals("https://example.com/w.js", AsyntaiSettings.scriptUrl(service(null, null),
                name -> "ASYNTAI_SCRIPT_URL".equals(name) ? "https://example.com/w.js" : null));
    }

    @Test
    void worksWithoutAService() {
        assertEquals("asyntai_env", AsyntaiSettings.widgetId(null, name -> "asyntai_env"));
    }

    static VaadinService service(String initParameter, Environment spring) {
        VaadinService service = mock(VaadinService.class);
        Instantiator instantiator = mock(Instantiator.class);
        DeploymentConfiguration configuration = mock(DeploymentConfiguration.class);
        when(service.getInstantiator()).thenReturn(instantiator);
        when(service.getDeploymentConfiguration()).thenReturn(configuration);
        when(service.getClassLoader()).thenReturn(AsyntaiSettingsTest.class.getClassLoader());
        when(instantiator.getOrCreate(Environment.class)).thenReturn(spring);
        when(configuration.getStringProperty(PROPERTY, null)).thenReturn(initParameter);
        return service;
    }
}
