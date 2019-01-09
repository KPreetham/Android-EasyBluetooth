package me.preetham.easybluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

import me.preetham.easybluetooth.enums.Mode
import me.preetham.easybluetooth.interfaces.SppConnectionListener
import me.preetham.easybluetooth.interfaces.SppDataListener

class BluetoothSpp private constructor(builder: Builder) {

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
        var uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        var dataListener: SppDataListener? = null
        var connectionListener: SppConnectionListener? = null

        var mode = Mode.INSECURE
        var bufferSize = 1024

        fun build(): BluetoothSpp {
            return BluetoothSpp(this)
        }

        fun setBufferSize(bufferSize: Int): Builder {
            this.bufferSize = bufferSize
            return this
        }

        fun setConnectionListener(connectionListener: SppConnectionListener): Builder {
            this.connectionListener = connectionListener
            return this
        }

        fun setDataListener(dataListener: SppDataListener): Builder {
            this.dataListener = dataListener
            return this
        }

        fun setMode(mode: Mode): Builder {
            this.mode = mode
            return this
        }

        fun setUuid(uuid: String): Builder {
            this.uuid = UUID.fromString(uuid)
            return this
        }

        fun setUuid(uuid: UUID): Builder {
            this.uuid = uuid
            return this
        }
    }

    private fun start(bluetoothSocket: BluetoothSocket) {
        try {
            inputStream = bluetoothSocket.inputStream
            outputStream = bluetoothSocket.outputStream

            if (connectionListener != null) {
                connectionListener.onConnectionSuccess()
                isRunning = true
            }
        } catch (e: IOException) {
            connectionListener?.onConnectionLost()
            return
        }

        val spp_thread = object : Thread() {
            internal var bytes_read = -1
            internal var byte_data = ByteArray(bufferSize)

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

        spp_thread.start()
    }

    fun connect(targetDevice: BluetoothDevice) {
        try {
            when (mode) {
                Mode.SECURE -> bluetoothSocket = targetDevice.createRfcommSocketToServiceRecord(uuid)
                Mode.INSECURE -> bluetoothSocket = targetDevice.createInsecureRfcommSocketToServiceRecord(uuid)
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
            if (inputStream != null) {
                inputStream!!.close()
            }

            if (outputStream != null) {
                outputStream!!.close()
            }

            if (bluetoothSocket != null) {
                bluetoothSocket!!.close()
            }

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