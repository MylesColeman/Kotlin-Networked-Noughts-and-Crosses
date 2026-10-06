package u0012604.tictactoe.screens

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.graphics.use

class GameOverScreen  : KtxScreen {

    private val font = BitmapFont().apply {
        data.setScale(3f)
    }
    private val batch = SpriteBatch()

    var resultText: String = "GAME OVER"

    override fun render(delta: Float) {
        clearScreen(red = 0.7f, green = 0.7f, blue = 0.7f)

        batch.use {
            font.draw(it, resultText, 100f, 500f)
        }
    }
}
