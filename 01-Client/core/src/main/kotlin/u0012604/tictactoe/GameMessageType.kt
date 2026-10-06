package u0012604.tictactoe

import java.nio.ByteBuffer

interface Serializable {
    fun serialize() : ByteArray
}

interface Deserializable {
    fun deserialize(bb: ByteBuffer) : GameMessage
}

enum class GameMessageType(val id: Byte) {
    JOIN_GAME(1),
    PLACE_TOKEN(2),
    START_GAME(3),
    GAME_OVER(4);

    companion object {
        fun fromByte(id: Byte) = entries.first { it.id == id }
    }
}

sealed class GameMessage(val type: GameMessageType) : Serializable {

    override fun serialize() = byteArrayOf(type.id)

    object StartGameMessage : GameMessage(GameMessageType.START_GAME)

    class GameOverMessage(val winnerToken: Byte) : GameMessage(GameMessageType.GAME_OVER) {
        companion object {
            fun deserialize(buffer: ByteBuffer): GameOverMessage {
                return GameOverMessage(buffer.get())
            }
        }
        override fun serialize() = byteArrayOf(type.id, winnerToken)
    }

    // -----------------------------------------------------------------------------------------------
    data class JoinGameMessage(val token: Token) : GameMessage(GameMessageType.JOIN_GAME) {

        override fun serialize(): ByteArray = with(ByteBuffer.allocate(2)) {
            put(token.type)
            super.serialize() + array()
        }

        companion object : Deserializable {

            override fun deserialize(bb: ByteBuffer) = with(bb) {
                JoinGameMessage(Token.fromByte(get()))
            }
        }
    }

    // -----------------------------------------------------------------------------------------------
    data class PlaceTokenMessage(val row: Int, val col: Int, val token: Token) : GameMessage(GameMessageType.PLACE_TOKEN) {

        override fun serialize(): ByteArray = with(ByteBuffer.allocate(10)) {
            putInt(row)
            putInt(col)
            put(token.type)
            super.serialize() + array()
        }

        companion object : Deserializable {
            override fun deserialize(bb: ByteBuffer) = with(bb) {

                val row = getInt()
                val col = getInt()
                val token = Token.fromByte(get())

                PlaceTokenMessage(row, col, token)
            }

        }

    }
}
