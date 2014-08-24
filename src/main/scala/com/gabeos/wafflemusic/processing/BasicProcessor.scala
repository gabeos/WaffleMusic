package com.gabeos.wafflemusic.processing

import java.awt.image.BufferedImage

import com.gabeos.wafflemusic.data.TrustworthyImagePXCMPoint3D
import rx._

/**
 * Created by Gabriel Schubiner on 8/8/2014.
 */
trait BasicProcessor extends Processor {

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
