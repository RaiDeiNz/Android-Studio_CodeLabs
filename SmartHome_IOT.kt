import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class SmartDevice(val name: String, val category: String) {

    var deviceStatus = "online"
        protected set

    open val deviceType = "unknown"

    open fun turnOn() {
        deviceStatus = "on"
    }

    open fun turnOff() {
        deviceStatus = "off"
    }

    open fun printDeviceInfo() {
        println("Device name: $name, category: $category, type: $deviceType")
    }
}

class SmartTvDevice(deviceName: String, deviceCategory: String) :
    SmartDevice(name = deviceName, category = deviceCategory) {

    override val deviceType = "Smart TV"

    private var speakerVolume by RangeRegulator(initialValue = 9, minValue = 0, maxValue = 100)
    private var channelNumber by RangeRegulator(initialValue = 199, minValue = 0, maxValue = 200)

    fun increaseSpeakerVolume() {
        speakerVolume++
        println("Speaker volume increased to $speakerVolume.")
    }

    fun decreaseVolume() {
        speakerVolume--
        println("Speaker volume decreased to $speakerVolume.")
    }

    fun nextChannel() {
        if (channelNumber == 200) {
            channelNumber = 0
            println("Homepage Menu")
        } else {
            channelNumber++
            println("Channel number increased to $channelNumber.")
        }
    }

    fun previousChannel() {
        when {
            channelNumber == 1 -> {
                channelNumber = 0
                println("Homepage Menu")
            }
            channelNumber == 0 -> {
                channelNumber = 200
                println("Channel number increased to $channelNumber.")
            }
            else -> {
                channelNumber--
                println("Channel number decreased to $channelNumber.")
            }
        }
    }

    override fun turnOn() {
        super.turnOn()
        println(
            "$name is turned on. Speaker volume is set to $speakerVolume and channel number is set to $channelNumber."
        )
    }

    override fun turnOff() {
        super.turnOff()
        println("$name turned off")
    }
}

class SmartLightDevice(deviceName: String, deviceCategory: String) :
    SmartDevice(name = deviceName, category = deviceCategory) {

    override val deviceType = "Smart Light"

    internal var brightnessLevel by RangeRegulator(initialValue = 1, minValue = 0, maxValue = 100)
    private var previousBrightnessLevel: Int = 1

    fun increaseBrightness() {
        brightnessLevel++
        println("Brightness increased to $brightnessLevel%.")
    }

    fun decreaseBrightness() {
        if (brightnessLevel > 0) {
            if (brightnessLevel == 1) {
                previousBrightnessLevel = 1
            } else {
                previousBrightnessLevel = brightnessLevel
            }
            brightnessLevel--
            println("Brightness decreased to $brightnessLevel%.")
        } else {
            println("The lights are off, you must turn them on first to increase the brightness")
        }
    }

    override fun turnOn() {
        super.turnOn()
        brightnessLevel = if (previousBrightnessLevel == 0) 1 else previousBrightnessLevel
        println("$name turned on. The brightness level is $brightnessLevel%.")
    }

    override fun turnOff() {
        super.turnOff()
        if (brightnessLevel != 0) {
            previousBrightnessLevel = brightnessLevel
            brightnessLevel = 0
            println("$name turned off manually.")
        } else {
            println("$name turned off by decreasing.")
        }
    }
}

