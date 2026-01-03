package de.westnordost.streetcomplete.quests.camping

import de.westnordost.streetcomplete.quests.camping.Campers.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_camp_type_caravans
import de.westnordost.streetcomplete.resources.quest_camp_type_cabins
import de.westnordost.streetcomplete.resources.quest_camp_type_tents
import org.jetbrains.compose.resources.StringResource

sealed interface CampTypeAnswer {
}

enum class Campers(val tag: String): CampTypeAnswer {
    CABINS("cabins"),
    TENTS("tents"),
    CARAVANS("caravans"),
}

val Campers.text: StringResource get() = when (this) {
    CABINS -> Res.string.quest_camp_type_cabins
    TENTS -> Res.string.quest_camp_type_tents
    CARAVANS -> Res.string.quest_camp_type_caravans
}
