package com.final_project

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.final_project.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class signup : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        binding.tvklikdisini.setOnClickListener {
            showDialog("Fitur belum tersedia")
        }




        binding.btnsignup.setOnClickListener {
            val email = binding.email.editText?.text.toString().trim()
            val password = binding.password.editText?.text.toString().trim()
            val nama = binding.nama.editText?.text.toString().trim()


            if (email.isNotEmpty() && password.isNotEmpty() && nama.isNotEmpty()) {
                if (isInternetAvailable()) {
                    if (isPasswordValid(password)) {
                        registerUser(email, password, nama)
                    } else {
                        showAlertDialog("Password minimal terdiri dari 8 karakter dan harus mengandung setidaknya satu angka, satu huruf besar, dan satu karakter khusus.")
                    }
                } else {
                    Toast.makeText(this, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Harap isi semua kolom!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun isPasswordValid(password: String): Boolean {
        if (password.length < 8) {
            return false
        }

        val passwordRegex = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}\$"
        return password.matches(passwordRegex.toRegex())
    }


    private fun registerUser(email: String, password: String, nama: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser? = auth.currentUser
                    val userId: String = firebaseUser?.uid ?: "nama"

                    val user = hashMapOf(
                        "Email" to email,
                        "Nama" to nama
                    )

                    databaseReference.child("users")
                        .child(userId)
                        .setValue(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registrasi berhasil", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, login::class.java)
                            intent.putExtra("registered_email", email)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Gagal menyimpan data pengguna",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    if (task.exception?.message == "E-mail sudah digunakan oleh akun lain") {
                        Toast.makeText(
                            this,
                            "Registrasi gagal. E-mail sudah terdaftar!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Registrasi gagal. Periksa kembali email dan password Anda",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    private fun showAlertDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
    private fun showDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
            .setCancelable(true)
            .setPositiveButton("Tutup") { dialog, _ ->
                dialog.dismiss()
            }

        val alert = builder.create()
        alert.show()
    }

}