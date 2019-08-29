package com.example.android.beacon.nearby

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.*
import com.google.android.gms.nearby.messages.MessageFilter
import java.util.*


class MainActivity : AppCompatActivity() {

    private val TAG : String = MainActivity::class.java.simpleName

    private var mMessageListener: MessageListener? = null
    private var mNearbyDevicesArrayAdapter : ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mMessageListener = object : MessageListener() {
            override fun onFound(message: Message?) {
                super.onFound(message)

                message?.let {

                    if(message.type == Message.MESSAGE_TYPE_I_BEACON_ID) {
                        val iBeaconId = IBeaconId.from(message)

                        val status = details("Found beacon", iBeaconId)

                        Log.d(TAG, status)

                        mNearbyDevicesArrayAdapter?.add(status)
                    }
                }

            }

            override fun onLost(message: Message?) {
                super.onLost(message)

                message?.let {
                    if(message.type == Message.MESSAGE_TYPE_I_BEACON_ID) {
                        val iBeaconId = IBeaconId.from(message)

                        val status = details("Lost sight of beacon", iBeaconId)
                        Log.d(TAG, status)
                        mNearbyDevicesArrayAdapter?.add(status)

                    }

                }

            }

        }


        val nearbyDevicesArrayList = ArrayList<String>()
        mNearbyDevicesArrayAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            nearbyDevicesArrayList
        )
        val nearbyDevicesListView : ListView= findViewById(
            R.id.am_nearby_devices_list_view
        )
        nearbyDevicesListView.adapter = mNearbyDevicesArrayAdapter


    }

    override fun onStart() {
        super.onStart()


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            subscribe()
        } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }

    }

    override fun onStop() {
        super.onStop()

        unsubscribe()

    }

    private fun subscribe() {

        val messageFilter = MessageFilter.Builder()
            .includeIBeaconIds(UUID.fromString(IBEACON_UUID), null, null)
            .build()

        val options = SubscribeOptions.Builder()
            .setStrategy(Strategy.BLE_ONLY)
            .setCallback(object: SubscribeCallback() {
                override fun onExpired() {
                    super.onExpired()
                    Log.i(TAG, "Subscription expired")
                }
            })
            .setFilter(messageFilter)
            .build()

        Nearby.getMessagesClient(this).subscribe(mMessageListener!!, options)
        Log.i(TAG, "Subscription start")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    subscribe()
                } else {
                    Log.w(TAG,"ACCESS_FINE_LOCATION Permission not granted")
                }
                return
            }
            else -> {
            }
        }


    }

    private fun details(event: String, iBeaconId: IBeaconId) : String {
        return "$event: ${iBeaconId.proximityUuid}:${iBeaconId.major}:${iBeaconId.minor}"
    }


    private fun unsubscribe() {
        mMessageListener?.let {
            Nearby.getMessagesClient(this).unsubscribe(it)
            Log.i(TAG, "Subscription end")
        }

    }

    companion object {
        const val IBEACON_UUID = "SET BEACON ID HERE"
        const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100
    }

}

