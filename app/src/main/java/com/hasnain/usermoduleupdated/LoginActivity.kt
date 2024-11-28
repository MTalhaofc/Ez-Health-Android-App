package com.hasnain.usermoduleupdated

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.hasnain.usermoduleupdated.databinding.ActivityLoginBinding
import com.hasnain.usermoduleupdated.models.User
import com.hasnain.usermoduleupdated.utils.FirebaseHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Check if the user is already logged in (session exists)
        if (isUserLoggedIn()) {
            val userEmail = getUserEmailFromSession()
            if (userEmail != null) {
                fetchUserData(userEmail)
            }
        }

        binding.signin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val dialog = showLoadingDialog("Logging In...")
                loginUser(email, password) {
                    dialog.dismiss() // Close the dialog once the login process finishes
                }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }



//        binding.buttonResendVerification.setOnClickListener {
//            resendVerificationEmail()
//        }

        binding.signup.setOnClickListener {
            startActivity(Intent(this, EmailSignupActivity::class.java))
        }
        binding.forgetPassword.setOnClickListener {
            startActivity(Intent(this, ForgetPasswordActivity::class.java))
        }
    }

    // Function to log in the user
    private fun loginUser(email: String, password: String, onComplete: () -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                onComplete() // Ensure dialog is dismissed

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        saveSession(email) // Save session when login is successful
                        fetchUserData(email)
                    } else {
                        Toast.makeText(this, "Please verify your email address.", Toast.LENGTH_SHORT).show()
                        resendVerificationEmail()
                    }
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                onComplete() // Ensure dialog is dismissed even in case of failure
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to fetch user data from the database
    private fun fetchUserData(email: String) {
        FirebaseHelper.usersRef.orderByChild("user_email").equalTo(email).get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val userData = dataSnapshot.children.first().getValue(User::class.java)
                    userData?.let {
                        navigateBasedOnUserData(it)
                    }
                } else {
                    // User not found in database
                    Toast.makeText(this, "User not found in database.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, CNICSubmissionActivity::class.java))
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Failed to retrieve user data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateBasedOnUserData(userData: User) {
        when {

            userData.user_cnic.isEmpty() -> {
                // Navigate to CNICActivity
                startActivity(Intent(this, CNICSubmissionActivity::class.java))
            }
            userData.user_account_status == "P" -> {
                // Navigate to UnderVerificationActivity
                startActivity(Intent(this, UnderVerificationActivity::class.java))
            }
            userData.user_account_status == "F" -> {
                // Display rejection message
                Toast.makeText(this, "Account Verification Rejected", Toast.LENGTH_LONG).show()
            }
            userData.user_account_status == "T" -> {
                // Navigate to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }

    // Resend email verification
    private fun resendVerificationEmail() {
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
            if (verifyTask.isSuccessful) {
                Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Session Management Functions

    private fun saveSession(email: String) {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_email", email)
            apply() // Save the email to session
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPref.contains("user_email") // Check if session exists
    }

    private fun getUserEmailFromSession(): String? {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPref.getString("user_email", null) // Get email from session
    }

    private fun clearSession() {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear() // Clear session data
            apply()
        }
    }
    private fun showLoadingDialog(message: String): AlertDialog {
        val dialogView = layoutInflater.inflate(R.layout.dialog_loading, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Prevent the user from dismissing the dialog manually
            .create()

        val lottieAnimation = dialogView.findViewById<LottieAnimationView>(R.id.lottie_animation)
        val textViewMessage = dialogView.findViewById<TextView>(R.id.text_message)

        // Load the animation from the assets folder
        lottieAnimation.setAnimation("Animation_1732726453432.json")
        lottieAnimation.playAnimation()
        textViewMessage.text = message

        dialog.show()
        return dialog
    }


}
