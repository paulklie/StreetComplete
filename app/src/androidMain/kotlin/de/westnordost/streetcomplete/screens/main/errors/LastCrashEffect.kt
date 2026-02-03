package de.westnordost.streetcomplete.screens.main.errors

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.crash_title
import de.westnordost.streetcomplete.util.ktx.toast
import org.jetbrains.compose.resources.stringResource

/** Offer to report the last occurred crash */
@Composable
fun LastCrashEffect(
    lastReport: String,
    onReport: (errorReport: String) -> Unit
) {
    var showErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(lastReport) { showErrorDialog = true }
    val context = LocalContext.current

    if (showErrorDialog) {
        SendErrorReportDialog(
            onDismissRequest = { showErrorDialog = false },
            onConfirmed = { onReport(lastReport) },
            title = stringResource(Res.string.crash_title),
            copy = {
                val clip = ClipData.newPlainText("SCEE error message", lastReport)
                (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                context.toast("crash report copied to clipboard")
            }
        )
    }
}
