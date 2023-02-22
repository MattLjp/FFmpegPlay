package com.matt.ffmpegplay

import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
//        assertEquals(4, 2 + 2)
        println(listOf(-1, -3, 1, 3, 5, 6, 7, 2, 4, 10, 9, 8).dropLastWhile { it > 5 })
    }
}