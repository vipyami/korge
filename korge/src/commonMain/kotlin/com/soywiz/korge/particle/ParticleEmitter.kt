package com.soywiz.korge.particle

import com.soywiz.klock.*
import com.soywiz.korag.*
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.*
import com.soywiz.korio.file.*
import com.soywiz.korio.serialization.xml.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.random.*
import kotlin.math.*
import kotlin.random.*

//e: java.lang.UnsupportedOperationException: Class literal annotation arguments are not yet supported: Factory
//@AsyncFactoryClass(ParticleEmitter.Factory::class)
class ParticleEmitter() {
	enum class Type { GRAVITY, RADIAL }

	var texture: BmpSlice? = null
	var sourcePosition = Point()
	var sourcePositionVariance = Point()
	var speed = 0f
	var speedVariance = 0f
	var lifeSpan = 0f
	var lifespanVariance = 0f
	var angle = 0.degrees
	var angleVariance = 0.degrees
	var gravity = Point()
	var radialAcceleration = 0f
	var tangentialAcceleration = 0f
	var radialAccelVariance = 0f
	var tangentialAccelVariance = 0f
	var startColor = RGBAf(1f, 1f, 1f, 1f)
	var startColorVariance = RGBAf(0f, 0f, 0f, 0f)
	var endColor = RGBAf(1f, 1f, 1f, 1f)
	var endColorVariance = RGBAf(0f, 0f, 0f, 0f)
	var maxParticles = 0
	var startSize = 0f
	var startSizeVariance = 0f
	var endSize = 0f
	var endSizeVariance = 0f
	var duration = 0f
	var emitterType = Type.GRAVITY
	var maxRadius = 0f
	var maxRadiusVariance = 0f
	var minRadius = 0f
	var minRadiusVariance = 0f
	var rotatePerSecond = 0f
	var rotatePerSecondVariance = 0f
	var blendFactors = BlendMode.NORMAL.factors
	var rotationStart = 0.degrees
	var rotationStartVariance = 0.degrees
	var rotationEnd = 0.degrees
	var rotationEndVariance = 0.degrees

	fun create(x: Double = 0.0, y: Double = 0.0, time: TimeSpan = Int.MAX_VALUE.seconds): ParticleEmitterView =
		ParticleEmitterView(this, Point(x, y)).apply {
			this.timeUntilStop = time
		}

	suspend fun load(file: VfsFile): ParticleEmitter = this.apply {
		val particleXml = file.readXml()

		var blendFuncSource = AG.BlendFactor.ONE
		var blendFuncDestination = AG.BlendFactor.ONE

		for (item in particleXml.allChildrenNoComments) {
			fun point() = Point(item.float("x"), item.float("y"))
			fun scalar() = item.float("value")
			fun blendFactor() = when (scalar().toInt()) {
				0 -> AG.BlendFactor.ZERO
				1 -> AG.BlendFactor.ONE
				0x300 -> AG.BlendFactor.SOURCE_COLOR
				0x301 -> AG.BlendFactor.ONE_MINUS_SOURCE_COLOR
				0x302 -> AG.BlendFactor.SOURCE_ALPHA
				0x303 -> AG.BlendFactor.ONE_MINUS_SOURCE_ALPHA
				0x304 -> AG.BlendFactor.DESTINATION_ALPHA
				0x305 -> AG.BlendFactor.ONE_MINUS_DESTINATION_ALPHA
				0x306 -> AG.BlendFactor.DESTINATION_COLOR
				0x307 -> AG.BlendFactor.ONE_MINUS_DESTINATION_COLOR
				else -> AG.BlendFactor.ONE
			}

			fun angle(): Angle = Angle.fromDegrees(item.float("value"))
			fun color(): RGBAf =
				RGBAf(item.float("red"), item.float("green"), item.float("blue"), item.float("alpha"))

			when (item.name.toLowerCase()) {
				"texture" -> texture = file.parent[item.str("name")].readBitmapSlice()
				"sourceposition" -> sourcePosition = point()
				"sourcepositionvariance" -> sourcePositionVariance = point()
				"speed" -> speed = scalar()
				"speedvariance" -> speedVariance = scalar()
				"particlelifespan" -> lifeSpan = scalar()
				"particlelifespanvariance" -> lifespanVariance = scalar()
				"angle" -> angle = angle()
				"anglevariance" -> angleVariance = angle()
				"gravity" -> gravity = point()
				"radialacceleration" -> radialAcceleration = scalar()
				"tangentialacceleration" -> tangentialAcceleration = scalar()
				"radialaccelvariance" -> radialAccelVariance = scalar()
				"tangentialaccelvariance" -> tangentialAccelVariance = scalar()
				"startcolor" -> startColor = color()
				"startcolorvariance" -> startColorVariance = color()
				"finishcolor" -> endColor = color()
				"finishcolorvariance" -> endColorVariance = color()
				"maxparticles" -> maxParticles = scalar().toInt()
				"startparticlesize" -> startSize = scalar()
				"startparticlesizevariance" -> startSizeVariance = scalar()
				"finishparticlesize" -> endSize = scalar()
				"finishparticlesizevariance" -> endSizeVariance = scalar()
				"duration" -> duration = scalar()
				"emittertype" -> emitterType =
						when (scalar().toInt()) { 0 -> Type.GRAVITY
							; 1 -> Type.RADIAL
							; else -> Type.GRAVITY
							; }
				"maxradius" -> maxRadius = scalar()
				"maxradiusvariance" -> maxRadiusVariance = scalar()
				"minradius" -> minRadius = scalar()
				"minradiusvariance" -> minRadiusVariance = scalar()
				"rotatepersecond" -> rotatePerSecond = scalar()
				"rotatepersecondvariance" -> rotatePerSecondVariance = scalar()
				"blendfuncsource" -> blendFuncSource = blendFactor()
				"blendfuncdestination" -> blendFuncDestination = blendFactor()
				"rotationstart" -> rotationStart = angle()
				"rotationstartvariance" -> rotationStartVariance = angle()
				"rotationend" -> rotationEnd = angle()
				"rotationendvariance" -> rotationEndVariance = angle()
			}
		}

		blendFactors = AG.Blending(blendFuncSource, blendFuncDestination)
	}

