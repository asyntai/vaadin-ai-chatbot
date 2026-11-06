# Asyntai AI Chatbot for Vaadin

Add the [Asyntai](https://asyntai.com) AI chat assistant to your Vaadin app. It
answers your users' questions from your own content, such as your help center,
docs and FAQs, in more than 80 languages, day and night.

Works with Vaadin 24 and 25, on Java 17 or later, with or without Spring Boot.

## Install

Add the Vaadin add-ons repository and the dependency to `pom.xml`:

```xml
<repositories>
    <repository>
        <id>vaadin-addons</id>
        <url>https://maven.vaadin.com/vaadin-addons</url>
    </repository>
</repositories>

<dependency>
    <groupId>org.vaadin.addons.asyntai</groupId>
    <artifactId>vaadin-ai-chatbot</artifactId>
    <version>1.0.0</version>
</dependency>
```

Then choose one of the two ways below.

### On every view, with no code

In a Spring Boot app, add your widget ID to `application.properties`:

```properties
asyntai.widget-id=asyntai_1a2b3c4d5e6f
```

In any other app, set the environment variable `ASYNTAI_WIDGET_ID`, the system
property `asyntai.widget-id`, or a servlet init parameter with that name.

Start the app. The chat bubble is on every view.

### From your Java code

Add the component to your main layout or to one view:

```java
import org.vaadin.addons.asyntai.AsyntaiChat;

add(new AsyntaiChat("asyntai_1a2b3c4d5e6f"));
```

The component has no visible element of its own. The chat bubble sits in the
corner of the page and stays open when the user moves between views.

## Where the widget ID comes from

Open the Install page in your [Asyntai dashboard](https://asyntai.com/dashboard).
The widget ID is the value of `data-asyntai-id` in the snippet shown there. It
starts with `asyntai_`.

Creating an account is free, and the Free plan is enough to try the widget.

## Personal answers

The assistant can know who is asking. Pass the details you want it to use:

```java
add(new AsyntaiChat("asyntai_1a2b3c4d5e6f")
        .setUserContext(Map.of(
                "name", user.getName(),
                "plan", user.getPlan())));
```

If the widget ID is already in `application.properties`, use `new AsyntaiChat()`.
User context is part of the Standard and Pro plans.

## Options

| Setting | What it does |
| --- | --- |
| `asyntai.widget-id` or `ASYNTAI_WIDGET_ID` | The widget ID for the whole app |
| `asyntai.script-url` or `ASYNTAI_SCRIPT_URL` | Advanced. Load the widget from another URL |
| `new AsyntaiChat("asyntai_...")` | A widget ID for this component |
| `.setUserContext(Map)` | Data about the signed-in user |
| `.setScriptUrl("...")` | Advanced. Load the widget from another URL |

A wrong widget ID does not stop your app. The add-on writes a warning to the log
and shows no chat bubble.

## How it works

The add-on adds one small script to the page. The script loads the widget after
your page has finished loading, so your app always renders first. It adds the
widget only once, also when you use both ways together.

## Tests

```bash
mvn test
mvn test -Dvaadin.version=25.3.5
```

## Documentation

https://asyntai.com/documentation/integrations/vaadin/

## License

MIT
