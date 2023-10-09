package com.example.chatapp.model

class User {
    var uid: String? = null
    var name: String? = null
    var user_name: String? = null
    var phoneNumber: String? = null
    var profileImage: String? = null
    var role: String? = null
    var isGroup: Boolean = false
    var lastMessage: String? = null // Added field for last message
    var unSeenCount : Int?= null

    constructor() {}

    constructor(
        uid: String?,
        name: String?,
        user_name: String?,
        phoneNumber: String?,
        profileImage: String?,
        role: String?,
        isGroup: Boolean,
        lastMessage: String?, // Initialize lastMessage field
        unSeenCount :Int?
    ) {
        this.uid = uid
        this.name = name
        this.user_name = user_name
        this.phoneNumber = phoneNumber
        this.profileImage = profileImage
        this.role = role
        this.isGroup = isGroup
        this.lastMessage = lastMessage // Set lastMessage during initialization
        this.unSeenCount = unSeenCount
    }
}
