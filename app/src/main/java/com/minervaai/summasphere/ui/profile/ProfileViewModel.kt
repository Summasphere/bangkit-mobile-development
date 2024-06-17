import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minervaai.summasphere.helper.GoogleLoginHelper
import com.minervaai.summasphere.helper.LogoutUtils
import com.minervaai.summasphere.helper.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class ProfileFragmentViewModel(private val context: Context) : ViewModel() {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    fun logout(googleSignInHelper: GoogleLoginHelper): LiveData<ResultState<Unit>> {
        val logoutState = MutableLiveData<ResultState<Unit>>()
        logoutState.value = ResultState.Loading

        viewModelScope.launch {
            try {
                val googleLogoutSuccess = withContext(Dispatchers.IO) {
                    googleSignInHelper.logOut()
                }
                if (googleLogoutSuccess) {
                    logoutState.postValue(ResultState.Success(Unit))
                } else {
                    logoutState.postValue(ResultState.Error("Failed to log out"))
                }
            } catch (e: HttpException) {
                logoutState.postValue(ResultState.Error("Network error: ${e.message()}"))
            } catch (e: IOException) {
                logoutState.postValue(ResultState.Error("IO error: ${e.message}"))
            }
        }
        return logoutState
    }

    fun logoutWithEmailPassword(): LiveData<ResultState<Unit>> {
        val logoutState = MutableLiveData<ResultState<Unit>>()
        logoutState.value = ResultState.Loading

        viewModelScope.launch {
            try {
                val token = withContext(Dispatchers.IO) {
                    sharedPreferences.getString("token", null)
                }
                if (!token.isNullOrEmpty()) {
                    val emailLogoutSuccess = withContext(Dispatchers.IO) {
                        LogoutUtils.emailPasswordLogout(token)
                    }

                    if (emailLogoutSuccess) {
                        logoutState.postValue(ResultState.Success(Unit))
                    } else {
                        logoutState.postValue(ResultState.Error("Failed to log out"))
                    }
                } else {
                    logoutState.postValue(ResultState.Error("Token is null or empty"))
                }
            } catch (e: HttpException) {
                logoutState.postValue(ResultState.Error("Network error: ${e.message()}"))
            } catch (e: IOException) {
                logoutState.postValue(ResultState.Error("IO error: ${e.message}"))
            }
        }
        return logoutState
    }
}
