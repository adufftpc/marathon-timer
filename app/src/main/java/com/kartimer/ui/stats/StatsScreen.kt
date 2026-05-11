package com.kartimer.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import com.kartimer.R
import com.kartimer.ui.theme.MinPilotTimeMarkColor
import com.kartimer.ui.theme.TimerGreen
import com.kartimer.ui.theme.TimerRed
import com.kartimer.util.TimeFormatter

@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val teamStats by viewModel.teamStats.collectAsState()
    val minPilotTimeSec by viewModel.minPilotTimeSec.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.stats_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
        )
        Text(
            text = stringResource(R.string.format_min_pilot_time, TimeFormatter.formatMinSec(minPilotTimeSec.toLong())),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (teamStats.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.msg_no_data),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                teamStats.forEach { group ->
                    item(key = "header_${group.teamId}") {
                        TeamHeader(group)
                    }
                    items(
                        items = group.pilots,
                        key = { it.pilotId }
                    ) { pilot ->
                        PilotStatsRow(
                            pilot = pilot,
                            minPilotTimeSec = minPilotTimeSec,
                            maxPilotTimeSec = group.maxPilotTimeSec
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamHeader(group: TeamStatsGroup) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "#${group.teamNumber}",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = group.teamName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.format_team_max_time, TimeFormatter.formatMinSec(group.maxPilotTimeSec.toLong())),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
        )
    }
}

@Composable
private fun PilotStatsRow(
    pilot: PilotStatsItem,
    minPilotTimeSec: Int,
    maxPilotTimeSec: Int
) {
    val met = pilot.totalSeconds >= minPilotTimeSec
    val barColor = if (met) TimerGreen else TimerRed

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pilot.pilotName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = TimeFormatter.formatMinSec(pilot.totalSeconds.toLong()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = barColor
                )
            }
            Spacer(Modifier.height(6.dp))
            PilotProgressBar(
                totalSeconds = pilot.totalSeconds,
                maxSeconds = maxPilotTimeSec,
                minPilotTimeSec = minPilotTimeSec,
                barColor = barColor,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PilotProgressBar(
    totalSeconds: Int,
    maxSeconds: Int,
    minPilotTimeSec: Int,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val fillFraction = if (maxSeconds > 0)
        (totalSeconds.toFloat() / maxSeconds).coerceIn(0f, 1f) else 0f
    val minFraction = if (maxSeconds > 0)
        minPilotTimeSec.toFloat() / maxSeconds else 0f

    // Pre-compute 15-min tick fractions (only ticks that fit within the bar)
    val tickFractions = remember(maxSeconds) {
        val stepSec = 15 * 60
        generateSequence(stepSec) { it + stepSec }
            .takeWhile { it < maxSeconds }
            .map { it.toFloat() / maxSeconds }
            .toList()
    }

    Canvas(modifier = modifier.height(22.dp)) {
        val barTop    = size.height * 0.30f
        val barBottom = size.height * 0.80f
        val barH      = barBottom - barTop
        val radius    = CornerRadius(barH / 2f)

        // Background track
        drawRoundRect(
            color = Color.Gray.copy(alpha = 0.22f),
            topLeft = Offset(0f, barTop),
            size = Size(size.width, barH),
            cornerRadius = radius
        )

        // Filled progress
        if (fillFraction > 0f) {
            drawRoundRect(
                color = barColor,
                topLeft = Offset(0f, barTop),
                size = Size(size.width * fillFraction, barH),
                cornerRadius = radius
            )
        }

        // 15-minute tick marks (inside bar bounds)
        val tickStroke = 1.5.dp.toPx()
        tickFractions.forEach { f ->
            val x = size.width * f
            drawLine(
                color = Color.White.copy(alpha = 0.55f),
                start = Offset(x, barTop + tickStroke),
                end   = Offset(x, barBottom - tickStroke),
                strokeWidth = tickStroke
            )
        }

        // Minimum pilot time mark — amber, extends beyond bar top and bottom
        if (minFraction in 0f..1f) {
            val minX = size.width * minFraction
            drawLine(
                color = MinPilotTimeMarkColor,
                start = Offset(minX, 0f),
                end   = Offset(minX, size.height),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
