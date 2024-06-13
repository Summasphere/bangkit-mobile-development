package com.minervaai.summasphere.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton

class CustomButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {

    private val icon: ImageView
    private val title: TextView
    private val subtitle: TextView

    init {
        // Inflate the custom layout
        val view = LayoutInflater.from(context).inflate(R.layout.custom_button_summarize, this, true) as ConstraintLayout
        icon = view.findViewById(R.id.btn_icon)
        title = view.findViewById(R.id.btn_title)
        subtitle = view.findViewById(R.id.btn_subtitle)

        // Handle custom attributes
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.CustomButton, 0, 0)
            val titleText = typedArray.getString(R.styleable.CustomButton_titleText)
            val subtitleText = typedArray.getString(R.styleable.CustomButton_subtitleText)
            val iconResId = typedArray.getResourceId(R.styleable.CustomButton_iconDrawable, -1)

            title.text = titleText
            subtitle.text = subtitleText
            if (iconResId != -1) {
                icon.setImageResource(iconResId)
            }
            typedArray.recycle()
        }

        // Set the background drawable
        background = context.getDrawable(R.drawable.custom_button_background)
    }

    fun setTitleText(text: String) {
        title.text = text
    }

    fun setSubtitleText(text: String) {
        subtitle.text = text
    }

    fun setIconDrawable(resId: Int) {
        icon.setImageResource(resId)
    }
}
