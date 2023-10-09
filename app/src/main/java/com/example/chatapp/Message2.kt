package com.example.chatapp



class Message2 {
    var text: String? = null
    var senderName: String? = null
    var senderUid: String? = null
    var timestamp: Long? = null

    constructor() {
    }

    constructor(text: String?, senderName:String?, senderUid: String?, timestamp: Long?) {
        this.text = text
        this.senderName=senderName
        this.senderUid = senderUid
        this.timestamp = timestamp
    }
}