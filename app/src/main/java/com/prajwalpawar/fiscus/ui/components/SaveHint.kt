package com.prajwalpawar.fiscus.ui.components

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme

/**
 * Says why Save is disabled ("Choose a category"). Enters and exits by expanding and collapsing
 * vertically (M3 enter/exit pattern: no scale) on the theme's springs; text changes cross-fade.
 */
@Composable
fun SaveHint(@StringRes hint: Int?, modifier: Modifier = Modifier) {
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = hint != null,
        modifier = modifier,
        enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
        exit = shrinkVertically(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
    ) {
        // Keep the last hint while collapsing, so the text doesn't vanish before the space does.
        var lastHint by remember { mutableIntStateOf(0) }
        if (hint != null) lastHint = hint
        AnimatedContent(
            targetState = lastHint,
            transitionSpec = {
                fadeIn(motion.fastEffectsSpec())
                    .togetherWith(fadeOut(motion.fastEffectsSpec()))
                    .using(SizeTransform { _, _ -> motion.fastSpatialSpec() })
            },
            label = "saveHint",
        ) { res ->
            if (res != 0) {
                Text(
                    text = stringResource(res),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = FiscusLayout.ItemSpacing)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun SaveHintPreview() {
    FiscusTheme(dynamicColor = false) {
        Surface { SaveHint(hint = R.string.account_hint_name) }
    }
}
