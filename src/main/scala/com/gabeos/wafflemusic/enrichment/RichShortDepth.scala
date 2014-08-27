package com.gabeos.wafflemusic.enrichment

import com.gabeos.wafflemusic.camera.DepthImageInfo

/**
 * Created by Gabriel Schubiner on 8/26/2014.
 */
trait RichShortDepth {
  implicit class RichShort(s: Short) {
    def untrusted(implicit depthInfo: DepthImageInfo): Boolean = depthInfo.untrusted(s)
  }
}
