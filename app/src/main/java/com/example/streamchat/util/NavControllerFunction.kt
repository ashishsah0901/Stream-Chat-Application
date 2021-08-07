package com.example.streamchat.util

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator

fun NavController.navigateSafely (
    @IdRes resID: Int,
    args: Bundle? = null,
    navOptions: NavOptions? = null,
    navExtras: Navigator.Extras? = null
) {
    val action = currentDestination?.getAction(resID) ?: graph.getAction(resID)
    if(action != null && currentDestination?.id != action.destinationId) {
        navigate(resID, args, navOptions, navExtras)
    }
}