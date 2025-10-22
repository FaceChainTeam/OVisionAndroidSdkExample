package com.ovision.cameraexample

import android.app.Activity
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ovision.camera.FaceCaptureActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var resultText by rememberSaveable { mutableStateOf("Результат ещё не получен") }
    var photoUri by rememberSaveable { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        resultText = when {
            result.resultCode == Activity.RESULT_OK -> {
                val uri = result.data?.data
                photoUri = uri?.toString()
                uri?.let { "Фото сохранено: $it" } ?: "Фото сохранено, но URI отсутствует"
            }

            else -> {
                photoUri = null
                val reason = result.data?.getStringExtra("result_reason")
                if (reason == "timeout") {
                    "Съёмка завершилась по таймауту"
                } else {
                    "Съёмка отменена"
                }
            }
        }
    }

    MainContent(
        resultText = resultText,
        photoUri = photoUri,
        onStartFaceCapture = {
            val intent = FaceCaptureActivity.newIntent(
                context = context,
                lensFacing = CameraSelector.LENS_FACING_FRONT
            )
            launcher.launch(intent)
        },
        modifier = modifier
    )
}

@Composable
fun MainContent(
    resultText: String,
    photoUri: String?,
    onStartFaceCapture: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(photoUri) {
        if (photoUri == null) {
            imageBitmap = null
        } else {
            val uri = Uri.parse(photoUri)
            imageBitmap = withContext(Dispatchers.IO) {
                runCatching {
                    // Decode the captured photo off the main thread.
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = resultText)
            imageBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = "Захваченное фото"
                )
            }
            Button(onClick = onStartFaceCapture) {
                Text(text = "Сделать фото")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    MainContent(
        resultText = "Результат появится здесь",
        photoUri = null,
        onStartFaceCapture = {}
    )
}
