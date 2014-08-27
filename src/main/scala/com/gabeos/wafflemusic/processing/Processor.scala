package com.gabeos.wafflemusic.processing

import com.gabeos.wafflemusic.ImageData
import com.gabeos.wafflemusic.enrichment.CVDataType
import com.typesafe.scalalogging.LazyLogging
import rx._

import scala.reflect.ClassTag

/**
 * Created by Gabriel Schubiner on 8/8/2014.
 */
trait Processor extends LazyLogging {
  val imgData: Var[ImageData]

  def squash[T](v: Array[T], filter: T => Boolean = (_: T) => true)(implicit cvd: CVDataType[T], tag: ClassTag[T]): Array[T] = {
    import cvd._
    val vd = v.filter(filter).map(_.dbl)
    if (vd.isEmpty) return Array.fill(v.size)(zero)
    val avg = vd.reduce(_ + _) / vd.length
    val stdDev = math.sqrt((zero.dbl /: vd) {
                                              (a, e) => a + math.pow(e - avg, 2.0)
                                            } / vd.length)
    logger.info(s"AVG: $avg")
    logger.info(s"stdDev: $stdDev")
    val r = v.map(t => if (filter(t)) one.dbl / (one.dbl + math.exp(-((t.dbl - avg) / stdDev))) else zero.dbl).map(typed)
    logger.info(s"VALS: ${r.toList}")
    r
  }

  def normalize[T](max: T, v: Array[T], filter: T => Boolean = (_: T) => true)(implicit cvd: CVDataType[T], tag: ClassTag[T]): Array[T] = {
    import cvd._
    squash(v,filter).map(max * _)
  }
}
