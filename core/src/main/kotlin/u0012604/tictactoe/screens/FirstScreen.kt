package u0012604.tictactoe.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.FitViewport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.graphics.use
import u0012604.tictactoe.Board
import u0012604.tictactoe.GameMessage
import u0012604.tictactoe.Token

class FirstScreen(
    val game: KtxGame<KtxScreen>,
    private val receiveChannel: ReceiveChannel<GameMessage>,
    private val sendChannel: SendChannel<GameMessage>
) : KtxScreen, InputProcessor {

    private var gameStarted = false

    private var gameOver = false

    private val batch = SpriteBatch()


    private val camera =
        OrthographicCamera(WIDTH, HEIGHT).apply {
            position.set(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f, 0f)
        };
    private val viewport = FitViewport(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat(), camera)

    private val board = Board(viewport)

    private var touchPosition = Vector2.Zero

    private var localPlayerToken: Token? = null

    private var localPlayerTurn = false

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        Gdx.input.inputProcessor = this

        initiateIncomingGameMessageHandling()
    }

    override fun render(delta: Float) {
        clearScreen(red = 0.7f, green = 0.7f, blue = 0.7f)

        if(gameOver) {
            game.setScreen<GameOverScreen>()
            disposeSafely()
            return
        }

        camera.update()

        batch.use { board.draw(it) }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)

        board.resize(width, height)
    }

    private fun initiateIncomingGameMessageHandling() {
        coroutineScope.launch {
            while(true) {
                val gm = receiveChannel.receive()

                when(gm) {

                    is GameMessage.StartGameMessage -> {
                        Gdx.app.log(TAG, "THE SERVER HAS STARTED THE GAME")

                        gameStarted = true
                    }

                    is GameMessage.GameOverMessage -> {
                        Gdx.app.log(TAG, "THE SERVER HAS SENT THE GAME OVER MESSAGE")

                        gameStarted = false

                        gameOver = true
                    }

                    is GameMessage.JoinGameMessage -> {
                        localPlayerToken = gm.token

                        localPlayerTurn = localPlayerToken == Token.NOUGHT
                    }

                    is GameMessage.PlaceTokenMessage -> {
                        Gdx.app.log(TAG, "The other player placed a token at: (${gm.row}, ${gm.col})")

                        board.placeToken(gm.row, gm.col, gm.token)

                        localPlayerTurn = true
                    }

                }

                Gdx.app.log(TAG, "THE MESSAGE RECEIVED IS: $gm")

                delay(10L)
            }
        }
    }

    override fun dispose() {
        batch.disposeSafely()
        board.disposeSafely()
    }

    override fun keyDown(keycode: Int) = true

    override fun keyUp(keycode: Int) = true

    override fun keyTyped(character: Char) = true

    override fun touchDown(
        screenX: Int,
        screenY: Int,
        pointer: Int,
        button: Int
    ): Boolean {

        if(!gameStarted || !localPlayerTurn)
            return true;

        val col = ((screenX.toFloat() / Gdx.graphics.width) * 3).toInt()
        val row = ((screenY.toFloat() / Gdx.graphics.height) * 3).toInt()

        localPlayerToken?.let {
            if (board.placeToken(row, col, it)) {

                localPlayerTurn = false

                val gameMessage = GameMessage.PlaceTokenMessage(row, col, it)

                coroutineScope.launch {
                    sendChannel.send(gameMessage)
                }
            }
        }

        Gdx.app.log(TAG, "THE ROW IS: ($row, $col)")

        touchPosition.set(screenX.toFloat(), screenY.toFloat())

        viewport.unproject(touchPosition)

        return true
    }

    override fun touchUp(
        screenX: Int,
        screenY: Int,
        pointer: Int,
        button: Int
    ) = true

    override fun touchCancelled(
        screenX: Int,
        screenY: Int,
        pointer: Int,
        button: Int
    ) = true

    override fun touchDragged(
        screenX: Int,
        screenY: Int,
        pointer: Int
    ) = true

    override fun mouseMoved(screenX: Int, screenY: Int) = true

    override fun scrolled(amountX: Float, amountY: Float) = true

    companion object {
        val TAG = FirstScreen::class.simpleName!!

        const val WIDTH = 100f
        const val HEIGHT = 16f * WIDTH / 9f

        const val NOUGHT_RADIUS = 100f
    }
}
