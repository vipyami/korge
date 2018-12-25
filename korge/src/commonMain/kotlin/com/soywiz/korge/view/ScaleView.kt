package com.soywiz.korge.view

import com.soywiz.korge.render.*

inline fun Container.scaleView(
	width: Number, height: Number, scale: Number = 2f, filtering: Boolean = false,
	callback: @ViewsDslMarker Container.() -> Unit = {}
) = ScaleView(width.toFloat(), height.toFloat(), scale.toFloat(), filtering).addTo(this).apply(callback)

class ScaleView(width: Float, height: Float, scale: Float = 2f, var filtering: Boolean = false) :
	FixedSizeContainer(width, height), View.Reference {
	init {
		this.scale = scale
	}

	//val once = Once()

	override fun renderInternal(ctx: RenderContext) {
		val iwidth = width.toInt()
		val iheight = height.toInt()

		ctx.renderToTexture(iwidth, iheight, render = {
			super.renderInternal(ctx)
		}, use = { renderTexture ->
			ctx.batch.drawQuad(
				tex = renderTexture,
				x = 0f, y = 0f,
				width = iwidth.toFloat(),
				height = iheight.toFloat(),
				m = globalMatrix,
				colorMulInt = renderColorMulInt,
				colorAdd = renderColorAdd,
				filtering = filtering,
				blendFactors = renderBlendMode.factors
			)
			ctx.flush()
		})
	}
}
