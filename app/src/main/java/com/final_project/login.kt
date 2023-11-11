package com.final_project

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.final_project.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth


class login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.klikdisini.setOnClickListener {
            val intent = Intent(this@login, signup::class.java)
            startActivity(intent)
        }


        binding.btnlogin.setOnClickListener {
            val email = binding.email.editText?.text.toString()
            val password = binding.password.editText?.text.toString()

            if (isInternetAvailable()) {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    loginUser(email, password)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Isi email dan password terlebih dahulu",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "Silahkan aktifkan koneksi internet",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.lupapassword.setOnClickListener {
            val email = EditText(this)
            val horizontalMarginInDp = 16

            val scale = resources.displayMetrics.density
            val horizontalMarginInPixel = (horizontalMarginInDp * scale + 0.5f).toInt()

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(horizontalMarginInPixel, 5, horizontalMarginInPixel, 5)
            email.layoutParams = params

            val inflater = layoutInflater
            val view = inflater.inflate(R.layout.alert_dialog_lupa_password, null)
            val emailEditText = view.findViewById<EditText>(R.id.editText)

            // Membuat pesan dengan warna merah
            val message =
                "Perhatian! Password baru harus mengandung setidaknya satu angka, satu huruf besar, dan satu karakter khusus. Silahkan masukkan alamat email Anda :"
            val spannableMessage = SpannableString(message)
            val redColor = ForegroundColorSpan(Color.RED)
            spannableMessage.setSpan(
                redColor,
                0,
                message.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Lupa Password")
                .setMessage(spannableMessage)
                .setView(view)
                .setPositiveButton("OK") { dialog, _ ->
                    val inputEmail = emailEditText.text.toString()
                    if (inputEmail.isNotEmpty()) {
                        // Hanya jika email tidak kosong, lakukan operasi reset password
                        sendPasswordResetEmail(inputEmail)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            alertDialog.show()
        }
    }

        private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Email reset password terkirim dengan sukses
                    showResetPasswordSuccessDialog()
                } else {
                    // Gagal mengirim email reset password
                    showResetPasswordErrorDialog(task.exception?.message ?: "Gagal mengirim email reset password")
                }
            }
    }

    private fun showResetPasswordSuccessDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Sukses")
            .setMessage("Email reset password telah terkirim. Silakan periksa kotak masuk email Anda.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    private fun showResetPasswordErrorDialog(errorMessage: String) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(errorMessage)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login berhasil
                    Toast.makeText(applicationContext, "Login berhasil!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@login, kalkulator_imt::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Login gagal
                    Toast.makeText(applicationContext, "Login gagal, periksa kembali email dan password Anda", Toast.LENGTH_SHORT).show()
                }
            }
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
}