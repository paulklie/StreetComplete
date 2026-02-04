package de.westnordost.streetcomplete.quests

import android.content.Context
import android.text.InputType
import android.text.method.LinkMovementMethod
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.core.widget.doAfterTextChanged
import com.github.difflib.text.DiffRow.Tag
import com.github.difflib.text.DiffRowGenerator
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.ParseException
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.common.dialogs.ScrollableAlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.PatternSyntaxException

fun OsmElementQuestType<*>.getPrefixedFullElementSelectionPref(prefs: Preferences) = "${questPrefix(prefs)}qs_${name}_full_element_selection"

fun questPrefix(prefs: Preferences) = if (prefs.getBoolean(Prefs.QUEST_SETTINGS_PER_PRESET, false))
    prefs.getLong(Preferences.SELECTED_EDIT_TYPE_PRESET, 0).toString() + "_"
else
    ""

fun getLabelSources(defaultValue: String, questType: OsmFilterQuestType<*>, prefs: Preferences) =
    prefs.getString(getPrefixedLabelSourcePref(questType, prefs),
        defaultValue
    ).split(",").map { it.trim() }

fun getPrefixedLabelSourcePref(questType: OsmElementQuestType<*>, prefs: Preferences) = "${questPrefix(prefs)}qs_${questType.name}_label_sources"

// old dialog, to be removed
fun fullElementSelectionDialog(context: Context, prefs: Preferences, pref: String, messageId: Int, defaultValue: String): AlertDialog {
    val textInput = EditText(context)
    val checkPrefix = if (pref.endsWith("_full_element_selection")) "" else "nodes with "

    val message = HtmlCompat.fromHtml(context.getString(messageId), HtmlCompat.FROM_HTML_MODE_LEGACY)

    val dialog = dialog(context, messageId, prefs.getString(pref, defaultValue.trimIndent()), textInput)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            if (textInput.text.toString() == prefs.getString(pref, defaultValue.trimIndent())) return@setPositiveButton
            prefs.putString(pref, textInput.text.toString())
            OsmQuestController.reloadQuestTypes()
        }
        .setNeutralButton(R.string.quest_settings_reset) { _, _ ->
            prefs.remove(pref)
            OsmQuestController.reloadQuestTypes()
        }
        .setMessage(message)
        .setView(LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(textInput)
            if (prefs.contains(pref))
                addView(getDiffButton(context, defaultValue) { textInput.text.toString() })
        })
        .create()
    textInput.doAfterTextChanged {
        val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val isValidFilterExpression by lazy {
            try {
                (checkPrefix + it).toElementFilterExpression()
                toastyJob?.cancel()
                true
            } catch(e: ParseException) {
                delayedToast(e.message, context)
                false
            } catch(e: PatternSyntaxException) {
                delayedToast(e.message, context)
                false
            }
        }
        button?.isEnabled = textInput.text.toString().let {
            // check other stuff first, because creation filter expression is relatively slow
            (checkPrefix.isEmpty() || it.lowercase().matches(elementSelectionRegex))
                && it.count { c -> c == '('} == it.count { c -> c == ')'}
                && (it.contains('=') || it.contains('~') || it.contains('!'))
                && isValidFilterExpression
        }
    }
    dialog.setOnShowListener {
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.isEnabled = prefs.contains(pref) // disable reset button if setting is default
        dialog.findViewById<TextView>(android.R.id.message)?.movementMethod = LinkMovementMethod.getInstance() // make the link actually open a browser
    }
    return dialog
}

