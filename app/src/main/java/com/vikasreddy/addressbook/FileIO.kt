package com.vikasreddy.addressbook

import android.content.Context
import android.location.Location
import android.util.Log
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class FileIO {
    companion object {
        fun writeAddressBook(context: Context, addressBook: MutableList<Address>) {
            try {
                if(addressBook.size <= 0)
                    return
                val serializableAddressBook: ArrayList<SerializableAddress> = ArrayList(2)
                for (address: Address in addressBook) {
                    if (address.location != null) {
                        val serializableAddress = SerializableAddress(
                            address.Id,
                            address.label,
                            address.location.latitude,
                            address.location.longitude,
                            address.location.accuracy
                        )
                        serializableAddressBook.add(serializableAddress)
                    }
                }

                val fileOutputStream = context.openFileOutput(Constants.ADDRESS_BOOK_SAVE_KEY, Context.MODE_PRIVATE)
                val objectOutputStream = ObjectOutputStream(fileOutputStream)
                objectOutputStream.writeObject(serializableAddressBook)
                Log.i("FileIO.writeAddressBook", "Saved ${serializableAddressBook.size}")
            } catch (ex: Exception) {
                Log.i("FileIO.writeAddressBook", "Exception occurred: ${ex.message}")
            }
        }

        fun readAddressBook(context: Context): ArrayList<Address> {
            val addressBook: ArrayList<Address> = ArrayList(2)
            try {
                val fileInputStream = context.openFileInput(Constants.ADDRESS_BOOK_SAVE_KEY)
                val objectInputStream = ObjectInputStream(fileInputStream)
                val serializableAddressBook = objectInputStream.readObject() as ArrayList<*>
                for (serializableAddress in serializableAddressBook) {
                    if (serializableAddress is SerializableAddress) {
                        val location = Location("")
                        location.latitude = serializableAddress.latitude
                        location.longitude = serializableAddress.longitude
                        location.accuracy = serializableAddress.accuracy
                        addressBook.add(Address(serializableAddress.Id, serializableAddress.label, location))
                    }
                }
                Log.i("FileIO.readAddressBook", "Loaded addresses: ${addressBook.size}")
            } catch (ex: Exception) {
                Log.i("FileIO.readAddressBook ", "Exception occurred: ${ex.message}")
            }
            return addressBook
        }
    }
}