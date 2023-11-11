package com.final_project

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.final_project.databinding.ActivityKalkulatorImtBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class kalkulator_imt : AppCompatActivity() {

    private lateinit var binding: ActivityKalkulatorImtBinding
    private lateinit var auth: FirebaseAuth

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKalkulatorImtBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()


        // Menampilkan nama pengguna dari database
        fetchDataFromDatabase()

        // Mengatur aksi ketika tombol "Hitung" ditekan
        binding.btnhitung.setOnClickListener {
            // Mendapatkan nilai tinggi dan berat dari input pengguna
            val tinggiText = binding.tinggibadan.editText?.text.toString()
            val beratText = binding.beratbadan.editText?.text.toString()

            if (tinggiText.isEmpty() || beratText.isEmpty()) {
                // Menampilkan pesan kesalahan jika tinggi atau berat kosong
                Toast.makeText(this, "Isi semua kolom !", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tinggi: Double = tinggiText.toDoubleOrNull() ?: 0.0
            val berat: Double = beratText.toDoubleOrNull() ?: 0.0

            // Menghitung IMT
            val imt = berat / ((tinggi / 100) * (tinggi / 100))

            // Menampilkan hasil IMT
            binding.hasilimt.text = String.format("%.1f", imt)

            // Menentukan status berat badan dan mengatur latar belakang hasil IMT
            if (imt < 18.5) {
                binding.barhasil.setBackgroundResource(R.drawable.bar_giziburuk)
                binding.barhasil.text = "Kurus"
                binding.textEdukasi.text = "\"Jika Anda mengalami gizi kurang, penting untuk meningkatkan asupan nutrisi Anda. Konsumsilah makanan yang kaya protein, vitamin, dan mineral untuk membantu memperbaiki kondisi gizi Anda. Pilih makanan sehat seperti daging tanpa lemak, ikan, telur, kacang-kacangan, sayuran hijau, dan buah-buahan. Konsultasikan dengan ahli gizi atau dokter untuk merencanakan diet yang sesuai dengan kebutuhan Anda.\""
            } else if (imt >= 18.5 && imt < 25) {
                binding.barhasil.setBackgroundResource(R.drawable.bar_ideal)
                binding.barhasil.text = "Ideal"
                binding.textEdukasi.text = "\"Selamat, Anda memiliki berat badan ideal!. Untuk menjaga keadaan ini, pertahankan pola makan seimbang dan rajin berolahraga. Pilihlah makanan sehat seperti sayuran, buah-buahan, biji-bijian, protein nabati, dan protein hewani dalam proporsi yang seimbang. Jangan lupa untuk tetap aktif secara fisik dan kunjungi dokter secara teratur untuk memantau kesehatan Anda.\""
            } else if (imt >= 25 && imt < 30) {
                binding.barhasil.setBackgroundResource(R.drawable.bar_kurang)
                binding.barhasil.text = "Lebih"
                binding.textEdukasi.text = "\"Jika Anda memiliki kelebihan berat badan, penting untuk memperhatikan pola makan dan gaya hidup Anda. Kurangi asupan kalori dengan menghindari makanan tinggi lemak dan gula. Fokus pada makanan sehat seperti buah-buahan, sayuran, protein rendah lemak, dan biji-bijian utuh. Olahraga teratur juga penting untuk membakar kalori dan meningkatkan kebugaran Anda.\""
            } else {
                binding.barhasil.setBackgroundResource(R.drawable.bar_giziburuk)
                binding.barhasil.text = "Obesitas"
                binding.textEdukasi.text = "\"Obesitas dapat meningkatkan risiko berbagai penyakit serius seperti diabetes, penyakit jantung, dan tekanan darah tinggi. Penting untuk mengambil langkah-langkah untuk menurunkan berat badan dengan cara yang sehat. Konsultasikan dengan ahli gizi atau dokter untuk merencanakan program penurunan berat badan yang aman dan berkelanjutan. Fokus pada pola makan rendah kalori, olahraga teratur, dan dukungan sosial untuk mencapai tujuan kesehatan Anda.\""
            }

            // Menampilkan blok kode ketika hasil IMT sudah dihitung
            binding.linear1.visibility = View.VISIBLE

            // Menyembunyikan keyboard saat tombol "Hitung" ditekan
            hideKeyboard()
        }

        // Menyembunyikan keyboard saat area di luar input diklik
        binding.root.setOnClickListener {
            hideKeyboard()
        }
        binding.keluar.setOnClickListener {
            signOut()
        }
    }

    // Metode untuk mengambil data pengguna dari database dan menampilkannya di TextView
    private fun fetchDataFromDatabase() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val database = FirebaseDatabase.getInstance() // Initialize FirebaseDatabase instance
            val userReference: DatabaseReference = database.reference.child("users").child(userId)
            userReference.child("Nama").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val namaPengguna = dataSnapshot.value as? String
                    // Menampilkan nama pengguna di TextView
                    binding.namapengguna.text = namaPengguna
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Penanganan kegagalan ketika mengambil data dari Realtime Database
                    Toast.makeText(
                        this@kalkulator_imt,
                        "Gagal mengambil data pengguna: ${databaseError.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            // Pengguna belum login
            Toast.makeText(
                this,
                "Pengguna belum login",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Fungsi untuk keluar (sign out)
    private fun signOut() {
        auth.signOut()
        Toast.makeText(this, "Berhasil keluar", Toast.LENGTH_SHORT).show()

        // Navigasi ke activity login
        val intent = Intent(this, login::class.java)
        startActivity(intent)
        finish()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        // Menghilangkan fokus dari EditText
        binding.tinggibadan.clearFocus()
        binding.beratbadan.clearFocus()
    }
}