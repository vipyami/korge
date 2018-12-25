package com.codeazur.as3swf

import com.soywiz.korim.vector.*
import com.soywiz.korio.lang.*

import com.soywiz.korma.geom.*

open class ShapeExporter {
	open fun beginShape() = Unit
	open fun endShape() = Unit

	open fun beginFills() = Unit
	open fun beginFill(color: Int, alpha: Float = 1f) = Unit
	open fun beginGradientFill(
		type: GradientType,
		colors: List<Int>,
		alphas: List<Float>,
		ratios: List<Int>,
		matrix: Matrix = Matrix(),
		spreadMethod: GradientSpreadMode = GradientSpreadMode.PAD,
		interpolationMethod: GradientInterpolationMode = GradientInterpolationMode.NORMAL,
		focalPointRatio: Float = 0f
	) = Unit

	open fun beginBitmapFill(
		bitmapId: Int,
		matrix: Matrix = Matrix(),
		repeat: Boolean = true,
		smooth: Boolean = false
	) = Unit

	open fun endFill() = Unit
	open fun endFills() = Unit

	open fun beginLines() = Unit
	open fun lineStyle(
		thickness: Float = Float.NaN,
		color: Int = 0,
		alpha: Float = 1f,
		pixelHinting: Boolean = false,
		scaleMode: Context2d.ScaleMode = Context2d.ScaleMode.NORMAL,
		startCaps: LineCapsStyle = LineCapsStyle.ROUND,
		endCaps: LineCapsStyle = LineCapsStyle.ROUND,
		joints: String? = null,
		miterLimit: Float = 3f
	) = Unit

	open fun lineGradientStyle(
		type: GradientType,
		colors: List<Int>,
		alphas: List<Float>,
		ratios: List<Int>,
		matrix: Matrix = Matrix(),
		spreadMethod: GradientSpreadMode = GradientSpreadMode.PAD,
		interpolationMethod: GradientInterpolationMode = GradientInterpolationMode.NORMAL,
		focalPointRatio: Float = 0f
	) = Unit

	open fun endLines() = Unit

	open fun moveTo(x: Float, y: Float) = Unit
	open fun lineTo(x: Float, y: Float) = Unit
	open fun curveTo(controlX: Float, controlY: Float, anchorX: Float, anchorY: Float) = Unit
	open fun closePath() = Unit
}

open class LoggerShapeExporter(val parent: ShapeExporter, val logger: (String) -> Unit = ::println) : ShapeExporter() {
	fun log(msg: String): LoggerShapeExporter = this.apply { logger(msg) }

	override fun beginShape() = log("beginShape()").parent.beginShape()
	override fun endShape() = log("endShape()").parent.endShape()
	override fun beginFills() = log("beginFills()").parent.beginFills()
	override fun endFills() = log("endFills()").parent.endFills()
	override fun beginLines() = log("beginLines()").parent.beginLines()
	override fun endLines() = log("endLines()").parent.endLines()
	override fun closePath() = log("closePath()").parent.closePath()

	override fun beginFill(color: Int, alpha: Float) =
		log("beginFill(${"%06X".format(color)}, $alpha)").parent.beginFill(color, alpha)

	override fun beginGradientFill(
		type: GradientType,
		colors: List<Int>,
		alphas: List<Float>,
		ratios: List<Int>,
		matrix: Matrix,
		spreadMethod: GradientSpreadMode,
		interpolationMethod: GradientInterpolationMode,
		focalPointRatio: Float
	) {
		log("beginGradientFill($type, $colors, $alphas, $ratios, $matrix, $spreadMethod, $interpolationMethod, $focalPointRatio)").parent.beginGradientFill(
			type,
			colors,
			alphas,
			ratios,
			matrix,
			spreadMethod,
			interpolationMethod,
			focalPointRatio
		)
	}

	override fun beginBitmapFill(bitmapId: Int, matrix: Matrix, repeat: Boolean, smooth: Boolean) {
		log("beginBitmapFill($bitmapId, $matrix, $repeat, $smooth)").parent.beginBitmapFill(
			bitmapId,
			matrix,
			repeat,
			smooth
		)
	}

	override fun endFill() = log("endFill()").parent.endFill()
	override fun lineStyle(
		thickness: Float,
		color: Int,
		alpha: Float,
		pixelHinting: Boolean,
		scaleMode: Context2d.ScaleMode,
		startCaps: LineCapsStyle,
		endCaps: LineCapsStyle,
		joints: String?,
		miterLimit: Float
	) {
		log("lineStyle($thickness, $color, $alpha, $pixelHinting, $scaleMode, $startCaps, $endCaps, $joints, $miterLimit)").parent.lineStyle(
			thickness,
			color,
			alpha,
			pixelHinting,
			scaleMode,
			startCaps,
			endCaps,
			joints,
			miterLimit
		)
	}

	override fun lineGradientStyle(
		type: GradientType,
		colors: List<Int>,
		alphas: List<Float>,
		ratios: List<Int>,
		matrix: Matrix,
		spreadMethod: GradientSpreadMode,
		interpolationMethod: GradientInterpolationMode,
		focalPointRatio: Float
	) {
		log("lineGradientStyle($type, $colors, $alphas, $ratios, $matrix, $spreadMethod, $interpolationMethod, $focalPointRatio)").parent.lineGradientStyle(
			type,
			colors,
			alphas,
			ratios,
			matrix,
			spreadMethod,
			interpolationMethod,
			focalPointRatio
		)
	}

	override fun moveTo(x: Float, y: Float) = log("moveTo($x, $y)").parent.moveTo(x, y)
	override fun lineTo(x: Float, y: Float) = log("lineTo($x, $y)").parent.lineTo(x, y)
	override fun curveTo(controlX: Float, controlY: Float, anchorX: Float, anchorY: Float) =
		log("curveTo($controlX, $controlY, $anchorX, $anchorY)").parent.curveTo(controlX, controlY, anchorX, anchorY)
}

class ShapeExporterBoundsBuilder : ShapeExporter() {
	val bb = BoundsBuilder()

	var lineWidth = 1f

	override fun lineStyle(
		thickness: Float,
		color: Int,
		alpha: Float,
		pixelHinting: Boolean,
		scaleMode: Context2d.ScaleMode,
		startCaps: LineCapsStyle,
		endCaps: LineCapsStyle,
		joints: String?,
		miterLimit: Float
	) {
		lineWidth = thickness
	}

	override fun beginFills() {
		lineWidth = 0f
	}

	override fun beginLines() {
		lineWidth = 1f
	}

	private fun addPoint(x: Float, y: Float) {
		bb.add(x - lineWidth, y - lineWidth)
		bb.add(x + lineWidth, y + lineWidth)
	}

	private fun addRect(rect: Rectangle) {
		addPoint(rect.left, rect.top)
		addPoint(rect.right, rect.bottom)
	}

	var lastX = 0f
	var lastY = 0f

	override fun moveTo(x: Float, y: Float) {
		addPoint(x, y)
		lastX = x
		lastY = y
	}

	override fun lineTo(x: Float, y: Float) {
		addPoint(x, y)
		lastX = x
		lastY = y
	}

	private val tempRect = Rectangle()
	override fun curveTo(controlX: Float, controlY: Float, anchorX: Float, anchorY: Float) {
		//addRect(Bezier.quadBounds(lastX, lastY, controlX, controlY, anchorX, anchorY, tempRect))
		addPoint(controlX, controlY)
		addPoint(anchorX, anchorY)
		lastX = anchorX
		lastY = anchorY
	}

	override fun closePath() {
	}
}
