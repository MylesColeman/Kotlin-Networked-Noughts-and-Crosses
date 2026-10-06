package u0012604.tictactoe

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.assets.disposeSafely
import ktx.graphics.use
import u0012604.tictactoe.screens.FirstScreen.Companion.NOUGHT_RADIUS

class Board(val viewport: Viewport) : Disposable {
    private var thirdOfWidth = 0f
    private var thirdOfHeight = 0f

    private var halfCellW = 0f
    private var halfCellH = 0f

    private val shapeRenderer = ShapeRenderer()

    private var boardLines = emptyArray<Pair<Vector2, Vector2>>()

    private val board = arrayOf(
        arrayOf(Token.EMPTY, Token.EMPTY, Token.EMPTY),
        arrayOf(Token.EMPTY, Token.EMPTY, Token.EMPTY),
        arrayOf(Token.EMPTY, Token.EMPTY, Token.EMPTY)
    )

    fun placeToken(row: Int, col: Int, token: Token) =
        if(row in (0..2) && col in (0 .. 2) && board[row][col] == Token.EMPTY) {
            board[row][col] = token
            true
        }
        else {
            false
        }

    fun draw(batch: Batch) {
        shapeRenderer.use(ShapeRenderer.ShapeType.Filled, viewport.camera.combined) { sr ->

            sr.color = Color.RED

            boardLines.forEach { line ->
//                    Gdx.app.log(TAG, "p0:${line.first}, ${line.second}")

                sr.rectLine(line.first, line.second, 10f)
            }

            board.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { colIndex, col  ->
                    when(col) {
                        Token.NOUGHT -> drawNought(rowIndex, colIndex, sr)

                        Token.CROSS -> drawCross(rowIndex, colIndex, sr)

                        else -> {}
                    }
                }
            }
        }
    }

    fun resize(width: Int, height: Int) {
        boardLines = emptyArray()

        thirdOfWidth = width / 3f
        thirdOfHeight = height / 3f

        // Vertical lines
        val x1 = thirdOfWidth
        val x2 = Gdx.graphics.width / 1.5f
        boardLines += Pair(Vector2(x1, 0f), Vector2(x1, height.toFloat()))
        boardLines += Pair(Vector2(x2, 0f), Vector2(x2, height.toFloat()))

        // Horizontal lines
        val y1 = thirdOfHeight
        val v2 = Gdx.graphics.height.toFloat() / 1.5f

        boardLines += Pair(Vector2(0f, y1), Vector2(width.toFloat(), y1))
        boardLines += Pair(Vector2(0f, v2), Vector2(width.toFloat(), v2))

        halfCellW = x1 / 2f
        halfCellH = y1 / 2f
    }

    private fun drawNought(row: Int, col: Int, sr: ShapeRenderer) {
        val x = col * thirdOfWidth + halfCellW
        val y = (2 - row) * thirdOfHeight + halfCellH

        sr.circle(x, y, NOUGHT_RADIUS)
    }

    private fun drawCross(row: Int, col: Int, sr: ShapeRenderer) {
        val flipRow = 2 - row

        val l1x1 = col * thirdOfWidth + 50f
        val l1y1 = flipRow * thirdOfHeight + 50f
        val l1x2 = col * thirdOfWidth - 50f + 2 * halfCellW
        val l1y2 = flipRow * thirdOfHeight - 50f + 2 * halfCellH

        sr.rectLine(l1x1, l1y1, l1x2, l1y2, 10f)
        sr.rectLine(l1x1, l1y2, l1x2, l1y1, 10f)
    }

    override fun dispose() {
        shapeRenderer.disposeSafely()
    }
}
