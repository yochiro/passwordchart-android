package org.ymkm.pwchart

import android.text.Editable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField

interface ViewCallback {
    fun onPwClicked()
    fun onInputChanged()
    fun maxChars(editable: Editable?)
    var includeNumbers: Boolean
    var includeSymbols: Boolean
    fun limitChars(limit: Boolean)
    val limitChars: ObservableBoolean
    val chartNumber: ObservableField<String?>
    val generatedPassword: ObservableField<String?>
}