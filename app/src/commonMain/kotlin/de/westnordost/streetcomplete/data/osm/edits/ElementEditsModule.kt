package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.osm.edits.upload.ElementEditUploader
import de.westnordost.streetcomplete.data.osm.edits.upload.ElementEditsUploader
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsDao
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsManager
import org.koin.dsl.module

val elementEditsModule = module {
    factory { ElementEditUploader(get(), get(), get()) }

    factory { ElementEditsDao(get(), get()) }
    factory { ElementIdProviderDao(get()) }
    factory { OpenChangesetsDao(get()) }
    factory { EditElementsDao(get()) }

    single { OpenChangesetsManager(get(), get(), get(), get()) }

    single { ElementEditsUploader(get(), get(), get(), get(), get(), get()) }

    single<ElementEditsSource> { get<ElementEditsController>() }
    single { ElementEditsController(get(), get(), get(), get()) }
    single { MapDataWithEditsSource(get(), get(), get()) }
}
