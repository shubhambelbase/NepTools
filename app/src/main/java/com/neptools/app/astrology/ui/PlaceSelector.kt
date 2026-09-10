package com.neptools.app.astrology.ui

import com.neptools.app.ui.strings.T

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.neptools.app.core.data.Place
import com.neptools.app.core.data.PlacesRepo

@Composable
fun PlaceSelector(
    initialSelected: Place?,
    onSelected: (Place) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var open by remember { mutableStateOf(false) }
    var chosen by remember { mutableStateOf(initialSelected) }

    Column(Modifier.fillMaxWidth()) {
        Text(T("place_hdr"), style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(5.dp))
        OutlinedTextField(
            value = if (open) query else chosen?.let { "${it.en} · ${it.district}" } ?: "",
            onValueChange = { v ->
                if (v.length <= 30) { query = v; open = true }
                else { open = false; query = "" }
            },
            label = { Text(if (com.neptools.app.ui.theme.ThemePrefs.lang.value == "en") "District / City — 852 places" else "जिल्ला / शहर / गाउँपालिका — ८५२ ठेगाना") },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { open = it.isFocused }
        )
        if (chosen != null && !open) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                    .padding(horizontal = 11.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(width = 3.dp, height = 14.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(Modifier.size(width = 8.dp, height = 0.dp))
                Text(
                    "${chosen!!.np} · ${chosen!!.district}" +
                        (chosen!!.province.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    if (com.neptools.app.ui.theme.ThemePrefs.lang.value == "en") "Tap to change" else "ट्याप गरेर परिवर्तन",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (open) {
            Spacer(Modifier.height(4.dp))
            val results = remember(query) { PlacesRepo.search(query) }
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 264.dp)
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
            ) {
                items(results) { p ->
                    PlaceRow(p) {
                        chosen = p
                        onSelected(p)
                        open = false
                        query = ""
                    }
                }
                if (results.isEmpty()) {
                    item {
                        Text(
                            if (com.neptools.app.ui.theme.ThemePrefs.lang.value == "en") "No place found — enter lat/lon manually below" else "कुनै ठेगाना भेटिएन — तल अक्षांश/देशान्तर हातले हाल्नुहोस्",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceRow(p: Place, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(p.en, style = MaterialTheme.typography.titleSmall)
            Text(p.np, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            p.district.ifBlank { p.type },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
