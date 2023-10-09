package com.example.chatapp.model

class Message {
    var messageId: String? = null
    var message: String? = null
    var senderId: String? = null
    var imageUrl: String? = null
    var timeStamp: Long = 0
    var seen: Boolean = true

    constructor() {}

    constructor(
        message: String?,
        senderId: String?,
        timeStamp: Long,
        seen: Boolean
    ) {
        this.message = message
        this.senderId = senderId
        this.timeStamp = timeStamp
        this.seen = seen
    }
}
