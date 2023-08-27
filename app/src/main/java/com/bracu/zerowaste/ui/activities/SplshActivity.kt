package com.bracu.zerowaste.ui.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.bracu.zerowaste.Const.SPLASH_WAIT_TIME
import com.bracu.zerowaste.R
import com.bracu.zerowaste.data.db.getUser
import com.bracu.zerowaste.data.utils.goto
import com.bracu.zerowaste.databinding.ActivitySplshBinding
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplshActivity : BaseActivity<ActivitySplshBinding>(R.layout.activity_splsh) {

    private lateinit var binding: ActivitySplshBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient

    override fun onActivityCreated(dataBinder: ActivitySplshBinding) {
        binding = dataBinder
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        initVars()
        checkLogin()
    }

    private fun initVars() {
        auth = Firebase.auth
        oneTapClient = Identity.getSignInClient(this)
    }

    private fun checkLogin() {
        val loggedInUser = getUser()
        Handler(Looper.getMainLooper()).postDelayed({
            if (auth.currentUser != null && loggedInUser != null) {
                goto<MainActivity>(clearStack = true)
            } else {
                oneTapClient.signOut()
                auth.signOut()
                goto<AuthActivity>(clearStack = true)
            }
        }, SPLASH_WAIT_TIME)
    }

}