    package id.utb.pertemuanuts

    import android.content.Intent
    import android.os.Bundle
    import android.widget.Button
    import android.widget.EditText
    import android.widget.Toast
    import androidx.activity.enableEdgeToEdge
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowInsetsCompat

    class MainActivity : AppCompatActivity() {

        companion object {
            var registeredUsername: String = ""
            var registeredPassword: String = ""
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            Thread.sleep(3000)
            installSplashScreen()
            enableEdgeToEdge()
            setContentView(R.layout.activity_main)


            val btnRegister: Button = findViewById(R.id.btnRegister)
            btnRegister.setOnClickListener {

                val usernameEditText: EditText = findViewById(R.id.usernameRegis)
                val phoneNumberEditText: EditText = findViewById(R.id.phoneRegis)
                val emailAddressEditText: EditText = findViewById(R.id.emailRegis)
                val passwordEditText: EditText = findViewById(R.id.passwordRegis)

                val username: String = usernameEditText.text.toString()
                val phoneNumber: String = phoneNumberEditText.text.toString()
                val emailAddress: String = emailAddressEditText.text.toString()
                val password: String = passwordEditText.text.toString()

                if (username.isEmpty() || phoneNumber.isEmpty() || emailAddress.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Mohon Lengkapi Semua Form!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (username.length !in 6..12) {
                    Toast.makeText(this, "Username Harus Terdiri dari 6-12 Karakter!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (phoneNumber.length !in 6..14) {
                    Toast.makeText(this, "Nomor Telepon Harus Terdiri dari 6-14 Angka!",  Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
                    Toast.makeText(this, "Alamat Email Tidak Valid!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (password.length !in 6..12) {
                    Toast.makeText(this, "Password Harus Terdiri dari 6-12 Karakter!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                registeredUsername = username
                registeredPassword = password

                Toast.makeText(this, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

        }

        fun getRegisteredUsername(): String {
            return registeredUsername
        }


        fun getRegisteredPassword(): String {
            return registeredPassword
        }
    }