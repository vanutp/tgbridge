package dev.vanutp.tgbridge.fabric.image

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.nbt.StringNbtReader
import net.minecraft.registry.RegistryOps
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text


class HoverImageContent internal constructor(private val id: String) {
    fun getId() = id

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other != null && this.javaClass == other.javaClass) {
            return this.id == (other as HoverImageContent).id
        }
        return false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        val CODEC = RecordCodecBuilder.create<HoverImageContent> { instance ->
            instance.group(
                Codec.STRING.fieldOf("id").forGetter { it.id },
            )
                .apply(instance, ::HoverImageContent)
        }

        val SHOW_IMAGE = HoverEvent.Action(
            "show_image", true, CODEC, ::legacySerializer
        )

        private fun legacySerializer(text: Text, ops: RegistryOps<*>?): DataResult<HoverImageContent> {
            try {
                val nbtCompound = StringNbtReader.parse(text.string)
                val id = nbtCompound.getString("id")
                return DataResult.success(HoverImageContent(id))
            } catch (e: Exception) {
                return DataResult.error { "Failed to parse image content: " + e.message }
            }
        }
    }
}
