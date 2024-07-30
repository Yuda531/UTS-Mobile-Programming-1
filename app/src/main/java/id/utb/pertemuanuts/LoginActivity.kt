package id.utb.pertemuanuts

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "LoginActivity"
    }
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var googleSignInButton: Button
    private lateinit var db: FirebaseFirestore

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "prefs"
    private val PREF_EMAIL = "email"
    private val PREF_PASSWORD = "password"
    private val PREF_REMEMBER = "remember"


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val btnLogin: Button = findViewById(R.id.loginBtn)
        val emailEditText: EditText = findViewById(R.id.emailLogin)
        val passwordEditText: EditText = findViewById(R.id.passwordLogin)
        val emailIcon: ImageView = findViewById(R.id.iconEmail)
        val passwordToggle: ImageView = findViewById(R.id.iconPassword)
        val rememberMe: CheckBox = findViewById(R.id.rememberMe)
        progressBar = findViewById(R.id.progressBar)
        googleSignInButton = findViewById(R.id.googleSignInButton)

        googleSignInButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.google, 0, 0, 0)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mAuth = FirebaseAuth.getInstance()
        googleSignInButton.setOnClickListener {
            signIn()
        }

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

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean(PREF_REMEMBER, false)) {
            emailEditText.setText(sharedPreferences.getString(PREF_EMAIL, ""))
            passwordEditText.setText(sharedPreferences.getString(PREF_PASSWORD, ""))
            rememberMe.isChecked = true
        }

        btnLogin.setOnClickListener {
            val email: String = emailEditText.text.toString().trim()
            val password: String = passwordEditText.text.toString().trim()

            if (validateInputs(email, password)) {
                progressBar.visibility = View.VISIBLE

                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        progressBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            val userId = mAuth.currentUser?.uid
                            userId?.let {
                                db.collection("users").document(it).get()
                                    .addOnSuccessListener { document ->
                                        if (document.exists()) {
                                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this, DashboardActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                            if (rememberMe.isChecked) {
                                                sharedPreferences.edit().apply {
                                                    putString(PREF_EMAIL, email)
                                                    putString(PREF_PASSWORD, password)
                                                    putBoolean(PREF_REMEMBER, true)
                                                    apply()
                                                }
                                            } else {
                                                sharedPreferences.edit().clear().apply()
                                            }
                                        } else {
                                            Toast.makeText(this, "User data not found in the database.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }

        }
        emailEditText.addTextChangedListener {
            val isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(emailEditText.text.toString().trim()).matches()
            emailIcon.setImageResource(if (isValid) R.drawable.done_icon_green else R.drawable.done_icon)
        }

        clickRegisterText()
    }

    private fun validateInputs( email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length !in 6..12) {
            Toast.makeText(this, "Password must be between 6-12 characters!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun clickRegisterText() {
        val registerTextView: TextView = findViewById(R.id.registerOr)
        val text = getString(R.string.or_login_with)
        val spannableString = SpannableString(text)
        val registerText = "Register"

        val registerStartIndex = text.indexOf(registerText)
        val registerEndIndex = registerStartIndex + registerText.length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }

        val colorSpan = ForegroundColorSpan(Color.RED)
        spannableString.setSpan(clickableSpan, registerStartIndex, registerEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(colorSpan, registerStartIndex, registerEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        registerTextView.text = spannableString
        registerTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id)
        val credential: AuthCredential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user: FirebaseUser? = mAuth.currentUser
                    Toast.makeText( this, "Authentication Successful.", Toast.LENGTH_SHORT).show()
                    val intent = Intent( this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText( this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }

                progressBar.visibility = View.GONE
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle (account!!)
            } catch (e: ApiException) {
                Log.w(TAG,  "Google sign in failed", e)
                progressBar.visibility = View.GONE
                Toast.makeText( this, "Google sign in failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signIn () {
        progressBar.visibility = View.VISIBLE
        val singInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(singInIntent, RC_SIGN_IN)
    }


}