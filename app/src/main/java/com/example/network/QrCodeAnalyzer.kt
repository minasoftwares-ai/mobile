package com.example.network

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val map = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
            DecodeHintType.TRY_HARDER to true,
            DecodeHintType.CHARACTER_SET to "UTF-8"
        )
        setHints(map)
    }

    private var isScanning = true

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        if (!isScanning) {
            image.close()
            return
        }

        val mediaImage = image.image
        if (mediaImage != null && image.planes.isNotEmpty()) {
            val buffer: ByteBuffer = image.planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            val width = image.width
            val height = image.height

            try {
                val source = PlanarYUVLuminanceSource(
                    data,
                    width,
                    height,
                    0,
                    0,
                    width,
                    height,
                    false
                )
                val bitmap = BinaryBitmap(HybridBinarizer(source))
                val result = reader.decodeWithState(bitmap)
                if (result != null && result.text.isNotBlank()) {
                    isScanning = false
                    onQrCodeScanned(result.text)
                }
            } catch (e: Exception) {
                // QR code not found in current frame, continue scanning
            } finally {
                reader.reset()
            }
        }
        image.close()
    }

    fun resume() {
        isScanning = true
    }
}
