package com.example.qr_prueba_gaby.data

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.util.*

/**
 * Maneja la lógica de un periférico BLE para la activación de la aplicación.
 * Levanta un servidor GATT y anuncia un Service UUID único.
 */
class BlePeripheralManager(
    private val context: Context,
    private val onActivationReceived: (String) -> Unit
) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter = bluetoothManager.adapter
    private var advertiser: BluetoothLeAdvertiser? = null
    private var gattServer: BluetoothGattServer? = null

    companion object {
        const val TAG = "BlePeripheralManager"
        val SERVICE_UUID: UUID = UUID.fromString("8f2a1234-5678-1234-5678-123456789abc")
        val CH_ACTIVACION_UUID: UUID = UUID.fromString("8f2a1235-5678-1234-5678-123456789abc")
        const val ACTIVATION_PROTOCOL_PREFIX = "ACTIVATE_GABY_" // Ejemplo de protocolo
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.d(TAG, "BLE Advertising iniciado con éxito")
        }
        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "Fallo al iniciar BLE Advertising: $errorCode")
        }
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            Log.d(TAG, "Estado de conexión cambiado: $device, nuevo estado: $newState")
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            if (characteristic.uuid == CH_ACTIVACION_UUID) {
                val data = value?.toString(Charsets.UTF_8) ?: ""
                Log.d(TAG, "Petición de escritura en CH_ACTIVACION: $data")

                // Validar contra el protocolo (el Admin debe enviar algo como ACTIVATE_GABY_TOKEN)
                if (data.startsWith(ACTIVATION_PROTOCOL_PREFIX)) {
                    onActivationReceived(data)
                }

                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        if (adapter == null || !adapter.isEnabled) {
            Log.e(TAG, "Bluetooth no disponible o desactivado")
            return
        }

        // 1. Iniciar GATT Server
        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)?.apply {
            val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
            val characteristic = BluetoothGattCharacteristic(
                CH_ACTIVACION_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
            )
            service.addCharacteristic(characteristic)
            addService(service)
        }

        // 2. Iniciar Advertising
        advertiser = adapter.bluetoothLeAdvertiser
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()

        advertiser?.startAdvertising(settings, data, advertiseCallback)
        Log.d(TAG, "Periférico BLE iniciado: UUID=$SERVICE_UUID")
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        advertiser?.stopAdvertising(advertiseCallback)
        gattServer?.close()
        gattServer = null
        Log.d(TAG, "Periférico BLE detenido")
    }
}
