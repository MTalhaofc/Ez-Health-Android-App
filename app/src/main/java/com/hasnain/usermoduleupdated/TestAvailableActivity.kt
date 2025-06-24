package com.hasnain.usermoduleupdated

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hasnain.usermoduleupdated.fragments.TestsAvailableFragment


import com.hasnain.usermoduleupdated.databinding.ActivityTestAvailableBinding


class TestAvailableActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestAvailableBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using view binding
        binding = ActivityTestAvailableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if the fragment is already added, to avoid multiple fragments being added
        if (savedInstanceState == null) {
            // Add the TestAvailableFragment to the activity dynamically
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TestsAvailableFragment())  // Replace with the container ID
                .commit()
        }
    }
}
