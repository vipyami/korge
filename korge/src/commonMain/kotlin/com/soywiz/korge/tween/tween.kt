@file:Suppress("NOTHING_TO_INLINE")

package com.soywiz.korge.tween

import com.soywiz.klock.*
import com.soywiz.kmem.*
import com.soywiz.korge.component.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korim.color.RGBA.Companion.interpolate
import com.soywiz.korma.geom.*
import com.soywiz.korma.interpolation.*
import kotlinx.coroutines.*
import kotlin.coroutines.*
import kotlin.reflect.*

class TweenComponent(
	override val view: View,
	private val vs: List<V2<*>>,
	val time: TimeSpan = TimeSpan.NULL,
	val easing: Easing = Easing.LINEAR,
	val callback: (Float) -> Unit,
	val c: CancellableContinuation<Unit>
) : UpdateComponent {
	var elapsed = 0.seconds
	val ctime : TimeSpan = if (time == TimeSpan.NULL) 1.seconds else time
	var cancelled = false
	var done = false

	init {
		c.invokeOnCancellation {
			cancelled = true
			//println("TWEEN CANCELLED[$this, $vs]: $elapsed")
		}
		update(0.seconds)
	}

	fun completeOnce() {
		if (!done) {
			done = true
			detach()
			c.resume(Unit)
			//println("TWEEN COMPLETED[$this, $vs]: $elapsed. thread=$currentThreadId")
		}
	}

	override fun update(time: TimeSpan) {
		//println("TWEEN UPDATE[$this, $vs]: $elapsed + $dtMs")
		if (cancelled) return completeOnce()
		elapsed += time

		val ratio = (elapsed / ctime).clamp(0.0, 1.0).toFloat()
		for (v in vs) {
			val durationInTween = if (v.duration == TimeSpan.NULL) (ctime - v.startTime) else v.duration
			val elapsedInTween = (elapsed - v.startTime).clamp(0.seconds, durationInTween)
			val ratioInTween =
				if (durationInTween <= 0.seconds) 1.0f else (elapsedInTween.seconds / durationInTween.seconds).toFloat()
			v.set(easing(ratioInTween))
		}
		callback(easing(ratio))

		if (ratio >= 1.0) return completeOnce()
	}

	override fun toString(): String = "TweenComponent($view)"
}

// @TODO: Move to klock
private fun TimeSpan.clamp(min: TimeSpan, max: TimeSpan) = this.seconds.clamp(min.seconds, max.seconds).seconds
private operator fun TimeSpan.div(other: TimeSpan): Double = this.seconds / other.seconds

private val emptyCallback: (Float) -> Unit = {}

suspend fun View?.tween(
	vararg vs: V2<*>,
	time: TimeSpan,
	easing: Easing = Easing.LINEAR,
	callback: (Float) -> Unit = emptyCallback
): Unit {
	if (this != null) {
		withTimeout(300 + time.millisecondsLong * 2) {
			suspendCancellableCoroutine<Unit> { c ->
				val view = this@tween
				//println("STARTED TWEEN at thread $currentThreadId")
				TweenComponent(view, vs.toList(), time, easing, callback, c).attach()
			}
		}
	}
}

suspend fun View.show(time: TimeSpan, easing: Easing = Easing.LINEAR) =
	tween(this::alpha[1f], time = time, easing = easing) { this.visible = true }

suspend fun View.hide(time: TimeSpan, easing: Easing = Easing.LINEAR) =
	tween(this::alpha[0f], time = time, easing = easing)

suspend inline fun View.moveTo(x: Number, y: Number, time: TimeSpan, easing: Easing = Easing.LINEAR) =
	tween(this::x[x.toFloat()], this::y[y.toFloat()], time = time, easing = easing)

suspend inline fun View.moveBy(dx: Number, dy: Number, time: TimeSpan, easing: Easing = Easing.LINEAR) =
	tween(this::x[this.x + dx.toFloat()], this::y[this.y + dy.toFloat()], time = time, easing = easing)

suspend inline fun View.scaleTo(sx: Number, sy: Number, time: TimeSpan, easing: Easing = Easing.LINEAR) =
	tween(this::scaleX[sx.toFloat()], this::scaleY[sy.toFloat()], time = time, easing = easing)

suspend inline fun View.rotateTo(deg: Angle, time: TimeSpan, easing: Easing = Easing.LINEAR) =
	tween(this::rotationRadians[deg.radians], time = time, easing = easing)

suspend inline fun View.rotateBy(ddeg: Angle, time: TimeSpan, easing: Easing = Easing.LINEAR) =
	tween(this::rotationRadians[this.rotationRadians + ddeg.radians], time = time, easing = easing)

