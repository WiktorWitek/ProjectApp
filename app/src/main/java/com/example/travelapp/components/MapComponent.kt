package com.example.travelapp.components

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun MapComponent(searchedLocation: LatLng?, currentLocation: LatLng?, modifier: Modifier = Modifier) {
    val mapView = rememberMapViewWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    AndroidView(factory = { mapView }, modifier = modifier.fillMaxSize()) { mapView ->
        mapView.getMapAsync { googleMap ->
            googleMap.uiSettings.isZoomControlsEnabled = true

            // Ustawienie kamery na aktualną lokalizację
            currentLocation?.let {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 12f))
                googleMap.addMarker(MarkerOptions().position(it).title("Your Location"))
            }

            // Ustawienie kamery na wyszukaną lokalizację
            searchedLocation?.let {
                coroutineScope.launch {
                    googleMap.clear()  // Wyczyszczenie poprzednich markerów
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 12f))
                    googleMap.addMarker(MarkerOptions().position(it).title("Searched Location"))
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun getCurrentLocation(context: Context, onLocationReceived: (LatLng) -> Unit) {
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        location?.let {
            onLocationReceived(LatLng(it.latitude, it.longitude))
        }
    }
}

fun getLocationFromAddress(context: Context, strAddress: String): LatLng? {
    val coder = Geocoder(context, Locale.getDefault())
    val address = coder.getFromLocationName(strAddress, 1)
    return if (address != null && address.isNotEmpty()) {
        val location = address[0]
        LatLng(location.latitude, location.longitude)
    } else {
        null
    }
}


@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle) {
        val observer = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                mapView.onCreate(null)
            }

            override fun onStart(owner: LifecycleOwner) {
                mapView.onStart()
            }

            override fun onResume(owner: LifecycleOwner) {
                mapView.onResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                mapView.onPause()
            }

            override fun onStop(owner: LifecycleOwner) {
                mapView.onStop()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                mapView.onDestroy()
            }
        }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    return mapView
}
