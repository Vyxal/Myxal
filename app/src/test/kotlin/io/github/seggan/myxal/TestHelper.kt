package io.github.seggan.myxal

import io.github.seggan.myxal.app.Main.doMain
import java.io.IOException

object TestHelper {
    @Throws(IOException::class)
    fun run(vararg args: String) {
        doMain(arrayOf(".", "-r", *args), true)
    }
}