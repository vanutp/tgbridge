package dev.vanutp.tgbridge.paper

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.Serializable

@Serializable
data class PaperCompatConfig(
    @YamlComment(
        "Setting this to true might fix compatibility with",
        "some plugins that alter chat messages.",
        "By default, the legacy listener is only used when",
        "known incompatible plugins are detected (currently Chatty)",
        "Default value: null (auto)"
    )
        val useLegacyChatListener: Boolean? = null,
    //    @YamlComment(
    //        "Changing this might fix compatibility with some plugins",
    //        "Possible values: null (auto), LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR",
    //        "Currently, if this is set to null, the priority is always LOWEST,",
    //        "but this might change in the future",
    //        "Default value: null (auto)"
    //    )
    //    val chatListenerPriority: EventPriority? = null,
)


@Serializable
data class PaperConfig(
    @YamlComment(
        "I aim for out of the box compatibility with most other plugins,",
        "so changing these settings should not be needed.",
        "If changing the settings below helped you, please open an issue at",
        "https://github.com/vanutp/tgbridge/issues"
    )
    val compat: PaperCompatConfig = PaperCompatConfig(),
)
