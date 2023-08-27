package com.bracu.zerowaste.ui.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.bracu.zerowaste.Const.RDBR_LOGGED_IN
import com.bracu.zerowaste.Const.RDBR_USER
import com.bracu.zerowaste.R
import com.bracu.zerowaste.data.db.getUser
import com.bracu.zerowaste.data.db.setUser
import com.bracu.zerowaste.data.models.User
import com.bracu.zerowaste.data.utils.hide
import com.bracu.zerowaste.data.utils.onClick
import com.bracu.zerowaste.data.utils.show
import com.bracu.zerowaste.data.utils.toast
import com.bracu.zerowaste.databinding.ActivityDumpingBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DumpingActivity : BaseActivity<ActivityDumpingBinding>(R.layout.activity_dumping) {

    private lateinit var binding: ActivityDumpingBinding

    private lateinit var authRef: DatabaseReference
    private lateinit var userRef: DatabaseReference

    private lateinit var user: User

    override fun onActivityCreated(dataBinder: ActivityDumpingBinding) {
        binding = dataBinder
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        initVars()
        initOnClick()
        checkAuthStatus()
    }

    private fun initVars() {
        user = getUser()!!
        authRef = Firebase.database.reference.child(RDBR_LOGGED_IN).child(user.rfid)
        userRef = Firebase.database.reference.child(RDBR_USER).child(user.uid)
    }

    private fun initOnClick() {
        with(binding) {
            backBtn.onClick {
                finish()
            }
        }
    }

    private fun checkAuthStatus() {
        authRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val authStatus = snapshot.value.toString()
                    if (authStatus == "1") {
                        updateWaitingForDumpingUi()
                    } else updateNotAuthUi()
                } else updateNotAuthUi()
            }

            override fun onCancelled(error: DatabaseError) {
                toast("Please Try Again Later")
                finish()
            }
        })
    }

    private fun updateNotAuthUi() {
        with(binding) {
            anim.cancelAnimation()
            anim.setAnimation(R.raw.lottie_rfid)
            anim.playAnimation()
            backBtn.hide()
            status.text = "Waiting for Authentication"
            helperTxt.text = "Swipe your RFID tag with the RFID reader"
        }
    }

    private fun updateWaitingForDumpingUi() {
        with(binding) {
            anim.cancelAnimation()
            anim.setAnimation(R.raw.lottie_clock)
            anim.playAnimation()
            backBtn.hide()
            status.text = "Throw Your Waste"
            helperTxt.text = "Put the waste gently into the bin"
        }
        updateUserBalance()
        Handler(Looper.getMainLooper()).postDelayed({
            updateDumpedUi()
        }, 5000)
    }

    private fun updateDumpedUi() {
        with(binding) {
            anim.cancelAnimation()
            anim.setAnimation(R.raw.lottie_completed)
            anim.playAnimation()
            backBtn.show()
            status.text = "New Waste Is Dumped!"
            helperTxt.text = "Your bonus point has been added to your account"
        }
    }

    private fun updateUserBalance() {
        user.points += 5
        setUser(user)
        userRef.setValue(user)
            .addOnCompleteListener {
                toast("User balance updated!")
            }
    }

}