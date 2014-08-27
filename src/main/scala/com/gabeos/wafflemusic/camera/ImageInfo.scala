package com.gabeos.wafflemusic.camera

import com.typesafe.scalalogging.LazyLogging
import org.opencv.highgui.VideoCapture
import org.opencv.videoio.Videoio._
import org.opencv.core.CvType

/**
 * Created by Gabriel Schubiner on 8/25/2014.
 */
case class ImageInfo(width: Int, height: Int, brightness: Double, contrast: Double, saturation: Double,
                     hue: Double, gamma: Double, sharpness: Double, gain: Double, backlight: Double, cvType: Int) extends LazyLogging {
  def log() = {
    val names = Seq("Width", "Height", "Brightness", "Contrast", "Saturation",
                    "Hue", "Gamma", "Sharpness", "Gain", "Backlight")
    val align = 30
    logger.info(s"\nImage Info:\n\t${names.zip(this.productIterator.toIterable).
      map(sa => sa._1 + ":" + " " * (align - sa._1.length) + sa._2).mkString("\n\t")}")
  }
}

object ImageInfo {
  def apply(capture: VideoCapture): ImageInfo = {
    apply(capture.get(CAP_INTELPERC_IMAGE_GENERATOR | CAP_PROP_FRAME_WIDTH).toInt,
          capture.get(CAP_INTELPERC_IMAGE_GENERATOR | CAP_PROP_FRAME_HEIGHT).toInt,
          capture.get(CAP_INTELPERC_IMAGE_GENERATOR | CAP_PROP_BRIGHTNESS),
          capture.get(CAP_INTELPERC_IMAGE_GENERATOR | CAP_PROP_CONTRAST),
          capture.get(CAP_INTELPERC_IMAGE_GENERATOR | CAP_PROP_SATURATION),
          capture.get(CAP_INTELPERC_IMAGE_GENERATOR | CAP_PROP_HUE),
          capture.get(CAP_INTELPERC_IMAGE_GENERATOR | CAP_PROP_GAMMA),
          capture.get(CAP_INTELPERC_IMAGE_GENERATOR | CAP_PROP_SHARPNESS),
          capture.get(CAP_INTELPERC_IMAGE_GENERATOR | CAP_PROP_GAIN),
          capture.get(CAP_INTELPERC_IMAGE_GENERATOR | CAP_PROP_BACKLIGHT),
          CvType.CV_8UC3)
  }
}