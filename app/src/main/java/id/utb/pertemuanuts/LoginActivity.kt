package id.utb.pertemuanuts

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val btnLogin: Button = findViewById(R.id.loginBtn)
        btnLogin.setOnClickListener{
            val usernameEditText: EditText = findViewById(R.id.usernameLogin)
            val passwordEditText: EditText = findViewById(R.id.passwordLogin)

            val username: String = usernameEditText.text.toString()
            val password: String = passwordEditText.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Mohon Lengkapi Semua Form!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (username.length !in 6..12) {
                Toast.makeText(this, "Username Harus Terdiri dari 6-12 Karakter!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length !in 6..12) {
                Toast.makeText(this, "Password Harus Terdiri dari 6-12 Karakter!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (username == MainActivity.registeredUsername && password == MainActivity.registeredPassword) {
                Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Username atau Password Salah!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}