package com.nyayozangu.labs.fursa.models

import io.reactivex.annotations.NonNull

class NotificationId {

    var NotificationId: String = ""
    fun <T : NotificationId> withId(@NonNull id: String): T {
        this.NotificationId = id
        return this as T
    }
}
