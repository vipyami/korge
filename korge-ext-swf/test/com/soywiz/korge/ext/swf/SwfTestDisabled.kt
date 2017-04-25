package com.soywiz.korge.ext.swf

import com.soywiz.korge.Korge
import com.soywiz.korge.animate.AnMovieClip
import com.soywiz.korge.animate.serialization.AnLibrarySerializer
import com.soywiz.korge.animate.serialization.readAni
import com.soywiz.korge.animate.serialization.writeTo
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.Container
import com.soywiz.korim.vector.Context2d
import com.soywiz.korio.async.sleep
import com.soywiz.korio.vfs.LocalVfs

class SwfTestDisabled {
	companion object {
		@JvmStatic fun main(args: Array<String>) = Korge(Module(), sceneClass = MyScene::class.java)
	}

	class MyScene : Scene() {
		suspend override fun sceneInit(sceneView: Container) {
			//sleep(12000)
			//val ani = LocalVfs("c:/temp/test2.swf").readSWF(views, mipmaps = true).createMainTimeLine().apply { play("frame172") }
			//val ani = LocalVfs("c:/temp/test3.swf").readSWF(views, mipmaps = true).createMainTimeLine()
			//val ani = LocalVfs("c:/temp/test27.swf").readSWF(views, mipmaps = true).createMainTimeLine()
			//val ani = LocalVfs("c:/temp/morph.ani").readAni(views, mipmaps = true).createMainTimeLine()
			val ani = LocalVfs("c:/temp/test6.swf").readSWF(views, mipmaps = true).createMainTimeLine()
			//val ani = LocalVfs("c:/temp/test9.swf").readSWF(views, mipmaps = true).createMainTimeLine()


			//val library = LocalVfs("c:/temp/test7.swf").readSWF(views)
			//val library = LocalVfs("c:/temp/test1.swf").readSWF(views)
			//library.writeTo(LocalVfs("c:/temp/test6.ani"))
			//library.writeTo(LocalVfs("c:/temp/test2.ani"))
			//println(ani.stateNames)
			//ani.play("frame172")
			sceneView += ani
		}
	}
}
