package com.smishguard

import ai.onnxruntime.*
import android.content.Context

class PhishingDetector(context: Context) {
    private val ortEnvironment = OrtEnvironment.getEnvironment()
    private val ortSession: OrtSession

    init {
        // Load the ONNX file from the raw folder
        // R.raw.smishguard points directly to your uploaded file
        val modelBytes = context.resources.openRawResource(R.raw.smishguard).readBytes()
        ortSession = ortEnvironment.createSession(modelBytes)
    }

    fun isPhishing(smsText: String): Boolean {
        // Force lowercase to match the Colab dictionary
        val cleanText = smsText.lowercase()
        val textArray = arrayOf(arrayOf(cleanText))

        // Convert the text into a format ONNX understands
        val inputTensor = OnnxTensor.createTensor(ortEnvironment, textArray)
        val inputs = mapOf("text_input" to inputTensor)

        // Run the prediction
        val result = ortSession.run(inputs)
        val output = result[0].value as LongArray

        // Return true if it's a scam (1), false if safe (0)
        return output[0] == 1L
    }
}