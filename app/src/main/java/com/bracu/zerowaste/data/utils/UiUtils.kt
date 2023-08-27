package com.bracu.zerowaste.data.utils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.bracu.zerowaste.Const
import com.bracu.zerowaste.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

fun TextInputEditText.txt(txt: String) {
    text?.clear()
    text = Editable.Factory.getInstance().newEditable(txt)
}

fun View.onClick(toDo: () -> Unit) {
    setOnClickListener {
        toDo()
    }
}

fun View.onSafeClick(toDo: () -> Unit) {
    with(context) {
        setOnClickListener {
            if (isConnectedToInternet()) {
                toDo()
            } else toast(Const.SAFE_CLICK_ERROR_MSG)
        }
    }
}

fun AlertDialog.showDialog() {
    if (!isShowing) {
        show()
    }
}

fun AlertDialog.dismissDialog() {
    if (isShowing) {
        dismiss()
    }
}

fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

fun Fragment.toast(msg: String) {
    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
}

fun View.hide() {
    if (visibility != View.GONE) {
        visibility = View.GONE
    }
}

fun View.show() {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
    }
}

fun AppCompatActivity.setupToolbar(toolbar: MaterialToolbar, drawerLayout: DrawerLayout) {
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayShowTitleEnabled(false)
    val drawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
    drawerLayout.addDrawerListener(drawerToggle)
    drawerToggle.syncState()
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeAsUpIndicator(
        ResourcesCompat.getDrawable(
            resources,
            R.drawable.round_menu_24,
            theme
        )
    )
}

fun Activity.loadingDialog(): AlertDialog {

    val dialogView = layoutInflater.inflate(R.layout.loading_dialog, null)

    val dialogBuilder = AlertDialog.Builder(this, R.style.TransparentDialog)
        .setView(dialogView)
        .setCancelable(false)

    val customDialog = dialogBuilder.create().apply {
        setCanceledOnTouchOutside(false)
    }

    return customDialog
}

fun Activity.animatedDialog(
    title: String,
    description: String,
    icon: Int,
    actionBtn: String,
    actionBtnColor: String? = null,
    toDo: (dialog: AlertDialog) -> Unit
): AlertDialog {

    val dialogView = layoutInflater.inflate(R.layout.animated_dialog, null)
    val animIcon = dialogView.findViewById<LottieAnimationView>(R.id.animationView)
    val dialogTitle = dialogView.findViewById<AppCompatTextView>(R.id.title)
    val dialogDescription = dialogView.findViewById<AppCompatTextView>(R.id.description)
    val dialogActionBtn = dialogView.findViewById<MaterialButton>(R.id.actionButton)

    animIcon.setAnimation(icon)
    animIcon.playAnimation()
    dialogTitle.text = title
    dialogDescription.text = description
    dialogActionBtn.text = actionBtn.uppercase()

    actionBtnColor?.let {
        dialogActionBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor(it))
    }

    val dialogBuilder = AlertDialog.Builder(this, R.style.RoundedCornersDialog)
        .setView(dialogView)
        .setCancelable(false)

    val customDialog = dialogBuilder.create().apply {
        setCanceledOnTouchOutside(false)
    }

    dialogActionBtn.onClick {
        toDo(customDialog)
    }

    customDialog.window?.let {
        val layoutParams = it.attributes.apply {
            dimAmount = 0.7f
        }
        it.attributes = layoutParams
        it.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
    }

    return customDialog
}

fun Activity.showInfoDialog(
    title: String,
    description: String,
    actionBtn: String,
    icon: Int,
    toDo: () -> Unit
): AlertDialog {
    val dialog = animatedDialog(title, description, icon, actionBtn) {
        it.dismiss()
        toDo()
    }
    return dialog
}

fun Activity.errorDialog(
    title: String,
    description: String,
    actionBtn: String,
    toDo: () -> Unit
): AlertDialog {
    val dialog = animatedDialog(title, description, R.raw.lottie_error, actionBtn) {
        it.dismiss()
        toDo()
    }
    return dialog
}