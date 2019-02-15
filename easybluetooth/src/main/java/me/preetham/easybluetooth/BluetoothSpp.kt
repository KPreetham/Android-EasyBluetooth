package me.preetham.easybluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import me.preetham.easybluetooth.enums.Mode
import me.preetham.easybluetooth.interfaces.SppConnectionListener
import me.preetham.easybluetooth.interfaces.SppDataListener
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

open class BluetoothSpp private constructor(builder: Builder) {

    private val uuid: UUID

    private val dataListener: SppDataListener?
    private val connectionListener: SppConnectionListener?

    private val mode: Mode
    private val bufferSize: Int
    var isRunning = false
        private set

    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    var targetDevice: BluetoothDevice? = null
        private set


    init {
        this.uuid = builder.uuid
        this.dataListener = builder.dataListener
        this.connectionListener = builder.connectionListener
        this.mode = builder.mode
        this.bufferSize = builder.bufferSize
    }

    class Builder {
        var uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            private set

        var dataListener: SppDataListener? = null
            private set

        var connectionListener: SppConnectionListener? = null
            private set

        var mode = Mode.INSECURE
            private set

        var bufferSize = 1024
            private set

        fun build(): BluetoothSpp {
            return BluetoothSpp(this)
        }

        fun setBufferSize(bufferSize: Int): Builder = apply {
            this.bufferSize = bufferSize
        }

        fun setConnectionListener(connectionListener: SppConnectionListener): Builder = apply {
            this.connectionListener = connectionListener
        }

        fun setDataListener(dataListener: SppDataListener): Builder = apply {
            this.dataListener = dataListener
        }

        fun setMode(mode: Mode): Builder = apply {
            this.mode = mode
        }

        fun setUuid(uuid: String): Builder = apply {
            this.uuid = UUID.fromString(uuid)
        }

        fun setUuid(uuid: UUID): Builder = apply {
            this.uuid = uuid
        }
    }

    private fun start(bluetoothSocket: BluetoothSocket) {
        try {
            inputStream = bluetoothSocket.inputStream
            outputStream = bluetoothSocket.outputStream

            connectionListener?.onConnectionSuccess()
            isRunning = true
        } catch (e: IOException) {
            connectionListener?.onConnectionLost()
            return
        }

        val sppThread = object : Thread() {
            var bytes_read = -1
            var byte_data = ByteArray(bufferSize)

            override fun run() {
                while (isRunning) {
                    try {
                        bytes_read = inputStream!!.read(byte_data)
                        dataListener?.onDataReceived(byte_data, bytes_read)
                    } catch (e: IOException) {
                        connectionListener?.onConnectionLost()
                        break
                    }
                }
            }
        }

        sppThread.start()
    }

    fun connect(targetDevice: BluetoothDevice) {
        try {
            bluetoothSocket = when (mode) {
                Mode.SECURE -> targetDevice.createRfcommSocketToServiceRecord(uuid)
                Mode.INSECURE -> targetDevice.createInsecureRfcommSocketToServiceRecord(uuid)
            }

            bluetoothSocket!!.connect()
            this.targetDevice = targetDevice

            start(bluetoothSocket!!)
        } catch (ex: IOException) {
            connectionListener?.onConnectionFailure()
        }
    }

    fun disconnect() {
        isRunning = false
        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
            connectionListener?.onConnectionClosed()
        } catch (ignored: IOException) {

        }
    }

    fun write(bytes: ByteArray) {
        try {
            outputStream!!.write(bytes)
            dataListener?.onDataSent(bytes)
        } catch (e: IOException) {
            connectionListener!!.onConnectionLost()
        }

    }

    fun write(data_string: String) {
        write(data_string.toByteArray())
    }

    companion object {
        private val TAG = BluetoothSpp::class.java.simpleName
    }
}
