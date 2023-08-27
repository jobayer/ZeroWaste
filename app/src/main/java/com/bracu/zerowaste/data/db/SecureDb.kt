package com.bracu.zerowaste.data.db

import android.app.Application
import android.content.SharedPreferences
import android.net.Uri
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.bracu.zerowaste.Const.DB_NAME
import com.bracu.zerowaste.data.models.MiscData
import com.bracu.zerowaste.data.models.User
import com.google.gson.Gson

object SecureDb {

    private lateinit var pref: SharedPreferences

    private const val DBK_USER_INFO = "ZWUser"
    private const val DBK_MISC_DATA = "ZWMiscData"
    private const val DBK_PROFILE_PIC = "ZWProfilePic"

    fun init(application: Application) {
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
        pref = EncryptedSharedPreferences.create(
            DB_NAME,
            mainKeyAlias,
            application,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getLoggedInUser(): User? {
        val data = pref.getString(DBK_USER_INFO, null)
        var returnData: User? = null
        data?.let {
            returnData = try {
                Gson().fromJson(it, User::class.java)
            } catch (_: Exception) {
                null
            }
        }
        return returnData
    }

    fun setLoggedInUser(user: User) =
        pref.edit().putString(DBK_USER_INFO, Gson().toJson(user)).apply()

    fun getMiscData(): MiscData {
        val data = pref.getString(DBK_MISC_DATA, null)
        var returnData = MiscData()
        data?.let {
            returnData = try {
                Gson().fromJson(it, MiscData::class.java)
            } catch (_: Exception) {
                MiscData()
            }
        }
        return returnData
    }

    fun setMiscData(data: MiscData) =
        pref.edit().putString(DBK_MISC_DATA, Gson().toJson(data)).apply()

    fun getProfilePic(): Uri? {
        val data = pref.getString(DBK_PROFILE_PIC, null)
        var returnData: Uri? = null
        data?.let {
            returnData = try {
                Uri.parse(data)
            } catch (_: Exception) {
                null
            }
        }
        return returnData
    }

    fun setProfilePic(img: Uri) =
        pref.edit().putString(DBK_PROFILE_PIC, img.toString()).apply()

}

fun getUser() = SecureDb.getLoggedInUser()

fun setUser(user: User) = SecureDb.setLoggedInUser(user)

fun getMiscData() = SecureDb.getMiscData()

fun setMiscData(data: MiscData) = SecureDb.setMiscData(data)

fun getProfilePic() = SecureDb.getProfilePic()

fun setProfilePic(img: Uri) = SecureDb.setProfilePic(img)