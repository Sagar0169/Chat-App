package com.example.chatapp.utilities.zoomimageview

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.*
import android.graphics.Bitmap.CompressFormat
import android.graphics.drawable.ColorDrawable
import android.media.ExifInterface
import android.net.ConnectivityManager
import android.net.Uri
import android.os.*
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.text.*
import android.text.format.DateUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.Base64
import android.util.Base64.*
import android.util.Log
import android.util.Patterns
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.utilities.zoomimageview.CryptUniqueIdGenerator.getCryptUniqueId
import com.google.android.material.snackbar.Snackbar

import java.io.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.HttpURLConnection
import java.net.URL
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.text.*
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.*
import kotlin.math.*


enum class BottomSheetType {
    DISTRICT,
    MEDAL,
    FEDERATION,
    SPORT
}

enum class QualificationUploadDocumentType {
    QUALIFICATION_DOCUMENT,
    LETTER_OF_ATTAINMENT,
    VERIFICATION_OF_COMPETENCY
}

object Utility {



    fun setLocale(activity: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources: Resources = activity.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        savePreferencesString(activity, "locale", languageCode)
    }

    fun encrypt(plainText: String, key: SecretKeySpec): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(iv + encrypted, Base64.DEFAULT)
    }

    fun decrypt(encryptedText: String, key: SecretKeySpec): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val encryptedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
        val iv = encryptedBytes.copyOfRange(0, cipher.blockSize)
        val encrypted = encryptedBytes.copyOfRange(cipher.blockSize, encryptedBytes.size)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        return String(cipher.doFinal(encrypted))
    }


