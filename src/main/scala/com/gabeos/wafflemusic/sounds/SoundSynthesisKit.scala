package com.gabeos.wafflemusic.sounds

import java.awt.image.{Raster, BufferedImage}

import com.gabeos.wafflemusic.data.TrustworthyImagePXCMPoint3D
import com.gabeos.wafflemusic.processing.{Processor, BasicProcessor}
import org.opencv.core.Mat
import rx._
import rx.ops._

/**
 * Created by Gabriel Schubiner on 8/8/2014.
 */
class SoundSynthesisKit(override val image: Var[Mat],
                        override val depth: Var[Mat],
                        override val trackingDepths: Var[Mat],
                        override val trackingImage: Var[Mat],
                        override val samples: Var[Int]) {
  this: Processor =>
}
