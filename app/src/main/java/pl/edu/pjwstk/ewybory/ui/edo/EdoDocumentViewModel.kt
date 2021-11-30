package pl.edu.pjwstk.ewybory.ui.edo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EdoDocumentViewModel : ViewModel() {
    private val documentNumber = MutableLiveData<String>()
    private val birthDate = MutableLiveData<String>()
    private val expirationDate = MutableLiveData<String>()

    val storedDocumentNumber: LiveData<String> get() = documentNumber
    val storedBirthDate: LiveData<String> get() = birthDate
    val storedExpirationDate: LiveData<String> get() = expirationDate


    fun setDocumentNumber(input: String) {
        documentNumber.value = input
    }
    fun setBirthDate(input: String) {
        birthDate.value = input
    }
    fun setExpirationDate(input: String) {
        expirationDate.value = input
    }
}