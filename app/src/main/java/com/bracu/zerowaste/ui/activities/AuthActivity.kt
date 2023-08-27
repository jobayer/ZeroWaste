package com.bracu.zerowaste.ui.activities

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.bracu.zerowaste.Const.GOOGLE_WEB_CLIENT_ID
import com.bracu.zerowaste.Const.RDBR_USER
import com.bracu.zerowaste.R
import com.bracu.zerowaste.data.db.setUser
import com.bracu.zerowaste.data.models.User
import com.bracu.zerowaste.data.utils.dismissDialog
import com.bracu.zerowaste.data.utils.errorDialog
import com.bracu.zerowaste.data.utils.goto
import com.bracu.zerowaste.data.utils.loadingDialog
import com.bracu.zerowaste.data.utils.onClick
import com.bracu.zerowaste.data.utils.showDialog
import com.bracu.zerowaste.data.utils.showInfoDialog
import com.bracu.zerowaste.data.utils.txt
import com.bracu.zerowaste.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AuthActivity : BaseActivity<ActivityAuthBinding>(R.layout.activity_auth) {

    private lateinit var binding: ActivityAuthBinding

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private lateinit var auth: FirebaseAuth

    private lateinit var rdb: DatabaseReference

    private lateinit var loadingDialog: AlertDialog

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val responseData = result.data
                if (responseData != null) {
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    if (idToken != null) {
                        firebaseSignIn(idToken)
                    } else {
                        oneTapClient.signOut()
                        showError(1)
                    }
                } else {
                    oneTapClient.signOut()
                    showError(2)
                }
            } else {
                oneTapClient.signOut()
                showError(3)
            }
        }

    override fun onActivityCreated(dataBinder: ActivityAuthBinding) {
        binding = dataBinder
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        initVars()
        configAuth()
        initOnClick()
    }

    private fun initVars() {
        loadingDialog = loadingDialog()
        rdb = Firebase.database.reference
    }

    private fun initOnClick() {
        with(binding) {
            loginBtn.onClick {
                loginBtn.isEnabled = false
                signInWithGoogle()
            }
        }
    }

    private fun configAuth() {
        auth = Firebase.auth
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(GOOGLE_WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()
    }

    private fun signInWithGoogle() {
        oneTapClient.beginSignIn(signInRequest).addOnCompleteListener {
            if (it.isSuccessful) {
                val signInIntent =
                    IntentSenderRequest.Builder(it.result.pendingIntent.intentSender).build()
                signInLauncher.launch(signInIntent)
            } else {
                showError(4)
            }
        }
    }

    private fun firebaseSignIn(idToken: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val recentLoggedInUser = task.result.user
                    if (recentLoggedInUser != null) {
                        val addData = task.result.additionalUserInfo
                        if (addData != null) {
                            if (addData.isNewUser) {
                                showNewUserAlert(recentLoggedInUser)
                            } else signIn(recentLoggedInUser.uid)
                        } else {
                            oneTapClient.signOut()
                            auth.signOut()
                            showError(5)
                        }
                    } else {
                        oneTapClient.signOut()
                        auth.signOut()
                        showError(6)
                    }
                } else showError(7)
            }
    }

    private fun signIn(userId: String) {
        loadingDialog.showDialog()
        rdb.child(RDBR_USER).child(userId).get()
            .addOnCompleteListener {
                if (it.isSuccessful && it.result.exists()) {
                    try {
                        val verifiedUser = it.result.getValue(User::class.java)
                        setUser(verifiedUser!!)
                        loadingDialog.dismissDialog()
                        goto<MainActivity>(clearStack = true)
                    } catch (_: Error) {
                        oneTapClient.signOut()
                        auth.signOut()
                        loadingDialog.dismissDialog()
                        showError(8)
                    }
                } else {
                    oneTapClient.signOut()
                    auth.signOut()
                    loadingDialog.dismissDialog()
                    showError(9)
                }
            }
    }

    private fun showUserInfoDialog(user: FirebaseUser) {
        val dialogView = layoutInflater.inflate(R.layout.layout_user_setup, null)
        val nameView = dialogView.findViewById<TextInputEditText>(R.id.tiName)
        val emailView = dialogView.findViewById<TextInputEditText>(R.id.tiEmail)
        val rfidView = dialogView.findViewById<TextInputEditText>(R.id.tiRfid)
        val continueBtn = dialogView.findViewById<MaterialButton>(R.id.continueBtn)

        emailView.txt(user.email!!)

        val dialogBuilder = AlertDialog.Builder(this, R.style.RoundedCornersDialog)
            .setTitle("User Information")
            .setView(dialogView)
            .setCancelable(false)

        val customDialog = dialogBuilder.create().apply {
            setCanceledOnTouchOutside(false)
        }

        continueBtn.onClick {
            if (nameView.text.isNullOrBlank()) {
                nameView.error = "Enter your name"
                return@onClick
            }
            if (rfidView.text.isNullOrBlank()) {
                rfidView.error = "Enter RFID number"
                return@onClick
            }

            val newUser = User().apply {
                uid = user.uid
                name = nameView.text!!.toString()
                email = user.email!!
                rfid = rfidView.text!!.toString()
            }

            customDialog.dismissDialog()

            createAccount(newUser)

        }

        customDialog.window?.let {
            val layoutParams = it.attributes.apply {
                dimAmount = 0.7f
            }
            it.attributes = layoutParams
            it.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        }

        customDialog.showDialog()
    }

    private fun createAccount(user: User) {
        loadingDialog.showDialog()

        // TODO: check for duplicate RFID number

        rdb.child(RDBR_USER).child(user.uid).setValue(user)
            .addOnCompleteListener {
                loadingDialog.dismissDialog()
                if (it.isSuccessful) {
                    setUser(user)
                    loadingDialog.dismissDialog()
                    goto<MainActivity>(clearStack = true)
                } else {
                    val curUser = auth.currentUser
                    if (curUser != null) {
                        curUser.delete().addOnCompleteListener {
                            oneTapClient.signOut()
                            auth.signOut()
                            loadingDialog.dismissDialog()
                            showError(10)
                        }
                    } else {
                        oneTapClient.signOut()
                        auth.signOut()
                        loadingDialog.dismissDialog()
                        showError(11)
                    }
                }
            }
    }

    private fun showNewUserAlert(user: FirebaseUser) {
        showInfoDialog(
            "Welcome to ${getString(R.string.app_name)}",
            getString(R.string.welcome_msg),
            "Ok",
            R.raw.lottie_welcome
        ) {
            showUserInfoDialog(user)
        }.showDialog()
    }

    private fun showError(s: Int) {
        errorDialog(
            "Login Error $s",
            "Failed to login with the selected account. Please try again in a few minutes.",
            "Ok"
        ) {
            binding.loginBtn.isEnabled = true
        }.showDialog()
    }

}