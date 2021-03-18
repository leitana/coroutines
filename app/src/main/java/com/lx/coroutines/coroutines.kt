package com.lx.coroutines

import kotlinx.coroutines.*
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

/**
 * @title：coroutines
 * @projectName coroutines
 * @description: <Description>
 * @author linxiao
 * @data Created in 2020/07/15
 */

/**
 * 第一个协程程序
 */
fun test1() {
    GlobalScope.launch {//在后台启动一个协程
        delay(1000L) //非阻塞的等待1s
        println("World")//在延迟后打印输出
    }
//    thread {
//        Thread.sleep(1000L)
//        println("World")
//    }
    println("Hello")// 协程还在等待时，主线程继续
//    Thread.sleep(2000L)//阻塞主线程2s，保证JVM存活
}

/**
 * 桥接阻塞与非阻塞的世界
 * 第一个示例混用了 非阻塞的delay(...)与阻塞的Thread.sleep(...)。
 * 下面例子用runBlocking协程构造器来阻塞
 */
fun test2() {
    GlobalScope.launch {
        delay(1000)
        println("World")
    }
    println("Hello")//主线程中的代码会立即执行
    runBlocking {// 但是这个表达式阻塞了主线程
        delay(2000L)
    }
    /**
     * 结果是相似的，但这些代码只使用了非阻塞的函数delay。
     * 调用 runBlocking 的主线程会一直阻塞直到 runBlocking 内部协程执行完毕
     */
}

/**
 * 延迟一段时间来等待另一个协程运行并不是一个好的选择。
 * 让我们显式（以非阻塞方式）等待所启动的后台 Job 执行结束
 */
fun test3() = runBlocking {
    val job = GlobalScope.launch { //启动一个新的协程并保持这个作业的引用
        delay(1000)
        println("World")
    }
    println("Hello")
    job.join()//等待直到子协程执行结束

    /**
     * 结果仍然相同，但是主协程与后台作业的持续时间没有任何关系。
     */
}

/**
 * 可以启动协程，而不需要显式的join
 * 因为外部协程直到在其作用域中启动的所有协程都执行完毕后才会结束。
 */
fun test4() = runBlocking {
    launch { // 在 runBlocking 作用域中启动一个新协程
        delay(1000)
        println("World")
    }
    println("Hello")
}

@ObsoleteCoroutinesApi
fun test41() {
    val coroutineDispatcher = newSingleThreadContext("ctx")
    // 启动协程 1
    GlobalScope.launch(coroutineDispatcher) {
        println("the first coroutine")
        delay(200)
        println("the first coroutine")
    }
    // 启动协程 2
    GlobalScope.launch(coroutineDispatcher) {
        println("the second coroutine")
        delay(100)
        println("the second coroutine")
    }
    // 保证 main 线程存活，确保上面两个协程运行完成
    Thread.sleep(500)
}

/**
 * 作用域构建器
 *
 * 除了由不同构建器提供协程作用域之外，还可以使用coroutineScope构建器声明自己的作用域
 * 它会创建一个协程作用域并且在所有已启动子协程执行完毕之前不会结束。
 *
 * runBlocking和coroutineScope看起来很类似，因为它们都会等待其他协程体以及所有子协程结束。
 * 主要区别在于，runBlocking 方法会阻塞当前线程来等待，而coroutineScope只是挂起，会释放底层线程用于其他用途。
 * 由于这个差距，runBlocking 是常规函数，而coroutineScope是挂起函数
 *
 * 重要
 * coroutineScope代码块中的执行完 才往下执行
 * coroutineScope代码块中的执行完 才往下执行
 * coroutineScope代码块中的执行完 才往下执行
 *
 */
fun test5() = runBlocking {
    launch {
        delay(1000)
        println("Task from runBlocking1000")
    }

    println("Coroutine scope is over1")// 这一行在内嵌 launch 执行完毕后才输出

    coroutineScope {
        launch {
            delay(150)
            println("Task from runBlocking 150")
        }
    }

    coroutineScope {//创建一个协程作用域
        launch {
            delay(500)
            println("Task from nested launch500")
        }

        delay(100)
        println("Task from coroutine scope100")//这一行会在内嵌 launch之前输出
    }

    println("Coroutine scope is over2")// 这一行在内嵌 launch 执行完毕后才输出
}

suspend fun test51() = coroutineScope {
    launch {
        delay(1000)
        println("Task from runBlocking1000")
    }

    println("Coroutine scope is over1")// 这一行在内嵌 launch 执行完毕后才输出

    coroutineScope {
        launch {
            delay(150)
            println("Task from runBlocking 150")
        }
    }

    coroutineScope {//创建一个协程作用域
        launch {
            delay(500)
            println("Task from nested launch500")
        }

        delay(100)
        println("Task from coroutine scope100")//这一行会在内嵌 launch之前输出
    }

    println("Coroutine scope is over2")// 这一行在内嵌 launch 执行完毕后才输出
}