/** for setting values of a single positive number */
@Composable fun NumberSelectionDialog(prefs: Preferences, pref: String, defaultValue: Int, messageId: Int, onDismissRequest: () -> Unit) {
    var text by remember {
        mutableStateOf(TextFieldValue(prefs.getInt(pref, defaultValue).toString()))
    }
    var isOk by remember { mutableStateOf(true) }
    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        content = {
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(
                    text = AnnotatedString.fromHtml(stringResource(messageId)),
                    style = MaterialTheme.typography.body1
                )
                TextField(
                    value = text,
                    onValueChange = {
                        isOk = it.text.toIntOrNull()?.let { it > 0 } == true
                        text = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        buttons = {
            ResetCancelOk(
                onDismissRequest = onDismissRequest,
                resetEnabled = prefs.contains(pref),
                onReset = { prefs.remove(pref); OsmQuestController.reloadQuestTypes() },
                okEnabled = isOk,
                onOk = {
                    val number = text.text.toIntOrNull() ?: return@ResetCancelOk
                    if (number == prefs.getInt(pref, defaultValue)) return@ResetCancelOk
                    prefs.putInt(pref, number)
                    if (prefs.getBoolean(Prefs.DYNAMIC_QUEST_CREATION, false))
                        OsmQuestController.reloadQuestTypes()
                }
            )
        },
    )
}

/** for setting values of a single key, comma separated */
@Composable fun SingleTypeElementSelectionDialog(
    prefs: Preferences,
    pref: String,
    defaultValue: String,
    messageId: Int,
    onDismissRequest: () -> Unit,
    onChanged: () -> Unit = { OsmQuestController.reloadQuestTypes() },
) {
    var text by remember {
        mutableStateOf(TextFieldValue(prefs.getString(pref, defaultValue).replace("|",", ")))
    }
    var isOk by remember { mutableStateOf(true) }
    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        content = {
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(
                    text = AnnotatedString.fromHtml(stringResource(messageId)),
                    style = MaterialTheme.typography.body1
                )
                TextField(
                    value = text,
                    onValueChange = {
                        isOk = checkValueText(it.text)
                        text = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )
            }
        },
        buttons = {
            ResetCancelOk(
                onDismissRequest = onDismissRequest,
                resetEnabled = prefs.contains(pref),
                onReset = { prefs.remove(pref); onChanged() },
                okEnabled = isOk,
                onOk = {
                    val prefText = text.text.split(",").joinToString("|") { it.trim() }
                    if (prefs.getString(pref, defaultValue) == prefText) return@ResetCancelOk
                    prefs.putString(pref, prefText)
                    onChanged()
                }
            )
        },
    )
}

/**
 *  For setting full element selection.
 *  This will check validity of input and only allow saving selection can be parsed.
 */
@Composable
fun FullElementSelectionDialog(prefs: Preferences, pref: String, messageId: Int, defaultValue: String, onDismissRequest: () -> Unit) {
    val checkPrefix = if (pref.endsWith("_full_element_selection")) "" else "nodes with "
    var text by remember {
        mutableStateOf(TextFieldValue(prefs.getString(pref, defaultValue.trimIndent())))
    }
    var isOk by remember { mutableStateOf(true) }
    val ctx = LocalContext.current
    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        content = {
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(
                    text = AnnotatedString.fromHtml(stringResource(messageId)),
                    style = MaterialTheme.typography.body1
                )
                TextField(
                    value = text,
                    onValueChange = {
                        isOk = checkText(it.text, checkPrefix, ctx)
                        text = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )
                if (prefs.contains(pref))
                    DiffButton(defaultValue) { text.text }
            }
        },
        buttons = {
            ResetCancelOk(
                onDismissRequest = onDismissRequest,
                resetEnabled = prefs.contains(pref),
                onReset = { prefs.remove(pref); OsmQuestController.reloadQuestTypes() },
                okEnabled = isOk,
                onOk = {
                    if (text.text != prefs.getString(pref, defaultValue.trimIndent())) {
                        prefs.putString(pref, text.text)
                        OsmQuestController.reloadQuestTypes()
                    }
                }
            )
        },
    )
}

@Composable fun FlowRowScope.ResetCancelOk(
    onDismissRequest: () -> Unit,
    resetEnabled: Boolean,
    onReset: () -> Unit,
    okEnabled: Boolean,
    onOk: () -> Unit
) {
    TextButton(
        onClick = { onReset(); onDismissRequest() },
        enabled = resetEnabled,
        modifier = Modifier.padding(end = 16.dp)
    ) {
        Text(stringResource(R.string.quest_settings_reset))
    }
    TextButton(onDismissRequest) { Text(stringResource(android.R.string.cancel)) }
    TextButton(
        onClick = { onOk(); onDismissRequest() },
        enabled = okEnabled
    ) {
        Text(stringResource(android.R.string.ok))
    }
}

@Composable
private fun DiffButton(defaultText: String, getCurrentText: () -> String) {
    var showDialog by remember { mutableStateOf(false) }
    androidx.compose.material.Button({
        showDialog = true
    }, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.quest_settings_highlight_changes_button))
    }
    if (showDialog) {
        val drg = DiffRowGenerator.create()
            .showInlineDiffs(true)
            .mergeOriginalRevised(true)
            .inlineDiffByWord(true)
            .ignoreWhiteSpaces(true)
            .oldTag { f -> if (f) "<b><i><del>" else "</del></i></b>" }
            .newTag { f -> if (f) "<b><u><ins>" else "</ins></u></b>" }
            .build()
        val thatSpace = " "
        val newDefault = defaultText.replace("|", "$thatSpace|$thatSpace") // replace with (nearly) invisible space, so word differences are used
        val newCurrent = getCurrentText().replace("|", "$thatSpace|$thatSpace")
        val diffRows = drg.generateDiffRows(newDefault.split("\n"), newCurrent.split("\n"))
        val diffText = diffRows.mapNotNull {
            if (it.tag == Tag.EQUAL) return@mapNotNull null
            it.oldLine
        }.joinToString("<br>")
        InfoDialog(
            onDismissRequest = { showDialog = false },
            text = { Text(AnnotatedString.fromHtml(diffText), style = MaterialTheme.typography.body1) }
        )
    }
}

