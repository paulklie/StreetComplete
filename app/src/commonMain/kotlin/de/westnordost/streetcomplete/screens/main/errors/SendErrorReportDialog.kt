package de.westnordost.streetcomplete.screens.main.errors

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.crash_compose_email
import de.westnordost.streetcomplete.resources.crash_message
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import org.jetbrains.compose.resources.stringResource

/** Dialog that asks user to send a crash report to the developer */
@Composable
fun SendErrorReportDialog(
    onDismissRequest: () -> Unit,
    onConfirmed: () -> Unit,
    title: String,
    copy: () -> Unit,
) {
    ConfirmationDialog(
        onDismissRequest = onDismissRequest,
        onConfirmed = onConfirmed,
        title = { Text(title) },
        text = {
            Text(stringResource(Res.string.crash_message))
            Button(copy) {
                Text(androidx.compose.ui.res.stringResource(android.R.string.copy)) }
            },
        confirmButtonText = stringResource(Res.string.crash_compose_email),
        // should be more of a modal dialog
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
}
