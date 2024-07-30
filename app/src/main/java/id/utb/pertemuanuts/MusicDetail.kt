package id.utb.pertemuanuts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class MusicDetail : AppCompatActivity() {
    lateinit var newsTitle: TextView
    lateinit var newsSubtitle: TextView
    lateinit var newsImage: ImageView

    lateinit var edit: Button
    lateinit var hapus: Button
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_music_detail)

        newsTitle = findViewById(R.id.newsTitle)
        newsSubtitle = findViewById(R.id.newsSubtitle)
        newsImage = findViewById(R.id.newsImage)
        edit = findViewById(R.id.editButton)
        hapus = findViewById(R.id.deleteButton)
        db = FirebaseFirestore.getInstance()


        val intent = intent
        val id = intent.getStringExtra( "id")
        val title = intent.getStringExtra( "title")
        val subtitle = intent.getStringExtra( "desc")
        val imageUrl = intent.getStringExtra( "imageUrl")


        newsTitle.text = title
        newsSubtitle.text = subtitle
        Glide.with(this).load(imageUrl).into(newsImage)

        edit.setOnClickListener{
            val editIntent = Intent( this@MusicDetail, MusicAdd::class.java).apply {
                putExtra( "id", id)
                putExtra( "title", title)
                putExtra( "desc", subtitle)
                putExtra( "imageUrl", imageUrl)
            }
            startActivity(editIntent)
        }

        hapus.setOnClickListener{
            id?.let { documentId ->
                db.collection("music").document(documentId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this@MusicDetail, "Music deleted successfully",
                            Toast.LENGTH_SHORT).show()
                        val mainIntent = Intent( this@MusicDetail, DashboardActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(mainIntent)
                        finish()
                    }

                    .addOnFailureListener { e ->
                        Toast.makeText( this@MusicDetail, "Error deleting Music: ${e.message}",
                            Toast.LENGTH_SHORT).show()
                        Log.w(  "MusicDetail",  "Error deleting document", e)
                    }
            }
        }
    }
}