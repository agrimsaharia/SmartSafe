package com.example.smartsafe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import com.example.smartsafe.models.HistoryAdapter
import com.example.smartsafe.models.HistoryItem
import com.google.firebase.database.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var adapter: HistoryAdapter
    private lateinit var historyRef: DatabaseReference
    private lateinit var historyListView: ListView
    private lateinit var historyList: MutableList<HistoryItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        historyListView = findViewById(R.id.history_listview)

        historyList = mutableListOf()
        adapter = HistoryAdapter(this@HistoryActivity, historyList)
        historyListView.adapter = adapter

        // Retrieve the data passed from the previous activity
        val arduinoID = intent.getStringExtra("ArduinoID")
        historyRef = FirebaseDatabase.getInstance().getReference("/Arduino/$arduinoID/history")

        updateHistoryList()

        historyRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val historyItem = snapshot.getValue(HistoryItem::class.java)
                if (historyItem != null) {
                    historyList.add(historyItem)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HistoryActivity", "Failed to read history item", error.toException())
            }
        })
    }

    private fun updateHistoryList() {
        val snapshot = historyRef.get().addOnSuccessListener {
            for (item in it.children)
            {
                val historyItem = item.getValue(HistoryItem::class.java)
                if (historyItem != null) {
                    historyList.add(historyItem)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }
}