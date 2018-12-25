package com.soywiz.korge.time

import com.soywiz.klock.*
import com.soywiz.korge.component.*
import com.soywiz.korge.view.*
import com.soywiz.korio.lang.*
import kotlinx.coroutines.*
import kotlin.collections.arrayListOf
import kotlin.collections.minusAssign
import kotlin.collections.plusAssign
import kotlin.coroutines.*

class TimerComponents(override val view: View) : UpdateComponent {
	private val timers = arrayListOf<(TimeSpan) -> Unit>()
	private val timersIt = arrayListOf<(TimeSpan) -> Unit>()

	override fun update(time: TimeSpan) {
		timersIt.clear()
		timersIt.addAll(timers)
		for (timer in timersIt) timer(time)
	}

	suspend fun waitFrame() = wait((1000.0 / 60.0).milliseconds)

	private var accumulated = 0.seconds

	fun takeAccumulated() = accumulated.also { accumulated = 0.seconds }
	fun incrAccumulated(time: TimeSpan) = run { accumulated += time }

	suspend fun wait(time: TimeSpan): Unit = suspendCancellableCoroutine { c ->
		wait(time) { c.resume(Unit) }
	}

	fun wait(time: TimeSpan, callback: () -> Unit = {}): Closeable {
		var elapsedTime = takeAccumulated()
		var timer: ((TimeSpan) -> Unit)? = null
		timer = {
			elapsedTime += it
			//println("TIMER: $elapsedTime")
			if (elapsedTime >= time) {
				incrAccumulated(elapsedTime - time)
				timers -= timer!!
				//println("DONE!")
				callback()
			}
		}
		timers += timer
		return Closeable { timers -= timer }
	}
}

val View.timers get() = this.getOrCreateComponent { TimerComponents(this) }
suspend fun View.wait(time: TimeSpan) = this.timers.wait(time)
suspend fun View.waitFrame() = this.timers.waitFrame()

suspend fun View.sleep(time: TimeSpan) = this.timers.wait(time)
suspend fun View.sleepFrame() = this.timers.waitFrame()

suspend fun View.delay(time: TimeSpan) = this.timers.wait(time)

fun View.timer(time: TimeSpan, callback: () -> Unit): Closeable = this.timers.wait(time, callback)
