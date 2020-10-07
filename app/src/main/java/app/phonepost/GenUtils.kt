package app.phonepost



import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import com.google.gson.Gson
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object GenUtil {
    val PATTERN = "yyyy-MM-dd'T'HH:mm:ss"

    fun dpToPixel(dp: Float): Int {
        val r = Resources.getSystem()
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.displayMetrics).toInt()
    }

    fun pxFromDp(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    fun toISO8601UTC(date: Date): String {
        val tz = TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat(PATTERN)
        df.setTimeZone(tz)
        return df.format(date)
    }

    fun fromISO8601UTC(dateStr: String): Date? {
        val tz = TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat(PATTERN)
        df.setTimeZone(tz)

        try {
            return df.parse(dateStr)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return null
    }

    fun listToJson(value: List<String>): String {

        return Gson().toJson(value)
    }

    fun jsonToList(value: String): List<String>? {

        val objects = Gson().fromJson(value, Array<String>::class.java) as Array<String>
        val list = objects.toList()
        return list
    }

    fun getReadableFileSize(size: Long): String {
        if (size <= 0) {
            return "0"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(
            size / Math.pow(
                1024.0,
                digitGroups.toDouble()
            )
        ) + " " + units[digitGroups]
    }
}

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

val String.colorCode: Int
    get() = (this.toLong() % 6).toInt()

fun getUserShortName(name: String): String {
    var x = ""
    val list = name.split(" ")
    if(list.size>0)
        x += list.get(0).first()
    if(list.size>1)
        x += list.get(1).first()
    return x
}

fun median(l: List<Float>) = l.sorted().let { (it[it.size / 2] + it[(it.size - 1) / 2]) / 2 }