fun test6() = runBlocking {
    launch { doWorld() }
    println("Hello")
}

suspend fun doWorld() {
    delay(1000)
    println("World")
}

/**
 * 协程很轻量（错误），本质和线程一样，不过可以自动帮你创建线程
 */
fun test7() = runBlocking {
    repeat(10000) {
        launch {
            delay(1000)
            print(".")
        }
    }
}

fun test8() = runBlocking {
    val job = launch {
        repeat(1000) {
            println("job: I'm sleeping $it ...")
            delay(500L)
        }
    }
    delay(1300)
    println("main: I'm tired of waiting!")
    job.cancel() // 取消该作业
    job.join() // 等待直到子协程执行结束
    println("main: Now I can quit.")
}

suspend fun test9() = withContext(Dispatchers.IO) {
    println("当前线程是: ${Thread.currentThread().name}")
}

/**
 * 协程是一个线程框架，并不轻量？
 */
fun test10() = runBlocking {
    val startTime = System.currentTimeMillis()
    val job = launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        while (i < 5) {  // 一个执行计算的循环，只是为了占用 CPU
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("job:I'm sleeping ${i++}")
                nextPrintTime += 500L
            }
        }
    }
    delay(1300L)
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // 取消一个作业并等待他结束
    println("main: Now I can quit.")
}

suspend fun doSomethingUsefulOne(): Int{
    delay(1000L)
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(1000L)
    return 29
}

/**
 * 默认顺序调用
 */
fun test11() = runBlocking{
    val time = measureTimeMillis{
        val one = doSomethingUsefulOne()
        val two = doSomethingUsefulTwo()
        println("The answer is ${one + two}")
    }
    println("Completed in $time ms")
}

/**
 * 使用async并发
 * 使用.await()在延期的值上得到最终结果
 * async 类似于 launch 它启动了一个单独的协程，这是一个轻量级的线程并与其他所有协程一起并发工作
 * 不同之处在于 launch 返回一个 Job 并且不带任何附加返回值，
 * 而async返回一个 Deferred(一个轻量级的非阻塞feature，也是一个Job)
 */
fun test12() = runBlocking {
    val time = measureTimeMillis {
        val one = async { doSomethingUsefulOne() }
        val two = async { doSomethingUsefulTwo() }
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
}

/**
 * 惰性启动的 async
 * async可以通过将 start 参数设置为 CoroutineStart.LAZY 而变为惰性的
 * 在这个模式下，只有结果通过 await 获取的时候协程才会启动
 * 或者在 Job 的 start 函数调用的时候才会驱动
 * PS: 在这个例子中，定义的两个协程并没有执行，控制权在调用 start
 *     如果只是在 println 中调用 await 而没有在单独的协程中调用 start 这将会导致顺序行为
 *     也就是没有并发
 */
fun test13() = runBlocking {
    val time = measureTimeMillis {
        val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne() }
        val two = async(start = CoroutineStart.LAZY) { doSomethingUsefulTwo() }
        /**
         * 如果不调用下面的 start 方法时间为2倍 感觉没有同步
         */
//        one.start()//启动第一个
//        two.start()//启动第二个
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
}


/**
 * async风格的函数
 * 这些 xxxAsync 函数不是挂起函数，他们可以在任何地方使用。
 * 然而，他们总是在调用他们的代码中意味着异步
 */
fun somethingUsefulOneAsync() = GlobalScope.async {
    // somethingUsefulOneAsync 函数的返回值类型是 Deferred<Int>
    doSomethingUsefulOne()
}

fun somethingUsefulTwoAsync() = GlobalScope.async {
    doSomethingUsefulTwo()
}

fun test14() {
    val time = measureTimeMillis {
        // 我们可以在协程外面启动异步执行
        val one = somethingUsefulOneAsync()
        val two = somethingUsefulTwoAsync()
        // 但是等待结果必须调用其它的挂起或者阻塞
        runBlocking {
            println("The answer is ${one.await() + two.await()}")
        }

    }
    println("Completed in $time ms")
}

fun test15() = runBlocking{
    launch {
        println("I'm working in thread ${Thread.currentThread().name}")
    }
    //不受限的
    launch(Dispatchers.Unconfined) {
        println("Unconfined    I'm working in thread ${Thread.currentThread().name}")
    }
    //默认调度器
    launch(Dispatchers.Default) {
        println("Default     I'm working in thread ${Thread.currentThread().name}")
    }
    /**
     * newSingleThreadContext 为协程启动一个新的线程
     */
    launch(newSingleThreadContext("myOwnThread")) {
        println("newSingleThreadContext     I'm working in thread ${Thread.currentThread().name}")
    }
}

suspend fun main(args: Array<String>) {
    test41()
}