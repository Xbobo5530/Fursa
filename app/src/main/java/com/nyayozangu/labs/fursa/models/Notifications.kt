package com.nyayozangu.labs.fursa.models

import java.util.*

data class Notifications(
        val title: String = "",
        val message: String = "",
        val notif_type: String? = "",
        val extra: String? = "",
        val notif_id: Int = 0,
        val status: Int? = 0,
        val timestamp: Date = Date(),
        var doc_id: String = ""
)