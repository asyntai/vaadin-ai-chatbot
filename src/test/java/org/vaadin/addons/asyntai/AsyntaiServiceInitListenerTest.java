package org.vaadin.addons.asyntai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.communication.IndexHtmlRequestListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

class AsyntaiServiceInitListenerTest {

    @Test
    void vaadinCanFindTheListener() {
        boolean found = ServiceLoader.load(VaadinServiceInitListener.class).stream()
                .anyMatch(provider -> provider.type() == AsyntaiServiceInitListener.class);
        assertTrue(found);
    }

    @Test
    void noWidgetIdAddsNothing() {
        assertEquals(0, listeners(null).size());
    }

    @Test
    void wrongWidgetIdAddsNothingAndDoesNotThrow() {
        assertEquals(0, listeners("not-a-widget-id").size());
        assertEquals(0, listeners("asyntai_\"><script>alert(1)</script>").size());
    }

    @Test
    void validWidgetIdAddsTheLoaderToTheEndOfTheBody() {
        List<IndexHtmlRequestListener> listeners = listeners("asyntai_abc");
        assertEquals(1, listeners.size());

        Document document = Jsoup.parse("<!doctype html><html><head></head><body><div id='outlet'></div></body></html>");
        IndexHtmlResponse response = mock(IndexHtmlResponse.class);
        when(response.getDocument()).thenReturn(document);
        listeners.get(0).modifyIndexHtmlResponse(response);

        Element last = document.body().children().last();
        assertEquals("script", last.tagName());
        assertTrue(last.hasAttr("data-asyntai-loader"));
        assertTrue(last.html().contains("\"asyntai_abc\""));
        assertTrue(last.html().contains(WidgetScript.DEFAULT_SCRIPT_URL));
        // jsoup must print the script as it is, without HTML entities.
        assertTrue(document.outerHtml().contains("readyState===\"complete\""));
    }

    private static List<IndexHtmlRequestListener> listeners(String widgetId) {
        ServiceInitEvent event = new ServiceInitEvent(AsyntaiSettingsTest.service(null, null));
        new AsyntaiServiceInitListener(name -> "ASYNTAI_WIDGET_ID".equals(name) ? widgetId : null).serviceInit(event);
        return event.getAddedIndexHtmlRequestListeners().collect(Collectors.toList());
    }
}