	data class Particle(
		var x: Float = 0f,
		var y: Float = 0f,
		var scale: Float = 1f,
		var rotation: Angle = 0.radians,
		var currentTime: TimeSpan = 0.seconds,
		var totalTime: TimeSpan = 0.seconds,

		//val colorArgb: RGBAf = RGBAf(),
		//val colorArgbDelta: RGBAf = RGBAf(),

		var colorR: Float = 1f,
		var colorG: Float = 1f,
		var colorB: Float = 1f,
		var colorA: Float = 1f,

		var colorRdelta: Float = 0f,
		var colorGdelta: Float = 0f,
		var colorBdelta: Float = 0f,
		var colorAdelta: Float = 0f,

		var startX: Float = 0f,
		var startY: Float = 0f,
		var velocityX: Float = 0f,
		var velocityY: Float = 0f,
		var radialAcceleration: Float = 0f,
		var tangentialAcceleration: Float = 0f,
		var emitRadius: Float = 0f,
		var emitRadiusDelta: Float = 0f,
		var emitRotation: Float = 0f,
		var emitRotationDelta: Float = 0f,
		var rotationDelta: Angle = 0.radians,
		var scaleDelta: Float = 0f
	) {
		val colorInt: RGBA get() = RGBA(RGBA.packf(colorR, colorG, colorB, colorA))
		val alive: Boolean get() = this.currentTime < this.totalTime
	}

