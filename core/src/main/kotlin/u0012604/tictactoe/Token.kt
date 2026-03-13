package u0012604.tictactoe

enum class Token(val type: Byte) {
    EMPTY(0), NOUGHT(1), CROSS(2);

    companion object {
        fun fromByte(type: Byte) = Token.entries.first { it.type == type }
    }
}
