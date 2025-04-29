package es.afinavila.feature.comunidad.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.afinavila.feature.comunidad.domain.GetPdfArchivoByIdUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import kotlin.math.absoluteValue

class WebViewActivity : ComponentActivity() {
    private val getPdfArchivoByIdUseCase: GetPdfArchivoByIdUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val archivoId = intent.getIntExtra("archivoId", -1)
            if (archivoId != -1) {
                PdfViewerScreen(archivoId, getPdfArchivoByIdUseCase)
            } else {
                Toast.makeText(this, "Archivo ID no encontrado", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}

@Composable
fun PdfViewerScreen(archivoId: Int, getPdfArchivoByIdUseCase: GetPdfArchivoByIdUseCase) {
    val context = LocalContext.current
    val viewModel = remember { PdfViewerViewModel(getPdfArchivoByIdUseCase) }
    val pdfRenderer by viewModel.pdfRenderer.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(archivoId) {
        viewModel.loadPdf(context, archivoId)
    }

    if (errorMessage != null) {
        Text(text = errorMessage!!)
    } else {
        pdfRenderer?.let { renderer ->
            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { renderer.pageCount }
            )
            val coroutineScope = rememberCoroutineScope()

            Box(modifier = Modifier.fillMaxSize()) {
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = true,
                    pageSpacing = 8.dp,
                    beyondViewportPageCount = 2,
                ) { pageIndex ->
                    Card(
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val pageOffset = (
                                        (pagerState.currentPage - pageIndex) + pagerState
                                            .currentPageOffsetFraction
                                        ).absoluteValue

                                alpha = lerp(
                                    start = 0.5f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    if (zoom != 1f) {
                                        coroutineScope.launch {
                                            pagerState.scrollToPage(
                                                pageIndex,
                                                pagerState.currentPageOffsetFraction + pan.x
                                            )
                                        }
                                    }
                                }
                            },
                    ) {
                        val page = remember(renderer, pageIndex) { renderer.openPage(pageIndex) }
                        PdfPageView(page)
                        DisposableEffect(page) {
                            var isPageClosed = false // Variable para rastrear si la página ya fue cerrada

                            onDispose {
                                try {
                                    if (!isPageClosed) { // Verifica si la página no está cerrada
                                        page.close()
                                        isPageClosed = true // Marca la página como cerrada
                                    }
                                } catch (e: IllegalStateException) {
                                    Log.e("PdfViewerScreen", "Error al cerrar la página PDF: Documento ya cerrado", e)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp)
                ) {
                    Button(
                        onClick = {
                            if (pagerState.currentPage > 0) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        },
                        enabled = pagerState.currentPage > 0
                    ) {
                        Text("Anterior")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            if (pagerState.currentPage < pagerState.pageCount - 1) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        enabled = pagerState.currentPage < pagerState.pageCount - 1
                    ) {
                        Text("Siguiente")
                    }
                }
            }
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            viewModel.closePdfRenderer()
        }
    }
}

@Composable
fun PdfPageView(page: PdfRenderer.Page) {
    var scale by remember { mutableFloatStateOf(1f) }
    val bitmap = remember(page) {
        createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
    }
    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 2f)
                }
            },
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}


class PdfViewerViewModel(
    private val getPdfArchivoByIdUseCase: GetPdfArchivoByIdUseCase
) : ViewModel() {
    private val _pdfRenderer = MutableStateFlow<PdfRenderer?>(null)
    val pdfRenderer: StateFlow<PdfRenderer?> = _pdfRenderer

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var fileDescriptor: ParcelFileDescriptor? = null

    fun loadPdf(context: Context, archivoId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pdfBytes = getPdfArchivoByIdUseCase(archivoId)
                if (pdfBytes != null) {
                    val file = File(context.cacheDir, "temp.pdf")
                    file.writeBytes(pdfBytes)

                    fileDescriptor =
                        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    _pdfRenderer.value = PdfRenderer(fileDescriptor!!)
                } else {
                    _errorMessage.value = "Error al obtener el PDF"
                }
            } catch (e: Exception) {
                Log.e("PdfViewerViewModel", "Error al cargar el PDF", e)
                _errorMessage.value = "Error al cargar el PDF"
            }
        }
    }

    fun closePdfRenderer() {
        try {
            _pdfRenderer.value?.let {
                it.close()
                _pdfRenderer.value = null
            }
            fileDescriptor?.let {
                it.close()
                fileDescriptor = null
            }
        } catch (e: Exception) {
            Log.e("PdfViewerViewModel", "Error al cerrar el PDF", e)
        }
    }
}
