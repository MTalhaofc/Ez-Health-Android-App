package com.hasnain.usermoduleupdated.helper


import android.content.res.AssetManager
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TfLiteModelHelper(assetManager: AssetManager) {

    private var interpreter: Interpreter
    private var labels: List<String>

    init {
        // Load model
        interpreter = Interpreter(loadModel(assetManager, "medical_recommendation_model.tflite"))
        // Load labels from the 'labels.txt' file in the assets
        labels = assetManager.open("labels.txt").bufferedReader().readLines()
    }

    private fun loadModel(assetManager: AssetManager, modelName: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    // Run inference and get the prediction result
    fun runModel(input: FloatArray): String {
        val inputBuffer = ByteBuffer.allocateDirect(4 * input.size).order(ByteOrder.nativeOrder())
        input.forEach { inputBuffer.putFloat(it) }

        val outputBuffer = ByteBuffer.allocateDirect(4 * labels.size).order(ByteOrder.nativeOrder())
        interpreter.run(inputBuffer, outputBuffer)

        // Rewind outputBuffer to read its content
        outputBuffer.rewind()

        // Get the maximum value from the output and its index
        val output = FloatArray(labels.size)
        outputBuffer.asFloatBuffer().get(output)

        val maxIndex = output.indices.maxByOrNull { output[it] } ?: -1
        return if (maxIndex != -1) labels.getOrElse(maxIndex) { "Unknown result" } else "Unknown result"
    }
}
