    package id.utb.pertemuanuts

    import android.content.Intent
    import android.graphics.Color
    import android.os.Bundle
    import android.text.SpannableString
    import android.text.Spanned
    import android.text.method.HideReturnsTransformationMethod
    import android.text.method.PasswordTransformationMethod
    import android.text.style.ClickableSpan
    import android.text.style.ForegroundColorSpan
    import android.util.Log
    import android.view.View
    import android.widget.Button
    import android.widget.EditText
    import android.widget.ImageView
    import android.widget.TextView
    import android.widget.Toast
    import androidx.activity.enableEdgeToEdge
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
    import android.widget.ProgressBar
    import androidx.core.widget.addTextChangedListener
    import com.google.firebase.FirebaseApp
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.FirebaseFirestore

    class MainActivity : AppCompatActivity() {

        private lateinit var progressBar: ProgressBar
        private lateinit var mAuth: FirebaseAuth
        private lateinit var db: FirebaseFirestore

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            Thread.sleep(3000)
            installSplashScreen()
            enableEdgeToEdge()
            setContentView(R.layout.activity_main)

            val usernameEditText: EditText = findViewById(R.id.usernameRegis)
            val phoneNumberEditText: EditText = findViewById(R.id.phoneRegis)
            val emailAddressEditText: EditText = findViewById(R.id.emailRegis)
            val passwordEditText: EditText = findViewById(R.id.passwordRegis)

            val usernameIcon: ImageView = findViewById(R.id.iconUsername)
            val phoneIcon: ImageView = findViewById(R.id.iconPhone)
            val emailIcon: ImageView = findViewById(R.id.iconEmail)
            val passwordToggle: ImageView = findViewById(R.id.iconPassword)


            val btnRegister: Button = findViewById(R.id.btnRegister)
            progressBar = findViewById(R.id.progressBar)
            mAuth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()

            passwordToggle.setOnClickListener {
                if (passwordEditText.transformationMethod is PasswordTransformationMethod) {
                    passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    passwordToggle.setImageResource(R.drawable.password_icon_visible)
                } else {
                    passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                    passwordToggle.setImageResource(R.drawable.password_icon)
                }
                passwordEditText.setSelection(passwordEditText.text.length)
            }

            btnRegister.setOnClickListener {

                val username: String = usernameEditText.text.toString().trim()
                val phoneNumber: String = phoneNumberEditText.text.toString().trim()
                val emailAddress: String = emailAddressEditText.text.toString().trim()
                val password: String = passwordEditText.text.toString().trim()

                if (validateInputs(username, phoneNumber, emailAddress, password)) {
                    progressBar.visibility = View.VISIBLE
                    mAuth.createUserWithEmailAndPassword(emailAddress, password)
                        .addOnCompleteListener { task ->
                            progressBar.visibility = View.GONE
                            if (task.isSuccessful) {
                                val userId = mAuth.currentUser?.uid
                                val user = hashMapOf(
                                    "username" to username,
                                    "phoneNumber" to phoneNumber,
                                    "email" to emailAddress
                                )
                                userId?.let {
                                    db.collection("users").document(it)
                                        .set(user)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this, LoginActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                }
                            } else {
                                Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                Log.v("errornya", task.exception?.message ?: "Unknown error")
                            }
                        }
                }
            }

            usernameEditText.addTextChangedListener {
                updateIcon(usernameEditText.text.toString().trim(), usernameIcon, 6..12)
            }
            phoneNumberEditText.addTextChangedListener {
                updateIcon(phoneNumberEditText.text.toString().trim(), phoneIcon, 6..14)
            }
            emailAddressEditText.addTextChangedListener {
                val isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddressEditText.text.toString().trim()).matches()
                emailIcon.setImageResource(if (isValid) R.drawable.done_icon_green else R.drawable.done_icon)
            }

            clickLoginText()
        }

        private fun validateInputs(username: String, phoneNumber: String, emailAddress: String, password: String): Boolean {
            if (username.isEmpty() || phoneNumber.isEmpty() || emailAddress.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please complete all fields!", Toast.LENGTH_SHORT).show()
                return false
            }
            if (username.length !in 6..12) {
                Toast.makeText(this, "Username must be 6-12 characters!", Toast.LENGTH_SHORT).show()
                return false
            }
            if (phoneNumber.length !in 6..14) {
                Toast.makeText(this, "Phone number must be 6-14 digits!", Toast.LENGTH_SHORT).show()
                return false
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
                Toast.makeText(this, "Invalid email address!", Toast.LENGTH_SHORT).show()
                return false
            }
            if (password.length !in 6..12) {
                Toast.makeText(this, "Password must be 6-12 characters!", Toast.LENGTH_SHORT).show()
                return false
            }
            return true
        }

        private fun updateIcon(text: String, icon: ImageView, lengthRange: IntRange) {
            val isValid = text.length in lengthRange
            icon.setImageResource(if (isValid) R.drawable.done_icon_green else R.drawable.done_icon)
        }

        private fun clickLoginText() {
            val haveAccountTextView: TextView = findViewById(R.id.haveAccount)
            val text = getString(R.string.have_account)
            val spannableString = SpannableString(text)
            val loginText = "Login"

            val loginStartIndex = text.indexOf(loginText)
            val loginEndIndex = loginStartIndex + loginText.length

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
            }

            val colorSpan = ForegroundColorSpan(Color.RED)
            spannableString.setSpan(clickableSpan, loginStartIndex, loginEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(colorSpan, loginStartIndex, loginEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            haveAccountTextView.text = spannableString
            haveAccountTextView.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        }
    }