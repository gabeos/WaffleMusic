package com.gabeos

import com.gabeos.wafflemusic.camera.{ImageInfo, DepthImageInfo}
import org.opencv.core.CvType._
import org.opencv.core.Mat
import com.gabeos.wafflemusic.enrichment._

/**
 * Created by Gabriel Schubiner on 8/26/2014.
 */
package object wafflemusic {

  case class ImageData(image: Mat, depth: Mat, imgDepth: Mat, uvMap: Mat, var samples: Int) {
    def increment() = samples += 1

    def projectDepthToImageCoord(row: Int, col: Int) = {
      require(row > 0 && row <= depth.height(), "Row dimension out of range")
      require(col > 0 && col <= depth.width(), "Col dimension out of range")
      val uvTransform = uvMap.tget[Float](row, col)
      ((uvTransform(0) * image.width() + 0.5f).toInt,
        (uvTransform(1) * image.height() + 0.5f).toInt)
    }
  }

  object ImageData {
    def apply()(implicit depthInfo: DepthImageInfo, imgInfo: ImageInfo): ImageData = {
      ImageData(new Mat(imgInfo.width, imgInfo.height, imgInfo.cvType),
                new Mat(depthInfo.width, depthInfo.height, depthInfo.cvType),
                new Mat(imgInfo.width, imgInfo.height, depthInfo.cvType),
                new Mat(depthInfo.width, depthInfo.height, depthInfo.uvCVType), 0)
    }
  }

}
