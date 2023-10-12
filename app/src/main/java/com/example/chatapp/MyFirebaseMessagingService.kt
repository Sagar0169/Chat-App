package com.example.chatapp

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson

import org.json.JSONException
import org.json.JSONObject


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        if(token!= ""){
            savePreferencesString(this,AppConstants.FirebaseToken,token)
            Log.e("New token ","token: $token")
        }
    }

    fun savePreferencesString(context: Context, key: String, value: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        // Handle the incoming message
//        if (remoteMessage.data.isNotEmpty()) {
//            // Handle data payload
//        }
//
//        remoteMessage.notification?.let {
//            // Handle notification payload
//            sendNotification(it.title, it.body)
//        }
//    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e("notify", "Data:" + Gson().toJson(remoteMessage))
        Log.e(TAG, "Data Notification: " + remoteMessage.notification.toString())
        if (remoteMessage.data.isNotEmpty()) {
            Log.e(TAG, "Data: " + remoteMessage.data.toString())
            Log.e(TAG, "Data: $remoteMessage")
            Log.e(TAG, "Data Notification: " + remoteMessage.notification.toString())
            val data: Map<String?, String?> = remoteMessage.data
            Log.e(TAG, "ddata: $data")

            val jdata=convertToJSON(data.toString())
            Log.d("jdata",jdata)
            val json = JSONObject(jdata)
            if (data.isNotEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) ==
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        sendPushNotification(json)
                    } else {

                    }
                } else {
                    sendPushNotification(json)
                }
            }
//            sendPushNotification(json)

        }
    }
    private fun sendNotification(
        json: JSONObject
    ) {
        try {
//            val value = json.getJSONObject("value")
            val body = json.getString("body").toString()
            val title = json.getString("title").toString()
//            val message = value.getString("message").toString()
//            val notificationType = json.getInt("noti_type").toString()

            val mNotificationManager = NotificationManager(applicationContext)
//            var contentIntent: PendingIntent? = null
            val resultIntent = Intent(this, MainActivity::class.java)
            resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

//            contentIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                PendingIntent
//                    .getActivity(
//                        this, System.currentTimeMillis().toInt(), resultIntent,
//                        PendingIntent.FLAG_MUTABLE
//                    )
//            } else {
//                PendingIntent
//                    .getActivity(
//                        this, System.currentTimeMillis().toInt(), resultIntent,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                    )
//            }
            mNotificationManager.showSmallNotification(title, body, resultIntent)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    fun convertToJSON(input: String): String {
        // Split the input string into key-value pairs
        val stringWithoutCurlyBraces = input.replace("{", "").replace("}", "")
        val keyValuePairs = stringWithoutCurlyBraces.split(", ")

        // Create a StringBuilder to build the JSON string
        val jsonBuilder = StringBuilder("{")

        // Iterate through key-value pairs
        for (pair in keyValuePairs) {
            // Split each pair into key and value
            val (key, value) = pair.split("=")

            // Append the key and value to the JSON string
            jsonBuilder.append("\"$key\":\"$value\"")

            // Append a comma to separate key-value pairs (except for the last one)
            if (keyValuePairs.indexOf(pair) < keyValuePairs.size - 1) {
                jsonBuilder.append(",")
            }
        }

        // Close the JSON object
        jsonBuilder.append("}")

        return jsonBuilder.toString()
    }

    private fun sendPushNotification(json: JSONObject) {
        //optionally we can display the json into log
        Log.e(TAG, "Notification JSON $json")
//        Log.e(TAG, "Notification JSON ${json.get("type")}")
        var intent: Intent?= null

        try {
//            val value = json.getJSONObject("value")
            val body = json.getString("body").toString()
            val title = json.getString("title").toString()
//            val message = value.getString("message").toString()



            val mNotificationManager = NotificationManager(applicationContext)
            val intent = Intent(applicationContext, MainActivity::class.java)
            //intent.putExtra(AppConstants.NOTIFICATION_DATA, "notification")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            mNotificationManager.showSmallNotification(title, body, intent)
        } catch (e: JSONException) {

            //  e.printStackTrace()
        }

    }


    private fun sendNotification(title: String?, body: String?) {
        // Create and show the notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Build the notification
        val builder = NotificationCompat.Builder(this, "channel_id")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Show the notification
        notificationManager.notify(0, builder.build())
    }
}