package pl.edu.pjwstk.ewybory

import org.jmrtd.PACEKeySpec

class CANKey {

    private var can: String = "";

    fun setCAN(value: String) {
        can = value;
    }

    fun isPrepared(): Boolean {
        return can.isNotBlank();
    }

    fun createPACEKey(): PACEKeySpec {
        return PACEKeySpec.createCANKey(can)
    }
}