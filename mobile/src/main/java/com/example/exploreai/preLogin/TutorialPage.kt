package com.mau.exploreai

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

// TutorialPage.kt
@Parcelize
data class TutorialPage(
    val title: String,
    val description: String
) : Parcelable