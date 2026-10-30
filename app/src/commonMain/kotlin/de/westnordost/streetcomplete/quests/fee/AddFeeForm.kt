package de.westnordost.streetcomplete.quests.fee

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_generic_hasFeature_no
import de.westnordost.streetcomplete.resources.quest_generic_hasFeature_yes
import de.westnordost.streetcomplete.resources.quest_toiletsFee_fee_for_non_customers
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.LocalQuestType
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddFeeForm(on: (QuestAction<FeeAnswer>) -> Unit, title: String = stringResource(LocalQuestType.current!!.title)) {
    QuestForm(
        on = on,
        title = title,
        answers = listOf(
            AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { on(Answer(Fee(false))) },
            AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) { on(Answer(Fee(true))) },
        ),
        otherAnswers = { listOf(
            AnswerItem(stringResource(Res.string.quest_toiletsFee_fee_for_non_customers)) {
                on(Answer(FeeForNonCustomers))
            },
        ) }
    )
}
