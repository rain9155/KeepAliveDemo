package com.example.keepalivedemo.utils

import android.os.Build
import android.util.Log

/**
 *
 * @author chenjianyu
 * @date 2020/9/23
 */
object RomUtil {

    private val ROM_HUAWE = arrayOf("huawei", "honor")
    private val ROM_XIAOMI = arrayOf("xiaomi")
    private val ROM_VIVO = arrayOf("vivo")
    private val ROM_OPPO = arrayOf("oppo")
    private val ROM_MEIZU = arrayOf("meizu")
    private val ROM_SAMSUNG = arrayOf("samsung")
    private val ROM_SMARTISAN = arrayOf("smartisan")


    fun isHuawei() = isRightRom(ROM_HUAWE)

    fun isXiaomi() = isRightRom(ROM_XIAOMI)

    fun isVivo() = isRightRom(ROM_VIVO)

    fun isOppo() = isRightRom(ROM_OPPO)

    fun isMeizu() = isRightRom(ROM_MEIZU)

    fun isSamsung() = isRightRom(ROM_SAMSUNG)

    fun isSmartisan() = isRightRom(ROM_SMARTISAN)

    private fun isRightRom(roms: Array<String>): Boolean{
        val brand = Build.BRAND//获取主板
        val manufacturer = Build.MANUFACTURER//获取硬件制造商
        for (rom in roms){
            if(rom.equals(brand, true) || rom.equals(manufacturer, true)){
                return true
            }
        }
        return false
    }
}