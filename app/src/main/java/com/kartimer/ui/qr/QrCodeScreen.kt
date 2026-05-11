package com.kartimer.ui.qr

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import com.google.zxing.BarcodeFormat
import com.kartimer.R
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.kartimer.ui.theme.MarathonTimerPrimary
import com.kartimer.ui.race.RaceViewModel
import com.kartimer.util.TimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.EnumMap

@Composable
fun QrCodeScreen(
    raceViewModel: RaceViewModel,
    onDismiss: () -> Unit
) {
    val currentPilot by raceViewModel.currentPilot.collectAsState()
    val currentKartNumber by raceViewModel.currentKartNumber.collectAsState()
    val currentTeam by raceViewModel.currentTeam.collectAsState()
    val lastChangeTimestamp by raceViewModel.lastChangeTimestamp.collectAsState()

    val naStr = stringResource(R.string.label_na)
    val timeStr = TimeFormatter.formatTimestamp(lastChangeTimestamp)
    val pilotName = currentPilot?.name ?: naStr
    val kartNum = currentKartNumber.toString()
    val teamNumber = currentTeam?.number ?: 0
    val teamName = currentTeam?.name ?: naStr

    val qrData = "$teamNumber,$teamName,$timeStr,$pilotName,$kartNum"

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(qrData) {
        qrBitmap = withContext(Dispatchers.IO) {
            generateQrBitmap(qrData, 512)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.qr_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.btn_close), tint = Color.Black)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // QR code image
                val bitmap = qrBitmap
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.cd_qr_image),
                        modifier = Modifier
                            .size(280.dp)
                            .background(Color.White)
                    )
                } else {
                    Box(
                        modifier = Modifier.size(280.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.Black)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Data breakdown
                HorizontalDivider(color = Color.LightGray)
                Spacer(Modifier.height(8.dp))

                QrDataRow(label = stringResource(R.string.label_team_number_prefix), value = teamNumber.toString())
                QrDataRow(label = stringResource(R.string.label_team), value = teamName)
                QrDataRow(label = stringResource(R.string.label_change_time), value = timeStr)
                QrDataRow(label = stringResource(R.string.label_pilot), value = pilotName)
                QrDataRow(label = stringResource(R.string.label_kart_number), value = kartNum)

                Spacer(Modifier.height(8.dp))

                // Raw QR content
                Text(
                    text = qrData,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MarathonTimerPrimary)
                ) {
                    Text(stringResource(R.string.btn_ok), fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun QrDataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
    }
}

private fun generateQrBitmap(content: String, size: Int): Bitmap {
    val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
    hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
    hints[EncodeHintType.MARGIN] = 1

    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(
                x, y,
                if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            )
        }
    }
    return bitmap
}
