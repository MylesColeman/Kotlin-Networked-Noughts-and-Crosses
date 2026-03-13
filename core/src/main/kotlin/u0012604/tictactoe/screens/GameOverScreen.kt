package u0012604.tictactoe.screens

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.graphics.use

class GameOverScreen  : KtxScreen {

    private val font = BitmapFont()
    private val batch = SpriteBatch()

    override fun render(delta: Float) {
        clearScreen(red = 0.7f, green = 0.7f, blue = 0.7f)

        batch.use {
            font.draw(it, "Game Over!", 10f, 10f)
        }
    }
}
