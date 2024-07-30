package id.utb.pertemuanuts

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import android.app.ProgressDialog
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import android.graphics.Color

class DashboardActivity : AppCompatActivity() {
    private lateinit var myAdapter: AdapterList
    private lateinit var itemList: MutableList<ItemList>
    private lateinit var db: FirebaseFirestore
    private lateinit var progressDialog: ProgressDialog

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        FirebaseApp.initializeApp( this)
        db = FirebaseFirestore.getInstance()

        val recyclerView = findViewById<RecyclerView>(R.id.rcvMusic)
        val floatingActionButton = findViewById<FloatingActionButton>(R.id.floatAddMusic); progressDialog = ProgressDialog( this@DashboardActivity).apply {
            setTitle("Loading...")
        }
        val accountLogout = findViewById<ImageView>(R.id.account)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager( this)
        itemList = ArrayList()
        myAdapter = AdapterList(itemList)
        recyclerView.adapter = myAdapter


        floatingActionButton.setOnClickListener {
            val toAddPage = Intent(this@DashboardActivity, MusicAdd::class.java)
            startActivity(toAddPage)
        }

        myAdapter.setOnItemClickListener(object: AdapterList.OnItemClickListener {
            override fun onItemClick(item: ItemList) {
                val intent = Intent( this@DashboardActivity, MusicDetail::class.java).apply {
                    putExtra(  "id", item.id)
                    putExtra( "title", item.judul)
                    putExtra( "desc", item.subJudul)
                    putExtra( "imageUrl", item.imageUrl)
                }
                startActivity(intent)
            }
        })
        accountLogout.setOnClickListener {
            showLogoutDialog()
        }

    }

    override fun onStart(){
        super.onStart()
        getData()
    }

    private fun getData() {
        progressDialog.show()
        db.collection("music")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    itemList.clear()
                    for (document in task.result) {
                        val item = ItemList(
                            document.id,
                            document.getString("title") ?: "",
                            document.getString("desc") ?: "",
                            document.getString("imageUrl") ?: ""
                        )
                        itemList.add(item)
                        Log.d("data", "${document.id} => ${document.data}")
                    }
                    myAdapter.notifyDataSetChanged()
                } else {
                    Log.w("data", "Error getting documents.", task.exception)
                }
                progressDialog.dismiss()
            }
    }

    private fun showLogoutDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        }

        dialog.show()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}