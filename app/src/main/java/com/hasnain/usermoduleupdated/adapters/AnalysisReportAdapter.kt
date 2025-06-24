package com.hasnain.usermoduleupdated.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hasnain.usermoduleupdated.R
import com.hasnain.usermoduleupdated.models.Report
import com.squareup.picasso.Picasso

class AnalysisReportAdapter(
    private val reportList: List<Report>,
    private val onReportSelected: (Report) -> Unit
) : RecyclerView.Adapter<AnalysisReportAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reportImageView: ImageView = itemView.findViewById(R.id.reportImageView)
        val reportNameTextView: TextView = itemView.findViewById(R.id.tv_report_name)
        val reportDateTextView: TextView = itemView.findViewById(R.id.tv_report_date)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onReportSelected(reportList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report_analysis, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reportList[position]

        // Load the image using Picasso
        Picasso.get().load(report.user_report_url).into(holder.reportImageView)

        // Set the report name and date
        holder.reportNameTextView.text = report.user_report_name
        holder.reportDateTextView.text = report.user_report_date
    }

    override fun getItemCount(): Int {
        return reportList.size
    }
}
