package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.resources.Res
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import de.westnordost.streetcomplete.resources.*

class UnknownEditType() : EditType {
    override val icon: DrawableResource = Res.drawable.quest_notes
    override val title: StringResource = Res.string.unknown_quest
    override val wikiLink: String? = null
    override val achievements: List<EditTypeAchievement> = emptyList()
}
