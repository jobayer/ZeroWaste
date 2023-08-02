package ac.bd.bracu.zerowaste.data.utils

import ac.bd.bracu.zerowaste.R
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar

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