package com.soywiz.korge.view

import com.soywiz.korge.render.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*
import com.soywiz.korim.vector.*

import com.soywiz.korma.geom.*
import com.soywiz.korma.geom.vector.*

inline fun Container.graphics(callback: Graphics.() -> Unit = {}): Graphics = Graphics().addTo(this).apply(callback)

class Graphics : Image(Bitmaps.transparent), VectorBuilder {
	private val shapes = arrayListOf<Shape>()
	private var fill: Context2d.Paint? = null
	@PublishedApi
	internal var currentPath = GraphicsPath()
	@PublishedApi
	internal var dirty = true

	inline fun dirty(callback: () -> Unit) = this.apply {
		this.dirty = true
		callback()
	}

	fun clear() {
		shapes.clear()
	}

	fun lineStyle(thickness: Double, color: RGBA, alpha: Float) = dirty {
	}

	override val lastX: Float get() = currentPath.lastX
	override val lastY: Float get() = currentPath.lastY
	override val totalPoints: Int get() = currentPath.totalPoints

	override fun close() = currentPath.close()
	override fun lineTo(x: Float, y: Float) = currentPath.lineTo(x, y)
	override fun moveTo(x: Float, y: Float) = currentPath.moveTo(x, y)
	override fun quadTo(cx: Float, cy: Float, ax: Float, ay: Float) = currentPath.quadTo(cx, cy, ax, ay)
	override fun cubicTo(cx1: Float, cy1: Float, cx2: Float, cy2: Float, ax: Float, ay: Float) = currentPath.cubicTo(cx1, cy1, cx2, cy2, ax, ay)

	// Inline Class ERROR: Platform declaration clash: The following declarations have the same JVM signature (beginFill(ID)Lcom/soywiz/korge/view/Graphics;):
	//fun beginFill(color: Int, alpha: Double) = beginFill(RGBA(color), alpha)

	inline fun fill(color: RGBA, alpha: Number = 1f, callback: () -> Unit) {
		beginFill(color, alpha.toFloat())
		try {
			callback()
		} finally {
			endFill()
		}
	}

	inline fun fill(paint: Context2d.Paint, callback: () -> Unit) {
		beginFill(paint)
		try {
			callback()
		} finally {
			endFill()
		}
	}

	fun beginFill(paint: Context2d.Paint) = dirty {
		fill = paint
		currentPath = GraphicsPath()
	}

	fun beginFill(color: RGBA, alpha: Float) = dirty {
		fill = Context2d.Color(RGBA(color.r, color.g, color.b, (alpha * 255).toInt()))
		currentPath = GraphicsPath()
	}

	inline fun shape(shape: VectorPath) = dirty { currentPath.write(shape) }

	fun endFill() = dirty {
		shapes += FillShape(currentPath, null, fill ?: Context2d.Color(Colors.RED), Matrix())
		currentPath = GraphicsPath()
	}

	internal val _sLeft get() = sLeft
	internal val _sTop get() = sTop

	override var sLeft = 0f
	override var sTop = 0f

	override fun renderInternal(ctx: RenderContext) {
		if (dirty) {
			dirty = false
			val bounds = shapes.map { it.getBounds() }.bounds()
			val image = NativeImage(bounds.width.toInt(), bounds.height.toInt())
			image.context2d {
				translate(-bounds.x, -bounds.y)
				for (shape in shapes) {
					shape.draw(this)
				}
			}
			this.bitmap = image.slice()
			sLeft = bounds.x
			sTop = bounds.y
		}
		super.renderInternal(ctx)
	}

	//override fun hitTestInternal(x: Double, y: Double): View? {
	//	val lx = globalToLocalX(x, y)
	//	val ly = globalToLocalY(x, y)
	//	for (shape in shapes) {
	//		if (shape.containsPoint(lx, ly)) return this
	//	}
	//	return null
	//}
}
