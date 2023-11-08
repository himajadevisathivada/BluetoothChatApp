import ChatAdapter
import ChatMessage
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.BluetoothChatApp3.R
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import com.example.bluetoothchatapp.ChatDatabaseHelper // Import your database manager

object Constants {
    const val MESSAGE_READ = 2
}

class ChatActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothDevice: BluetoothDevice? = null // Change the type to nullable
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var connectedThread: ConnectedThread

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages: ArrayList<ChatMessage> = ArrayList()

    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button

    private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val BLUETOOTH_PERMISSION_REQUEST = 1

    private lateinit var chatDatabase: ChatDatabaseHelper // Create an instance of your database manager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.chatRecyclerView)
        chatAdapter = ChatAdapter(chatMessages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available on this device", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            if (!bluetoothAdapter.isEnabled) {
                // You can request to enable Bluetooth here
            } else {
                // Request Bluetooth permissions if they are not granted
                requestBluetoothPermissions()
            }
        }

        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        for (device in pairedDevices) {
            if (device.name == "Himaja Devi's M31") { // Replace with the actual device name
                bluetoothDevice = device
                break
            }
        }

        if (bluetoothDevice != null) {
            ConnectThread(bluetoothDevice!!).start() // Use safe call operator and non-null assertion
        } else {
            Toast.makeText(this, "Bluetooth device not found", Toast.LENGTH_SHORT).show()
            finish()
        }

        sendButton.setOnClickListener {
            val message = messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                messageEditText.text.clear()
            }
        }

        // Initialize your database manager
        chatDatabase = ChatDatabaseHelper(this)
        chatMessages.addAll(chatDatabase.getAllChatMessages())
        chatAdapter.notifyDataSetChanged()
    }

    private fun sendMessage(message: String) {
        if (::connectedThread.isInitialized) {
            connectedThread.write(message.toByteArray())
            chatMessages.add(ChatMessage(0, message, true))
            chatAdapter.notifyDataSetChanged()
        }
    }

    private val messageHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == Constants.MESSAGE_READ) {
                val readBuffer = msg.obj as ByteArray
                val receivedMessage = String(readBuffer, 0, msg.arg1)
                chatMessages.add(ChatMessage(0, receivedMessage, false))
                chatAdapter.notifyDataSetChanged()
            }
        }
    }

    inner class ConnectThread(private val device: BluetoothDevice) : Thread() {
        override fun run() {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
                bluetoothSocket.connect()
                connectedThread = ConnectedThread(bluetoothSocket)
                connectedThread.start()
            } catch (e: IOException) {
                // Handle the exception
            }
        }
    }

    inner class ConnectedThread(socket: BluetoothSocket) : Thread() {
        private lateinit var inputStream: InputStream
        private lateinit var outputStream: OutputStream

        init {
            try {
                inputStream = socket.inputStream
                outputStream = socket.outputStream
            } catch (e: IOException) {
                // Handle the exception
            }
        }

        override fun run() {
            // Implement Bluetooth communication here
        }

        fun write(buffer: ByteArray) {
            try {
                outputStream.write(buffer)
            } catch (e: IOException) {
                // Handle the exception
            }
        }
    }

    private fun requestBluetoothPermissions() {
        val bluetoothPermission = Manifest.permission.BLUETOOTH
        val bluetoothAdminPermission = Manifest.permission.BLUETOOTH_ADMIN

        if (ContextCompat.checkSelfPermission(this, bluetoothPermission) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, bluetoothAdminPermission) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(bluetoothPermission, bluetoothAdminPermission),
                BLUETOOTH_PERMISSION_REQUEST
            )
        }
    }

    // Handle permission request results if needed
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST) {
            // Handle permission request results if necessary
        }
    }
}
