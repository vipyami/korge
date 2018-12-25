package com.soywiz.korge.animate

import com.soywiz.klock.*
import com.soywiz.kmem.*
import com.soywiz.korio.util.*
import kotlin.test.*

class TimedTest {
	// @TODO: kotlin-js kotlin.js inconsistency with double toString
	//val tostr = fun(index: Int, left: String?, right: String?, ratio: Double) = "$index,$left,$right,$ratio"
	//
	//@Test
	//fun name() {
	//	val timed = Timed<String>()
	//	timed.add(2, "a")
	//	timed.add(4, "b")
	//	timed.add(6, "c")
	//	assertEquals("0,null,a,0.0", timed.findAndHandle(-1, tostr))
	//	assertEquals("0,a,null,0.0", timed.findAndHandle(2, tostr))
	//	assertEquals("0,a,b,0.5", timed.findAndHandle(3, tostr))
	//	assertEquals("1,b,null,0.0", timed.findAndHandle(4, tostr))
	//	assertEquals("1,b,c,0.5", timed.findAndHandle(5, tostr))
	//	assertEquals("2,c,null,0.0", timed.findAndHandle(6, tostr))
	//	assertEquals("3,c,null,1.0", timed.findAndHandle(10, tostr))
	//}

	val tostr = fun(index: Int, left: String?, right: String?, ratio: Float) = "$index,$left,$right,${ratio.niceStr}"

	@Test
	fun name() {
		val timed = Timed<String>()
		timed.add(2.milliseconds, "a")
		timed.add(4.milliseconds, "b")
		timed.add(6.milliseconds, "c")
		assertEquals("0,null,a,0", timed.findAndHandle((-1).milliseconds, tostr))
		assertEquals("0,a,null,0", timed.findAndHandle(2.milliseconds, tostr))
		assertEquals("0,a,b,0.5", timed.findAndHandle(3.milliseconds, tostr))
		assertEquals("1,b,null,0", timed.findAndHandle(4.milliseconds, tostr))
		assertEquals("1,b,c,0.5", timed.findAndHandle(5.milliseconds, tostr))
		assertEquals("2,c,null,0", timed.findAndHandle(6.milliseconds, tostr))
		assertEquals("3,c,null,1", timed.findAndHandle(10.milliseconds, tostr))
	}


	@Test
	fun repeated() {
		val timed = Timed<String>()
		timed.add(1.milliseconds, "a")
		timed.add(1.milliseconds, "b")
		timed.add(1.milliseconds, "c")
		assertEquals(listOf("a", "b", "c"), timed.getRangeValues(1.milliseconds, 1.milliseconds))
	}

	@Test
	fun unsorted() {
		Timed<String>().apply {
			add(6.milliseconds, "c")
			add(4.milliseconds, "b")
			add(8.milliseconds, "d")
			add(2.milliseconds, "a")
			assertEquals(
				"[(2, a), (4, b), (6, c), (8, d)]",
				entries.toString()
			)
		}

		Timed<String>().apply {
			add(2.milliseconds, "a")
			add(4.milliseconds, "b")
			add(6.milliseconds, "c")
			add(8.milliseconds, "d")
			assertEquals(
				"[(2, a), (4, b), (6, c), (8, d)]",
				entries.toString()
			)
		}
	}
}
