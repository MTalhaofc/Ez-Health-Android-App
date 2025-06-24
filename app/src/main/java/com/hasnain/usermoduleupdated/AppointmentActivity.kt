package com.hasnain.usermoduleupdated

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hasnain.usermoduleupdated.databinding.ActivityAppointmentBinding
import com.hasnain.usermoduleupdated.models.Appointment
import com.hasnain.usermoduleupdated.models.Profile
import com.hasnain.usermoduleupdated.models.TimeSlot
import com.hasnain.usermoduleupdated.utils.FirebaseHelper
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.logging.Handler

class AppointmentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppointmentBinding
    private lateinit var database: DatabaseReference
    private lateinit var usersDatabase: DatabaseReference  // Reference for fetching user CNIC

    private var currentAppointmentId: String? = null
    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private lateinit var spinner: Spinner
    private lateinit var selectedTimeId: String
    private lateinit var selectedTimeBook: String
    private val databaseRef = FirebaseDatabase.getInstance().getReference("timeslots")
    private val timeSlotList = mutableListOf<TimeSlot>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        spinner= binding.spinner
        loadTimeSlotsFromFirebase()
        resetTimeSlotsIfAfterMidnight()

        database = FirebaseDatabase.getInstance().getReference("appointments")
        usersDatabase = FirebaseDatabase.getInstance().getReference("users") // Reference to users collection

        binding.backArrow.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
        }


        // Book appointment button listener
        binding.buttonBookAppointment.setOnClickListener {
            val medicalIssues = binding.editTextMedicalIssues.text.toString()
            val address = binding.editTextAddress.text.toString()
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedSlot = timeSlotList[position]
                    selectedTime = selectedSlot.time
                    selectedTimeId = selectedSlot.time_id
                    selectedTimeBook= selectedSlot.time_booked
                    Toast.makeText(this@AppointmentActivity, "Selected: $selectedTime", Toast.LENGTH_SHORT).show()

                    if (selectedSlot.time_booked == "NB") {

                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
            val selectedSpinnerValue = spinner.selectedItem?.toString()
            Toast.makeText(this,"time selected + $selectedSpinnerValue",Toast.LENGTH_SHORT).show()

            if (selectedSpinnerValue != null && medicalIssues.isNotEmpty() && address.isNotEmpty()) {
                selectedTime = selectedSpinnerValue
                fetchUserCnicAndBookAppointment(medicalIssues, address, selectedSpinnerValue)

                // Clear all fields after booking
                binding.editTextMedicalIssues.text.clear()
                binding.editTextAddress.text.clear()
//                binding.etSelectDate.text.clear()
//                spinner.setSelection(0)
                // Resets to "Select Time"
//                binding.textViewSelectedDate.text = ""
                binding.textViewSelectedTime.text = ""

                // Reset internal variables
                selectedDate = null
//                selectedTime = null

                // Show confirmation dialog and dismiss it after 5 seconds
                showAppointmentAddedDialog()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }


        // Refresh status button listener
        binding.buttonRefreshStatus.setOnClickListener {
            currentAppointmentId?.let {
                refreshAppointmentStatus(it)
            } ?: Toast.makeText(this, "No appointment to refresh", Toast.LENGTH_SHORT).show()
        }
    }

    // Fetch the logged-in user's CNIC by matching their email in the users collection
    private fun fetchUserCnicAndBookAppointment(medicalIssues: String, address: String, time : String) {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        if (userEmail != null) {
            // Fetch users where the user email matches the current user's email
            usersDatabase.orderByChild("user_email").equalTo(userEmail)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Loop through all matching users and get their CNIC
                            for (userSnapshot in dataSnapshot.children) {
                                val userCnic = userSnapshot.child("user_cnic").getValue(String::class.java)
                                if (userCnic != null) {
                                    // User CNIC found, proceed with booking the appointment
                                    bookAppointment(medicalIssues, address, userCnic, time)
                                    return
                                }
                            }
                            Toast.makeText(this@AppointmentActivity, "CNIC not found for the user", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@AppointmentActivity, "No matching user found for this email", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@AppointmentActivity, "Error fetching user CNIC: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // Book the appointment with all required fields
    private fun bookAppointment(medicalIssues: String, address: String, userCnic: String, time: String) {
        val appointmentId = database.push().key ?: return
        val key = generateRandomNumericId()
        val today = getCurrentDate().toString()
        println("Today is $today")

        fetchUserPhoneNumber { userName,phoneNum ->
            if(phoneNum != null && userName != null){
                val appointment = Appointment(
                    user_test_appointment = medicalIssues,
                    user_cnic_appointment = userCnic,
                    user_time_appointment = time ?: "",
                    user_name_appointment = userName,
                    user_key_appointment = key,
                    user_date_appointment = today,
                    user_status_appointment = "P",  // Default to Pending
                    user_address_appointment = address,
                    user_number_appointment = phoneNum!!.toString()
                )

                // Save appointment data to Firebase
                database.child(appointmentId).setValue(appointment)
                    .addOnSuccessListener {
                        binding.textViewAppointmentStatus.text = "Appointment Status: Pending"
                        binding.textViewAppointmentStatus.visibility = View.VISIBLE
                        binding.buttonRefreshStatus.visibility = View.VISIBLE
                        currentAppointmentId = appointmentId
                        listenForAppointmentStatus(appointmentId)
                        if (selectedTimeBook == "NB") {
                            selectedTime?.let { updateTimeBookedToBByTimeId(it) }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to book appointment. Please try again.", Toast.LENGTH_SHORT).show()
                    }
            }else{
                Toast.makeText(this,"Complete Profile", Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun generateRandomNumericId(): String {
        return (1..8)
            .map { (0..9).random() }  // This generates a random number between 0 and 9
            .joinToString("")  // Join all the random numbers into a single string
    }
    private fun getCurrentDate(): LocalDate {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }


    // Listen for appointment status changes
    private fun listenForAppointmentStatus(appointmentId: String) {
        val appointmentRef = database.child(appointmentId)
        appointmentRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val appointment = snapshot.getValue(Appointment::class.java)
                appointment?.let {
                    updateUIBasedOnStatus(it.user_status_appointment)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AppointmentActivity, "Failed to load appointment status.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Refresh appointment status manually
    private fun refreshAppointmentStatus(appointmentId: String) {
        val appointmentRef = database.child(appointmentId)
        appointmentRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val appointment = snapshot.getValue(Appointment::class.java)
                appointment?.let {
                    updateUIBasedOnStatus(it.user_status_appointment)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AppointmentActivity, "Failed to refresh status.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Update UI based on appointment status
    @SuppressLint("SetTextI18n")
    private fun updateUIBasedOnStatus(status: String) {
        when (status) {
            "P" -> binding.textViewAppointmentStatus.text = "Appointment Status: Pending"
            "R" -> {
                binding.textViewAppointmentStatus.text = "Appointment Status: Rejected"
                Toast.makeText(this, "Appointment was rejected. Please try booking again.", Toast.LENGTH_SHORT).show()
                binding.buttonRefreshStatus.visibility = View.GONE
            }
            "T" -> binding.textViewAppointmentStatus.text = "Appointment Status: Booked"
            else -> binding.textViewAppointmentStatus.text = "Unknown Status"
        }
    }


    private fun fetchUserPhoneNumber(callback: (String?,String?) -> Unit) {
        val currentUserEmail = FirebaseHelper.getAuth().currentUser?.email
        if (currentUserEmail != null) {
            FirebaseHelper.profileRef.orderByChild("user_email").equalTo(currentUserEmail)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var userName: String? = null
                        var phoneNumber: String? = null
                        if (snapshot.exists()) {
                            for (child in snapshot.children) {
                                val profile = child.getValue(Profile::class.java)
                                if(profile != null) {
                                    userName = profile.user_name

                                    if (profile.user_phone.isNotEmpty()) {
                                        phoneNumber = profile.user_phone
                                        break
                                    }
                                }
                            }
                        }
                        callback(userName,phoneNumber)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        callback(null,null) // Handle error case if needed
                    }
                })
        } else {
            callback(null,null)
        }
    }



    @SuppressLint("DefaultLocale", "SetTextI18n")

    private fun showAppointmentAddedDialog() {
        // Inflate the custom layout with Lottie animation and message
        val dialogView = layoutInflater.inflate(R.layout.dialog_appointment_added, null)

        // Create the dialog with the custom layout
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Prevent the user from dismissing the dialog manually
            .create()

        // Set up the Lottie Animation
        val lottieAnimation = dialogView.findViewById<LottieAnimationView>(R.id.lottie_animation)
        lottieAnimation.setAnimation("Animation_1732726453432.json") // Set your Lottie animation
        lottieAnimation.playAnimation()

        // Set up the message
        val textMessage = dialogView.findViewById<TextView>(R.id.text_message)
        textMessage.text = "Appointment Added"  // You can change the message if needed

        // Show the dialog
        builder.show()


        android.os.Handler(Looper.getMainLooper()).postDelayed({
            builder.dismiss()
            startActivity(Intent(this,MainActivity::class.java))
        }, 1500)
    // 2000ms = 2 seconds

    }
    private fun loadTimeSlotsFromFirebase() {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                timeSlotList.clear()
                for (slotSnapshot in snapshot.children) {
                    val slot = slotSnapshot.getValue(TimeSlot::class.java)
                    if (slot != null) {
                        if (slot.time_booked == "NB") {
                            timeSlotList.add(slot)
                        }
                    }
                }
                // Sort by time
                timeSlotList.sortBy { it.time_id }

                val adapter = ArrayAdapter(this@AppointmentActivity, android.R.layout.simple_spinner_item, timeSlotList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedSlot = timeSlotList[position]
                        selectedTime = selectedSlot.time
                        selectedTimeId = selectedSlot.time_id
                        selectedTimeBook= selectedSlot.time_booked
                        Toast.makeText(this@AppointmentActivity, "Selected: $selectedTime", Toast.LENGTH_SHORT).show()

                        if (selectedSlot.time_booked == "NB") {

                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AppointmentActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTimeBookedToBByTimeId(timeId: String) {
        databaseRef.orderByChild("time").equalTo(timeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (slotSnapshot in snapshot.children) {
                        slotSnapshot.ref.child("time_booked").setValue("B")
                            .addOnSuccessListener {
                                Toast.makeText(this@AppointmentActivity, "Slot marked as booked", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@AppointmentActivity, "Update failed", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AppointmentActivity, "Failed to update", Toast.LENGTH_SHORT).show()
                }
            })
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun resetTimeSlotsIfAfterMidnight() {
        // Step 1: Get current time in Asia/Karachi
        val zoneId = ZoneId.of("Asia/Karachi")
        val currentTime = ZonedDateTime.now(zoneId)

        // Step 2: Define the reset time (12:01 AM)
        val resetTime = currentTime.toLocalDate().atTime(0, 1).atZone(zoneId)

        // Step 3: Check if current time is after reset time
        if (currentTime.isAfter(resetTime)) {
            val databaseRef = FirebaseDatabase.getInstance().getReference("timeslots")

            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (slotSnapshot in snapshot.children) {
                        val timeId = slotSnapshot.getValue(TimeSlot::class.java)
                        if (timeId != null) {
                            if (timeId.time.isNullOrEmpty()) {
                                val slotRef = databaseRef.child(timeId.time_id)
                                slotRef.child("time_booked").setValue("NB")
                            }
                        }
                    }
                    Log.d("ResetSlots", "All time slots reset to NB.")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ResetSlots", "Failed to reset time slots: ${error.message}")
                }
            })
        }
    }
    private fun getMessage(input: String): String {
        return input
    }





}