	class Simulator(
		private val emitter: ParticleEmitter,
		var emitterPos: Point = Point(),
		val seed: Long = Random.nextLong()
	) {
		val random = Random(seed)
		var totalElapsedTime = 0.seconds
		var timeUntilStop = Int.MAX_VALUE.seconds
		var emitting = true
		val textureWidth = emitter.texture?.width ?: 16
		val particles = (0 until emitter.maxParticles).map { init(Particle()) }
		val aliveCount: Int get() = particles.count { it.alive }
		val anyAlive: Boolean get() = aliveCount > 0

		private fun randomVariance(base: Double, variance: Double): Double {
			return base + variance * (random.nextDouble() * 2.0 - 1.0)
		}

		private fun randomVariance(base: Float, variance: Float): Float {
			return base + variance * (random.nextFloat() * 2.0f - 1.0f)
		}

		private fun randomVariance(base: Angle, variance: Angle): Angle {
			return randomVariance(base.radians, variance.radians).radians
		}

		private fun randomVariance(base: TimeSpan, variance: TimeSpan): TimeSpan {
			return randomVariance(base.seconds, variance.seconds).seconds
		}

		fun init(particle: Particle): Particle {
			val lifespan = randomVariance(emitter.lifeSpan, emitter.lifespanVariance).seconds

			particle.currentTime = 0.seconds
			particle.totalTime = max(0.0, lifespan.seconds).seconds

			val emitterX = emitterPos.x
			val emitterY = emitterPos.y

			particle.x = randomVariance(emitterX, emitter.sourcePositionVariance.x)
			particle.y = randomVariance(emitterY, emitter.sourcePositionVariance.y)
			particle.startX = emitterX
			particle.startY = emitterY

			val angle = randomVariance(emitter.angle.radians, emitter.angleVariance.radians).radians
			val speed = randomVariance(emitter.speed, emitter.speedVariance)
			particle.velocityX = speed * cos(angle.radians)
			particle.velocityY = speed * sin(angle.radians)

			val startRadius = randomVariance(emitter.maxRadius, emitter.maxRadiusVariance)
			val endRadius = randomVariance(emitter.minRadius, emitter.minRadiusVariance)
			particle.emitRadius = startRadius
			particle.emitRadiusDelta = (endRadius - startRadius) / lifespan.seconds.toFloat()
			particle.emitRotation = randomVariance(emitter.angle, emitter.angleVariance).radians
			particle.emitRotationDelta = randomVariance(emitter.rotatePerSecond, emitter.rotatePerSecondVariance)
			particle.radialAcceleration = randomVariance(emitter.radialAcceleration, emitter.radialAccelVariance)
			particle.tangentialAcceleration =
					randomVariance(emitter.tangentialAcceleration, emitter.tangentialAccelVariance)

			val startSize = max(0.1f, randomVariance(emitter.startSize, emitter.startSizeVariance))
			val endSize = max(0.1f, randomVariance(emitter.endSize, emitter.endSizeVariance))
			particle.scale = startSize / textureWidth
			particle.scaleDelta = ((endSize - startSize) / lifespan.seconds.toFloat()) / textureWidth

			particle.colorR = randomVariance(emitter.startColor.r, emitter.startColorVariance.r)
			particle.colorG = randomVariance(emitter.startColor.g, emitter.startColorVariance.g)
			particle.colorB = randomVariance(emitter.startColor.b, emitter.startColorVariance.b)
			particle.colorA = randomVariance(emitter.startColor.a, emitter.startColorVariance.a)

			val endColorR = randomVariance(emitter.endColor.r, emitter.endColorVariance.r)
			val endColorG = randomVariance(emitter.endColor.g, emitter.endColorVariance.g)
			val endColorB = randomVariance(emitter.endColor.b, emitter.endColorVariance.b)
			val endColorA = randomVariance(emitter.endColor.a, emitter.endColorVariance.a)

			particle.colorRdelta = ((endColorR - particle.colorR) / lifespan.seconds.toFloat())
			particle.colorGdelta = ((endColorG - particle.colorG) / lifespan.seconds.toFloat())
			particle.colorBdelta = ((endColorB - particle.colorB) / lifespan.seconds.toFloat())
			particle.colorAdelta = ((endColorA - particle.colorA) / lifespan.seconds.toFloat())

			val startRotation = randomVariance(emitter.rotationStart, emitter.rotationStartVariance)
			val endRotation = randomVariance(emitter.rotationEnd, emitter.rotationEndVariance)

			particle.rotation = startRotation
			particle.rotationDelta = (endRotation - startRotation) / lifespan.seconds.toFloat()

			return particle
		}

		fun advance(particle: Particle, _elapsedTime: TimeSpan) {
			val restTime = particle.totalTime - particle.currentTime
			val elapsedTimeTS = if (restTime > _elapsedTime) _elapsedTime else restTime
			val elapsedTime = elapsedTimeTS.seconds.toFloat()
			particle.currentTime += elapsedTimeTS

			when (emitter.emitterType) {
				Type.RADIAL -> {
					particle.emitRotation += particle.emitRotationDelta * elapsedTime
					particle.emitRadius += particle.emitRadiusDelta * elapsedTime
					particle.x = emitter.sourcePosition.x - cos(particle.emitRotation) * particle.emitRadius
					particle.y = emitter.sourcePosition.y - sin(particle.emitRotation) * particle.emitRadius
				}
				Type.GRAVITY -> {
					val distanceX = particle.x - particle.startX
					val distanceY = particle.y - particle.startY
					val distanceScalar = max(0.01f, sqrt(distanceX * distanceX + distanceY * distanceY))
					var radialX = distanceX / distanceScalar
					var radialY = distanceY / distanceScalar
					var tangentialX = radialX
					var tangentialY = radialY

					radialX *= particle.radialAcceleration
					radialY *= particle.radialAcceleration

					val newY = tangentialX
					tangentialX = -tangentialY * particle.tangentialAcceleration
					tangentialY = newY * particle.tangentialAcceleration

					particle.velocityX += elapsedTime * (emitter.gravity.x + radialX + tangentialX)
					particle.velocityY += elapsedTime * (emitter.gravity.y + radialY + tangentialY)
					particle.x += particle.velocityX * elapsedTime
					particle.y += particle.velocityY * elapsedTime
				}
			}

			particle.scale += particle.scaleDelta * elapsedTime
			particle.rotation += particle.rotationDelta * elapsedTime

			particle.colorR += (particle.colorRdelta * elapsedTime).toFloat()
			particle.colorG += (particle.colorGdelta * elapsedTime).toFloat()
			particle.colorB += (particle.colorBdelta * elapsedTime).toFloat()
			particle.colorA += (particle.colorAdelta * elapsedTime).toFloat()

			if (!particle.alive && emitting) init(particle)
		}

		fun simulate(time: TimeSpan) {
			totalElapsedTime += time

			if (totalElapsedTime >= timeUntilStop) {
				emitting = false
			}

			for (p in particles) advance(p, time)
		}
	}
}

suspend fun VfsFile.readParticle(): ParticleEmitter = ParticleEmitter().load(this)

suspend fun Container.attachParticleAndWait(
	particle: ParticleEmitter,
	x: Double,
	y: Double,
	time: TimeSpan = 1000.milliseconds,
	speed: Float = 1f
) {
	val p = particle.create(x, y, time)
	p.speed = speed
	this += p
	p.waitComplete()
	this -= p
}
