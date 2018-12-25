package com.soywiz.korge.view

import com.soywiz.klock.*

interface Updatable {
	fun update(time: TimeSpan): Unit
}
