package u0012604.tictactoe


import kotlinx.coroutines.channels.Channel
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.async.KtxAsync
import u0012604.tictactoe.networking.NetworkHandler
import u0012604.tictactoe.screens.FirstScreen
import u0012604.tictactoe.screens.GameOverScreen

class Main : KtxGame<KtxScreen>() {

    private val clientChannel = Channel<GameMessage>(10)
    private val serverChannel = Channel<GameMessage>(10)

    private lateinit var networkHandler: NetworkHandler

    override fun create() {
        KtxAsync.initiate()

        networkHandler = NetworkHandler("10.0.2.2", 4300, serverChannel, clientChannel)

        addScreen(FirstScreen(this, clientChannel, serverChannel))

        addScreen(GameOverScreen())

        setScreen<FirstScreen>()

    }

    override fun dispose() {
        super.dispose()
        serverChannel.close()
        clientChannel.close()
        networkHandler.disposeSafely()
    }
}

