package com.gabeos.wafflemusic

import java.awt.image.{DataBufferByte, BufferedImage}

import org.opencv.core.{Scalar, Size, Mat}
import org.opencv.core.CvType._

/**
 * Created by Gabriel Schubiner on 8/24/2014.
 */
package object enrichment extends RichMatDef with CVNumeric with RichShortDepth {}
