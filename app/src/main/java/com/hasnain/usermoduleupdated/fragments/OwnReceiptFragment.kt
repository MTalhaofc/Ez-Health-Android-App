package com.hasnain.usermoduleupdated.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hasnain.usermoduleupdated.adapters.ReceiptAdapter
import com.hasnain.usermoduleupdated.databinding.FragmentOwnReceiptBinding
import com.hasnain.usermoduleupdated.models.Report
import com.hasnain.usermoduleupdated.utils.FirebaseHelper

class OwnReceiptFragment : Fragment() {

    private var _binding: FragmentOwnReceiptBinding? = null
    private val binding get() = _binding!!
    private lateinit var receiptAdapter: ReceiptAdapter
    private val reportList = mutableListOf<Report>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOwnReceiptBinding.inflate(inflater, container, false)

        // Set up RecyclerView
        setupRecyclerView()

        // Fetch user reports after getting their email and CNIC
        fetchUserEmail { userEmail ->
            fetchUserCnic(userEmail) { userCnic ->
                fetchReports(userCnic)
            }
        }

        return binding.root
    }

    // Set up RecyclerView with the adapter
    private fun setupRecyclerView() {
        receiptAdapter = ReceiptAdapter(reportList, isParentalReport = false) // User report, set false
        binding.recyclerViewReceipts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = receiptAdapter
        }
    }

    // Fetch the current user's email from Firebase Auth
    private fun fetchUserEmail(callback: (String) -> Unit) {
        val user = FirebaseHelper.getAuth().currentUser
        user?.let {
            val userEmail = it.email
            if (!userEmail.isNullOrEmpty()) {
                callback(userEmail)
            } else {
                Toast.makeText(requireContext(), "User email not found", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // Fetch the CNIC of the user whose email matches the authenticated email
    private fun fetchUserCnic(userEmail: String, callback: (String) -> Unit) {
        FirebaseHelper.usersRef.orderByChild("user_email").equalTo(userEmail).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val userCnic = userSnapshot.child("user_cnic").value as String?
                        userCnic?.let {
                            callback(it) // Pass the CNIC back to the callback
                            return@addOnSuccessListener
                        }
                    }
                    Toast.makeText(requireContext(), "CNIC not found for the user", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "No matching user found for this email", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching user CNIC: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fetch the user reports based on their CNIC and sum the total price
    private fun fetchReports(userCnic: String) {
        FirebaseHelper.reportsRef.orderByChild("user_report_cnic").equalTo(userCnic)
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    reportList.clear()
                    var totalPrice = 0.0
                    for (reportSnapshot in snapshot.children) {
                        val report = reportSnapshot.getValue(Report::class.java)
                        report?.let {
                            reportList.add(it)
                            val price = it.user_report_price?.toString()?.toDoubleOrNull() ?: 0.0
                            totalPrice += price                        }
                    }
                    receiptAdapter.notifyDataSetChanged()

                    // Update total price TextView
                    binding.textViewTotalPrice.text = "Total Price: Rs$totalPrice"
                } else {
                    Toast.makeText(requireContext(), "No reports found for this user", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching reports: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

