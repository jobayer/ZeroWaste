package ac.bd.bracu.zerowaste.ui.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseActivity<in T>(@LayoutRes private val layoutResId: Int? = null) :
    AppCompatActivity() where T : ViewDataBinding {

    abstract fun onActivityCreated(dataBinder: T)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this@BaseActivity.init()
    }

    private fun init() {
        this@BaseActivity.layoutResId?.let { layoutId ->
            val dataBinder = DataBindingUtil.setContentView<T>(this@BaseActivity, layoutId)
            this@BaseActivity.onActivityCreated(dataBinder)
        }
    }

    protected fun registerOnBackPresses(toDo: () -> Unit) {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                toDo()
            }
        })
    }

}