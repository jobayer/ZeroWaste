package com.bracu.zerowaste.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bracu.zerowaste.Const.RDBR_USER
import com.bracu.zerowaste.Const.SAFE_CLICK_ERROR_MSG
import com.bracu.zerowaste.data.db.getMiscData
import com.bracu.zerowaste.data.db.getProfilePic
import com.bracu.zerowaste.data.db.getUser
import com.bracu.zerowaste.data.db.setMiscData
import com.bracu.zerowaste.data.db.setProfilePic
import com.bracu.zerowaste.data.db.setUser
import com.bracu.zerowaste.data.models.User
import com.bracu.zerowaste.data.utils.dismissDialog
import com.bracu.zerowaste.data.utils.isConnectedToInternet
import com.bracu.zerowaste.data.utils.loadingDialog
import com.bracu.zerowaste.data.utils.onClick
import com.bracu.zerowaste.data.utils.onSafeClick
import com.bracu.zerowaste.data.utils.showDialog
import com.bracu.zerowaste.data.utils.toast
import com.bracu.zerowaste.databinding.FragmentProfileBinding
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.nguyenhoanglam.imagepicker.model.ImagePickerConfig
import com.nguyenhoanglam.imagepicker.model.IndicatorType
import com.nguyenhoanglam.imagepicker.ui.imagepicker.registerImagePicker

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var loadingDialog: AlertDialog

    private lateinit var rdb: DatabaseReference

    private val profilePicLauncher = registerImagePicker { images ->
        if (images.isNotEmpty()) {
            val image = images[0]
            setProfilePic(image.uri)
            Glide.with(requireActivity())
                .load(image.uri)
                .into(binding.profilePic)
        } else {
            toast("No Image Selected")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        init()
        return binding.root
    }

    private fun init() {
        initVars()
        initUi()
        initOnClick()
    }

    private fun initVars() {
        loadingDialog = requireActivity().loadingDialog()
        rdb = Firebase.database.reference
    }

    private fun initUi() {
        with(binding) {
            user = com.bracu.zerowaste.data.db.getUser()
            miscData = com.bracu.zerowaste.data.db.getMiscData()
            executePendingBindings()

            getProfilePic()?.let {
                Glide.with(requireActivity())
                    .load(it)
                    .into(profilePic)
            }
            update.isEnabled = true
        }
    }

    private fun initOnClick() {
        with(binding) {
            profilePic.onClick {
                openImgPicker()
            }
            update.onSafeClick {
                if (tiName.text.isNullOrBlank()) {
                    tiName.error = "Enter Valid Name"
                    return@onSafeClick
                }
                if (tiRfid.text.isNullOrBlank()) {
                    tiRfid.error = "Enter RFID ID"
                    return@onSafeClick
                }
                if (tiTargetPoints.text.isNullOrBlank()) {
                    tiTargetPoints.error = "Enter Your Monthly Target"
                    return@onSafeClick
                }

                update.isEnabled = false

                tiName.error = null
                tiRfid.error = null
                tiTargetPoints.error = null

                updateProfile(
                    tiName.text!!.toString(),
                    tiRfid.text!!.toString(),
                    tiTargetPoints.text!!.toString().toInt()
                )
            }
            srl.setOnRefreshListener {
                if (!requireContext().isConnectedToInternet()) {
                    toast(SAFE_CLICK_ERROR_MSG)
                    return@setOnRefreshListener
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    srl.isRefreshing = false
                }, 2000)
                getProfile()
            }
        }
    }

    private fun openImgPicker() {
        val config = ImagePickerConfig(
            isFolderMode = true,
            isShowCamera = true,
            limitSize = 1,
            selectedIndicatorType = IndicatorType.NUMBER,
            isMultiSelectMode = false
        )
        profilePicLauncher.launch(config)
    }

    private fun updateProfile(name: String, rfid: String, monthlyTarget: Int) {
        loadingDialog.showDialog()

        val loggedInUser = getUser()!!.apply {
            this.name = name
            this.rfid = rfid
        }

        val miscData = getMiscData().apply {
            this.targetPoints = monthlyTarget
        }

        rdb.child(RDBR_USER).child(loggedInUser.uid).setValue(loggedInUser)
            .addOnCompleteListener {
                loadingDialog.dismissDialog()
                if (it.isSuccessful) {
                    setUser(loggedInUser)
                    setMiscData(miscData)
                    toast("Profile Updated Successfully")
                    initUi()
                } else {
                    toast("Failed to Update Profile")
                    binding.update.isEnabled = true
                }
            }
    }

    private fun getProfile() {
        binding.tiName.text?.clear()
        binding.tiRfid.text?.clear()

        loadingDialog.showDialog()

        val loggedInUser = getUser()!!

        rdb.child(RDBR_USER).child(loggedInUser.uid).get()
            .addOnCompleteListener {
                loadingDialog.dismissDialog()
                if (it.isSuccessful) {
                    setUser(it.result.getValue(User::class.java)!!)
                    initUi()
                } else {
                    toast("Error While Refreshing Profile")
                }
            }
    }

}