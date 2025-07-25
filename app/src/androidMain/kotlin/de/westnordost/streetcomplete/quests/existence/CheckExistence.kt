package de.westnordost.streetcomplete.quests.existence

import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.LAST_CHECK_DATE_KEYS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateCheckDate
import de.westnordost.streetcomplete.util.ktx.containsAll

class CheckExistence(
    private val getFeature: (Element) -> Feature?
) : OsmElementQuestType<Unit>, AndroidQuest {

    private val nodesFilter by lazy { """
        nodes with ((
          (
            amenity = atm
            or amenity = telephone
            or amenity = vending_machine and vending !~ fuel|parking_tickets|public_transport_tickets
            or amenity = parcel_locker
            or amenity = public_bookcase
            or amenity = give_box
            or barrier = log
          )
          and (${lastChecked(2.0)})
        ) or (
          (
            amenity = clock
            or amenity = post_box
            or leisure = picnic_table
            or amenity = bbq
            or amenity = car_sharing
            or leisure = firepit
            or (leisure = pitch and sport ~ table_tennis|chess|table_soccer|teqball)
            or leisure = fitness_station
            or amenity = grit_bin and seasonal = no
            or amenity = vending_machine and vending ~ parking_tickets|public_transport_tickets
            or amenity = ticket_validator
            or tourism = information and information ~ board|terminal|map
            or advertising ~ column|board|poster_box
            or (highway = emergency_access_point or emergency = access_point) and ref
            or emergency ~ life_ring|phone
            or emergency = defibrillator and (indoor = no or access = yes)
            or (
              man_made = surveillance and surveillance:type = camera and surveillance ~ outdoor|public
              and !highway
            )
          )
          and (${lastChecked(4.0)})
        ) or (
          (
            amenity = bench
            or amenity = lounger
            or amenity = waste_basket
            or amenity = recycling and recycling_type = container
            or amenity = toilets
            or amenity = drinking_water
            or man_made = planter
          )
          and (${lastChecked(6.0)})
        ) or (
          (
            amenity ~ bicycle_parking|motorcycle_parking|taxi
          )
          and (${lastChecked(10.0)})
        ) or (
          (
            traffic_calming ~ bump|mini_bumps|hump|cushion|rumble_strip|dip|double_dip
            or traffic_calming = table and !highway and !crossing
          )
          and (${lastChecked(14.0)})
        ))
        and access !~ no|private
        and (!seasonal or seasonal = no)
        and (!intermittent or intermittent = no)
        and (!permanent or permanent = yes)
    """.toElementFilterExpression() }
    // - traffic_calming = table is often used as a property of a crossing: we don't want the app
    //    to delete the crossing if the table is not there anymore, so exclude that
    // - postboxes are in 4 years category so that postbox collection times is asked instead more often
    // - bicycle parkings, motorcycle parkings have capacity quests asked every
    //    few years already, so if it's gone now, it will be noticed that way.
    //    But some users disable this quests as spammy or boring or unimportant,
    //    so asking about this anyway would be a good idea.

    override val changesetComment = "Survey if places still exist"
    override val wikiLink: String? = null
    override val icon = R.drawable.ic_quest_check
    override val achievements = listOf(CITIZEN, OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_existence_title2

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element) =
        nodesFilter.matches(element) && getFeature(element) != null

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry): Sequence<Element> {
        /* put markers for objects that are exactly the same as for which this quest is asking for
           e.g. it's a ticket validator? -> display other ticket validators. Etc. */
        val feature = getFeature(element) ?: return emptySequence()
        return getMapData().filter { it.tags.containsAll(feature.tags) }.asSequence()
    }

    override fun createForm() = CheckExistenceForm()

    override fun applyAnswerTo(answer: Unit, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateCheckDate()
    }

    private fun lastChecked(yearsAgo: Double): String = """
        older today -$yearsAgo years
        or ${LAST_CHECK_DATE_KEYS.joinToString(" or ") { "$it < today -$yearsAgo years" }}
    """.trimIndent()
}
