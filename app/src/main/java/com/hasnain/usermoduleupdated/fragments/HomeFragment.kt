package com.hasnain.usermoduleupdated.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.hasnain.usermoduleupdated.*
import com.hasnain.usermoduleupdated.adapters.ArticlesAdapter
import com.hasnain.usermoduleupdated.adapters.HomeViewPagerAdapter
import com.hasnain.usermoduleupdated.databinding.FragmentHomeBinding
import com.hasnain.usermoduleupdated.models.NewsViewModel
import com.hasnain.usermoduleupdated.models.Profile
import com.hasnain.usermoduleupdated.utils.FirebaseHelper
import de.hdodenhof.circleimageview.CircleImageView

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HomeViewPagerAdapter
    private val sharedPrefsKey = "UserProfileData"
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var articlesAdapter: ArticlesAdapter
    private val slideInterval = 3000L
    private val slideRunnable = object : Runnable {
        override fun run() {
            _binding?.let {
                val currentItem = it.viewPager.currentItem
                val nextItem = if (currentItem == adapter.itemCount - 1) 0 else currentItem + 1
                it.viewPager.setCurrentItem(nextItem, true)
                handler.postDelayed(this, slideInterval)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize ViewPager with sliders
        val fragmentList = arrayListOf<Fragment>(
            SliderFragment.newInstance(R.drawable.dr_image, "Healthy Up", "Find a doctor easily from your phone"),
            SliderFragment.newInstance(R.drawable.dr_image2, "Stay Healthy", "Track your health and appointments easily"),
            SliderFragment.newInstance(R.drawable.dr_image3, "Contact us", "Track your health and appointments easily")
        )
        adapter = HomeViewPagerAdapter(fragmentList, childFragmentManager, lifecycle)
        _binding?.viewPager?.adapter = adapter
        handler.postDelayed(slideRunnable, slideInterval)

        // Initialize News Section
        initializeNewsSection()

        // Handle clicks for navigation
        _binding?.apply {
            cardBookAppointments.setOnClickListener {
                startActivity(Intent(requireActivity(), AppointmentActivity::class.java))
            }

            imgNoti.setOnClickListener {
                startActivity(Intent(requireActivity(), NotificationActivity::class.java))
            }

            cardBmiCalculator.setOnClickListener {
                startActivity(Intent(requireActivity(), BmiActivity::class.java))
            }

            cardParentalRecipts.setOnClickListener {
                startActivity(Intent(requireActivity(), ReceiptHandlingActivity::class.java))
            }

            cardReports.setOnClickListener {
                startActivity(Intent(requireActivity(), HomeReportAccessActivity::class.java))
            }

            cardBookedAppointments.setOnClickListener {
                startActivity(Intent(requireActivity(), BookedAppointmentActivity::class.java))
            }
        }

        return _binding?.root
    }

    private fun initializeNewsSection() {
        // Initialize NewsViewModel
        newsViewModel = ViewModelProvider(this).get(NewsViewModel::class.java)

        // Initialize ArticlesAdapter and RecyclerView
        articlesAdapter = ArticlesAdapter()  // Initialize the ArticlesAdapter

        // Set the adapter to the RecyclerView
        binding.newsRecyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL, false)
        // Initialize the RecyclerView

        binding.newsRecyclerView.adapter = articlesAdapter

        // Observe the articles LiveData from NewsViewModel
        newsViewModel.articles.observe(viewLifecycleOwner) { articles ->
            articlesAdapter.submitList(articles) // Submit only 3 articles
        }

        // Fetch news headlines
        newsViewModel.fetchNewsHeadlines()

        // Handle "See All" button click
        binding.seeAll.setOnClickListener {
            // Navigate to ArticlesActivity
            // In HomeFragment
            val intent = Intent(requireContext(), ArticlesActivity::class.java)
            startActivity(intent)

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreferences = requireContext().getSharedPreferences(sharedPrefsKey, Context.MODE_PRIVATE)
        loadUserData(sharedPreferences)
        fetchUserProfile(sharedPreferences)
    }

    @SuppressLint("SetTextI18n")
    private fun loadUserData(sharedPreferences: android.content.SharedPreferences) {
        val cachedUserName = sharedPreferences.getString("user_name", null)
        val cachedProfilePic = sharedPreferences.getString("user_profile_pic", null)

        if (cachedUserName != null && cachedProfilePic != null) {
            _binding?.profileName?.text = "Hi, $cachedUserName"
            loadImage(cachedProfilePic, _binding?.profile)
        }
    }

    private fun fetchUserProfile(sharedPreferences: android.content.SharedPreferences) {
        val currentUserEmail = FirebaseHelper.getAuth().currentUser?.email

        if (currentUserEmail != null) {
            FirebaseHelper.profileRef.orderByChild("user_email").equalTo(currentUserEmail)
                .addValueEventListener(object : ValueEventListener {
                    @SuppressLint("SetTextI18n")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (child in snapshot.children) {
                                val profile = child.getValue(Profile::class.java)
                                profile?.let {
                                    val editor = sharedPreferences.edit()
                                    editor.putString("user_name", it.user_name)
                                    editor.putString("user_profile_pic", it.user_profile_pic)
                                    editor.apply()

                                    _binding?.profileName!!.text = "Hi, ${it.user_name}"
                                    loadImage(it.user_profile_pic, _binding?.profile)
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        _binding?.profileName?.text = getString(R.string.error_profile)
                    }
                })
        } else {
            _binding?.profileName?.text = getString(R.string.no_user_authenticated)
        }
    }

    private fun loadImage(imageUrl: String?, imageView: CircleImageView?) {
        if (imageView == null || imageUrl.isNullOrEmpty()) {
            imageView?.setImageResource(R.drawable.profile_image)
            return
        }

        Glide.with(requireContext())
            .load(imageUrl)
            .into(imageView)
    }

    private fun openPhoneDialer(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(slideRunnable)
        _binding = null
    }
    // Initialize the RecyclerView

}
