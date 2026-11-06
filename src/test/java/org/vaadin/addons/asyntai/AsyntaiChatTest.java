package org.vaadin.addons.asyntai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class AsyntaiChatTest {

    private UI ui;

    @BeforeEach
    void setUp() {
        VaadinService service = AsyntaiSettingsTest.service(null, null);
        VaadinService.setCurrent(service);
        VaadinSession session = mock(VaadinSession.class);
        when(session.hasLock()).thenReturn(true);
        when(session.getService()).thenReturn(service);
        ui = new UI();
        ui.getInternals().setSession(session);
    }

    @AfterEach
    void tearDown() {
        VaadinService.setCurrent(null);
        System.clearProperty(AsyntaiSettings.WIDGET_ID_PROPERTY);
    }

    @Test
    void hasNoVisibleElement() {
        assertEquals("none", new AsyntaiChat("asyntai_abc").getElement().getStyle().get("display"));
    }

    @Test
    void attachRunsTheLoaderOnce() {
        ui.add(new AsyntaiChat("asyntai_abc"));
        List<String> scripts = scripts();
        assertEquals(1, scripts.size());
        assertTrue(scripts.get(0).contains("\"asyntai_abc\""));
        assertTrue(scripts.get(0).contains(WidgetScript.DEFAULT_SCRIPT_URL));
    }

    @Test
    void nothingRunsBeforeAttach() {
        new AsyntaiChat("asyntai_abc");
        assertEquals(0, scripts().size());
    }

    @Test
    void wrongWidgetIdRunsNothingAndDoesNotThrow() {
        ui.add(new AsyntaiChat("not-a-widget-id"));
        assertEquals(0, scripts().size());
    }

    @Test
    void noWidgetIdAnywhereRunsNothingAndDoesNotThrow() {
        ui.add(new AsyntaiChat());
        assertEquals(0, scripts().size());
    }

    @Test
    void noArgumentConstructorUsesTheAppWideWidgetId() {
        System.setProperty(AsyntaiSettings.WIDGET_ID_PROPERTY, "asyntai_appwide");
        ui.add(new AsyntaiChat());
        assertTrue(scripts().get(0).contains("\"asyntai_appwide\""));
    }

    @Test
    void userContextIsSetBeforeTheLoader() {
        ui.add(new AsyntaiChat("asyntai_abc").setUserContext(Map.of("name", "</script>Ana")));
        List<String> scripts = scripts();
        assertEquals(2, scripts.size());
        assertTrue(scripts.get(0).startsWith("window.Asyntai=window.Asyntai||{}"));
        assertTrue(scripts.get(0).contains("\\u003c/script\\u003eAna"));
        assertFalse(scripts.get(0).contains("</script>"));
        assertTrue(scripts.get(1).contains("data-asyntai-id"));
    }

    @Test
    void emptyUserContextAddsNoScript() {
        ui.add(new AsyntaiChat("asyntai_abc").setUserContext(Map.of()));
        assertEquals(1, scripts().size());
    }

    @Test
    void scriptUrlCanBeChanged() {
        ui.add(new AsyntaiChat("asyntai_abc").setScriptUrl("https://example.com/w.js"));
        assertTrue(scripts().get(0).contains("\"https://example.com/w.js\""));
    }

    private List<String> scripts() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        return ui.getInternals().dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .map(invocation -> invocation.getExpression())
                .filter(expression -> expression.contains("Asyntai") || expression.contains("asyntai"))
                .collect(Collectors.toList());
    }
}
