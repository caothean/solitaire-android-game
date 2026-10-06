package com.solitaire.game

import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.solitaire.game.managers.SaveManager
import com.solitaire.game.models.Difficulty
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    
    private lateinit var saveManager: SaveManager
    private lateinit var listViewHistory: ListView
    private lateinit var spinnerDifficulty: Spinner
    private lateinit var btnBack: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        
        saveManager = SaveManager(this)
        initializeViews()
        loadHistory()
    }
    
    private fun initializeViews() {
        listViewHistory = findViewById(R.id.list_view_history)
        spinnerDifficulty = findViewById(R.id.spinner_difficulty)
        btnBack = findViewById(R.id.btn_back)
        
        // Setup difficulty spinner
        val difficulties = listOf("All", "Easy", "Normal", "Hard", "Extreme", "Legendary")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, difficulties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDifficulty.adapter = adapter
        
        spinnerDifficulty.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                loadHistory()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        
        btnBack.setOnClickListener { finish() }
    }
    
    private fun loadHistory() {
        lifecycleScope.launch {
            val selectedDifficulty = spinnerDifficulty.selectedItem.toString()
            
            val history = if (selectedDifficulty == "All") {
                saveManager.getAllWinHistory()
            } else {
                saveManager.getWinHistoryByDifficulty(selectedDifficulty.uppercase())
            }
            
            val items = history.map { win ->
                "${win.getShortDate()} | ${win.difficulty} | ${win.getFormattedTime()} | Score: ${win.score}"
            }
            
            val adapter = ArrayAdapter(
                this@HistoryActivity,
                android.R.layout.simple_list_item_1,
                items
            )
            listViewHistory.adapter = adapter
        }
    }
}
