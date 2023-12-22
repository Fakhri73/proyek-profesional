package com.example.proyek_profesional

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.proyek_profesional.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var auth: FirebaseAuth
    lateinit var googleSignin: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignin = GoogleSignIn.getClient(this, gso)

        findViewById<ImageButton>(R.id.btnGoogle).setOnClickListener {
            signInGoogle()
        }

        checkauth()



        binding.tvToRegister.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        binding.tvLupaPass.setOnClickListener{
            val email = binding.emailLogin.text.toString()
            if (email.isEmpty()){
                binding.emailLogin.error = "Email harus terisi"
                binding.emailLogin.requestFocus()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        Toast.makeText(
                            this,
                            "Email reset kata sandi telah dikirim",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Gagal mengirim email reset kata sandi",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.emailLogin.text.toString()
            val password = binding.passwordLogin.text.toString()

            //Validasi email
            if (email.isEmpty()) {
                binding.emailLogin.error = "Email harus terisi"
                binding.emailLogin.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailLogin.error = "Email tidak valid"
                binding.emailLogin.requestFocus()
                return@setOnClickListener
            }

            //validasi password
            if (password.isEmpty()) {
                binding.passwordLogin.error = "Password harus terisi"
                binding.passwordLogin.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 8) {
                binding.passwordLogin.error = "Password minimal 8 karakter"
                binding.passwordLogin.requestFocus()
                return@setOnClickListener
            }

            LoginFirebase(email, password)

        }

    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage("Apakah yakin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                finishAffinity() // mengakhiri semua aktivitas yang terbuka
                System.exit(0) // keluar dari aplikasi
            }
            .setNegativeButton("Tidak", null)
            .show()


    }

    private fun LoginFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Selamat datang $email", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkauth() {
        if (auth.currentUser != null) {
            startActivity(Intent(this, home::class.java))
            finish()
        }
    }

    private fun signInGoogle() {
        val signInIntent = googleSignin.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResult(task)
            }
        }

    private fun handleResult(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                updateUI(account)
            }
        } else {
            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
        }

    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Selamat datang", Toast.LENGTH_SHORT).show()
                val intent: Intent = Intent(this, home::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

}