package dev.ragnarok.fenrir.module

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import androidx.annotation.Keep
import java.nio.ByteBuffer

@Keep
class ZXingWrapper {
    // Enumerates barcode formats known to this package.
    // Note that this has to be kept synchronized with native (C++/JNI) side.
    enum class Format(val value: Int) {
        NONE(0x0000),
        ALL(0x2A2A),
        ALL_READABLE(0x722A),
        ALL_CREATABLE(0x772A),
        ALL_LINEAR(0x6C2A),
        ALL_MATRIX(0x6D2A),
        ALL_GS1(0x472A),
        ALL_RETAIL(0x522A),
        ALL_INDUSTRIAL(0x492A),
        CODABAR(0x2046),
        CODE_39(0x2041),
        CODE_39_STD(0x7341),
        CODE_39_EXT(0x6541),
        CODE_32(0x3241),
        PZN(0x7041),
        CODE_93(0x2047),
        CODE_128(0x2043),
        ITF(0x2049),
        ITF_14(0x3449),
        DATA_BAR(0x2065),
        DATA_BAR_OMNI(0x6F65),
        DATA_BAR_STK(0x7365),
        DATA_BAR_STK_OMNI(0x4F65),
        DATA_BAR_LTD(0x6C65),
        DATA_BAR_EXP(0x6565),
        DATA_BAR_EXP_STK(0x4565),
        EAN_UPC(0x2045),
        EAN_13(0x3145),
        EAN_8(0x3845),
        EAN_5(0x3545),
        EAN_2(0x3245),
        ISBN(0x6945),
        UPC_A(0x6145),
        UPC_E(0x6545),
        OTHER_BARCODE(0x2058),
        DX_FILM_EDGE(0x7858),
        PDF_417(0x204C),
        COMPACT_PDF_417(0x634C),
        MICRO_PDF_417(0x6D4C),
        AZTEC(0x207A),
        AZTEC_CODE(0x637A),
        AZTEC_RUNE(0x727A),
        QR_CODE(0x2051),
        QR_CODE_MODEL_1(0x3151),
        QR_CODE_MODEL_2(0x3251),
        MICRO_QR_CODE(0x6D51),
        RMQR_CODE(0x7251),
        DATA_MATRIX(0x2064),
        MAXI_CODE(0x2055),
    }

    enum class ContentType {
        TEXT, BINARY, MIXED, GS1, ISO15434, UNKNOWN_ECI
    }

    enum class Binarizer {
        LOCAL_AVERAGE, GLOBAL_HISTOGRAM, FIXED_THRESHOLD, BOOL_CAST
    }

    enum class EanAddOnSymbol {
        IGNORE, READ, REQUIRE
    }

    enum class TextMode {
        PLAIN, ECI, HRI, HEX, HEX_ECI
    }

    enum class ErrorType {
        NONE, FORMAT, CHECKSUM, UNSUPPORTED
    }

    enum class ErrorCorrectionLevel {
        LOW, MEDIUM, QUALITY, HIGH, INVALID
    }

    enum class CharacterSet {
        UNKNOWN,
        ASCII, ISO8859_1, ISO8859_2, ISO8859_3, ISO8859_4, ISO8859_5, ISO8859_6, ISO8859_7, ISO8859_8, ISO8859_9,
        ISO8859_10, ISO8859_11, ISO8859_13, ISO8859_14, ISO8859_15, ISO8859_16, CP437, CP1250, CP1251, CP1252,
        CP1256, SHIFT_JIS, BIG5, GB2312, GB18030, EUC_JP, EUC_KR, UTF16BE, UTF8, UTF16LE, UTF32BE, UTF32LE, BINARY
    }

