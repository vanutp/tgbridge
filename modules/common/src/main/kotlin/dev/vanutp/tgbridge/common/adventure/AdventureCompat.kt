package dev.vanutp.tgbridge.common.adventure

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.event.ClickEvent

private val isOldAdventure = try {
    TranslatableComponent::class.java.getMethod("args")
    true
} catch (_: NoSuchMethodException) {
    false
}

private val argsGetter = if (isOldAdventure) {
    TranslatableComponent::class.java.getMethod("args")
} else {
    null
}

private val argsSetter = if (isOldAdventure) {
    TranslatableComponent::class.java.getMethod("args", List::class.java)
} else {
    null
}

@Suppress("UNCHECKED_CAST")
fun TranslatableComponent.args(): List<Component> =
    if (argsGetter != null) {
        argsGetter.invoke(this) as List<Component>
    } else {
        this.arguments().map { it.asComponent() }
    }

fun TranslatableComponent.args(args: List<Component>): TranslatableComponent =
    if (argsSetter != null) {
        argsSetter.invoke(this, args) as TranslatableComponent
    } else {
        this.arguments(args)
    }

private val OLD_OPEN_URL = if (isOldAdventure) {
    ClickEvent.Action::class.java.getField("OPEN_URL").get(null)
} else {
    null
}

fun ClickEvent<*>.isOpenUrl(): Boolean = if (isOldAdventure) {
    this.action() == OLD_OPEN_URL
} else {
    this.action() is ClickEvent.Action.OpenUrl
}

private val clickEventValueGetter = if (isOldAdventure) {
    ClickEvent::class.java.getMethod("value")
} else {
    null
}

fun ClickEvent<*>.value(): String = if (clickEventValueGetter != null) {
    clickEventValueGetter.invoke(this) as String
} else {
    when (val payload = payload()) {
        is ClickEvent.Payload.Text -> payload.value()
        is ClickEvent.Payload.Int -> payload.integer().toString()
        else -> ""
    }
}

// old and new adventure have different return types for ComponentBuilder::build (BuildableComponent vs Component)
private val textComponentBuild = TextComponent.Builder::class.java.getMethod("build")

fun TextComponent.Builder.tgbridgeBuild(): TextComponent =
    textComponentBuild.invoke(this) as TextComponent
