package id.utb.pertemuanuts

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.annotation.Nullable;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

class MusicAdd : AppCompatActivity() {
    private var id: String? = ""
    private var judul: String? = null
    private var deskripsi: String? = null
    private var image: String? = null

    private val PICK_IMAGE_REQUEST = 1

    private lateinit var title: EditText
    private lateinit var desc: EditText
    private lateinit var imageView: ImageView
    private lateinit var saveNews: Button
    private lateinit var chooseImage: Button
    private var imageUri: Uri? = null

    private lateinit var dbNews: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_music_add)

        dbNews = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize UI components
        title = findViewById(R.id.title)
        desc= findViewById(R.id.desc)
        imageView = findViewById(R.id.imageView)
        saveNews = findViewById(R.id.btnAdd)
        chooseImage = findViewById(R.id.btnChooseImage)

        val updateOption = intent
        if (updateOption != null) {
            id = updateOption.getStringExtra( "id")
            judul = updateOption.getStringExtra(  "title")
            deskripsi = updateOption.getStringExtra(  "desc")
            image = updateOption.getStringExtra(  "imageUrl")

            title.setText(judul)
            desc.setText(deskripsi)
            Glide.with ( this).load(image). into (imageView)
        }

        progressDialog = ProgressDialog(this@MusicAdd).apply {
            setTitle("Loading....")
        }

        chooseImage.setOnClickListener{
            openFileChooser()
        }

        saveNews.setOnClickListener{
            val newsTitle = title.text.toString().trim()
            val newsDesc = desc.text.toString().trim()

            if (newsTitle.isEmpty() || newsDesc.isEmpty()) {
                Toast.makeText(this@MusicAdd, "Title and Description cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progressDialog.show()
            if (imageUri != null ){
                uploadImageToStorage(newsTitle, newsDesc)
            } else {
                saveData(newsTitle, newsDesc, image ?: "")
            }
        }



    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null ){
            imageUri = data.data
            imageView.setImageURI(imageUri)
        }
    }

    private fun uploadImageToStorage (newsTitle: String, newsDesc: String) {
        imageUri?.let { uri ->
            val storageRef =
                storage.reference.child("music_images/" + System.currentTimeMillis() + ".jpg")
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val imageUrl = downloadUri.toString()
                        saveData(newsTitle, newsDesc, imageUrl)
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@MusicAdd,
                        "Failed to upload image: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                }
        }
    }

    private fun saveData (newsTitle: String, newsDesc: String, imageUrl: String) {
        val news = HashMap<String, Any>()
        news["title"] = newsTitle
        news["desc"] = newsDesc
        news["imageUrl"]= imageUrl

        if (id != null) {
            dbNews.collection("music")
                .document(id?:"")
                .update(news)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this@MusicAdd, "Music added successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@MusicAdd,
                        "Error updating Music: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                    Log.w("MusicAdd", "Error updating document ", e)
                }

        } else {
            dbNews.collection("music")
                .add(news)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this@MusicAdd, "Music added successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@MusicAdd,
                        "Error adding Music: ${e.message}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    Log.w("MusicAdd", "Error adding document ", e)
                }
        }

    }
}