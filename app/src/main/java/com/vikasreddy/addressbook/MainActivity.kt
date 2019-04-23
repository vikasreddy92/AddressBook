package com.vikasreddy.addressbook

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity()
        , GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener {

//    private var addressBook: MutableList<Address> = mutableListOf()
    private var addressBook: ArrayList<Address> = ArrayList(2)
    private var lastLocation: Location? = null
    private var isPermissionGranted = false
    private lateinit var resultReceiver: AddressResultReceiver
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        requestLocationPermission()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        addFab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            if (!isPermissionGranted)
                requestLocationPermission()
            else
                fetchAddressButtonHandler(view)
        }
        loadAppData()

        address_recycler_view.layoutManager = LinearLayoutManager(this)
        address_recycler_view.adapter = AddressRecyclerAdapter(addressBook, this)
    }

    override fun onPause() {
        super.onPause()
        FileIO.writeAddressBook(this, addressBook)
    }

    override fun onResume() {
        super.onResume()
        loadAppData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onConnected(p0: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun startIntentService() {
        val intent = Intent(this, FetchAddressIntentService::class.java).apply {
            putExtra(Constants.RECEIVER, resultReceiver)
            putExtra(Constants.LOCATION_DATA_EXTRA, lastLocation)
        }
        startService(intent)
    }

    private fun startIntentService(id: Int, location: Location, addressResultReceiver: AddressResultReceiver) {
        val intent = Intent(this, FetchAddressIntentService::class.java).apply {
            putExtra(Constants.ADDRESS_ID_EXTRA, id)
            putExtra(Constants.RECEIVER, addressResultReceiver)
            putExtra(Constants.LOCATION_DATA_EXTRA, location)
        }
        startService(intent)
    }

    @SuppressLint("MissingPermission")
    private fun fetchAddressButtonHandler(view: View) {
        fusedLocationClient.lastLocation?.addOnSuccessListener { location: Location? ->
            if (location == null) return@addOnSuccessListener

            if (!Geocoder.isPresent()) {
                Snackbar.make(
                        view,
                        R.string.no_geocoder_available,
                        Snackbar.LENGTH_LONG
                ).show()
                return@addOnSuccessListener
            }
            lastLocation = location
            resultReceiver = AddressResultReceiver(Handler())
            startIntentService()
        }
    }

    internal inner class AddressResultReceiver(handler: Handler) : ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            val id = resultData?.getInt(Constants.ADDRESS_ID_EXTRA) as Int
            val addressOutput = resultData.getParcelable(Constants.RESULT_ADDRESS_DATA_KEY)
                    as android.location.Address
            val locationOutput = resultData.getParcelable(Constants.RESULT_LOCATION_DATA_KEY)
                    as Location

            if (resultCode == Constants.SUCCESS_RESULT) {
                var address = addressBook.find { it.Id == id }
                if(address == null) {
                    address =  Address(
                        addressBook.size,
                        "Address ${addressBook.size + 1}",
                        locationOutput,
                        addressOutput
                    )
                    addressBook.add(address)
                    Log.i(TAG, "Displaying ${address.label}")
                    if(addressBook.size == 1)
                        address_recycler_view.adapter = AddressRecyclerAdapter(addressBook, applicationContext)
                    else{
                        address_recycler_view.adapter?.notifyItemInserted(addressBook.indexOf(address))
                        address_recycler_view.adapter?.notifyDataSetChanged()
                    }
                }
                else {
                    address.address = addressOutput
                    address_recycler_view.adapter?.notifyItemChanged(addressBook.indexOf(address))
                    address_recycler_view.adapter?.notifyDataSetChanged()
                }
                Log.i(TAG, "Address Book Size:" + addressBook.size)
            }
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_LOCATION
                )
            }
        } else {
            isPermissionGranted = true
        }
    }

    private fun loadAppData() {
        if(addressBook.isEmpty()) {
            addressBook = FileIO.readAddressBook(this)
            addressBook.forEach {
                if(it.location != null)
                    startIntentService(it.Id, it.location, AddressResultReceiver(Handler()))
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    isPermissionGranted = true
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_LOCATION = 1
        private const val TAG = "MainActivity"
    }
}
