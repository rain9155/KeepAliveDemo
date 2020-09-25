package com.example.keepalivedemo.utils

import java.util.concurrent.Executors

/**
 *
 * @author chenjianyu
 * @date 2020/9/25
 */
object Executors {

    private val SINGLE_EXECUTOR = Executors.newSingleThreadExecutor()

    @JvmStatic
    fun executeOrder(task: Runnable){
        SINGLE_EXECUTOR.execute(task)
    }

}