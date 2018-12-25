package com.soywiz.korge.view

import com.soywiz.korim.color.*

inline fun Container.solidRect(
	width: Number, height: Number, color: RGBA, callback: @ViewsDslMarker SolidRect.() -> Unit = {}
) = SolidRect(width.toFloat(), height.toFloat(), color).addTo(this).apply(callback)

class SolidRect(width: Float, height: Float, color: Int) : RectBase() {
	companion object {
		inline operator fun invoke(width: Number, height: Number, color: RGBA) =
			SolidRect(width.toFloat(), height.toFloat(), color.rgba)
	}

	override var width: Float = width; set(v) = run { field = v }.also { dirtyVertices = true }
	override var height: Float = height; set(v) = run { field = v }.also { dirtyVertices = true }

	init {
		this.colorMulInt = color
	}

	override fun createInstance(): View = SolidRect(width, height, colorMulInt)
}