    data class ReaderOptions(
        var formats: Set<Format> = setOf(),
        var tryHarder: Boolean = false,
        var tryRotate: Boolean = false,
        var tryInvert: Boolean = false,
        var tryDownscale: Boolean = false,
        var isPure: Boolean = false,
        var binarizer: Binarizer = Binarizer.LOCAL_AVERAGE,
        var downscaleFactor: Int = 3,
        var downscaleThreshold: Int = 500,
        var minLineCount: Int = 2,
        var maxNumberOfSymbols: Int = 0xff,
        var returnErrors: Boolean = false,
        var eanAddOnSymbol: EanAddOnSymbol = EanAddOnSymbol.IGNORE,
        var textMode: TextMode = TextMode.HRI
    )

    data class QRWriterOptions(
        var encoding: CharacterSet = CharacterSet.UNKNOWN,
        var errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.LOW,
        var version: Int = 0,
        var useGs1Format: Boolean = false,
        var maskPattern: Int = -1
    )

    data class Error(
        val type: ErrorType,
        val message: String
    )

    data class Position(
        val topLeft: Point,
        val topRight: Point,
        val bottomRight: Point,
        val bottomLeft: Point,
        val orientation: Double
    )

    data class ReaderResult(
        val format: Format,
        val text: String?,
        val contentType: ContentType,
        val position: Position,
        val orientation: Int,
        val ecLevel: String?,
        val symbologyIdentifier: String?,
        private val extraJsonString: String?,
        val sequenceSize: Int,
        val sequenceIndex: Int,
        val sequenceId: String?,
        val readerInit: Boolean,
        val lineCount: Int,
        val error: Error?
    ) {
        // Additional symbology-specific metadata (e.g. "UPCE" for the original UPC-E text), keyed by name.
        val extra: Map<String, Any?> by lazy {
            val json = extraJsonString?.takeIf { it.isNotEmpty() }?.let { org.json.JSONObject(it) }
                ?: org.json.JSONObject()
            json.keys().asSequence()
                .associateWith { json.get(it).takeIf { v -> v != org.json.JSONObject.NULL } }
        }
    }

    data class ByteMatrix(
        val width: Int,
        val height: Int,
        val data: ByteArray
    ) {
        operator fun get(x: Int, y: Int): Byte {
            return data[y * width + x]
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ByteMatrix

            if (width != other.width) return false
            if (height != other.height) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = width
            result = 31 * result + height
            result = 31 * result + data.contentHashCode()
            return result
        }
    }

    fun generateQR(
        content: String,
        width: Int,
        height: Int
    ): ByteMatrix? {
        return generateQRNative(content, width, height, qrWriterOptions)
    }

    fun readYBuffer(
        yBuffer: ByteBuffer,
        pixelStride: Int,
        cropRect: Rect = Rect(),
        width: Int,
        height: Int,
        rotation: Int = 0,
        flip: Boolean = false
    ): List<ReaderResult>? {
        return readYBufferNative(
            yBuffer,
            pixelStride,
            cropRect.left,
            cropRect.top,
            width, height,
            cropRect.width(),
            cropRect.height(),
            rotation,
            flip,
            readerOptions
        )
    }

    fun readBitmap(
        bitmap: Bitmap, cropRect: Rect = Rect(), rotation: Int = 0
    ): List<ReaderResult> {
        return readBitmapNative(
            bitmap,
            cropRect.left,
            cropRect.top,
            cropRect.width(),
            cropRect.height(),
            rotation,
            readerOptions
        )
    }

    private external fun generateQRNative(
        content: String,
        width: Int,
        height: Int,
        options: QRWriterOptions
    ): ByteMatrix?

    private external fun readYBufferNative(
        yBuffer: ByteBuffer,
        pixelStride: Int,
        left: Int,
        top: Int,
        origWidth: Int,
        origHeight: Int,
        width: Int,
        height: Int,
        rotation: Int,
        flip: Boolean,
        options: ReaderOptions
    ): List<ReaderResult>?

    private external fun readBitmapNative(
        bitmap: Bitmap,
        left: Int,
        top: Int,
        width: Int,
        height: Int,
        rotation: Int,
        options: ReaderOptions
    ): List<ReaderResult>

    var readerOptions: ReaderOptions = ReaderOptions()
    var qrWriterOptions: QRWriterOptions = QRWriterOptions()
}