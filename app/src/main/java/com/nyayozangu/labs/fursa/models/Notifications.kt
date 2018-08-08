package com.nyayozangu.labs.fursa.models

import com.nyayozangu.labs.fursa.helpers.CoMeth.NOTIFICATION_STATUS_UNREAD
import java.util.*

data class Notifications(
        val title: String = "",
        val message: String = "",
        val notif_type: String? = "",
        val extra: String? = "",
        val notif_id: Int = 0,
        val status: Int? = NOTIFICATION_STATUS_UNREAD,
        val timestamp: Date = Date(),
        var doc_id: String = ""
)