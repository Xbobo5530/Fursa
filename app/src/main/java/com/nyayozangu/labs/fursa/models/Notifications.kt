package com.nyayozangu.labs.fursa.models

data class Notifications(
        val title: String,
        val message: String,
        val notif_type: String,
        val extra: String,
        val notif_id: Int,
        val status: Int
)