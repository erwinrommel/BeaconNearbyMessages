package com.example.android.beacon.nearby

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.*
import java.util.ArrayList
import com.google.android.gms.nearby.messages.MessageFilter



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
                    Log.i(TAG, "Message found: $it")
                    Log.i(TAG, "Message string:  ${it.content}")
                    Log.i(TAG, "Message namespaced type: ${it.namespace} / ${it.type}")

                    val status = details("Found Message", it)

                    Log.d(TAG, status)

                    mNearbyDevicesArrayAdapter?.add(status)
                }

            }

            override fun onLost(message: Message?) {
                super.onLost(message)

                message?.let {
                    val status = details("Lost sight of message", it)

                    Log.d(TAG, status)
                    mNearbyDevicesArrayAdapter?.add(status)
                }

            }

            override fun onBleSignalChanged(message: Message?, bleSignal: BleSignal?) {
                super.onBleSignalChanged(message, bleSignal)

                if(!SHOW_SIGNAL_CHANGED) return

                message?.let {
                    val status = details("Signal Changed", it)

                    Log.i(TAG, status)

                    mNearbyDevicesArrayAdapter?.add(
                        "$status rssi=${bleSignal?.rssi} txPower=${bleSignal?.txPower}"
                    )
                }


            }

            override fun onDistanceChanged(message: Message?, distance: Distance?) {
                super.onDistanceChanged(message, distance)

                if(!SHOW_DISTANCE_CHANGED) return

                message?.let {
                    val status = details("Distance changed", it)

                    Log.i(TAG, status)

                    mNearbyDevicesArrayAdapter?.add(
                        "$status accuracy=${distance?.accuracy}"
                    )


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

        subscribe()

    }

    override fun onStop() {
        super.onStop()

        unsubscribe()

    }

    private fun subscribe() {

        val messageFilter = MessageFilter.Builder()
            .includeNamespacedType(NAMESPACE, TYPE)
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

        mMessageListener?.let {
            Nearby.getMessagesClient(this).subscribe(it, options)
            Log.i(TAG, "Subscription start")
        }
    }

    private fun details(event: String, message: Message) : String {
        return "$event: ${message.namespace}/${message.type}/${String(message.content)}"
    }


    private fun unsubscribe() {
        mMessageListener?.let {
            Nearby.getMessagesClient(this).unsubscribe(it)
            Log.i(TAG, "Subscription end")
        }

    }

    companion object {
        const val NAMESPACE = "beacon-namespace"
        const val TYPE = "message type"
        const val SHOW_DISTANCE_CHANGED = false
        const val SHOW_SIGNAL_CHANGED = false
    }

}

