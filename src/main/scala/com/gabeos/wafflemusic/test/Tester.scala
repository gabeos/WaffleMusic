package com.gabeos.wafflemusic.test

import org.opencv.core.{Scalar, Size, Mat, Core}
import org.opencv.core.CvType._

/**
 * Created by Gabriel Schubiner on 8/25/2014.
 */
object Tester extends App {

  System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

  val m = new Mat(new Size(5,5),CV_16SC1,new Scalar(-1))
  val sa = Array.ofDim[Short](1)
  println(s"channels: ${m.channels()}")
  println(s"val(0,0): ${m.get(0,0,sa)} => ${sa.toList}")
}
