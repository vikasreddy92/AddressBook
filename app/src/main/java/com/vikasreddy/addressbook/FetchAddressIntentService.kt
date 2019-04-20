package com.vikasreddy.addressbook

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import java.io.IOException
import java.util.*

class FetchAddressIntentService(name: String? = null) : IntentService(name) {
    private var receiver: ResultReceiver? = null

    override fun onHandleIntent(intent: Intent?) {

        intent ?: return
        val geocoder = Geocoder(this, Locale.getDefault())
        var errorMessage = ""
        var addresses: List<Address> = emptyList()

        // Get the data passed to this service through an extra.
        val location = intent.getParcelableExtra(
                Constants.LOCATION_DATA_EXTRA
        ) as Location
        receiver = intent.getParcelableExtra(Constants.RECEIVER)

        try {
            addresses = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
            )
        } catch (ioException: IOException) {
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_not_available)
            Log.e(TAG, errorMessage, ioException)
        } catch (illegalArgumentException: IllegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used)
            Log.e(
                    TAG, "$errorMessage. Latitude = $location.latitude , " +
                    "Longitude =  $location.longitude", illegalArgumentException
            )
        } catch (exception: Throwable) {
            errorMessage = getString(R.string.invalid_lat_long_used)
            Log.e(
                    TAG, "$errorMessage. Latitude = $location.latitude , " +
                    "Longitude =  $location.longitude", exception
            )
        }

        // Handle case where no address was found.
        if (addresses.isEmpty()) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found)
                Log.e(TAG, errorMessage)
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage)
        } else {
            val address = addresses[0]
            Log.i(TAG, getString(R.string.address_found))
            deliverResultToReceiver(
                    Constants.SUCCESS_RESULT,
                    address,
                    location
            )
        }
    }

    private fun deliverResultToReceiver(resultCode: Int, message: String) {
        val bundle = Bundle().apply {
            putString(Constants.RESULT_DATA_KEY, message)
        }
        receiver?.send(resultCode, bundle)
    }

    private fun deliverResultToReceiver(resultCode: Int, address: Address, location: Location) {
        val bundle = Bundle().apply {
            putParcelable(Constants.RESULT_ADDRESS_DATA_KEY, address)
            putParcelable(Constants.RESULT_LOCATION_DATA_KEY, location)
        }
        receiver?.send(resultCode, bundle)
    }

    companion object {
        private const val TAG = "FetchAddressIS"
    }
}