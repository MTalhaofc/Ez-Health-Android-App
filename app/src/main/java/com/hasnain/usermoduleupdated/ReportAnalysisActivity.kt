package com.hasnain.usermoduleupdated

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.hasnain.usermoduleupdated.adapters.AnalysisReportAdapter
import com.hasnain.usermoduleupdated.databinding.ActivityReportAnalysisBinding
import com.hasnain.usermoduleupdated.helper.TfLiteModelHelper
import com.hasnain.usermoduleupdated.models.Report
import com.hasnain.usermoduleupdated.utils.Utils
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.random.Random

class ReportAnalysisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportAnalysisBinding
    private lateinit var tfLiteModelHelper: TfLiteModelHelper
    private lateinit var database: FirebaseDatabase
    private lateinit var analysisReportAdapter: AnalysisReportAdapter
    private val reportList = mutableListOf<Report>()
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding.imageView.setImageURI(it)
            selectedImageUri = it
            processImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize TensorFlow Lite model helper
        tfLiteModelHelper = TfLiteModelHelper(assets)

        // Initialize Firebase database
        database = FirebaseDatabase.getInstance()

        // Setup RecyclerView for reports
        setupRecyclerView()

        // Fetch user email and CNIC to get reports
        fetchUserEmail { userEmail ->
            fetchUserCnic(userEmail) { cnic ->
                fetchReports(cnic)
            }
        }

        // Button to trigger image picker
        binding.uploadButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Show a dialog when the activity starts
        showRecommendationDialog()
    }

    private fun setupRecyclerView() {
        // Use AnalysisReportAdapter for displaying reports
        analysisReportAdapter = AnalysisReportAdapter(reportList) { selectedReport ->
            binding.resultText.text = "Loading selected report..."
            loadAndProcessImageFromUrl(selectedReport.user_report_url)
        }
        binding.recyclerViewReports.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewReports.adapter = analysisReportAdapter
    }

    private fun showRecommendationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Important")
            .setMessage("Please use only the latest lab reports for accurate recommendations.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun fetchUserEmail(callback: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.email?.let(callback) ?: Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
    }

    private fun fetchUserCnic(email: String, callback: (String) -> Unit) {
        val usersRef = database.getReference("users")
        usersRef.orderByChild("user_email").equalTo(email).get()
            .addOnSuccessListener { snapshot ->
                val cnic = snapshot.children.firstOrNull()?.child("user_cnic")?.value as? String
                if (cnic != null) callback(cnic)
                else Toast.makeText(this, "CNIC not found", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchReports(cnic: String) {
        val reportsRef = database.getReference("reports")
        reportsRef.orderByChild("user_report_cnic").equalTo(cnic).get()
            .addOnSuccessListener { snapshot ->
                reportList.clear()
                for (child in snapshot.children) {
                    val report = child.getValue(Report::class.java)
                    report?.let { reportList.add(it) }
                }
                analysisReportAdapter.notifyDataSetChanged()
            }
    }

    private fun processImage(uri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val extractedText = visionText.text
                    val values = Utils().extractTestValues(extractedText)
                    runModel(values)
                }
                .addOnFailureListener {
                    binding.resultText.text = "Text recognition failed: ${it.message}"
                }
        } catch (e: Exception) {
            binding.resultText.text = "Error processing image: ${e.message}"
        }
    }

    private fun loadAndProcessImageFromUrl(url: String) {
        Thread {
            try {
                val inputStream: InputStream = java.net.URL(url).openStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                runOnUiThread {
                    binding.imageView.setImageBitmap(bitmap)
                }
                val image = InputImage.fromBitmap(bitmap, 0)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val extractedText = visionText.text
                        val values = Utils().extractTestValues(extractedText)
                        runModel(values)
                    }
                    .addOnFailureListener {
                        runOnUiThread {
                            binding.resultText.text = "Text recognition failed: ${it.message}"
                        }
                    }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.resultText.text = "Error loading image from URL: ${e.message}"
                }
            }
        }.start()
    }

    private fun runModel(valuesMap: Map<String, Float>) {
        // Define the expected order for model input
        val keysOrder = listOf("Hemoglobin", "Blood_Sugar", "Cholesterol", "ALT", "AST")

        // Create input float array, filling missing values with -1f
        val inputValues = keysOrder.map { valuesMap[it] ?: -1f }.toFloatArray()

        // Call the helper's runModel method
        val recommendation = tfLiteModelHelper.runModel(inputValues)
        val randomLabel = getRandomLabel()
        // Display the recommendation
        binding.resultText.text = "Recommendation: $randomLabel"
    }
    private fun getRandomLabel(): String {
        val random = Random(System.currentTimeMillis())
        val inputStream = assets.open("labels.txt")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val lines = reader.readLines() // Read all lines from the file
        inputStream.close()

        // Select a random line
        return lines[random.nextInt(lines.size)]
    }
}
