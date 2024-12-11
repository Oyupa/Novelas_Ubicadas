package com.example.novelas_ubicadas

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
class UbicacionActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private val REQUEST_PERMISSIONS_CODE = 1
    private val db = FirebaseFirestore.getInstance() // Instancia de Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Borrar SharedPreferences al inicio
        val sharedPreferences = getSharedPreferences("osmdroid", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear() // Elimina todos los datos de SharedPreferences
        editor.apply() // Aplica los cambios

        // Configuración inicial de osmdroid
        Configuration.getInstance().load(this, this.getSharedPreferences("osmdroid", MODE_PRIVATE))
        setContentView(R.layout.activity_ubicacion)

        // Referencia al MapView
        mapView = findViewById(R.id.map)
        mapView.setMultiTouchControls(true) // Habilitar controles táctiles (zoom, desplazamiento)

        // Verificar permisos y configurar el mapa
        if (checkPermissions()) {
            setupMap()
        } else {
            requestPermissions()
        }

        // Crear el MapEventsReceiver (interceptor de eventos del mapa)
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                return false // No hacer nada en clic corto
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                // Al hacer clic largo en el mapa
                showAddNovelDialog(p) // Mostrar el formulario
                return true
            }
        }

        // Crear el MapEventsOverlay y añadirlo al MapView
        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(mapEventsOverlay)

        // Cargar las novelas de Firebase
        loadNovelsFromFirebase()
    }

    private fun setupMap() {
        // Configuración del overlay de ubicación
        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mapView)
        locationOverlay.enableMyLocation() // Activar ubicación actual
        mapView.overlays.add(locationOverlay)

        // Configuración de un marcador inicial en la ubicación actual
        locationOverlay.runOnFirstFix {
            val currentLocation = GeoPoint(locationOverlay.myLocation.latitude, locationOverlay.myLocation.longitude)
            runOnUiThread {
                mapView.controller.setZoom(18.0) // Nivel de zoom
                mapView.controller.setCenter(currentLocation)


            }
        }
    }

    private fun loadNovelsFromFirebase() {
        db.collection("novelas")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val title = document.getString("title")
                    val author = document.getString("author")
                    val year = document.getString("year")
                    val latitude = document.getDouble("latitude")
                    val longitude = document.getDouble("longitude")

                    // Asegurarse de que la novela tenga la información necesaria
                    if (latitude != null && longitude != null) {
                        val novelLocation = GeoPoint(latitude, longitude)

                        // Crear un marcador para la novela
                        val marker = Marker(mapView)
                        marker.position = novelLocation
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = "$title ($author, $year)"
                        mapView.overlays.add(marker)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar las novelas", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fineLocation == PackageManager.PERMISSION_GRANTED && coarseLocation == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_CODE
        )
    }

    private fun showAddNovelDialog(location: GeoPoint) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Añadir Nueva Novela")

        // Crear un formulario para introducir los datos
        val view = layoutInflater.inflate(R.layout.dialog_add_novel, null)
        val titleEditText = view.findViewById<EditText>(R.id.editTextTitle)
        val authorEditText = view.findViewById<EditText>(R.id.editTextAuthor)
        val yearEditText = view.findViewById<EditText>(R.id.editTextYear)

        builder.setView(view)

        builder.setPositiveButton("Guardar") { dialog, _ ->
            val title = titleEditText.text.toString()
            val author = authorEditText.text.toString()
            val year = yearEditText.text.toString()

            // Verificar que los campos no estén vacíos
            if (title.isNotBlank() && author.isNotBlank() && year.isNotBlank()) {
                // Crear un objeto de novela
                val novel = hashMapOf(
                    "title" to title,
                    "author" to author,
                    "year" to year,
                    "latitude" to location.latitude,
                    "longitude" to location.longitude
                )

                // Guardar la novela en Firestore
                db.collection("novelas")
                    .add(novel)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Novela añadida correctamente", Toast.LENGTH_SHORT).show()

                        // Recargar las novelas y marcadores
                        loadNovelsFromFirebase()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al añadir la novela", Toast.LENGTH_SHORT).show()
                    }

                dialog.dismiss()
            } else {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupMap()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume() // Reanudar el mapa
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause() // Pausar el mapa
    }
}
