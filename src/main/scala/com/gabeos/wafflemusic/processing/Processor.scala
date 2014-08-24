package com.gabeos.wafflemusic.processing

import java.awt.image.{Raster, BufferedImage}

import com.gabeos.wafflemusic.data.TrustworthyImagePXCMPoint3D
import org.opencv.core.Mat
import rx._

/**
 * Created by Gabriel Schubiner on 8/8/2014.
 */
trait Processor {
  val image: Var[Mat]
  val depth: Var[Mat]
  val trackingDepths: Var[Mat]
  val trackingImage: Var[Mat]
  val samples: Var[Int]
}
