package com.soywiz.korge.view

import com.soywiz.korim.bitmap.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.geom.vector.*

inline fun Container.image(
	texture: BmpSlice, anchorX: Number = 0f, anchorY: Number = 0f, callback: @ViewsDslMarker Image.() -> Unit = {}
): Image = Image(texture, anchorX.toFloat(), anchorY.toFloat()).addTo(this).apply(callback)

inline fun Container.image(
	texture: Bitmap, anchorX: Number = 0f, anchorY: Number = 0f, callback: @ViewsDslMarker Image.() -> Unit = {}
): Image = Image(texture, anchorX.toFloat(), anchorY.toFloat()).addTo(this).apply(callback)

//typealias Sprite = Image

open class Image(
	bitmap: BmpSlice,
	anchorX: Float = 0f,
	anchorY: Float = anchorX,
	hitShape: VectorPath? = null,
	smoothing: Boolean = true
) : RectBase(anchorX, anchorY, hitShape, smoothing) {
	companion object {
	    operator fun invoke(
			bitmap: Bitmap,
			anchorX: Float = 0f,
			anchorY: Float = anchorX,
			hitShape: VectorPath? = null,
			smoothing: Boolean = true
		) = Image(bitmap.slice(), anchorX, anchorY, hitShape, smoothing)
	}

	var bitmap: BmpSlice get() = baseBitmap; set(v) = run { baseBitmap = v }
	var texture: BmpSlice get() = baseBitmap; set(v) = run { baseBitmap = v }

	init {
		this.baseBitmap = bitmap
	}

	override val bwidth: Float get() = bitmap.width.toFloat()
	override val bheight: Float get() = bitmap.height.toFloat()

	override fun createInstance(): View = Image(bitmap, anchorX, anchorY, hitShape, smoothing)

	override fun toString(): String = super.toString() + ":bitmap=$bitmap"

}

inline fun <T : Image> T.anchor(ax: Number, ay: Number): T =
	this.apply { anchorX = ax.toFloat() }.apply { anchorY = ay.toFloat() }
