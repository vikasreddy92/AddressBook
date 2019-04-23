package com.vikasreddy.addressbook

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.address_card.view.*

class AddressRecyclerAdapter(private val addressBook: ArrayList<Address>, private val context: Context) :
    RecyclerView.Adapter<AddressRecyclerAdapter.ViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.address_card, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvLabel.text = addressBook[position].address?.locality
        holder.tvAddress.text =
            getAddressLine(addressBook[position].address) + "\n" + addressBook[position].location?.accuracy + " meters accuracy"
    }

    override fun getItemCount() = addressBook.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLabel: TextView = view.label_tv
        val tvAddress: TextView = view.address_tv
    }

    private fun getAddressLine(address: android.location.Address?): String {
        if (address == null) return ""
        val addressFragments = with(address) {
            (0..maxAddressLineIndex).map { getAddressLine(it) }
        }
        return addressFragments.joinToString(separator = ", ")
    }
}