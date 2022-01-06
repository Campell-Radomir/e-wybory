package pl.edu.pjwstk.ewybory

import org.jmrtd.BACKey

class DocumentKey {

    private var documentNumber: String = ""
    private var birthDate: String = ""
    private var expirationDate: String = ""

    fun setDocumentNumber(value: String) {
        documentNumber = value;
    }

    fun setBirthDate(value: String) {
        birthDate = value;
    }

    fun setExpirationDate(value: String) {
        expirationDate = value;
    }

    fun isPrepared(): Boolean {
        return documentNumber.isNotBlank()
                && expirationDate.isNotBlank()
                && birthDate.isNotBlank()
    }

    fun createBACKey(): BACKey {
        return BACKey(documentNumber, birthDate, expirationDate)
    }
}