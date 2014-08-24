package com.gabeos.wafflemusic.sounds

import java.awt.image.{Raster, BufferedImage}

import com.gabeos.wafflemusic.data.TrustworthyImagePXCMPoint3D
import com.gabeos.wafflemusic.processing.BasicProcessor
import org.opencv.core.Mat
import rx._

/**
 * Created by Gabriel Schubiner on 8/8/2014.
 */
class BasicSoundKit(override val image: Var[Mat],
                    override val depth: Var[Mat],
                    override val trackingDepths: Var[Mat],
                    override val trackingImage: Var[Mat],
                    override val samples: Var[Int]) extends SoundSynthesisKit(image,depth,trackingDepths,trackingImage,samples) {
  this: BasicProcessor =>

}
