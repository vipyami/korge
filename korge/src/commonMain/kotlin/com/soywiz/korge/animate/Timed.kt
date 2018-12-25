package com.soywiz.korge.animate

import com.soywiz.kds.*
import com.soywiz.klock.*
import com.soywiz.kmem.*
import kotlin.math.*

open class Timed<T>(initialCapacity: Int = 7) {
	val times = IntArrayList(initialCapacity)
	val objects = ArrayList<T>(initialCapacity)
	val size: Int get() = times.size

	val entries: List<Pair<Int, T>> get() = times.zip(objects)

	@PublishedApi internal val Int.timespan get() = this.milliseconds
	@PublishedApi internal val TimeSpan.intValue get() = this.millisecondsInt

	fun add(time: TimeSpan, obj: T) {
		val time = time.intValue
		times.add(time)
		objects.add(obj)

		// Insert in order
		var m = times.size - 2
		while (m >= 0 && time < times[m]) {
			swap(m, m + 1)
			m--
		}
	}

	private fun swap(a: Int, b: Int) {
		val tempTime = this.times[b]
		val tempObject = this.objects[b]
		this.times[b] = this.times[a]
		this.objects[b] = this.objects[a]
		this.times[a] = tempTime
		this.objects[a] = tempObject
	}

	fun findNearIndex(time: TimeSpan): Int {
		val time = time.intValue
		val res = times.binarySearch(time).raw
		return if (res < 0) (-res - 1).clamp(0, times.size - 1) else res
	}

	data class RangeResult(var startIndex: Int = 0, var endIndex: Int = 0)

	fun getRangeValues(startTime: TimeSpan, endTime: TimeSpan, out: ArrayList<T> = arrayListOf()): List<T> {
		val range = getRangeIndices(startTime, endTime)
		for (n in range.startIndex..range.endIndex) {
			out += objects[n]
		}
		return out
	}

	fun getRangeIndices(startTime: TimeSpan, endTime: TimeSpan, out: RangeResult = RangeResult()): RangeResult {
		val startIndex = (findNearIndex(startTime) - 1).clamp(0, size - 1)
		val endIndex = (findNearIndex(endTime) + 1).clamp(0, size - 1)
		var min = Int.MAX_VALUE
		var max = Int.MIN_VALUE
		for (n in startIndex..endIndex) {
			val time = times[n]
			//if (time in (startTime + 1)..endTime) {
			if (time.timespan in startTime..endTime) {
				min = min(min, n)
				max = max(max, n)
			}
		}
		out.startIndex = min
		out.endIndex = max
		return out
	}

	inline fun forEachInRange(
		startTime: TimeSpan,
		endTime: TimeSpan,
		maxCalls: Int = Int.MAX_VALUE,
		callback: (index: Int, time: Int, left: T) -> Unit
	) {
		val startIndex = (findNearIndex(startTime) - 1).clamp(0, size - 1)
		val endIndex = (findNearIndex(endTime) + 1).clamp(0, size - 1)
		var totalCalls = 0
		for (n in startIndex..endIndex) {
			val time = times[n]
			val obj = objects[n]
			if (time in (startTime.intValue + 1)..endTime.intValue) {
				callback(n, time, obj)
				totalCalls++
				if (totalCalls >= maxCalls) break
			}
		}
	}

	data class Result<T>(
		var index: Int = 0,
		var left: T? = null,
		var right: T? = null,
		var ratio: Float = 0f
	)

	fun find(time: TimeSpan, out: Result<T> = Result()): Result<T> {
		return findAndHandle(time) { index, left, right, ratio ->
			out.index = index
			out.left = left
			out.right = right
			out.ratio = ratio
			out
		}
	}

	inline fun <TR> findAndHandle(time: TimeSpan, callback: (index: Int, left: T?, right: T?, ratio: Float) -> TR): TR {
		if (objects.isEmpty()) return callback(0, null, null, 0f)
		val index = findNearIndex(time)
		val timeAtIndex = times[index].timespan

		if (time < timeAtIndex && index <= 0) {
			return callback(0, null, objects[0], 0f)
		} else {
			val idx = if (time < timeAtIndex) index - 1 else index
			val curTimeAtIndex = times[idx + 0].timespan
			if (curTimeAtIndex == time) {
				return callback(idx, objects[idx], null, 0f)
			} else {
				if (idx >= times.size - 1) {
					return callback(objects.size, objects[objects.size - 1], null, 1f)
				} else {
					val nextTimeAtIndex = times[idx + 1].timespan
					val elapsedTime = (time - curTimeAtIndex)
					val totalTime = (nextTimeAtIndex - curTimeAtIndex)
					return callback(idx, objects[idx], objects[idx + 1], (elapsedTime.milliseconds / totalTime.milliseconds).toFloat())
				}
			}
		}
	}

	fun findWithoutInterpolation(time: TimeSpan, out: Result<T> = Result()): Result<T> {
		return findAndHandleWithoutInterpolation(time) { index, left ->
			out.index = index
			out.left = left
			out.right = null
			out.ratio = 0f
			out
		}
	}

	inline fun <TR> findAndHandleWithoutInterpolation(time: TimeSpan, callback: (index: Int, left: T?) -> TR): TR {
		if (objects.isEmpty()) return callback(0, null)
		val index = findNearIndex(time)
		val timeAtIndex = times[index].timespan
		return if (time < timeAtIndex && index <= 0) {
			callback(0, null)
		} else {
			val idx = if (time < timeAtIndex) index - 1 else index
			val curTimeAtIndex = times[idx + 0].timespan
			when {
				curTimeAtIndex == time -> callback(idx, objects[idx])
				idx >= times.size - 1 -> callback(objects.size, objects[objects.size - 1])
				else -> callback(idx, objects[idx])
			}
		}
	}

	override fun toString(): String = "Timed($entries)"
}
