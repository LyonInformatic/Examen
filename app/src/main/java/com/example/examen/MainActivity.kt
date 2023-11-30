package com.example.examen

import android.os.Bundle
import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.io.File
import java.io.FileOutputStream


class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        isGranted ->
        val message = if(isGranted) "Permission Granted" else "Permission Rejected"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    private val placeViewModel: PlaceViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyNavHost(placeViewModel)

        }
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
}

@Composable
fun MainScreen(navController: NavController, placeViewModel: PlaceViewModel = viewModel()) {
    var isAddingPlace by remember { mutableStateOf(false) }
    var isEditingPlace by remember { mutableStateOf(false) }
    var placeName by remember { mutableStateOf("") }
    var order by remember { mutableStateOf(0) }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var imageUrl by remember { mutableStateOf("") }
    var additionalComments by remember { mutableStateOf("") }

    val places: List<Place> by placeViewModel.allPlaces.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isAddingPlace || isEditingPlace) {
            // Form for adding a new place
            AddPlaceForm(
                onPlaceAdded = {
                    placeViewModel.insertOrUpdatePlace(it)
                    isAddingPlace = false
                    isEditingPlace = false
                    placeName = ""
                    order = 0
                    latitude = 0.0
                    longitude = 0.0
                    imageUrl = ""
                    additionalComments = ""
                },
                onPlaceUpdated = {
                    placeViewModel.insertOrUpdatePlace(it)
                    isAddingPlace = false
                    isEditingPlace = false
                },
                onCancel = {
                    isAddingPlace = false
                    isEditingPlace = false
                    placeName = ""
                    order = 0
                    latitude = 0.0
                    longitude = 0.0
                    imageUrl = ""
                    additionalComments = ""
                },
                placeName = placeName,
                onPlaceNameChange = { placeName = it },
                order = order,
                onOrderChange = { order = it },
                latitude = latitude,
                onLatitudeChange = { latitude = it },
                longitude = longitude,
                onLongitudeChange = { longitude = it },
                imageUrl = imageUrl,
                onImageUrlChange = { imageUrl = it },
                additionalComments = additionalComments,
                onAdditionalCommentsChange = { additionalComments = it },
                existingPlace = if (isEditingPlace) places.find { it.name == placeName } else null
            )
        } else {
            // Button to add a new place
            Button(
                onClick = { isAddingPlace = true
                    placeName = ""
                    order = 0
                    latitude = 0.0
                    longitude = 0.0
                    imageUrl = ""
                    additionalComments = ""},

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.button_add))
            }
            LazyColumn {
                items(places) { place ->
                    // Display each place in the list
                    PlaceListItem(
                        place = place,
                        onEditClick = {
                            isAddingPlace = true
                            isEditingPlace = true
                            placeName = place.name
                            order = place.order
                            latitude = place.latitude
                            longitude = place.longitude
                            imageUrl = place.imageUrl
                            additionalComments = place.additionalComments
                        },
                        onDeleteClick = {
                            placeViewModel.deletePlace(place)
                        },
                        onDetailsClick = {
                            navController.navigate("place_details/${place.id}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceListItem(
    place: Place,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDetailsClick: () -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Image(
            painter = rememberImagePainter(data = place.imageUrl),
            contentDescription = "Imagen del lugar",
            modifier = Modifier
                .width(150.dp)
                .height(150.dp)
                .clip(shape = MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Customize the layout for each place item
            Text(
                text = stringResource(
                    R.string.place_name,
                    place.name
                ) + "\n" +
                        stringResource(
                            R.string.cost_night,
                            place.accommodationCost
                        ) + "\n" +
                        stringResource(
                            R.string.transfer,
                            place.transportationCost
                        ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    modifier = Modifier
                        .clickable {
                            onEditClick()
                        }
                        .padding(8.dp)
                )
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    modifier = Modifier
                        .clickable {
                            onDeleteClick()
                        }
                        .padding(8.dp)
                )
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Detalles del lugar",
                    modifier = Modifier
                        .clickable {
                            onDetailsClick()
                        }
                        .padding(8.dp)
                )
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun AddPlaceForm(
    onPlaceAdded: (Place) -> Unit,
    onPlaceUpdated: (Place) -> Unit,
    onCancel: () -> Unit,
    placeName: String,
    onPlaceNameChange: (String) -> Unit,
    order: Int,
    onOrderChange: (Int) -> Unit,
    latitude: Double,
    onLatitudeChange: (Double) -> Unit,
    longitude: Double,
    onLongitudeChange: (Double) -> Unit,
    imageUrl: String,
    onImageUrlChange: (String) -> Unit,
    additionalComments: String,
    onAdditionalCommentsChange: (String) -> Unit,
    existingPlace: Place? = null
) {
    var accommodationCost by remember { mutableStateOf(0.0) }
    var transportationCost by remember { mutableStateOf(0.0) }
    var isMapVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TextField(
            value = placeName,
            onValueChange = { onPlaceNameChange(it) },
            label = { Text(stringResource(id = R.string.name_place)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        TextField(
            value = order.toString(),
            onValueChange = { onOrderChange(it.toIntOrNull() ?: 0) },
            label = { Text(stringResource(id = R.string.order)) },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    //LocalSoftwareKeyboardController.current?.hide()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        TextField(
            value = imageUrl,
            onValueChange = { onImageUrlChange(it) },
            label = { Text(stringResource(id = R.string.image)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        TextField(
            value = latitude.toString(),
            onValueChange = { onLatitudeChange(it.toDoubleOrNull() ?: 0.0) },
            label = { Text(stringResource(id = R.string.latitude)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        TextField(
            value = longitude.toString(),
            onValueChange = { onLongitudeChange(it.toDoubleOrNull() ?: 0.0) },
            label = { Text(stringResource(id = R.string.longitude)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        TextField(
            value = accommodationCost.toString(),
            onValueChange = { accommodationCost = it.toDoubleOrNull() ?: 0.0 },
            label = { Text(stringResource(id = R.string.cost_accommodation)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        TextField(
            value = transportationCost.toString(),
            onValueChange = { transportationCost = it.toDoubleOrNull() ?: 0.0 },
            label = { Text(stringResource(id = R.string.cost_transportation)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        TextField(
            value = additionalComments,
            onValueChange = { onAdditionalCommentsChange(it) },
            label = { Text(stringResource(id = R.string.comments)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        Row(
            modifier = Modifier
                .padding(8.dp)
        ) {
            Button(
                onClick = {
                    val newPlace = Place(
                        name = placeName,
                        order = order,
                        accommodationCost = accommodationCost,
                        transportationCost = transportationCost,
                        additionalComments = additionalComments,
                        latitude = latitude,
                        longitude = longitude,
                        imageUrl = imageUrl
                    )
                    if (existingPlace != null) {
                        onPlaceUpdated(newPlace.copy(id = existingPlace.id))
                    } else {
                        onPlaceAdded(newPlace)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(if (existingPlace != null) stringResource(id = R.string.button_modificar) else stringResource(
                    id = R.string.button_add
                ))
            }

            Button(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(stringResource(id = R.string.button_cancel))
            }

        }
        Row(modifier = Modifier
            .padding(8.dp) ) {
            Button(modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    isMapVisible = true
                }) {
                Text(stringResource(id = R.string.button_location))
            }
        }
        if (isMapVisible) {
            MapSelection(
                onLocationSelected = { lat, lon ->
                    // Actualiza las coordenadas en tu formulario
                    onLatitudeChange(lat)
                    onLongitudeChange(lon)

                    // Cierra el mapa
                    isMapVisible = false
                },
                onDismiss = {
                    // Manejar el cierre del mapa sin seleccionar una ubicación
                    isMapVisible = false
                }
            )
        }
    }
}
@Composable
fun MyNavHost(placeViewModel: PlaceViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main_screen"
    ) {
        composable("main_screen") {
            MainScreen(navController = navController)
        }
        composable("place_details/{placeId}") { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId")
            if (placeId != null) {
                val place: Place? by placeViewModel.getPlaceById(placeId).observeAsState(null)// Obtener el lugar con el ID desde tu ViewModel o fuente de datos
                place?.let {
                    PlaceDetailsScreen(it, placeViewModel, navController) {
                        navController.popBackStack()
                    }
                }
            }
        }
        composable("edit_place/{placeId}") { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId")
            if (placeId != null) {
                val place: Place? by placeViewModel.getPlaceById(placeId).observeAsState(null)
                place?.let {
                    // Utiliza tu composable de edición existente, por ejemplo, EditPlaceScreen
                    EditPlaceScreen(place!!, onPlaceUpdated = { updatedPlace ->
                        placeViewModel.insertOrUpdatePlace(updatedPlace)
                        navController.popBackStack()
                    }, onBack = {
                        navController.popBackStack()
                    })
                }
            }
        }
    }
}
@Composable
fun PlaceDetailsScreen(place: Place, placeViewModel: PlaceViewModel, navController: NavController, onBack: () -> Unit) {
    var selectedLatitude by remember { mutableStateOf(place.latitude) }
    var selectedLongitude by remember { mutableStateOf(place.longitude) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val imagePainter = rememberImagePainter(
        data = capturedBitmap?.let { bitmapToImageUri(it, place.id, placeViewModel) } ?: place.imageUrl
    )
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        capturedBitmap = bitmap

    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the name of the place
        Text(
            text = "${place.name}",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.Center)
                .padding(vertical = 8.dp)
        )
        // Display the image of the place
        Image( painter = imagePainter,
            contentDescription = "Imagen del lugar",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(shape = MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )

        // Display the accommodation cost
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.accommodation,"\n",place.accommodationCost),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            // Display the transportation cost
            Text(
                text = stringResource(id = R.string.transportation,"\n",place.transportationCost),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        )
        {
            // Display additional comments
            Text(
                text = stringResource(id = R.string.comments_details,"\n",place.additionalComments),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
        Row{
        PlaceIcons(
            onEditClick = {
                navController.navigate("edit_place/${place.id}")
            },
            onDeleteClick = {
                placeViewModel.deletePlace(place)
                onBack()
            },
            onDetailsClick = {
            },
            onCamera = {
                takePictureLauncher.launch(null)
            },
            onUpdateImage = {
            }

        )
        }

        Spacer(modifier = Modifier.height(80.dp))
    Row {
        OsmdroidMapView(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            latitude = place.latitude,
            longitude = place.longitude,
            showMarker = true,
            onLocationChanged = {latitude, longitude ->
                selectedLatitude = latitude
                selectedLongitude = longitude}


        )
    }
        Spacer(modifier = Modifier.height(140.dp))
        // Button to go back to the main screen
        Button(onClick = onBack,
            modifier = Modifier
                .width(150.dp)
                .padding(16.dp)) {
            Text(stringResource(id = R.string.button_back))

        }
    }
}

@Composable
fun PlaceIcons(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onCamera: () -> Unit,
    onUpdateImage: (imageUrl: String) -> Unit

) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Editar",
            modifier = Modifier
                .clickable {
                    onEditClick()
                }
                .padding(8.dp)
        )
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Eliminar",
            modifier = Modifier
                .clickable {
                    onDeleteClick()
                }
                .padding(8.dp)
        )
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Detalles del lugar",
            modifier = Modifier
                .clickable {
                    onDetailsClick()
                }
                .padding(8.dp)
        )
        Icon(
            painter = painterResource(id = R.drawable.baseline_camera_alt_24),
            contentDescription = "foto",
            modifier = Modifier
                .clickable {
                    onCamera()
                }
                .padding(8.dp)
        )


    }
}
@Composable
fun EditPlaceScreen(
    place: Place,
    onPlaceUpdated: (Place) -> Unit,
    onBack: () -> Unit
) {
    var placeName by remember { mutableStateOf(place.name) }
    var order by remember { mutableStateOf(place.order) }
    var latitude by remember { mutableStateOf(place.latitude) }
    var longitude by remember { mutableStateOf(place.longitude) }
    var imageUrl by remember { mutableStateOf(place.imageUrl) }
    var additionalComments by remember { mutableStateOf(place.additionalComments) }
    // Tu interfaz de usuario para la edición del lugar aquí
    // Asegúrate de utilizar los datos del lugar pasado como parámetro

    // Por ejemplo, puedes reutilizar tu AddPlaceForm y ajustar el comportamiento
    AddPlaceForm(
        onPlaceAdded = { /* No se usará en la edición */ },
        onPlaceUpdated = {
            onPlaceUpdated(it) // Llama a la función de actualización proporcionada
        },
        onCancel = {
            onBack() // Llama a la función de retroceso proporcionada
        },
        placeName = placeName,
        onPlaceNameChange = { placeName = it },
        order = order,
        onOrderChange = { order = it },
        latitude = latitude,
        onLatitudeChange = { latitude = it },
        longitude = longitude,
        onLongitudeChange = { longitude = it },
        imageUrl = imageUrl,
        onImageUrlChange = { imageUrl = it },

        additionalComments = additionalComments,
        onAdditionalCommentsChange = { additionalComments = it },
        existingPlace = place
    )


}
@Composable
fun OsmdroidMapView(modifier: Modifier = Modifier, latitude: Double, longitude: Double,
                    onLocationChanged: (Double, Double) -> Unit, showMarker: Boolean = true) {
    val contexto = LocalContext.current
    val mapView = remember { MapView(contexto) }
    val marker = remember { Marker(mapView) }


    AndroidView(
        factory = { context ->
            val mapView = MapView(context)
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            Configuration.getInstance().userAgentValue = contexto.packageName
            mapView.setBuiltInZoomControls(true)
            mapView.controller.setZoom(10.0)
            mapView.controller.setCenter(GeoPoint(latitude, longitude))
            if (showMarker) {
                // Add the marker
                val geoPoint = GeoPoint(latitude, longitude)
                marker.position = geoPoint
                mapView.overlays.add(marker)
            }
            mapView.overlays.add(object : Overlay() {

                override fun draw(c: Canvas, osmv: MapView, shadow: Boolean) {}

                override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                    val projection = mapView.projection
                    val touchedGeoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt())
                    val latitude = touchedGeoPoint.latitude
                    val longitude = touchedGeoPoint.longitude
                    onLocationChanged(latitude, longitude)

                    if(showMarker) {
                        mapView.overlays.remove(marker)

                        // Add the new marker
                        val geoPoint = GeoPoint(latitude, longitude)
                        marker.position = geoPoint
                        mapView.overlays.add(marker)

                        mapView.invalidate()
                    }
                    return true
                }
            })
            mapView
        },
        update = {

            val geoPoint = GeoPoint(latitude, longitude)
            it.controller.animateTo(geoPoint)

        },
        modifier = modifier
            .size(100.dp)
    )
}
@Composable
fun MapSelection(
    onLocationSelected: (Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedLatitude by remember { mutableStateOf(0.0) }
    var selectedLongitude by remember { mutableStateOf(0.0) }
    // Agrega tu lógica para mostrar el mapa y permitir la selección de ubicación
    AlertDialog(
        onDismissRequest = {
            // Manejar el cierre del diálogo sin seleccionar una ubicación
            onDismiss()
        },
        title = {
            Text("Selecciona una Ubicación")
        },
        text = {
            // Mostrar el mapa utilizando OsmdroidMapView
            OsmdroidMapView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                latitude = selectedLatitude,
                longitude = selectedLongitude,
                onLocationChanged = {latitude, longitude ->
                    selectedLatitude = latitude
                    selectedLongitude = longitude}
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    // Llama a onLocationSelected con las coordenadas seleccionadas
                    onLocationSelected(selectedLatitude, selectedLongitude)
                }
            ) {
                Text("Seleccionar")
            }
        }
    )
}
@Composable
fun bitmapToImageUri(
    bitmap: Bitmap,
    placeId: Long,
    placeViewModel: PlaceViewModel
): Uri {
    val context = LocalContext.current

    // Guardar la imagen en el directorio de archivos de la aplicación
    val imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imageFile = File(imagesDir, "image_${System.currentTimeMillis()}.png")

    // Escribir el bitmap en el archivo
    FileOutputStream(imageFile).use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    }

    // Obtener la URI del archivo guardado
    val imageUri = FileProvider.getUriForFile(
        context,
        "com.example.examen.fileprovider",
        imageFile
    )

    // Guardar la URL de la imagen en la base de datos a través de placeViewModel
    placeViewModel.updatePlaceImage(placeId.toString(), imageUri.toString())

    // Devolver la URI del archivo guardado
    return imageUri
}















