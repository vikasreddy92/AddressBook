package com.vikasreddy.addressbook

import android.location.Address
import android.location.Location

data class Address(val Id: Int, val label: String, val location: Location? = null, val address: Address? = null)