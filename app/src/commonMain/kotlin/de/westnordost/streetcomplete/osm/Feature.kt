package de.westnordost.streetcomplete.osm

import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.osm.places.isDisusedPlace
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.allDrawableResources
import org.jetbrains.compose.resources.DrawableResource

/** Apply this feature to the given [tags], optionally removing a [previousFeature] first, i.e.
 *  replacing it. */
fun Feature.applyTo(tags: Tags, previousFeature: Feature? = null) {
    if (previousFeature != null) {
        for ((key, value) in previousFeature.removeTags) {
            if (tags[key] == value) tags.remove(key)
        }
        for (key in previousFeature.removeTagKeys) {
            tags.remove(key)
        }
    }
    for ((key, value) in addTagKeys.associateWith { "yes" } + addTags) {
        if (key !in tags || preserveTags.none { it.containsMatchIn(key) }) {
            tags[key] = value
        }
    }
}

/** Return an exemplary element that would match this feature. */
fun Feature.toElement(): Element {
    val allTags = tagKeys.associateWith { "yes" } + tags
    return when {
        GeometryType.POINT in geometry ||
        GeometryType.VERTEX in geometry -> {
            Node(-1L, NULL_ISLAND, allTags)
        }
        GeometryType.LINE in geometry || GeometryType.AREA in geometry -> {
            Way(-1L, NULL_ISLAND_NODES, allTags)
        }
        GeometryType.RELATION in geometry -> {
            Relation(-1L, listOf(), allTags)
        }
        else -> {
            Node(-1L, NULL_ISLAND, allTags)
        }
    }
}

/** return the id of the feature, without any brand stuff */
val Feature.featureId get() = if (isSuggestion) id.substringBeforeLast("/") else id

/** Whether this feature is a subtype of another feature */
fun Feature.isChildOf(other: Feature): Boolean =
    id.startsWith(other.id)

/** return whether the feature has a fixed name which should not be changed */
val Feature.hasFixedName get() =
    addTags.containsKey("name") && preserveTags.none { it.containsMatchIn("name") }
        || isDisusedPlace()

/** Return the drawable resource of this feature's icon, if any */
val Feature.iconDrawableResource: DrawableResource? get() {
    val iconResourceName = icon?.let { "preset_" + it.replace('-', '_') }
    return Res.allDrawableResources[iconResourceName]
}

/** Return the wiki URL for this feature, using localized wiki pages from the given entities if available. */
fun Feature.getWikiUrl(
    langCodes: List<String>,
    rtypeEntity: WikiEntity? = null,
    tagEntity: WikiEntity? = null,
    keyEntity: WikiEntity? = null
): String? {
    if(isSuggestion)
        return null
    val wikis = listOfNotNull(
        rtypeEntity?.monolingualP31Claims(),
        tagEntity?.monolingualP31Claims(),
        keyEntity?.monolingualP31Claims()
    )

    for (wiki in wikis) {
        for (code in langCodes) {
            val title = wiki[code.lowercase()]
            if (!title.isNullOrBlank()) return "https://wiki.openstreetmap.org/wiki/$title"
        }
    }

    for (wiki in wikis) {
        val title = wiki.values.firstOrNull { it.isNotBlank() }
        if (title != null) return "https://wiki.openstreetmap.org/wiki/$title"
    }

    val entry = tags.entries.firstOrNull()?.let { it.key to it.value }
        ?: tagKeys.firstOrNull()?.let { it to "yes" }
        ?: return "https://wiki.openstreetmap.org/wiki/Main_Page"
    val (key, value) = entry

    return if (value == "yes") {
        "https://wiki.openstreetmap.org/wiki/Key:$key"
    } else {
        "https://wiki.openstreetmap.org/wiki/Tag:$key=$value"
    }
}

data class WikiEntity(val claims: Map<String, List<WikiStatement>> = emptyMap())
data class WikiStatement(val mainsnak: WikiMainSnak?)
data class WikiMainSnak(val datavalue: WikiDataValue?)
data class WikiDataValue(val value: WikiMonolingualValue?)
data class WikiMonolingualValue(val language: String?, val text: String?)

private fun WikiEntity.monolingualP31Claims(): Map<String, String> {
    val claims = claims["P31"] ?: return emptyMap()
    val acc = mutableMapOf<String, String>()
    for (stmt in claims) {
        val mv = stmt.mainsnak?.datavalue?.value
        if (mv?.language != null && mv.text != null) {
            acc[mv.language.lowercase()] = mv.text
        }
    }
    return acc
}

private val NULL_ISLAND = LatLon(0.0, 0.0)
private val NULL_ISLAND_NODES = listOf(-1L, -2L, -3L, -1L)
