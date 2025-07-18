package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.util.ktx.toShortString

sealed interface Length {
    fun toMeters(): Double
    fun toOsmValue(): String

    data class Meters(val meters: Double) : Length {
        override fun toMeters() = meters
        override fun toOsmValue() = meters.toShortString()
    }

    data class FeetAndInches(val feet: Int, val inches: Int) : Length {
        override fun toMeters() = (feet * 12 + inches) * 0.0254
        override fun toOsmValue() = "$feet'$inches\""
    }
}

val Length.unit: LengthUnit get() = when (this) {
    is Length.FeetAndInches -> LengthUnit.FOOT_AND_INCH
    is Length.Meters -> LengthUnit.METER
}
