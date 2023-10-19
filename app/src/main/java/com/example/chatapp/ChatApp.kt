package com.example.chatapp

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class ChatApp:Application()
{
    override fun onCreate() {
        super.onCreate()
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}