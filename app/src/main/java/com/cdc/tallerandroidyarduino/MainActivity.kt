package com.cdc.tallerandroidyarduino

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.cdc.tallerandroidyarduino.ui.theme.TallerAndroidYArduinoTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var realtimeDB: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        realtimeDB = Firebase.database
        setContent {
            MainScreen()
        }
    }

    private fun saveDistance(context: Context, distance: Int) {
        val database = realtimeDB.getReference("detectionDistance")
        database
            .setValue(distance)
            .addOnSuccessListener {
                Toast.makeText(context, "Se guardo la distancia de deteccción", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(
                    context,
                    "Ocurrio un error al guardar la distancia de deteccción",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun listenLedStateDataFromFirebase(onDataChange: (Boolean) -> Unit) {
        val database = realtimeDB.getReference("ledState")
        val greetingValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ledStater: Boolean = snapshot.getValue<Boolean>() ?: false
                onDataChange(ledStater)
            }

            override fun onCancelled(error: DatabaseError) {
                onDataChange(false)
            }

        }
        database.addValueEventListener(greetingValueEventListener)
    }

    private fun listenCurrentDistanceDataFromFirebase(onDataChange: (Int) -> Unit) {
        val database = realtimeDB.getReference("currentDistance")
        val greetingValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentDistance: Int = snapshot.getValue<Int>() ?: 0
                onDataChange(currentDistance)
            }

            override fun onCancelled(error: DatabaseError) {
                onDataChange(0)
            }

        }
        database.addValueEventListener(greetingValueEventListener)
    }

    @Composable
    fun MainScreen() {
        TallerAndroidYArduinoTheme(
            darkTheme = false
        ) {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                val context = LocalContext.current
                var ledStateData by remember { mutableStateOf(false) }
                var currentDistanceData by remember { mutableIntStateOf(0) }

                LaunchedEffect(Unit) {
                    listenLedStateDataFromFirebase { ledState ->
                        ledStateData = ledState
                    }
                    listenCurrentDistanceDataFromFirebase { currentDistance ->
                        currentDistanceData = currentDistance
                    }
                }
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Taller de IOT con Android y Arduino")
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    private fun PreviewMainScreen() {
        MainScreen()
    }

}