//    fun showConfirmationAlertDialog(
//        context: Context,
//        callback: DialogCallback
//    ) {
//        val dialog = Dialog(context, android.R.style.Theme_Translucent_NoTitleBar)
//        dialog.setCancelable(false)
//        dialog.setCanceledOnTouchOutside(false)
//        dialog.window!!.setLayout(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.WRAP_CONTENT
//        )
//        dialog.window?.setGravity(Gravity.CENTER)
//        val lp: WindowManager.LayoutParams = dialog.window!!.attributes
//        lp.dimAmount = 0.5f
//        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.window
//        dialog.window?.attributes = lp
//        dialog.setContentView(R.layout.item_confirmation_dialog)
//        val tvCancel = dialog.findViewById(R.id.tvCancel) as TextView
//        val ivConfirm = dialog.findViewById(R.id.tvConfirm) as TextView
//
//        ivConfirm.setOnClickListener {
//            dialog.dismiss()
//            callback.onYes()
//        }
//        tvCancel.setOnClickListener {
//            dialog.dismiss()
//        }
//        dialog.show()
//    }

    fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val theta = lon1 - lon2
        var dist = (sin(deg2rad(lat1))
                * sin(deg2rad(lat2))
                + (cos(deg2rad(lat1))
                * cos(deg2rad(lat2))
                * cos(deg2rad(theta))))
        dist = acos(dist)
        dist = rad2deg(dist)
        dist *= 60 * 1.1515
        return dist
    }

    fun getDecryptedString(value: String?, cryptKey: String?): String? {
        var newValue: String? = ""
        try {
            if (!TextUtils.isEmpty(value) && !TextUtils.isEmpty(cryptKey)) {
                newValue = CryptLib().DecryptString(value, cryptKey)
            } else {
                // Handle empty or null value or key, return a default value or log a message.
                newValue = "Decryption failed: Empty value or key"
                Log.e("UtilsCommon setStringEncrypted", "Decryption failed: Empty value or key")
            }
        } catch (e: Exception) {
            Log.e("UtilsCommon setStringEncrypted", "Decryption error: " + e.message)
            // Handle the exception or log the error message.
        }
        return newValue
    }


    fun getEncryptedString(value: String?, cryptKey: String?): String? {
        var newValue: String? = ""
        try {
            newValue =
                if (!TextUtils.isEmpty(value) && !TextUtils.isEmpty(cryptKey)) CryptLib().EncryptString(
                    value,
                    cryptKey
                ) else ""
        } catch (e: InvalidKeyException) {
            Log.e("UtilsCommon  setStringEncrypted", e.toString())
        } catch (e: NoSuchPaddingException) {
            Log.e("UtilsCommon  setStringEncrypted", e.toString())
        } catch (e: NoSuchAlgorithmException) {
            Log.e("UtilsCommon  setStringEncrypted", e.toString())
        } catch (e: BadPaddingException) {
            Log.e("UtilsCommon  setStringEncrypted", e.toString())
        } catch (e: IllegalBlockSizeException) {
            Log.e("UtilsCommon  setStringEncrypted", e.toString())
        } catch (e: InvalidAlgorithmParameterException) {
            Log.e("UtilsCommon  setStringEncrypted", e.toString())
        } catch (e: UnsupportedEncodingException) {
            Log.e("UtilsCommon  setStringEncrypted", e.toString())
        }
        return newValue
    }

    fun getUniqueIDWithRandomString(): String? {
        return getCryptUniqueId() + "-" + randomString()
    }

    fun randomString(): String {
        val generator = SecureRandom()
        val randomStringBuilder = StringBuilder()
        val randomLength = 5
        var tempChar: Int
        val symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        for (i in 0 until randomLength) {
            tempChar = generator.nextInt(symbols.length - 1)
            randomStringBuilder.append(symbols[tempChar])
        }
        return randomStringBuilder.toString()
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }

    fun convertIntoKms(miles: Double): Double {
        return 1.609 * miles
    }

    fun convertIntoMiles(km: Double): Double {
        return km / 1.609
    }

    fun showImageDialog(context: Context,image:String) {
        val dialog = Dialog(context,android.R.style.Theme_Translucent_NoTitleBar)
        dialog.setContentView(R.layout.imagedialog)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setGravity(Gravity.CENTER)
        val lp: WindowManager.LayoutParams = dialog.window!!.attributes
        lp.dimAmount = 0.75f
        dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.attributes = lp
        val dialogImage = dialog.findViewById(R.id.ivImg) as ImageView
        val ivClose = dialog.findViewById(R.id.ivClose) as ImageView

        Log.d("image",image)

        Glide.with(context).load(image)
            .placeholder(R.color.black)
            .into(dialogImage)

        ivClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun dateConvertToEndDate(dateString: String): String {
        return try {
            val originalFormat: DateFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            val targetFormat: DateFormat = SimpleDateFormat("dd MMM, yyyy")
            val date: Date = originalFormat.parse(dateString)

            targetFormat.format(date).toUpperCase()
        } catch (e: Exception) {
            ""
        }
    }
    fun dateConvertToFormat(dateString: String): String {
        return try {
            val originalFormat: DateFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            val targetFormat: DateFormat = SimpleDateFormat("dd MMM yyyy")
            val date: Date = originalFormat.parse(dateString)

            targetFormat.format(date).toUpperCase()
        } catch (e: Exception) {
            ""
        }
    }
    fun dateConvertToString(dateString: String): String {
        return try {
            val originalFormat: DateFormat =
                SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            val targetFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'00:00:00.000'Z'")
            val date: Date = originalFormat.parse(dateString)

            targetFormat.format(date).toUpperCase()
        } catch (e: Exception) {
            ""
        }
    }

    fun dateconvertWithTime(dateString: String): String {
        try {
            val originalFormat: DateFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val targetFormat: DateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
            val date: Date = originalFormat.parse(dateString)
            val formattedDate: String = targetFormat.format(date)
            return formattedDate
        } catch (e: Exception) {
            return ""
        }
    }

    fun isValidPasswordFormat(password: String?): Boolean {
        val passwordREGEX = Pattern.compile(
                "(?=.*[0-9])" +         //at least 1 digit
                "(?=.*[a-z])" +         //at least 1 lower case letter
                "(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                "(?=.*[@#$%^&+=])" +    //at least 1 special character
                ".{8,}"            //at least 8 characters
                )
        return passwordREGEX.matcher(password).matches()
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateDifference(FutureDate: String): String {
        var totalTime = 0
        var unit: String? = null
        try {
            // val netDate = java.sql.Date(java.lang.Long.parseLong(FutureDate) * 1000)
            val parseFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            parseFormat.timeZone = TimeZone.getTimeZone("UTC")
            var date: Date? = null
            try {
                date = parseFormat.parse(FutureDate)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            val oldDate = Date()

            var diff = oldDate.time - date!!.time

            val year = diff / (24 * 60 * 60 * 24 * 365 * 1000L)
            diff -= year * (24 * 60 * 60 * 24 * 365 * 1000L)

            val month = diff / (24 * 60 * 60 * 30 * 1000L)
            diff -= month * (24 * 60 * 60 * 30 * 1000L)

            val week = diff / (24 * 60 * 60 * 7 * 1000)
            diff -= week * (24 * 60 * 60 * 7 * 1000)

            val days = diff / (24 * 60 * 60 * 1000)
            diff -= days * (24 * 60 * 60 * 1000)

            val hours = diff / (60 * 60 * 1000)
            diff -= hours * (60 * 60 * 1000)

            val minutes = diff / (60 * 1000)
            diff -= minutes * (60 * 1000)

            val seconds = diff / 1000

            if (year != 0L) {
                totalTime = year.toInt()
                if (totalTime == 1) {
                    unit = "Yr"
                } else {
                    unit = "Yrs"
                }
                // unit = "day"
            } else if (month != 0L) {
                totalTime = month.toInt()
                if (totalTime == 1) {
                    unit = "Month"
                } else {
                    unit = "Months"
                }
                // unit = "day"
            } else if (week != 0L) {
                totalTime = week.toInt()
                if (totalTime == 1) {
                    unit = "Week"
                } else {
                    unit = "Weeks"
                }
                // unit = "day"
            } else if (days != 0L) {
                totalTime = days.toInt()
                if (totalTime == 1) {
                    unit = "Day"
                } else {
                    unit = "Days"
                }
                // unit = "day"
            } else if (hours != 0L) {
                totalTime = hours.toInt()
                if (totalTime == 1) {
                    unit = "Hr"
                } else {
                    unit = "Hrs"
                }
            } else if (minutes != 0L) {
                totalTime = minutes.toInt()
                unit = "Min"
            } else if (seconds != 0L) {
                totalTime = seconds.toInt()
                unit = "Sec"

            } else {
                totalTime = 1
                unit = "Sec"
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return "$totalTime $unit ago"
    }

    fun getTimeAgo2(dateString: String): String {
        try {
            var dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            //dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            Log.d("Time----", TimeZone.getDefault().toString())
            val pasTime = dateFormat.parse(dateString)
            //dateFormat.timeZone = TimeZone.getDefault();
            val msDiff = Calendar.getInstance().timeInMillis - pasTime.time;
            val calDiff = Calendar.getInstance()
            calDiff.timeInMillis = msDiff
            val msg = "${calDiff.get(Calendar.YEAR) - 1970} year, " +
                    "${calDiff.get(Calendar.MONTH)} months, " +
                    "${calDiff.get(Calendar.DAY_OF_MONTH)} days"
            val years = calDiff.get(Calendar.YEAR) - 1970
            val months = calDiff.get(Calendar.MONTH)
            val days = calDiff.get(Calendar.DAY_OF_MONTH)

            if (years == 0) {
                if (months == 0) {
                    dateFormat = if (days <= 1) {
                        if (DateUtils.isToday(pasTime.time)) {
                            SimpleDateFormat("hh:mm a")
                        } else {
                            SimpleDateFormat("'Yesterday'")
                        }

                    } else {
                        if (days < 7) {
                            SimpleDateFormat("EEEE")
                        } else {
                            SimpleDateFormat("dd MMM")
                        }
                    }
                } else {
                    dateFormat = SimpleDateFormat("dd MMM")
                }
            } else {
                dateFormat = SimpleDateFormat("MM-dd-yyyy")
            }

            return dateFormat?.format(pasTime).toString()
        } catch (e: Exception) {

        }
        return ""
    }

    fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    fun isValidPassword(password: String?): Boolean {
        val pattern: Pattern
        val PASSWORD_PATTERN = "(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z]).{8,15}"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher: Matcher = pattern.matcher(password)
        return matcher.matches()
    }

    object ProcessDialog {
        private var progressDialog: Dialog? = null
        fun start(context: Context) {
            if (!isShowing) {
                if (!(context as Activity).isFinishing) {
                    progressDialog = Dialog(context)
                    progressDialog!!.setCancelable(false)
                    /*     progressDialog.setCancelable(true);
                progressDialog.setCanceledOnTouchOutside(false);*/progressDialog!!.window!!
                        .setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//                    progressDialog!!.setContentView(R.layout.progress_loader)
                    progressDialog!!.show()
                }
            }
        }

        fun dismiss() {
            try {
                if (progressDialog != null && progressDialog!!.isShowing) {
                    progressDialog!!.dismiss()
                }
            } catch (e: IllegalArgumentException) {
                // Handle or log or ignore
            } catch (e: java.lang.Exception) {
                // Handle or log or ignore
            } finally {
                progressDialog = null
            }
        }

        val isShowing: Boolean
            get() = if (progressDialog != null) {
                progressDialog!!.isShowing
            } else {
                false
            }
    }


    fun getDatePatter(dateString: String): List<String>? {
        try {
            return dateString.split("/")
        } catch (e: Exception) {
            return null
        }
    }


    fun savePreferencesBoolean(context: Context, key: String, value: Boolean) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getPreferencesBoolean(context: Context, key: String): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getBoolean(key, false)
    }
    fun getPreferencesInt(context: Context, key: String): Int {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getInt(key, 0)
    }

    fun checkGif(path: String): Boolean {
        return path.matches(GIF_PATTERN.toRegex())
    }

    fun pad(num: Int): String {
        return if (num < 10) "0$num" else "$num"
    }

    fun getBitmapFromURL(src: String): Bitmap? {
        class Converter : AsyncTask<Void, Void, Bitmap>() {
            lateinit var myBitmap: Bitmap
            override fun doInBackground(vararg params: Void?): Bitmap {
                try {
                    val url = URL(src)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    val input = connection.inputStream
                    myBitmap = BitmapFactory.decodeStream(input)
                    return myBitmap
                } catch (e: IOException) {
                }
                return myBitmap
            }
        }
        Converter().execute()
        return null
    }

    fun saveTempBitmap(context: Context, mBitmap: Bitmap): String? {

        val outputDir = context.cacheDir

        var file: File? = null
        try {
            file = File.createTempFile("temp_post_img", ".jpg", outputDir)
            //outputFile.getAbsolutePath();
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val f3 = File(Environment.getExternalStorageDirectory().toString() + "/inpaint/")
        if (!f3.exists()) {
            f3.mkdirs()
        }
        var outStream: OutputStream? = null
        //File file = new File(Environment.getExternalStorageDirectory() + "/inpaint/"+"seconds"+".png");
        try {
            outStream = FileOutputStream(file!!)
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            outStream.flush()
            outStream.close()

            //Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        //getPath( Uri.parse(file.getAbsolutePath()), context);

        return file.absolutePath//getPath( Uri.parse(file.getAbsolutePath()), context);
    }

    @Throws(IOException::class)
    fun rotateRequiredImage(context: Context, img: Bitmap, selectedImage: Uri): Bitmap {

        val input = context.contentResolver.openInputStream(selectedImage)
        val ei: ExifInterface
        if (Build.VERSION.SDK_INT > 23)
            ei = ExifInterface(input!!)
        else
            ei = ExifInterface(selectedImage.path!!)

        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> return rotateImage(img, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> return rotateImage(img, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> return rotateImage(img, 270)
            else -> return img
        }
    }

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        return Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    }


    fun scaleDown(
        realImage: Bitmap, maxImageSize: Float,
        filter: Boolean
    ): Bitmap {
        val ratio = Math.min(
            maxImageSize / realImage.width,
            maxImageSize / realImage.height
        )
        val width = Math.round(ratio * realImage.width)
        val height = Math.round(ratio * realImage.height)

        return Bitmap.createScaledBitmap(
            realImage, width,
            height, filter
        )
    }

    fun getTimeFromTimestamp(timestamp: Long): String {
        val formatter = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        val dateString: String = formatter.format(Date(timestamp))
        return dateString
    }

    fun getDateFromTimestamp(timestamp: Long): String {
        val formatter = SimpleDateFormat("dd/MMM/yy", Locale.ENGLISH)
        val dateString: String = formatter.format(Date(timestamp))
        return dateString
    }

    fun convertSecondsInFormat(sec: Long): String {
        val d = Date(sec * 1000L)
        val df = SimpleDateFormat("HH:mm:ss") // HH for 0-23

        df.timeZone = TimeZone.getTimeZone("GMT")
        val time = df.format(d)
        return time
    }


    private const val GIF_PATTERN = "(.+?)\\.gif$"
    fun getRealPathFromURI(context: Context, contentURI: Uri): String? {
        val result: String?
        val cursor = context.contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

    class SafeClickListener(
        private var defaultInterval: Int = 1000,
        private val onSafeCLick: (View) -> Unit
    ) : View.OnClickListener {
        private var lastTimeClicked: Long = 0
        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
                return
            }
            lastTimeClicked = SystemClock.elapsedRealtime()
            onSafeCLick(v)
        }
    }

    fun isOnline(context: Context): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun changeStatusBar(context: AppCompatActivity) {
        val window = context.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#4D4D4D")
        }
    }

    fun savePreferencesString(context: Context, key: String, value: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun savePreferencesArrayList(context: Context, key: String, value: ArrayList<String>) {
        val set = HashSet<String>()
        set.addAll(value)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putStringSet(key, set)
        editor.apply()
    }


    fun getPreferencesArrayList(context: Context, key: String): ArrayList<String> {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val set = sharedPreferences.getStringSet(key, null)
        val list = ArrayList<String>()
        if (set != null && set.size > 0) {
            list.addAll(set)
        }
        return list
    }

    fun savePreferencesInt(context: Context, key: String, k: Int) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putInt(key, k)
        editor.apply()
    }

    fun saveLanguageInPreference(context: Context, key: String, value: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getLanguageInPreference(context: Context, key: String): String? {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context) as SharedPreferences
        return sharedPreferences.getString(key, "")
    }

    fun getPreferenceString(context: Context, key: String): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getString(key, "").toString()
    }

    fun getPreferenceInt(context: Context, key: String): Int {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getInt(key, 0)
    }

    fun getPreferenceBoolean(context: Context?, key: String): Boolean? {
        val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sp.getBoolean(key, false)
    }



    fun showSnackbar(view: View, message: String) {
        var view = view
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        view = snackbar.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.BOTTOM
        view.layoutParams = params
        view.setBackgroundColor(Color.parseColor("#AC0000"))
        snackbar.show()
    }
    fun showSnackbarSuccess(view: View, message: String) {
        var view = view
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        view = snackbar.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.BOTTOM
        view.layoutParams = params
        view.setBackgroundColor(Color.parseColor("#2DA836"))
        snackbar.show()
    }

    fun getCorrectedDayOrMonth(value: Int): String {
        return if (value < 10) {
            "0$value"
        } else {
            value.toString()
        }
    }

    fun roundOff(value: Double, places: Int): Double {
        //  require(places >= 0)
        var bd = BigDecimal.valueOf(value)
        bd = bd.setScale(1, RoundingMode.HALF_UP)
        return bd.toDouble()
    }

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun hideKeyboardOnOutSideTouch(view: View, activity: Activity) {
        if (view !is EditText) {
            view.setOnTouchListener { v, event ->
                hideKeyboard(activity)
                false
            }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                hideKeyboardOnOutSideTouch(innerView, activity)
            }
        }
    }

    fun convertNumberToGerman(num: Double): String {
        val nf = NumberFormat.getNumberInstance(Locale.GERMAN)
        return nf.format(num)
    }

    fun convertDateEnglish(pre: String, dateString: String, post: String): String {
        val parseFormat = SimpleDateFormat(pre, Locale.ENGLISH)
        parseFormat.timeZone = TimeZone.getTimeZone("UTC") as TimeZone
        var date = Date()
        try {
            date = parseFormat.parse(dateString)
            parseFormat.timeZone = TimeZone.getDefault()
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        @SuppressLint("SimpleDateFormat")
        val format = SimpleDateFormat(post, Locale.ENGLISH)
        format.timeZone = TimeZone.getDefault()
        // Log.e("dateTimeStamp",date)
        return format.format(date)
    }


    fun checkPhonePermission(context: Context): Boolean {
        val result = ContextCompat.checkSelfPermission(context!!, Manifest.permission.CALL_PHONE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    fun changeStatusBarColor(context: AppCompatActivity, color: Int) {
        val window = context.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(context, color)
        }
    }

    fun getDateInEnglish(timestamp: Long): String {
        val formatter = SimpleDateFormat("dd MMMM, yyyy", Locale.ENGLISH)
        val dateString: String = formatter.format(Date(timestamp))
        return dateString
    }

    @SuppressLint("SimpleDateFormat")
    fun convertDate(pre: String, dateString: String, post: String): String {
        val parseFormat = SimpleDateFormat(pre)
        parseFormat.timeZone = TimeZone.getTimeZone("UTC")
        var date: Date? = null
        try {
            date = parseFormat.parse(dateString)
            parseFormat.timeZone = TimeZone.getDefault()
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val format = SimpleDateFormat(post)
        format.timeZone = TimeZone.getDefault()
        // Log.e("dateTimeStamp",date)

        return format.format(date)

    }

    fun getDateWithTime(timestamp: Long): String {
        val formatter = SimpleDateFormat("dd MMMM - HH:mm", Locale.ENGLISH)
        val dateString: String = formatter.format(Date(timestamp))
        return dateString
    }

    fun getTime(timestamp: Long): String {
        val formatter = SimpleDateFormat("HH:mm a", Locale.ENGLISH)
        val dateString: String = formatter.format(Date(timestamp))
        return dateString
    }

    fun isMiUi(): Boolean {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"))
    }

    fun getSystemProperty(propName: String): String? {
        val line: String
        var input: BufferedReader? = null
        try {
            val p = Runtime.getRuntime().exec("getprop $propName")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
        } catch (ex: IOException) {
            return null
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return line
    }



    class Run {
        companion object {
            fun after(delay: Long, process: () -> Unit) {
                Handler().postDelayed({
                    process()
                }, delay)
            }
        }
    }

    fun dp2px(context: Context, dp: Float): Float {
        val scale = context.resources.displayMetrics.density
        return dp * scale + 0.5f
    }

    fun showImageFromGlide(context: Context?, view: ImageView, imageUrl: String, placeHolder: Int) {
        if (imageUrl.isNotEmpty()) {
            if (context != null) {
                /*  Glide.with(context).load(imageUrl).diskCacheStrategy(DiskCacheStrategy.NONE)
                      .skipMemoryCache(true).placeholder(placeHolder).error(placeHolder)
                      .into(view)*/

                Glide.with(context).load(imageUrl).placeholder(placeHolder).error(placeHolder)
                    .into(view)
            }
        }
    }



    fun spannableString() {
    }

    fun setWindowFlag(activity: Activity,bits: Int, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }




    fun setSpannableWhite(context: Context, msg: String, startingIndex: Int, view: TextView) {
        val clickableSpan: ClickableSpan = object : ClickableSpan() {

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(context, R.color.white)
                view.highlightColor = Color.TRANSPARENT;
            }

            override fun onClick(widget: View) {
            }
        }
        val content = SpannableString(msg)
        content.setSpan(clickableSpan, startingIndex, msg.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        view.text = content
        view.movementMethod = LinkMovementMethod.getInstance()
    }





    private fun dateConvert(dateString: String): String {
        return try {
            val originalFormat: DateFormat =
                SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val targetFormat: DateFormat = SimpleDateFormat("dd-MMM-yyyy")
            val date: Date = originalFormat.parse(dateString)
            val formattedDate: String = targetFormat.format(date).toString().toUpperCase()
            formattedDate
        } catch (e: Exception) {
            ""
        }
    }
    fun dateConvert1(dateString: String): String {
        return try {
            val originalFormat: DateFormat =
                SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val targetFormat: DateFormat = SimpleDateFormat("dd MMM yyyy")
            val date: Date = originalFormat.parse(dateString)
            val formattedDate: String = targetFormat.format(date).toString().toUpperCase()
            formattedDate
        } catch (e: Exception) {
            ""
        }
    }
    fun dateConvert2(dateString: String): String {
        return try {
            val originalFormat: DateFormat =
                SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            val targetFormat: DateFormat = SimpleDateFormat("dd MMM yyyy")
            val date: Date = originalFormat.parse(dateString)
            val formattedDate: String = targetFormat.format(date).toString().toUpperCase()
            formattedDate
        } catch (e: Exception) {
            ""
        }
    }

    fun singleClick() {
        var mLastClickTime: Long = 0

        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
    }



    fun Long.readableFormat(): String {
        if (this <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(this / 1024.0.pow(digitGroups.toDouble()))
            .toString() + " " + units[digitGroups]
    }




    fun isFileLessThan3MB(file: File): Boolean {
        val maxFileSize = 5 * 1024 * 1024
        val l = file.length()
        val fileSize = l.toString()
        val finalFileSize = fileSize.toInt()
        return finalFileSize < maxFileSize
    }

    fun isFileGreaterThan2MB(file: File): Boolean {
        val maxFileSize = 2 * 1024 * 1024
        val l = file.length()
        val fileSize = l.toString()
        val finalFileSize = fileSize.toInt()
        return finalFileSize > maxFileSize
    }

    fun highlightTextString(completeText: String, searchText: String): CharSequence? {
        val temp = completeText.toLowerCase()
        val highlightText = SpannableStringBuilder(completeText)
        val pattern: Pattern = Pattern.compile(searchText.toLowerCase())
        val matcher: Matcher = pattern.matcher(temp)
        while (matcher.find()) {
            val styleSpan = StyleSpan(Typeface.BOLD)
            highlightText.setSpan(styleSpan, matcher.start(), matcher.end(), 0)
        }
        return highlightText
    }

    fun getBitmapFromPath(filePath: String?): File? {

        val imageFile = File(filePath)
        val fout: OutputStream = FileOutputStream(imageFile)
        val bitmap = BitmapFactory.decodeFile(filePath)
        bitmap.compress(CompressFormat.PNG, 80, fout)
        fout.flush()
        fout.close()
        return imageFile
    }

    fun isValidUrl(url: String?): Boolean {
        if (url == null) {
            return false
        }
        if (URLUtil.isValidUrl(url)) {
            // Check host of url if youtube exists
            val uri = Uri.parse(url)
            if ("www.youtube.com" == uri.host) {
                return true
            }
            // Other way You can check into url also like
            //if (url.startsWith("https://www.youtube.com/")) {
            //return true;
            //}
        }
        // In other any case
        return false
    }

    fun printDifference(startDate: String, endDate: String): String {
        //milliseconds
        var calculate: String = ""

        var dateFormat: SimpleDateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
        var date1: Date = dateFormat.parse(startDate)
        var date2: Date = dateFormat.parse(endDate)

        Log.e("current", date1.toString() + "," + date2)

        var different = date1.time - date2.time


        println("startDate : $startDate")
        println("endDate : $endDate")
        println("different : $different")
        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24
        val elapsedDays = different / daysInMilli
        different = different % daysInMilli
        val elapsedHours = different / hoursInMilli
        different = different % hoursInMilli
        val elapsedMinutes = different / minutesInMilli
        different = different % minutesInMilli
        val elapsedSeconds = different / secondsInMilli

        try {

            if ((elapsedDays.toString().replace("-", "")).toInt() > 365) {
                calculate = (elapsedDays / 365).toString().plus(" years ago")

            } else if ((elapsedDays.toString().replace("-", "")).toInt() in 31..364) {
                calculate = (elapsedDays / 30).toString().plus(" months ago")


            } else if ((elapsedDays.toString().replace("-", "")).toInt() in 1..30) {
                calculate = elapsedDays.toString().plus(" days ago")


            } else if ((elapsedHours.toString().replace("-", "")).toInt() in 1..24) {
                calculate = elapsedHours.toString().plus(" hours ago")

            } else if ((elapsedMinutes.toString().replace("-", "")).toInt() in 1..60) {
                calculate = elapsedMinutes.toString().plus(" min ago")

            } else if ((elapsedSeconds.toString().replace("-", "")).toInt() in 1..60) {
                calculate = elapsedSeconds.toString().plus(" sec ago")

            }
        } catch (e: Exception) {

        }

        Log.e(
            "datesss",
            elapsedDays.toString() + "," + elapsedHours + "," + elapsedMinutes + "," + elapsedSeconds
        )
        System.out.printf(
            "%d days, %d hours, %d minutes, %d seconds%n",
            elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds
        )
        return calculate.replace("-", "")
    }

    fun currentSimpleDateFormat(): SimpleDateFormat {
        val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault())

        return sdf

    }

    fun openWebPages(url: String, context: Context) {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.setPackage("com.android.chrome")
        try {
            context.startActivity(i)
        } catch (e: ActivityNotFoundException) {
            // Chrome is probably not installed
            // Try with the default browser
            i.setPackage(null)
            context.startActivity(i)
        }
    }
    fun getFormattedDate(dateStr: String):String{
        try {
            var format:SimpleDateFormat?=null

            if (dateStr.startsWith("1") && !dateStr.startsWith("11"))
                format = SimpleDateFormat("dd'st' MMMM yyyy")
            else if (dateStr.startsWith("2") && !dateStr.startsWith("12"))
                format = SimpleDateFormat("dd'nd' MMMM yyyy")
            else if (dateStr.startsWith("3") && !dateStr.startsWith("13"))
                format = SimpleDateFormat("dd'rd' MMMM yyyy")
            else
                format = SimpleDateFormat("dd'th' MMMM yyyy")

           var format1 = SimpleDateFormat("dd MMM yyyy")
            val date: Date = format.parse(dateStr)
            val formattedDate: String = format1.format(date).toString().toUpperCase()

            return formattedDate
        }
        catch (ex: Exception){
            return dateStr
        }
    }

     fun getFormatedAmount(amount: Double): String? {
         val formatter = DecimalFormat("#,###.00")
        return  formatter.format(amount)
    }
}
