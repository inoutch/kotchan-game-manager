package io.github.inoutch.kotchan.game.extension

inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
    var x = 0
    while (x < size) {
        action(this[x++])
    }
}
