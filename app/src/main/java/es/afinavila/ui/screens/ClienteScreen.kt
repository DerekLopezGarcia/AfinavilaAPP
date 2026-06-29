package es.afinavila.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.afinavila.feature.comunidad.domain.Archivo
import es.afinavila.ui.theme.*
import es.afinavila.ui.viewmodel.ClienteViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteScreen(
    viewModel: ClienteViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Solo carga si no hay datos aún (la precarga desde onLoginSuccess ya pudo iniciar la carga)
    LaunchedEffect(Unit) {
        if (state.archivos.isEmpty() && !state.loading) viewModel.load()
    }
    val pdfFile by viewModel.pdfFile.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.comunidadNombre.ifEmpty { "Documentación" }, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = accent, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).navigationBarsPadding()) {
            // Calcular tabs dinámicamente según categorías disponibles
            val baseTabs = listOf("Actas", "Evoluciones anuales", "Extractos bancarios", "Otros")
            // Mapeo: nombre visible → categoría interna (de la API)
            val tabToCategoria = mapOf(
                "Actas" to "Actas",
                "Evoluciones anuales" to "Evoluciones",
                "Extractos bancarios" to "Extractos",
                "Otros" to "Otros",
                "Lecturas" to "Lecturas"
            )
            val tabs = remember(state.archivos) {
                if (state.archivos.any { it.categoria == "Lecturas" }) baseTabs + "Lecturas" else baseTabs
            }
            // Si el tab actual ya no está disponible (ej: cambió de comunidad), resetear a "Actas"
            LaunchedEffect(tabs) {
                if (state.tab !in tabs) viewModel.setTab("Actas")
            }
            val selectedTabIndex = remember(state.tab, tabs) { tabs.indexOf(state.tab).coerceAtLeast(0) }
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White
            ) {
                tabs.forEach { t ->
                    Tab(
                        selected = state.tab == t,
                        onClick = { viewModel.setTab(t) },
                        text = { Text(t, fontWeight = if (state.tab == t) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // Cachear filtrado para evitar recalcular en cada recomposición
            val filtered = remember(state.archivos, state.tab) {
                state.archivos.filter { (it.categoria ?: "Otros") == tabToCategoria[state.tab] }
            }

            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primary)
                }
            } else if (state.error != null && state.archivos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = errorColor)
                }
            } else if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay documentos en esta categoría.", color = textGray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(140.dp),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered.size) { i ->
                        val a = filtered[i]
                        ArchivoCard(a) { viewModel.openPdf(a) }
                    }
                }
            }
        }
    }

    // PDF loading
    if (state.pdfOpen != null && pdfFile == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.padding(32.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = primary)
                    Spacer(Modifier.height(16.dp))
                    Text("Cargando documento...", color = textGray)
                }
            }
        }
    }

    // PDF Viewer Dialog
    if (pdfFile != null) {
        PdfViewerDialog(
            file = pdfFile!!,
            title = state.pdfOpen?.nombreMostrar ?: state.pdfOpen?.nombre ?: "Documento",
            viewModel = viewModel,
            onClose = { viewModel.closePdf() }
        )
    }
}

@Composable
private fun PdfViewerDialog(
    file: File,
    title: String,
    viewModel: ClienteViewModel,
    onClose: () -> Unit,
) {
    var zoom by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val density = LocalContext.current.resources.displayMetrics.density
    val screenWidthPx = (LocalConfiguration.current.screenWidthDp * density).toInt()
    val scrollState = rememberScrollState()

    // Renderiza TODAS las páginas del PDF en un solo produceState (solo cuando cambia el archivo)
    val pages by produceState<List<Bitmap>?>(null, file) {
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fd)
        val count = renderer.pageCount
        viewModel.setPageCount(count)

        val bitmaps = mutableListOf<Bitmap>()
        for (i in 0 until count) {
            val page = renderer.openPage(i)
            val scale = screenWidthPx.toFloat() / page.width.coerceAtLeast(1)
            val w = (page.width * scale).toInt()
            val h = (page.height * scale).toInt()
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { bmp ->
                bmp.eraseColor(android.graphics.Color.WHITE)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bmp)
            }
            page.close()
        }
        renderer.close()
        fd.close()
        value = bitmaps
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
            Column {
                // Barra superior: cerrar + título + zoom
                Row(
                    modifier = Modifier.fillMaxWidth().background(accent).padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, "Cerrar", tint = Color.White)
                    }
                    Text(title, color = Color.White, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { zoom = (zoom - 0.5f).coerceAtLeast(1f); offsetX = 0f; offsetY = 0f }) {
                            Text("\u2212", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("${(zoom * 100).toInt()}%", color = Color.White.copy(0.7f), fontSize = 12.sp)
                        IconButton(onClick = { zoom = (zoom + 0.5f).coerceAtMost(5f) }) {
                            Text("+", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Área scrolleable con TODAS las páginas en vertical
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clipToBounds()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoomChange, _ ->
                                zoom = (zoom * zoomChange).coerceIn(1f, 5f)
                                if (zoom > 1f) {
                                    val (dx, dy) = pan
                                    offsetX += dx
                                    offsetY += dy
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        }
                ) {
                    pages?.let { bitmapList ->
                        if (bitmapList.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(scrollState)
                                    .graphicsLayer {
                                        scaleX = zoom
                                        scaleY = zoom
                                        translationX = offsetX
                                        translationY = offsetY
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                bitmapList.forEachIndexed { index, bmp ->
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "P\u00E1gina ${index + 1}",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    // Separador entre páginas (excepto tras la última)
                                    if (index < bitmapList.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .background(Color.DarkGray)
                                        )
                                    }
                                }
                            }
                        }
                    } ?: run {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(40.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchivoCard(archivo: Archivo, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick).padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                archivo.nombreMostrar ?: archivo.nombre,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                archivo.descripcion,
                fontSize = 11.sp,
                color = textGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
