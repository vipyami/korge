package com.soywiz.korge.ui

import com.soywiz.korge.view.*

abstract class Widget(val factory: UIFactory, val skin: UISkin = factory.skin) : FixedSizeContainer() {
	override var width: Float = 100f
		set(value) {
			field = value
			updateSize()
		}
	override var height: Float = 32f
		set(value) {
			field = value
			updateSize()
		}

	protected open fun updateSize() {
	}
}
