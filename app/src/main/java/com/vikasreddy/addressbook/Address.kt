package com.vikasreddy.addressbook

import android.location.Address
import android.location.Location
import java.io.Serializable

data class Address(val Id: Int, val label: String, val location: Location? = null, var address: Address? = null) {
    override fun toString(): String {
        return """Address Id $Id; Label: $label; Location: {${location?.latitude}, ${location?.longitude}}
                  Address: ${address.toString()}"""
    }
}

data class SerializableAddress(
    val Id: Int,
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float
) :
    Serializable {
    override fun toString(): String {
        return "Address Id $Id; Label: $label; Location: {$latitude, $longitude, $accuracy}"
    }
}