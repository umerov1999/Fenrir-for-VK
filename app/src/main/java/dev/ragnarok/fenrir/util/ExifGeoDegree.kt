package dev.ragnarok.fenrir.util

import androidx.exifinterface.media.ExifInterface
import java.util.regex.Pattern

class ExifGeoDegree(exif: ExifInterface) {
    var isValid = false
    var latitude: Double? = null
    var longitude: Double? = null
    private fun convertToDegree(stringDMS: String): Double {
        val DMS = stringDMS.split(Pattern.compile(","), 3).toTypedArray()
        val stringD = DMS[0].split(Pattern.compile("/"), 2).toTypedArray()
        val D0 = java.lang.Double.valueOf(stringD[0])
        val D1 = java.lang.Double.valueOf(stringD[1])
        val FloatD = D0 / D1
        val stringM = DMS[1].split(Pattern.compile("/"), 2).toTypedArray()
        val M0 = java.lang.Double.valueOf(stringM[0])
        val M1 = java.lang.Double.valueOf(stringM[1])
        val FloatM = M0 / M1
        val stringS = DMS[2].split(Pattern.compile("/"), 2).toTypedArray()
        val S0 = java.lang.Double.valueOf(stringS[0])
        val S1 = java.lang.Double.valueOf(stringS[1])
        val FloatS = S0 / S1
        return FloatD + FloatM / 60 + FloatS / 3600
    }

    override fun toString(): String {
        return latitude.toString() + ", " + longitude
    }

    init {
        val attrLATITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
        val attrLATITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
        val attrLONGITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
        val attrLONGITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
        if (attrLATITUDE != null
            && attrLATITUDE_REF != null
            && attrLONGITUDE != null
            && attrLONGITUDE_REF != null
        ) {
            isValid = true
            latitude = if (attrLATITUDE_REF == "N") {
                convertToDegree(attrLATITUDE)
            } else {
                0 - convertToDegree(attrLATITUDE)
            }
            longitude = if (attrLONGITUDE_REF == "E") {
                convertToDegree(attrLONGITUDE)
            } else {
                0 - convertToDegree(attrLONGITUDE)
            }
        }
    }
}