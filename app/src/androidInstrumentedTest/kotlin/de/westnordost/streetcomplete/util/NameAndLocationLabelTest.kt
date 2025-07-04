package de.westnordost.streetcomplete.util

import android.content.res.Configuration
import android.content.res.Resources
import androidx.test.platform.app.InstrumentationRegistry
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.osmfeatures.create
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class NameAndLocationLabelTest {
    private var featureDictionary: FeatureDictionary
    private var englishResources: Resources

    init {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        featureDictionary = FeatureDictionary.create(context.assets, "osmfeatures/default", "osmfeatures/brands")

        val conf = Configuration(context.resources.configuration)
        conf.setLocale(Locale.ENGLISH)
        val localizedContext = context.createConfigurationContext(conf)
        englishResources = localizedContext.resources
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2512
    @Test fun newspaperVendingMachineWithName() {
        assertEquals("Bild (Newspaper\u00A0Vending\u00A0Machine)", getQuestLabelForNode(mapOf(
            "amenity" to "vending_machine",
            "vending" to "newspapers",
            "name" to "Bild",
        )))
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2512
    @Test fun newspaperVendingMachineWithBrand() {
        assertEquals("Abendzeitung (Newspaper\u00A0Vending\u00A0Machine)", getQuestLabelForNode(mapOf(
            "amenity" to "vending_machine",
            "vending" to "newspapers",
            "brand" to "Abendzeitung",
        )))
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2640
    @Test fun postBox() {
        assertEquals("Deutsche\u00A0Post (Mail\u00A0Drop\u00A0Box)", getQuestLabelForNode(mapOf(
            "amenity" to "post_box",
            "brand" to "Deutsche Post",
            "operator" to "Deutsche Post AG",
            "ref" to "Hauptsmoorstr. 101, 96052 Bamberg",
        )))
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2806
    @Test fun namedBench() {
        assertEquals("Sergey's\u00A0Seat (Bench)", getQuestLabelForNode(mapOf(
            "amenity" to "bench",
            "name" to "Sergey's Seat",
            "ref" to "600913",
            "brand" to "Google",
            "operator" to "Google RESTful",
        )))
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2806
    @Test fun unnamedBench() {
        assertEquals("Bench", getQuestLabelForNode(mapOf(
            "amenity" to "bench",
        )))
    }

    // https://github.com/streetcomplete/StreetComplete/issues/2840#issuecomment-831245075
    @Test fun schoki() {
        assertEquals("Schoko\u00A0Lädchen\u00A0[3680] (Vending\u00A0Machine)", getQuestLabelForNode(mapOf(
            "amenity" to "vending_machine",
            "ref" to "3680",
            "operator" to "Schoko Lädchen",
        )))
    }

    // https://github.com/streetcomplete/StreetComplete/issues/5549
    @Test fun pointNotVertex() {
        assertEquals("Bollard", getQuestLabelForNode(mapOf(
            "barrier" to "bollard",
        )))
    }

    // https://github.com/streetcomplete/StreetComplete/issues/5427
    @Test fun roadWithName() {
        assertEquals("Main\u00A0Street (Residential\u00A0Road)", getQuestLabelForWay(mapOf(
            "highway" to "residential",
            "name" to "Main Street",
            "operator" to "Road Agency",
        )))
    }

    @Test fun roadWitRef() {
        assertEquals("A1 (Residential\u00A0Road)", getQuestLabelForWay(mapOf(
            "highway" to "residential",
            "ref" to "A1",
            "operator" to "Road Agency",
        )))
    }

    @Test fun roadWithNameAndRef() {
        assertEquals("Main\u00A0Street\u00A0[A1] (Residential\u00A0Road)", getQuestLabelForWay(mapOf(
            "highway" to "residential",
            "name" to "Main Street",
            "ref" to "A1",
            "operator" to "Road Agency",
        )))
    }

    private fun getQuestLabelForNode(tags: Map<String, String>): String? =
        getNameAndLocationSpanned(
            Node(0, LatLon(0.0, 0.0), tags),
            englishResources,
            featureDictionary
        )?.toString()

    private fun getQuestLabelForWay(tags: Map<String, String>): String? =
        getNameAndLocationSpanned(
            Way(0, listOf(), tags),
            englishResources,
            featureDictionary
        )?.toString()
}
