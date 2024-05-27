package com.minervaai.summasphere.ui.customview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.textfield.TextInputLayout
import com.minervaai.summasphere.R

class ConfirmPasswordInputEditText : AppCompatEditText, View.OnTouchListener {

    private var passwordEditText: PasswordInputEditText? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }


    private fun init() {
        setOnTouchListener(this)

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val parentLayout = parent.parent as? TextInputLayout
                return when {
                    s.isNullOrEmpty() -> {
                        parentLayout?.error = context.getString(R.string.confirm_password_required)
                    }
                    passwordEditText?.text.toString() != s.toString() -> {
                        parentLayout?.error = context.getString(R.string.passwords_do_not_match)
                    }
                    else -> {
                        parentLayout?.error = null
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            val parentLayout = parent.parent as? TextInputLayout
            if (text.isNullOrEmpty()) {
                parentLayout?.error = context.getString(R.string.confirm_password_required)
            }
        }
        return false
    }
}
