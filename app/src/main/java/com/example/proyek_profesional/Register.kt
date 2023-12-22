package com.example.proyek_profesional

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.proyek_profesional.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import java.util.regex.Pattern

class Register : AppCompatActivity() {

    lateinit var binding: ActivityRegisterBinding
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnDaftar.setOnClickListener{
            val nama = binding.registerNama.text.toString()
            val email = binding.registerEmail.text.toString()
            val password = binding.registerPassword.text.toString()

            //Validasi nama
            if(nama.isEmpty()){
                binding.registerNama.error = "Nama harus terisi"
                binding.registerNama.requestFocus()
                return@setOnClickListener
            }

            //Validasi email
            if (email.isEmpty()){
                binding.registerEmail.error = "Email harus terisi"
                binding.registerEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                binding.registerEmail.error = "Email tidak valid"
                binding.registerEmail.requestFocus()
                return@setOnClickListener
            }

            //validasi password
            if (password.isEmpty()){
                binding.registerPassword.error = "Password harus terisi"
                binding.registerPassword.requestFocus()
                return@setOnClickListener
            }

            if (password.length <8){
                binding.registerPassword.error = "Password minimal 8 karakter"
                binding.registerPassword.requestFocus()
                return@setOnClickListener
            }

            RegisterFirebase(nama, email, password)
        }

        binding.btnMasuk.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun RegisterFirebase(nama: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){
                if (it.isSuccessful){
                    val user = FirebaseAuth.getInstance().currentUser
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(nama)
                        .build()
                    user?.updateProfile(profileUpdate)
                    Toast.makeText(this,"Register Berhasil", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                } else{
                    Toast.makeText(this, "${it.exception?.message}",Toast.LENGTH_LONG).show()
                }
            }
    }
}