package com.hasnain.usermoduleupdated.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.hasnain.usermoduleupdated.R
import com.hasnain.usermoduleupdated.fragments.HomeFragment
import com.hasnain.usermoduleupdated.models.SliderItem

class SliderAdapter(
    private val fragment:Fragment,
    private val items: List<SliderItem>,
    private val onButtonClick: (Int) -> Unit
) : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {

    inner class SliderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.title)
        val desc = view.findViewById<TextView>(R.id.description)
        val button = view.findViewById<Button>(R.id.btn_action)
        val layout = view.findViewById<View>(R.id.slider_background)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val view = LayoutInflater.from(fragment.requireContext()).inflate(R.layout.item_slider, parent, false)
        return SliderViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.desc.text = item.description
        holder.button.text = item.buttonText
        holder.layout.setBackgroundResource(item.backgroundGradient)
        holder.button.setOnClickListener {
            onButtonClick(position)
        }
    }
}
