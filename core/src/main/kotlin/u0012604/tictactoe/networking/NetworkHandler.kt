package u0012604.tictactoe.networking

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import u0012604.tictactoe.GameMessage
import u0012604.tictactoe.GameMessageType
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.nio.ByteBuffer

class NetworkHandler(
    private val host: String,
    private val port: Int,
    private val toServerChannel: Channel<GameMessage>, // Messages being received elsewhere in the client to send onwards to the client
    private val fromServerChannel: SendChannel<GameMessage>,    // Messages being sent from the server to elsewhere within client
    private val connectionTimeout: Int = 3000,
    private val maxRetries: Int = 10
) : Disposable
{
    fun ByteArray.processMessage() = decodeToString().trimEnd{it == Char(0)} // Removes trailing null character

    private val coroutineScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO).apply {
            launch {
                var retries = 0

                while(retries < maxRetries) {
                    if(startNetwork(host, port)) {
                        break;
                    }
                    delay(retries * 500L)
                    retries++
                }
                if(retries == maxRetries) {
                    Gdx.app.error(TAG, "Maximum retries ($maxRetries) exceeded, giving up :(")
                }
            }
        }

    private var socket: Socket? = null

    val isReady: Boolean
        get() = socket?.isConnected ?: false


    @OptIn(ExperimentalStdlibApi::class)
    private fun startNetwork(
        host: String,
        port: Int
    ) = try {
        var assignedClientId: UShort = 0U

        // Create our socket
        socket = Socket()

        // Connect with timeout set.
        socket?.let {
            val socketAddress = InetSocketAddress(host, port)
            it.connect(socketAddress, connectionTimeout)

            // -------------------------------------------------------------
            // Coroutine to handle messages
            // to be sent to the server
            // -------------------------------------------------------------
            coroutineScope.launch {
                try {
                    it.outputStream.apply {
                        while (true) {
                            // 1. Get the next message in the channel
                            //    to send onward to the server.
                            val nextMessage = toServerChannel.receive()

                            // 2. Write the message to the server
                            //    via the socket's output stream
                            write(nextMessage.serialize())

                            flush()

                            Gdx.app.log(TAG, "Sent the message $nextMessage")

                            delay(10L)
                        }
                    }
                } catch (ex: java.net.SocketException) {
                    Gdx.app.error(TAG, "[SEND] Socket Failed: ${ex.message}")
                }
            }
            // -------------------------------------------------------------
            // Coroutine to handle messages
            // being received from the server
            //
            // Messages received are then forwarded via the
            // -------------------------------------------------------------
            coroutineScope.launch {
                try {
                    it.inputStream.apply {
                        launch {
                            while (true) {
                                val byteArray = ByteArray(1024)

//                                delay(250L)

                                // 1. Read data from the socket's
                                //    input stream.
                                val count = read(byteArray, 0, 1024)

                                if (count == -1) {
                                    Gdx.app.error(TAG, "Socket Read Error!")
                                    break
                                }

                                val gameMessage = buildGameMessage(byteArray)

                                fromServerChannel.send(gameMessage)
                            }
                        }
                    }
                } catch (ex: java.net.SocketException) {
                    Gdx.app.log(TAG, "[RECEIVE] Socket Failure::[${ex.message}")
                }
            }

        }

        true
    } catch (ex: java.net.SocketTimeoutException) {
        Gdx.app.error(TAG, "Timeout Exception: ${ex.message}")
        false
    } catch (ex: java.net.ConnectException) {
        Gdx.app.error(TAG, "Connection Exception: ${ex.message}")
        false
    } catch (ex: java.net.SocketException) {
        Gdx.app.error(TAG, "Exception Raised: ${ex.message}")
        false
    }

    fun buildGameMessage(ba: ByteArray) = with(ByteBuffer.wrap(ba)) {
        val messageType = GameMessageType.fromByte(get())

        when(messageType) {
            GameMessageType.JOIN_GAME -> GameMessage.JoinGameMessage.deserialize(this)

            GameMessageType.PLACE_TOKEN -> GameMessage.PlaceTokenMessage.deserialize(this)
        }
    }

    override fun dispose() {
        coroutineScope.cancel()
    }

    companion object {
        val TAG = NetworkHandler::class.simpleName!!
    }
}
