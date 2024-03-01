package org.ymkm.pwchart

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.children
import androidx.core.view.isInvisible
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import org.ymkm.passwordchart.R
import org.ymkm.passwordchart.databinding.ActivityTopBinding
import java.util.Locale

class TopAct : AppCompatActivity(), ViewCallback {
    private lateinit var binding: ActivityTopBinding

    override val limitChars: ObservableBoolean = ObservableBoolean(false)
    override fun limitChars(limit: Boolean) {
        limitChars.set(limit)
    }

    override var includeNumbers: Boolean = true
        set(isChecked) {
            field = isChecked
            if (isChecked) chartType or PWChartUtils.CHART_TYPE_INCLUDE_NUMBERS else chartType and PWChartUtils.CHART_TYPE_INCLUDE_NUMBERS.inv()
            updateChart()
            updatePassword()
        }
    override var includeSymbols: Boolean = true
        set(isChecked) {
            field = isChecked
            if (isChecked) chartType or PWChartUtils.CHART_TYPE_INCLUDE_SYMBOLS else chartType and PWChartUtils.CHART_TYPE_INCLUDE_SYMBOLS.inv()
            updateChart()
            updatePassword()
        }
    override val chartNumber: ObservableField<String?> = ObservableField()
    override val generatedPassword: ObservableField<String?> = ObservableField()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { false }
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_top)
        binding.callback = this
    }

    override fun onInputChanged() {
        updateChart()
        updatePassword()
    }

    override fun onPwClicked() {
        val generatedPassword = generatedPassword.get() ?: return
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(generatedPassword, generatedPassword)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, R.string.toast_copied_pw_to_clipboard_text, Toast.LENGTH_SHORT).show()
    }

    private fun updateChart() {
        val seedStr = binding.tvSeed.text.toString()
        binding.tvChartNumber.isInvisible = seedStr.isEmpty()
        binding.charTable.isInvisible = seedStr.isEmpty()
        if (seedStr.isEmpty()) {
            chartNumber.set(null)
            return
        }
        val chartNum = PWChartUtils.chartNumber(seedStr, chartType)
        chartNumber.set(
            TextUtils.expandTemplate(
                getString(R.string.pwchart_number),
                chartNum.toString()
            ).toString()
        )
        updateCharTable(
            seed = seedStr,
            useNumbers = binding.useNumbers.isChecked,
            useSymbols = binding.useSymbols.isChecked
        )
    }

    private fun updatePassword() {
        val seedStr: String = binding.tvSeed.getText().toString()
        val pw: String = binding.tvPw.text.toString().uppercase(Locale.getDefault())
        if (seedStr.isEmpty() || pw.isEmpty()) {
            _generatedPassword = null
            updateGeneratedPassword()
            return
        }
        _generatedPassword =
            PWChartUtils.genPasswordWithSeed(
                seedStr,
                pw,
                includeNumbers,
                includeSymbols
            )
        updateGeneratedPassword()
    }

    private fun updateGeneratedPassword() {
        val currentPassword = _generatedPassword.orEmpty()
        val passMaxLength = maxChars ?: currentPassword.length
        generatedPassword.set(currentPassword.take(passMaxLength))
    }

    @SuppressLint("InflateParams")
    private fun updateCharTable(seed: String, useNumbers: Boolean, useSymbols: Boolean) {
        binding.charTable.isInvisible = binding.tvSeed.text.isEmpty()
        val map = PWChartUtils.getTable(seed, useNumbers, useSymbols)
        binding.charTable.children.forEach { child ->
            val b = child as Button
            val charMap = b.text[0]
            b.setOnClickListener { v ->
                if (binding.charTable.tag != null) {
                    (binding.charTable.tag as View).isActivated = false
                }
                val currentPopup = if (shownPopup == null) {
                    val tv = LayoutInflater.from(this@TopAct)
                        .inflate(R.layout.char_popup, null) as TextView
                    tv.text = map[charMap]
                    tv.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                    shownPopup = PopupWindow(tv, tv.measuredWidth, tv.measuredHeight)
                    binding.charTable.tag = b
                    requireNotNull(shownPopup)
                } else {
                    requireNotNull(shownPopup).also {
                        (it.contentView as TextView).text = map[charMap]
                    }
                }
                currentPopup.isOutsideTouchable = true
                currentPopup.showAtLocation(v, Gravity.CENTER, 0, 0)
                currentPopup.setOnDismissListener {
                    if (binding.charTable.tag != null) {
                        (binding.charTable.tag as View).isActivated = false
                    }
                }
                binding.charTable.tag = b
                b.isActivated = true
            }
        }
    }

    override fun maxChars(editable: Editable?) {
        maxChars = editable?.toString()?.toIntOrNull()
        updateGeneratedPassword()
    }

    private var shownPopup: PopupWindow? = null
    private var chartType = PWChartUtils.CHART_TYPE_NORMAL
    private var maxChars: Int? = null
    private var _generatedPassword: String? = null
}

@BindingAdapter("enabled")
fun TextView.enabled(isEnabled: Boolean) {
    this.isEnabled = isEnabled
}

@BindingAdapter("isVisible")
fun View.isVisible(isVisible: Boolean?) {
    this.visibility = if (isVisible == true) View.VISIBLE else View.GONE
}

@BindingAdapter("isInvisible")
fun View.isInvisible(isInvisible: Boolean?) {
    this.visibility = if (isInvisible == true) View.INVISIBLE else View.VISIBLE
}