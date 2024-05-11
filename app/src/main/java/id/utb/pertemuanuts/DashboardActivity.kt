package id.utb.pertemuanuts

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        val listView = findViewById<ListView>(R.id.list_song)

        val songs = arrayOf(
            "Sweet Child o' Mine - Guns N' Roses",
            "Bohemian Rhapsody - Queen",
            "Livin' on a Prayer - Bon Jovi",
            "Let It Be - The Beatles",
            "Hotel California - Eagles",
            "Until I Found You - Stephen Sanchez ft. Em Beihold  ",
            "I Just Couldn't Save You Tonight - Ardhito Pramono",
            "Let Her Go - Passenger",
            "Night Changes - One Direction",
            "Khayalan Tingkat Tinggi - Peterpan",
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, songs)
        listView.adapter = adapter

    }
}