package com.example.proyek_profesional

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import com.example.proyek_profesional.databinding.ActivityLogoutBinding
import com.google.firebase.auth.FirebaseAuth

class Logout : AppCompatActivity() {



    private lateinit var binding: ActivityLogoutBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityLogoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()


        if(auth.currentUser != null){
            auth.currentUser?.let {
                binding.tvEmail.text = it.displayName + "\n" + it.email

            }
        }

        binding.btnInfo.setOnClickListener {
            val intent = Intent(this, info::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener{
            auth.signOut()
            startActivity(
                Intent(this, MainActivity::class.java)
            )
        }
    }
}