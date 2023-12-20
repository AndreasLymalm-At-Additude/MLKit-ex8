package se.magictechnology.intromlkit

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import se.magictechnology.intromlkit.ui.theme.IntroMLKitTheme
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var foundTexts = remember { mutableStateListOf<String>() }
            var resourceId by remember { mutableStateOf(0) }
            var isProcessing by remember { mutableStateOf(false) }

            IntroMLKitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .padding(30.dp)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                resourceId = R.drawable.img1
                                isProcessing = true
                            },
                            enabled = !isProcessing
                        ) {
                            Text("Process Cereal Package")
                        }
                        Button(
                            onClick = {
                                resourceId = R.drawable.img2
                                isProcessing = true
                            },
                            enabled = !isProcessing
                        ) {
                            Text("Process Map")
                        }
                        Button(
                            onClick = {
                                resourceId = R.drawable.img3
                                isProcessing = true
                            },
                            enabled = !isProcessing
                        ) {
                            Text("Process Shampoo Bottle")
                        }

                        for (text in foundTexts) {
                            RowText(name = text)
                        }
                    }
                }
            }

            LaunchedEffect(resourceId, isProcessing) {
                if (isProcessing) {
                    foundTexts.clear()
                    foundTexts.addAll(runTextRecognition(resourceId))
                    isProcessing = false
                }
            }
        }
    }

    private suspend fun runTextRecognition(resourceId : Int) : List<String> {

        var selectedImage = BitmapFactory.decodeResource(resources, resourceId)

        val image = InputImage.fromBitmap(selectedImage, 0)
        var textRecognizerOptions = TextRecognizerOptions.Builder().build()
        val recognizer = TextRecognition.getClient(textRecognizerOptions)

        return suspendCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { texts ->
                    continuation.resume(processTextRecognitionResult(texts))
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    continuation.resume(emptyList())
                }
        }
    }

    private fun processTextRecognitionResult(texts: Text) : List<String> {
        val blocks: List<Text.TextBlock> = texts.getTextBlocks()

        val foundTexts : MutableList<String> = mutableListOf()
        for (i in blocks.indices) {
            val lines: List<Text.Line> = blocks[i].getLines()
            for (j in lines.indices) {
                val elements: List<Text.Element> = lines[j].getElements()
                for (k in elements.indices) {
                    foundTexts.add(elements[k].text)
                }
            }
        }
        return foundTexts
    }
}

@Composable
fun RowText(name: String, modifier: Modifier = Modifier) {
    Column {
        Text(
            text = name,
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IntroMLKitTheme {
        RowText("Android")
    }
}
