package com.example.kotlinplayground

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.*
import java.util.concurrent.Executors
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DEBUG"
        private const val THUMBNAIL =
            "https://raw.githubusercontent.com/mrkazansky/sample-image/master/img_thumbnail.png"
        private const val RAW =
            "https://raw.githubusercontent.com/mrkazansky/sample-image/master/img_raw.png"

        private val JSON_RAW = "{'image':'${RAW}'}"
        private val JSON_THUMB_RAW = "{'thumb':'${THUMBNAIL}, 'image':'${RAW}'}"
    }

    private var JSON_GEN_RAW = ""
    private var dataImageHeader: ByteArray? = null
    private var dataImageContent: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initImage()
        setupUI()
    }

    private fun initImage() {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.img_raw)
        val config = Bitmap.Config.RGB_565
        val convertedBitmap = bitmap.copy(config, false)
        val thumb = scaleBitmap(convertedBitmap, 8)
        val width = thumb.width
        val height = thumb.height
        val pixels = IntArray(width * height)
        thumb.getPixels(pixels, 0, width, 0, 0, width, height)
        val file = File(cacheDir, "temp_image.png")
        val outputStream = FileOutputStream(file)
        thumb.compress(Bitmap.CompressFormat.PNG, 10, outputStream)
        outputStream.close()


        val inputStream = FileInputStream(file)
        val data = splitPngHeaderAndImageData(inputStream) ?: return

        dataImageHeader = data.first
        val imageData = data.second
        dataImageContent = Base64.encodeToString(imageData, Base64.DEFAULT)

        Log.d(
            TAG,
            "initImage: ${file.length()} header ${dataImageHeader?.size} | content ${dataImageContent.length}"
        )

        JSON_GEN_RAW = "{'thumb':'${dataImageContent}, 'image':'${RAW}'}"
    }

    fun combinePngHeaderAndImageData(pngHeaderData: ByteArray, imageData: ByteArray): Bitmap? {
        // Create a ByteArrayOutputStream to combine the header data and image data
        val outputStream = ByteArrayOutputStream()

        // Write the PNG header data to the output stream
        outputStream.write(pngHeaderData)

        // Write the image data to the output stream
        outputStream.write(imageData)

        // Convert the output stream to a byte array
        val pngData = outputStream.toByteArray()

        // Decode the byte array into a Bitmap
        val bitmap = BitmapFactory.decodeByteArray(pngData, 0, pngData.size)

        return bitmap
    }

    private fun scaleBitmap(originalBitmap: Bitmap, targetSize: Int): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val newWidth = targetSize
        val newHeight = targetSize
        val scaleX = newWidth.toFloat() / width.toFloat()
        val scaleY = newHeight.toFloat() / height.toFloat()
        val matrix = Matrix()
        matrix.postScale(scaleX, scaleY)

        return Bitmap.createBitmap(originalBitmap, 0, 0, width, height, matrix, true)
    }


    private fun setupUI() {
        btnStart.setOnClickListener {
            it.isEnabled = false
            startLoadRawImage()
            startLoadThumbnailAndRawImage()
            startGenerateThumbnailAndLoadRawImage()
        }
    }

    private suspend fun delayLoadingAPI(content: String) {
        delay(content.length * Random.nextDouble(3.0, 4.0).toLong())
    }

    private fun startGenerateThumbnailAndLoadRawImage() {
        val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        lifecycleScope.launch(dispatcher) {
            Log.d(TAG, "startGenerateThumbnailAndLoadRawImage: ${Thread.currentThread().name}")
            val time = System.currentTimeMillis()
            delayLoadingAPI(JSON_GEN_RAW)
            withContext(Dispatchers.Main) {
                txtDes3.text = "${txtDes3.text} ${JSON_GEN_RAW.length}"
                txt3.text = "${System.currentTimeMillis() - time}"
            }
            val decodeBitmap =
                combinePngHeaderAndImageData(
                    dataImageHeader!!,
                    Base64.decode(dataImageContent, Base64.DEFAULT)
                )
            withContext(Dispatchers.Main) {
                image3.setImageBitmap(decodeBitmap)
                txt3.text = "${txt3.text} | ${System.currentTimeMillis() - time}"
            }
            val bitmap = Glide.with(this@MainActivity).asBitmap().load(RAW)
                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).timeout(60000)
                .submit().get()
            withContext(Dispatchers.Main) {
                image3.setImageBitmap(bitmap)
                txt3.text = "${txt3.text} | ${System.currentTimeMillis() - time}"
            }
        }
    }

    private fun startLoadThumbnailAndRawImage() {
        val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        lifecycleScope.launch(dispatcher) {
            Log.d(TAG, "startLoadThumbnailAndRawImage: ${Thread.currentThread().name}")
            val time = System.currentTimeMillis()
            delayLoadingAPI(JSON_THUMB_RAW)
            withContext(Dispatchers.Main) {
                txtDes2.text = "${txtDes2.text} ${JSON_THUMB_RAW.length}"
                txt2.text = "${System.currentTimeMillis() - time}"
            }
            val thumbnail = Glide.with(this@MainActivity).asBitmap().load(THUMBNAIL)
                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).timeout(60000)
                .submit().get()
            withContext(Dispatchers.Main) {
                image2.setImageBitmap(thumbnail)
                txt2.text = "${txt2.text} | ${System.currentTimeMillis() - time}"
            }
            val bitmap = Glide.with(this@MainActivity).asBitmap().load(RAW)
                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).timeout(60000)
                .submit().get()
            withContext(Dispatchers.Main) {
                image2.setImageBitmap(bitmap)
                txt2.text = "${txt2.text} | ${System.currentTimeMillis() - time}"
            }
        }
    }

    private fun startLoadRawImage() {
        val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        lifecycleScope.launch(dispatcher) {
            Log.d(TAG, "startLoadRawImage: ${Thread.currentThread().name}")
            val time = System.currentTimeMillis()
            delayLoadingAPI(JSON_RAW)
            withContext(Dispatchers.Main) {
                txtDes1.text = "${txtDes1.text} ${JSON_RAW.length}"
                txt1.text = "${System.currentTimeMillis() - time}"
            }
            val bitmap = Glide.with(this@MainActivity).asBitmap().load(RAW)
                .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).timeout(60000)
                .submit().get()
            withContext(Dispatchers.Main) {
                image1.setImageBitmap(bitmap)
                txt1.text = "${txt1.text} | ${System.currentTimeMillis() - time}"
            }
        }
    }

    fun splitPngHeaderAndImageData(inputStream: InputStream): Pair<ByteArray, ByteArray>? {
        // Read the PNG signature and the IHDR chunk header
        val header = ByteArray(8)
        if (inputStream.read(header) != 8 || !isPngHeader(header)) {
            // Invalid PNG header
            return null
        }

        // Read the IHDR chunk data
        val ihdrLength = readChunkLength(inputStream)
        val ihdrHeader = ByteArray(4)
        inputStream.read(ihdrHeader)
        if (!isIhdrHeader(ihdrHeader)) {
            // Invalid IHDR header
            return null
        }
        val ihdrData = ByteArray(ihdrLength - 4)
        inputStream.read(ihdrData)

        // Read the image data
        val imageData = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        while (true) {
            val bytesRead = inputStream.read(buffer, 0, bufferSize)
            if (bytesRead == -1) {
                break
            }
            imageData.write(buffer, 0, bytesRead)
        }

        return Pair(
            header + intToByteArray(ihdrLength) + ihdrHeader + ihdrData,
            imageData.toByteArray()
        )
    }

    fun isPngHeader(header: ByteArray): Boolean {
        return header.contentEquals(
            byteArrayOf(
                0x89.toByte(),
                0x50.toByte(),
                0x4E.toByte(),
                0x47.toByte(),
                0x0D.toByte(),
                0x0A.toByte(),
                0x1A.toByte(),
                0x0A.toByte()
            )
        )
    }

    fun isIhdrHeader(header: ByteArray): Boolean {
        return header.contentEquals(
            byteArrayOf(
                0x49.toByte(),
                0x48.toByte(),
                0x44.toByte(),
                0x52.toByte()
            )
        )
    }

    fun readChunkLength(inputStream: InputStream): Int {
        val lengthBytes = ByteArray(4)
        inputStream.read(lengthBytes)
        return byteArrayToInt(lengthBytes)
    }

    fun byteArrayToInt(byteArray: ByteArray): Int {
        return ((byteArray[0].toInt() and 0xFF) shl 24) or ((byteArray[1].toInt() and 0xFF) shl 16) or ((byteArray[2].toInt() and 0xFF) shl 8) or (byteArray[3].toInt() and 0xFF)
    }

    fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value shr 24).toByte(),
            (value shr 16).toByte(),
            (value shr 8).toByte(),
            value.toByte()
        )
    }

}