class SmartHome(
    private val smartTvDevice: SmartTvDevice,
    private val smartLightDevice: SmartLightDevice
) {
    var deviceTurnOnCount = 0
        private set

    fun turnOnTv() {
        if (smartTvDevice.deviceStatus != "on") {
            smartTvDevice.turnOn()
            deviceTurnOnCount++
        }
    }

    fun turnOffTv() {
        if (smartTvDevice.deviceStatus == "on") {
            smartTvDevice.turnOff()
            deviceTurnOnCount--
        }
    }

    fun turnOnLight() {
        if (smartLightDevice.deviceStatus != "on") {
            smartLightDevice.turnOn()
            deviceTurnOnCount++
        }
    }

    fun turnOffLight() {
        if (smartLightDevice.deviceStatus == "on") {
            smartLightDevice.turnOff()
            deviceTurnOnCount--
        }
    }

    fun increaseTvVolume() {
        if (smartTvDevice.deviceStatus == "on") {
            smartTvDevice.increaseSpeakerVolume()
        } else {
            println("TV is off. Cannot increase volume.")
        }
    }

    fun decreaseTvVolume() {
        if (smartTvDevice.deviceStatus == "on") {
            smartTvDevice.decreaseVolume()
        } else {
            println("TV is off. Cannot decrease volume.")
        }
    }

    fun changeTvChannelToNext() {
        if (smartTvDevice.deviceStatus == "on") {
            smartTvDevice.nextChannel()
        } else {
            println("TV is off. Cannot change channel.")
        }
    }

    fun changeTvChannelToPrevious() {
        if (smartTvDevice.deviceStatus == "on") {
            smartTvDevice.previousChannel()
        } else {
            println("TV is off. Cannot change channel.")
        }
    }

    fun increaseLightBrightness() {
        if (smartLightDevice.brightnessLevel == 0) {
            turnOnLight()
        } else {
            smartLightDevice.increaseBrightness()
        }
    }

    fun decreaseLightBrightness() {
        if (smartLightDevice.deviceStatus == "on") {
            smartLightDevice.decreaseBrightness()
            if (smartLightDevice.brightnessLevel == 0) {
                turnOffLight()
            }
        } else {
            println("The lights are off, you must turn them on first to increase the brightness")
        }
    }

    fun printSmartTvInfo() {
        smartTvDevice.printDeviceInfo()
    }

    fun printSmartLightInfo() {
        smartLightDevice.printDeviceInfo()
    }

    fun turnOffAllDevices() {
        turnOffTv()
        turnOffLight()
    }
}

class RangeRegulator(initialValue: Int, private val minValue: Int, private val maxValue: Int) :
    ReadWriteProperty<Any?, Int> {

    private var fieldData = initialValue

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return fieldData
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        if (value in minValue..maxValue) {
            fieldData = value
        }
    }
}

fun main() {
    val smartHome =
        SmartHome(
            smartTvDevice = SmartTvDevice("Samsung Android TV", "Entertainment"),
            smartLightDevice = SmartLightDevice("Hacker' Lights", "Utility")
        )

    println("----- Tv -----")
    smartHome.turnOnTv()
    smartHome.changeTvChannelToNext() // #200
    smartHome.changeTvChannelToNext() // HomePage Menu ( #0 )
    smartHome.changeTvChannelToNext() // #1
    smartHome.changeTvChannelToPrevious() // HomePage Menu
    smartHome.changeTvChannelToPrevious() // #200
	println("----- Sound -----")
    smartHome.increaseTvVolume()
    smartHome.increaseTvVolume()
    smartHome.decreaseTvVolume()
	smartHome.turnOffAllDevices() // When it's off
	smartHome.decreaseTvVolume() // Should print "TV is off. Cannot decrease volume."
    smartHome.increaseTvVolume()
    smartHome.turnOnTv()
    smartHome.increaseTvVolume() // It already remembers the last volume level, no need to write new code
    smartHome.decreaseTvVolume()
	println("-----")
    smartHome.printSmartTvInfo()
    println("-----\n")
    println("----- Light -----")
    smartHome.turnOnLight() // power Level #1
    smartHome.increaseLightBrightness() // #2
    smartHome.decreaseLightBrightness() // #1
    smartHome.decreaseLightBrightness() // #0 = Activating turn off method
    println("-----")
    smartHome.decreaseLightBrightness() // Still #0, else method must be work
    smartHome.increaseLightBrightness() // Lights should turn on automatically at level 1. Also tested before smartHome.turnOnLight() and off
    smartHome.increaseLightBrightness() // #2
    smartHome.increaseLightBrightness() // #2
    smartHome.increaseLightBrightness() // #3
    println("-----")
    smartHome.turnOffLight() // #3 but turned off
    smartHome.turnOnLight() // must be start at #3
    println("-----")
    smartHome.printSmartLightInfo()
    println("-----\n")

    println(
        """
=======================================
Total turned on devices: ${smartHome.deviceTurnOnCount}
    """
    )
    smartHome.turnOffAllDevices()
    println(
        """
Running device check: ${smartHome.deviceTurnOnCount} running device
======================================"""
    )
}
