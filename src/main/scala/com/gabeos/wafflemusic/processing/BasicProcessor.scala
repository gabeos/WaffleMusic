package com.gabeos.wafflemusic.processing

import java.awt.image.BufferedImage
import java.util

import com.gabeos.wafflemusic.ImageData
import com.gabeos.wafflemusic.camera.{ImageInfo, DepthImageInfo}
import com.typesafe.scalalogging.LazyLogging
import org.opencv
import org.opencv.core.{Scalar, MatOfPoint, Mat}
import org.opencv.core.CvType._
import org.opencv.imgproc.Imgproc._
import org.opencv.core.Core
import rx._
import rx.ops._
import com.gabeos.wafflemusic.enrichment._
import com.gabeos.wafflemusic.Cfg.Processing._

import scala.util.Random

/**
 * Created by Gabriel Schubiner on 8/8/2014.
 */
class BasicProcessor(override val imgData: Var[ImageData])(implicit depthInfo: DepthImageInfo, imgInfo: ImageInfo) extends Processor with LazyLogging {

  val cannyImg = imgData map { case ImageData(img, _, _, _, _) =>
    val m = img.clone()
    cvtColor(m, m, COLOR_BGR2GRAY)
    Canny(m, m, cannyThreshold._1, cannyThreshold._2)
    m
  }

  val sobelImg = imgData map { case ImageData(img, _, _, _, _) =>
    val m = img.clone()
    cvtColor(m, m, COLOR_BGR2GRAY)
    Sobel(m, m, -1, 1, 1)
    m
  }

  val contours = cannyImg map { edges =>
    val m = edges.clone()
    val cont = new util.ArrayList[MatOfPoint]()
    val hier = new Mat()
    findContours(m, cont, hier, RETR_EXTERNAL, CHAIN_APPROX_TC89_KCOS)
    val rm = edges.clone()
    for (i <- 0 until hier.rows())
      drawContours(rm,cont,i,new Scalar(Random.nextInt(255),Random.nextInt(255),Random.nextInt(255)),-5)
    rm
  }

  val zeroedDepth = imgData map {
    case ImageData(_,depth,_,_,_) => depth.map((s: Short) => if (depthInfo.untrusted(s)) 0.toShort else s)
  }

  val normalizedDepth = zeroedDepth map { depth =>
//      val dvals = depth.fillCVTypeBuffer[Short]
      val nd = new Mat()
      Core.normalize(depth,nd,0,255,Core.NORM_MINMAX)
      println(s"NonXeroElements: ${Core.countNonZero(nd)}")
//      val nd = normalize(255.toShort,dvals,depthInfo.trusted)
//      val m = new Mat(depth.width(),depth.height(),depth.`type`())
      nd.convertTo(nd,CV_8UC1)
    nd
  }

//  val depthSegmentation = imgData map {case ImageData(_,depth,_,_,_) => }
  /* Depth */
  // Basic Stats
  //  val avgDepth = Rx {trackingDepths().foldLeft(0.0f)((sum,point) => sum + point.z) / trackingDepths().length}
  //  var runningAvg = 0.0
  //  val runnAvgUpdate = Obs(runningAvgDepth,skipInitial = true) {runningAvg += runningAvgDepth()}
  //  val runningAvgDepth = Rx {(avgDepth() + (samples() - 1) * runningAvg) / samples()}
  //TODO:

  /* Color */
  //  val avgRGB = Rx {trackingImage()}
  //
}