private fun getDiffButton(context: Context, defaultText: String, getCurrentText: () -> String) =
    Button(context).apply {
        setText(R.string.quest_settings_highlight_changes_button)
        setOnClickListener {
            val drg = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .inlineDiffByWord(true)
                .ignoreWhiteSpaces(true)
                .oldTag { f -> if (f) "<b><i><del>" else "</del></i></b>" }
                .newTag { f -> if (f) "<b><u><ins>" else "</ins></u></b>" }
                .build()
            val thatSpace = " "
            val newDefault = defaultText.replace("|", "$thatSpace|$thatSpace") // replace with (nearly) invisible space, so word differences are used
            val newCurrent = getCurrentText().replace("|", "$thatSpace|$thatSpace")
            val diffRows = drg.generateDiffRows(newDefault.split("\n"), newCurrent.split("\n"))
            val diffText = diffRows.mapNotNull {
                if (it.tag == Tag.EQUAL) return@mapNotNull null
                it.oldLine
            }.joinToString("<br>")
            AlertDialog.Builder(context)
                .setMessage(HtmlCompat.fromHtml(diffText, HtmlCompat.FROM_HTML_MODE_LEGACY))
                .setNegativeButton(R.string.close, null)
                .show()
        }
    }

fun booleanQuestSettingsDialog(context: Context, prefs: Preferences, pref: String, messageId: Int, answerYes: Int, answerNo: Int): AlertDialog =
    AlertDialog.Builder(context)
        .setMessage(messageId)
        .setNeutralButton(android.R.string.cancel, null)
        .setPositiveButton(answerYes) { _,_ ->
            prefs.putBoolean(pref, true)
            OsmQuestController.reloadQuestTypes()
        }
        .setNegativeButton(answerNo) { _,_ ->
            prefs.putBoolean(pref, false)
            OsmQuestController.reloadQuestTypes()
        }
        .create()

fun dialog(context: Context, messageId: Int, initialValue: String, input: EditText): AlertDialog.Builder {
    input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
    val padding = 16
    input.setPadding(2 * padding, padding, 2 * padding, padding) // should be less than default padding to allow more text per line
    input.setText(initialValue)
    input.maxLines = 15 // if lines are not limited, the edit text might get so big that buttons are off screen (thanks, google for allowing this)
    return AlertDialog.Builder(context)
        .setMessage(messageId)
        .setView(input)
        .setNegativeButton(android.R.string.cancel, null)
}

private val valueRegex = "[a-z\\d_?,/\\s]+".toRegex()
private fun checkValueText(text: String) =
    text.lowercase().matches(valueRegex)
        && !text.trim().endsWith(',')
        && !text.contains(",,")
        && text.isNotEmpty()

// relax a little bit? but e.g. A-Z is very uncommon and might lead to mistakes
private val elementSelectionRegex = "[a-z\\d_=!?\"~*\\[\\]()|:.,<>\\s+-]+".toRegex()
private fun checkText(text: String, checkPrefix: String, context: Context): Boolean {
    val isValidFilterExpression by lazy {
        try {
            (checkPrefix + text).toElementFilterExpression()
            toastyJob?.cancel()
            true
        } catch(e: ParseException) {
            delayedToast(e.message, context)
            false
        } catch(e: PatternSyntaxException) {
            delayedToast(e.message, context)
            false
        }
    }
    // check other stuff first, because creation filter expression is relatively slow
    return (checkPrefix.isEmpty() || text.lowercase().matches(elementSelectionRegex))
        && text.count { c -> c == '('} == text.count { c -> c == ')'}
        && (text.contains('=') || text.contains('~') || text.contains('!'))
        && isValidFilterExpression
}

private var toastyJob: Job? = null
private fun delayedToast(message: String?, context: Context) {
    toastyJob?.cancel()
    toastyJob = GlobalScope.launch(Dispatchers.IO) {
        delay(3000)
        withContext(Dispatchers.Main) { Toast.makeText(context, "Error: $message", Toast.LENGTH_LONG).show() }
    }
}
