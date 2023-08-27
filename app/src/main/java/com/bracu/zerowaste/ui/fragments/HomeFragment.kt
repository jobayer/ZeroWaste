package com.bracu.zerowaste.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bracu.zerowaste.Const
import com.bracu.zerowaste.Const.RDBR_USER_POINTS
import com.bracu.zerowaste.R
import com.bracu.zerowaste.data.db.getMiscData
import com.bracu.zerowaste.data.db.getUser
import com.bracu.zerowaste.data.models.MiscData
import com.bracu.zerowaste.data.models.User
import com.bracu.zerowaste.data.utils.goto
import com.bracu.zerowaste.data.utils.onSafeClick
import com.bracu.zerowaste.data.utils.showNotification
import com.bracu.zerowaste.databinding.FragmentHomeBinding
import com.bracu.zerowaste.ui.activities.DumpingActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var mMiscData: MiscData
    private lateinit var loggedInUser: User

    private lateinit var user: User

    private lateinit var pointsRef: DatabaseReference

    private var startUp: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        init()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initUi()
    }

    private fun init() {
        initVars()
        initUi()
        initOnClick()
        chkForPoints()
    }

    private fun initVars() {
        user = getUser()!!
        mMiscData = getMiscData()
        pointsRef = Firebase.database.reference.child(Const.RDBR_USER).child(user.uid)
            .child(RDBR_USER_POINTS)
    }

    private fun initUi() {
        with(binding) {
            loggedInUser = com.bracu.zerowaste.data.db.getUser()!!
            user = loggedInUser
            executePendingBindings()
            if (mMiscData.targetPoints > loggedInUser.points) {
                earningTarget.max = mMiscData.targetPoints
                earningTarget.progress = loggedInUser.points
            } else {
                earningTarget.progress = 45
            }
        }
    }

    private fun initOnClick() {
        with(binding) {
            track.onSafeClick {
                requireActivity().goto<DumpingActivity>()
            }
            srl.setOnRefreshListener {
                initUi()
                Handler(Looper.getMainLooper()).postDelayed({
                    srl.isRefreshing = false
                }, 2000)
            }
        }
    }

    private fun chkForPoints() {
        pointsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    try {
                        if (!startUp) {
                            requireActivity().showNotification(
                                "New Dumping Completed!",
                                "You have received 5 points for dumping a new waste! Click here to check.",
                                R.drawable.gift
                            )
                        }
                        val p = snapshot.value as Long
                        binding.totalPoints.text = p.toString()
                    } catch (e: Exception) {
                        Log.e("TAG", "onDataChange: " + e.message )
                    }
                }
                if (startUp) {
                    startUp = false
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}