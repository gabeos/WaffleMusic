package com.gabeos.wafflemusic.camera

import com.typesafe.scalalogging.LazyLogging
import org.opencv.highgui.VideoCapture
import org.opencv.videoio.Videoio._
import org.opencv.core.CvType

/**
 * Created by Gabriel Schubiner on 8/25/2014.
 */
case class DepthImageInfo(width: Int, height: Int, lowConfidence: Short,
                          saturation: Short, confidenceThreshold: Short,
                          cvType: Int, uvCVType: Int) extends LazyLogging {

  def untrusted(value: Short) = value == saturation || value == lowConfidence || value < 0
  def trusted(value: Short) = !untrusted(value)
  def log() = {
    val align = 30
    val names = Seq("Width","Height","Low Confidence","Saturation","Confidence Threshold")
    logger.info(s"\nDepth Image Info\n\t${names.zip(productIterator.toIterable).
      map(sa => sa._1 + ":" + " " * (align - sa._1.length) + sa._2).mkString("\n\t")}")
  }
}

object DepthImageInfo {
  def apply(capture: VideoCapture): DepthImageInfo = {
    DepthImageInfo(capture.get(CAP_INTELPERC_DEPTH_GENERATOR | CAP_PROP_FRAME_WIDTH).toInt,
                   capture.get(CAP_INTELPERC_DEPTH_GENERATOR | CAP_PROP_FRAME_HEIGHT).toInt,
                   capture.get(CAP_INTELPERC_DEPTH_GENERATOR | CAP_PROP_INTELPERC_DEPTH_LOW_CONFIDENCE_VALUE).toShort,
                   capture.get(CAP_INTELPERC_DEPTH_GENERATOR | CAP_PROP_INTELPERC_DEPTH_SATURATION_VALUE).toShort,
                   capture.get(CAP_INTELPERC_DEPTH_GENERATOR | CAP_PROP_INTELPERC_DEPTH_CONFIDENCE_THRESHOLD).toShort,
                   CvType.CV_16SC1, CvType.CV_32FC2)
  }
}