package com.hasnain.usermoduleupdated.utils


class Utils {

    fun extractTestValues(text: String): Map<String, Float> {
        val values = mutableMapOf<String, Float>()

        val hemo = Regex("Hemoglobin.*?(\\d+(\\.\\d+)?)").find(text)?.groupValues?.get(1)
        val sugar = Regex("Blood Sugar.*?(\\d+(\\.\\d+)?)").find(text)?.groupValues?.get(1)
        val chol = Regex("Cholesterol.*?(\\d+(\\.\\d+)?)").find(text)?.groupValues?.get(1)
        val alt = Regex("ALT.*?(\\d+(\\.\\d+)?)").find(text)?.groupValues?.get(1)
        val ast = Regex("AST.*?(\\d+(\\.\\d+)?)").find(text)?.groupValues?.get(1)

        // Map the labels to the extracted values
        values["Hemoglobin"] = hemo?.toFloat() ?: -1f
        values["Blood_Sugar"] = sugar?.toFloat() ?: -1f
        values["Cholesterol"] = chol?.toFloat() ?: -1f
        values["ALT"] = alt?.toFloat() ?: -1f
        values["AST"] = ast?.toFloat() ?: -1f

        return values
    }
}


