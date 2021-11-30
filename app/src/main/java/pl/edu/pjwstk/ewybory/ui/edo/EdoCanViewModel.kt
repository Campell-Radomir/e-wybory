package pl.edu.pjwstk.ewybory.ui.edo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EdoCanViewModel : ViewModel() {
    private val can = MutableLiveData<String>()

    val storedCan: LiveData<String> get() = can


    fun setCan(input: String) {
        can.value = input
    }
}