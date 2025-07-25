package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.util.ktx.attribute
import de.westnordost.streetcomplete.util.ktx.attributeOrNull
import kotlinx.datetime.Instant
import kotlinx.io.Source
import kotlinx.serialization.SerializationException
import nl.adaptivity.xmlutil.EventType.END_ELEMENT
import nl.adaptivity.xmlutil.EventType.START_ELEMENT
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.core.kxio.newReader
import nl.adaptivity.xmlutil.xmlStreaming

class MapDataApiParser {
    fun parseMapData(
        source: Source,
        ignoreRelation: (tags: Map<String, String>) -> Boolean = { false }
    ): MutableMapData =
        xmlStreaming.newReader(source).parseMapData(ignoreRelation)

    fun parseElementUpdates(source: Source): Map<ElementKey, ElementUpdate> =
        xmlStreaming.newReader(source).parseElementUpdates()
}

private fun XmlReader.parseMapData(ignoreRelation: (tags: Map<String, String>) -> Boolean): MutableMapData = try {
    val result = MutableMapData()
    var tags: MutableMap<String, String>? = null
    var nodes: MutableList<Long> = ArrayList()
    var members: MutableList<RelationMember> = ArrayList()
    var id: Long? = null
    var position: LatLon? = null
    var version: Int? = null
    var timestamp: Long? = null

    forEach { when (it) {
        START_ELEMENT -> when (localName) {
            "tag" -> {
                if (tags == null) tags = HashMap()
                tags!![attribute("k")] = attribute("v")
            }
            "nd" -> nodes.add(attribute("ref").toLong())
            "member" -> members.add(
                RelationMember(
                type = ElementType.valueOf(attribute("type").uppercase()),
                ref = attribute("ref").toLong(),
                role = attribute("role")
            )
            )
            "bounds" -> result.boundingBox = BoundingBox(
                minLatitude = attribute("minlat").toDouble(),
                minLongitude = attribute("minlon").toDouble(),
                maxLatitude = attribute("maxlat").toDouble(),
                maxLongitude = attribute("maxlon").toDouble()
            )
            "node", "way", "relation" -> {
                tags = null
                id = attribute("id").toLong()
                version = attribute("version").toInt()
                timestamp = Instant.parse(attribute("timestamp")).toEpochMilliseconds()
                when (localName) {
                    "node" -> position = LatLon(attribute("lat").toDouble(), attribute("lon").toDouble())
                    "way" -> nodes = ArrayList()
                    "relation" -> members = ArrayList()
                }
            }
        }
        END_ELEMENT -> when (localName) {
            "node" -> result.add(Node(id!!, position!!, tags.orEmpty(), version!!, timestamp!!))
            "way" -> result.add(Way(id!!, nodes, tags.orEmpty(), version!!, timestamp!!))
            "relation" -> if (!ignoreRelation(tags.orEmpty())) {
                result.add(Relation(id!!, members, tags.orEmpty(), version!!, timestamp!!))
            }
        }
        else -> {}
    } }
    result
} catch (e: Exception) { throw SerializationException(e) }

private fun XmlReader.parseElementUpdates(): Map<ElementKey, ElementUpdate> = try {
    val result = HashMap<ElementKey, ElementUpdate>()
    forEach {
        if (it == START_ELEMENT) {
            when (localName) {
                "node", "way", "relation" -> {
                    val key = ElementKey(
                        ElementType.valueOf(localName.uppercase()),
                        attribute("old_id").toLong()
                    )
                    val newId = attributeOrNull("new_id")?.toLong()
                    val newVersion = attributeOrNull("new_version")?.toInt()
                    val action =
                        if (newId != null && newVersion != null) ElementUpdate.Update(newId, newVersion)
                        else ElementUpdate.Delete

                    result[key] = action
                }
            }
        }
    }
    result
} catch (e: Exception) { throw SerializationException(e) }
