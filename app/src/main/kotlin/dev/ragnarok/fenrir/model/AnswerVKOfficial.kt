package dev.ragnarok.fenrir.model

import androidx.annotation.IntDef
import com.google.gson.annotations.SerializedName
import kotlin.math.abs

class AnswerVKOfficial {
    var footer: String? = null
    var header: String? = null
    var text: String? = null
    var iconURL: String? = null
    var iconType: String? = null
    var hide_query: String? = null
    var time: Long = 0
    var images: ArrayList<ImageAdditional>? = null
    var attachments: ArrayList<Photo>? = null
    var action: Action? = null
    fun getImage(prefSize: Int): ImageAdditional? {
        var result: ImageAdditional? = null
        if (images.isNullOrEmpty()) return null
        for (image in images.orEmpty()) {
            if (result == null) {
                result = image
                continue
            }
            if (abs(image.calcAverageSize() - prefSize) < abs(result.calcAverageSize() - prefSize)) {
                result = image
            }
        }
        return result
    }

    @IntDef(Action_Types.MESSAGE, Action_Types.URL)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Action_Types {
        companion object {
            const val MESSAGE = 0
            const val URL = 1
        }
    }

    abstract class Action {
        @Action_Types
        abstract fun getType(): Int
    }

    class ActionMessage(private val peerId: Int, private val messageId: Int) : Action() {
        fun getPeerId(): Int {
            return peerId
        }

        fun getMessageId(): Int {
            return messageId
        }

        override fun getType(): Int {
            return Action_Types.MESSAGE
        }
    }

    class ActionURL(private val url: String?) : Action() {
        fun getUrl(): String? {
            return url
        }

        override fun getType(): Int {
            return Action_Types.URL
        }
    }

    class ImageAdditional {
        @SerializedName("url")
        var url: String? = null

        @SerializedName("width")
        var width = 0

        @SerializedName("height")
        var height = 0
        fun calcAverageSize(): Int {
            return (width + height) / 2
        }
    }

    class Attachment {
        @SerializedName("type")
        var type: String? = null

        @SerializedName("object_id")
        var object_id: String? = null
    }
}