@Suppress("UNCHECKED_CAST")
data class V2<V>(
	internal val key: KMutableProperty0<V>,
	internal val initial: V,
	internal val end: V,
	internal val interpolator: (V, V, Float) -> V,
	internal val startTime: TimeSpan = 0.seconds,
	internal val duration: TimeSpan = TimeSpan.NULL
) {
	val endTime = startTime + (if (duration == TimeSpan.NULL) 0.seconds else duration)

	@Deprecated("", replaceWith = ReplaceWith("key .. (initial...end)", "com.soywiz.korge.tween.rangeTo"))
	constructor(key: KMutableProperty0<V>, initial: V, end: V) : this(key, initial, end, ::_interpolateAny)

	fun set(ratio: Float) = key.set(interpolator(initial, end, ratio))

	override fun toString(): String =
		"V2(key=${key.name}, range=[$initial-$end], startTime=$startTime, duration=$duration)"
}

@PublishedApi
internal fun <T> _interpolateAny(min: T, max: T, ratio: Float): T = ratio.interpolateAny(min, max)

@PublishedApi
internal fun _interpolateRGBAInt(src: Int, dst: Int, ratio: Float): Int = RGBA.interpolateInt(src, dst, ratio.toFloat())

@PublishedApi
internal fun _interpolate(src: Float, dst: Float, ratio: Float): Float = ratio.interpolate(src, dst)

@PublishedApi
internal fun _interpolateInt(src: Int, dst: Int, ratio: Float): Int = ratio.interpolate(src, dst)

operator fun <V> KMutableProperty0<V>.get(end: V) = V2(this, this.get(), end, ::_interpolateAny)
operator fun <V> KMutableProperty0<V>.get(initial: V, end: V) = V2(this, initial, end, ::_interpolateAny)

inline operator fun KMutableProperty0<Int>.get(end: Int) = V2(this, this.get(), end, ::_interpolateInt)
inline operator fun KMutableProperty0<Int>.get(initial: Int, end: Int) =
	V2(this, initial, end, ::_interpolateInt)

inline operator fun KMutableProperty0<Float>.get(end: Number) = V2(this, this.get(), end.toFloat(), ::_interpolate)
inline operator fun KMutableProperty0<Float>.get(initial: Number, end: Number) =
	V2(this, initial.toFloat(), end.toFloat(), ::_interpolate)

fun <V> V2<V>.withEasing(easing: Easing): V2<V> =
	this.copy(interpolator = { a, b, ratio -> this.interpolator(a, b, easing(ratio)) })

fun V2<Int>.color(): V2<Int> = this.copy(interpolator = ::_interpolateRGBAInt)

fun <V> V2<V>.easing(easing: Easing): V2<V> =
	this.copy(interpolator = { a, b, ratio -> this.interpolator(a, b, easing(ratio)) })

inline fun <V> V2<V>.delay(startTime: TimeSpan) = this.copy(startTime = startTime)
inline fun <V> V2<V>.duration(duration: TimeSpan) = this.copy(duration = duration)

inline fun <V> V2<V>.linear() = this
inline fun <V> V2<V>.easeIn() = this.withEasing(Easing.EASE_IN)
inline fun <V> V2<V>.easeOut() = this.withEasing(Easing.EASE_OUT)
inline fun <V> V2<V>.easeInOut() = this.withEasing(Easing.EASE_IN_OUT)
inline fun <V> V2<V>.easeOutIn() = this.withEasing(Easing.EASE_OUT_IN)
inline fun <V> V2<V>.easeInBack() = this.withEasing(Easing.EASE_IN_BACK)
inline fun <V> V2<V>.easeOutBack() = this.withEasing(Easing.EASE_OUT_BACK)
inline fun <V> V2<V>.easeInOutBack() = this.withEasing(Easing.EASE_IN_OUT_BACK)
inline fun <V> V2<V>.easeOutInBack() = this.withEasing(Easing.EASE_OUT_IN_BACK)

inline fun <V> V2<V>.easeInElastic() = this.withEasing(Easing.EASE_IN_ELASTIC)
inline fun <V> V2<V>.easeOutElastic() = this.withEasing(Easing.EASE_OUT_ELASTIC)
inline fun <V> V2<V>.easeInOutElastic() = this.withEasing(Easing.EASE_IN_OUT_ELASTIC)
inline fun <V> V2<V>.easeOutInElastic() = this.withEasing(Easing.EASE_OUT_IN_ELASTIC)

inline fun <V> V2<V>.easeInBounce() = this.withEasing(Easing.EASE_IN_BOUNCE)
inline fun <V> V2<V>.easeOutBounce() = this.withEasing(Easing.EASE_OUT_BOUNCE)
inline fun <V> V2<V>.easeInOutBounce() = this.withEasing(Easing.EASE_IN_OUT_BOUNCE)
inline fun <V> V2<V>.easeOutInBounce() = this.withEasing(Easing.EASE_OUT_IN_BOUNCE)

inline fun <V> V2<V>.easeInQuad() = this.withEasing(Easing.EASE_IN_QUAD)
inline fun <V> V2<V>.easeOutQuad() = this.withEasing(Easing.EASE_OUT_QUAD)
inline fun <V> V2<V>.easeInOutQuad() = this.withEasing(Easing.EASE_IN_OUT_QUAD)

inline fun <V> V2<V>.easeSine() = this.withEasing(Easing.EASE_SINE)
