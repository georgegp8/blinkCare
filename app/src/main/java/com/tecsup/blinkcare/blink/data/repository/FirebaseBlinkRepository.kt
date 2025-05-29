package com.tecsup.blinkcare.blink.data.repository

import com.google.firebase.database.*

class FirebaseBlinkRepository {

    private val database = FirebaseDatabase.getInstance()
    private val blinkRef = database.getReference("blink_data")

    fun listenBlinkData(
        onBlinkCountChanged: (Int) -> Unit,
        onAlertChanged: (Boolean) -> Unit
    ) {
        blinkRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val blinkCount = snapshot.child("blink_count").getValue(Int::class.java) ?: 0
                val alert = snapshot.child("alert").getValue(Boolean::class.java) ?: false
                onBlinkCountChanged(blinkCount)
                onAlertChanged(alert)
            }
            override fun onCancelled(error: DatabaseError) {
                // Manejar error si es necesario
            }
        })
    }
}