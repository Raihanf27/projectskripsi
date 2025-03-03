package project.capstone.percobaan_capstone.clasifier

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.File

class CattleWeightClassifier(context: Context) {
    private var weightModel: Interpreter
    private var classificationModel: Interpreter

    init {
        try {
            val weightModelFile = FileUtil.loadMappedFile(context, "modelskripsi.tflite")
            val classificationModelFile = FileUtil.loadMappedFile(context, "klasifikasisapi2.tflite")

            val options = Interpreter.Options().apply { setNumThreads(1) }

            weightModel = Interpreter(weightModelFile, options)
            classificationModel = Interpreter(classificationModelFile, options)
        } catch (e: Exception) {
            throw RuntimeException("Error loading models: ${e.message}")
        }
    }

    /**
     * Fungsi untuk memprediksi apakah gambar merupakan sapi atau bukan.
     * Jika output > 0.5, maka gambar dianggap sebagai sapi.
     */
    fun isCattle(bitmap: Bitmap): Boolean {
        val input = preprocessImage(bitmap)
        val output = Array(1) { FloatArray(1) }
        classificationModel.run(input, output)
        Log.d("Classification Output", output.contentDeepToString())
        return output[0][0] > 0.5
    }

    /**
     * Fungsi untuk memprediksi bobot sapi.
     */
    fun predictWeight(bitmap: Bitmap): Float {
        val input = preprocessImage(bitmap)
        val output = Array(1) { FloatArray(1) }
        weightModel.run(input, output)
        Log.d("Weight Output", output.contentDeepToString())
        return output[0][0]
    }

    /**
     * Fungsi untuk melakukan preprocessing gambar.
     */
    private fun preprocessImage(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val input = Array(1) { Array(224) { Array(224) { FloatArray(3) } } }

        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val pixel = resizedBitmap.getPixel(x, y)

                input[0][y][x][0] = ((pixel shr 16 and 0xFF) / 255.0f)
                input[0][y][x][1] = ((pixel shr 8 and 0xFF) / 255.0f)
                input[0][y][x][2] = ((pixel and 0xFF) / 255.0f)
            }
        }
        return input
    }

    fun close() {
        weightModel.close()
        classificationModel.close()
    }
}
