package project.capstone.percobaan_capstone.detection

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import project.capstone.percobaan_capstone.R
import project.capstone.percobaan_capstone.clasifier.CattleWeightClassifier
import project.capstone.percobaan_capstone.customview.BtnCamera
import project.capstone.percobaan_capstone.customview.BtnGallery

class CattleWeightActivity : AppCompatActivity() {

    private lateinit var classifier: CattleWeightClassifier
    private lateinit var imageView: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var weightCategoryTextView: TextView
    private lateinit var buttonCamera: BtnCamera
    private lateinit var buttonGallery: BtnGallery

    private val REQUEST_CAMERA = 1
    private val REQUEST_GALLERY = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detect)

        imageView = findViewById(R.id.imageView)
        resultTextView = findViewById(R.id.result)
        weightCategoryTextView = findViewById(R.id.weightCategory)
        buttonCamera = findViewById(R.id.buttonCamera)
        buttonGallery = findViewById(R.id.buttonGallery)

        classifier = CattleWeightClassifier(this)

        buttonCamera.setOnClickListener {
            val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, REQUEST_CAMERA)
        }

        buttonGallery.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, REQUEST_GALLERY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            try {
                val bitmap: Bitmap? = when (requestCode) {
                    REQUEST_CAMERA -> data?.extras?.get("data") as? Bitmap
                    REQUEST_GALLERY -> {
                        val imageUri = data?.data
                        imageUri?.let {
                            val inputStream = contentResolver.openInputStream(it)
                            BitmapFactory.decodeStream(inputStream)
                        }
                    }
                    else -> null
                }

                bitmap?.let {
                    imageView.setImageBitmap(it)

                    // Cek apakah gambar merupakan sapi
                    val isCattle = classifier.isCattle(it)

                    if (isCattle) {
                        val predictedWeight = classifier.predictWeight(it)
                        val adjustedWeight = predictedWeight + 11
                        val weightCategory = categorizeWeight(adjustedWeight)

                        resultTextView.text = "Bobot: $adjustedWeight kg"
                        weightCategoryTextView.text = "Kategori: $weightCategory"
                    } else {
                        resultTextView.text = "Gambar bukan sapi, tidak bisa diprediksi"
                        weightCategoryTextView.text = ""
                    }
                } ?: run {
                    resultTextView.text = "Error: Unable to process image"
                    weightCategoryTextView.text = ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                resultTextView.text = "Error occurred: ${e.message}"
                weightCategoryTextView.text = ""
                Log.e("CattleWeightActivity", "Error: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Fungsi untuk mengkategorikan bobot sapi.
     * - Kecil: < 300 kg
     * - Sedang: 300 - 599 kg
     * - Besar: > 600 kg
     */
    private fun categorizeWeight(weight: Float): String {
        return when {
            weight < 300 -> "Kecil"
            weight in 300f..599f -> "Sedang"
            weight > 600 -> "Besar"
            else -> "Tidak diketahui"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        classifier.close()
    }
}
