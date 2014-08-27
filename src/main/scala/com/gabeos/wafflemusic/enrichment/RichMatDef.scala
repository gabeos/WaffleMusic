package com.gabeos.wafflemusic.enrichment

import java.awt.image.{DataBufferInt, DataBufferUShort, BufferedImage, DataBufferByte}

import com.gabeos.wafflemusic.camera.DepthImageInfo
import org.opencv.core.{Mat, Size}
import org.opencv.core.CvType._

/**
 * Created by Gabriel Schubiner on 8/26/2014.
 */
trait RichMatDef {
  implicit def fillMat[T:CVDataType](mat: Mat, buff: Array[T]) = {
    import CVDataType._
    implicitly[CVDataType[T]] match {
      case CV_Int => mat.put(0,0,buff.asInstanceOf[Array[Int]])
      case CV_Short => mat.put(0,0,buff.asInstanceOf[Array[Short]])
      case CV_Float => mat.put(0,0,buff.asInstanceOf[Array[Float]])
      case CV_Byte => mat.put(0,0,buff.asInstanceOf[Array[Byte]])
      case _ => throw new UnsupportedOperationException
    }
  }

  implicit class RichMat(m: Mat) {

    import CVDataType._

    val numChannels = m.channels()
    val dataDepth = m.depth()
    val size = m.channels() * m.total().toInt


    def getBufferedImageType(): Int = {
      m.`type`() match {
        case CV_8UC3 => BufferedImage.TYPE_3BYTE_BGR
        case CV_8UC1 => BufferedImage.TYPE_BYTE_GRAY
        case CV_16SC1 => BufferedImage.TYPE_USHORT_GRAY
        case _ => BufferedImage.TYPE_INT_BGR
      }
    }

    def fillCVBuffer = {
      m.depth() match {
        case CV_8U => fillCVTypeBuffer[Byte]
        case CV_16S => fillCVTypeBuffer[Short]
        case CV_32F => fillCVTypeBuffer[Float]
        case CV_64F => fillCVTypeBuffer[Double]
      }
    }

    def fillCVTypeBuffer[T:CVDataType]: Array[T] =
      implicitly[CVDataType[T]] match {
        case CV_Double =>
          val b = Array.ofDim[Double](size)
          m.get(0,0,b); b.asInstanceOf[Array[T]]
        case CV_Short =>
          val b = Array.ofDim[Short](size)
          m.get(0,0,b); b.asInstanceOf[Array[T]]
        case CV_Int =>
          val b = Array.ofDim[Int](size)
          m.get(0,0,b); b.asInstanceOf[Array[T]]
        case CV_Float =>
          val b = Array.ofDim[Float](size)
          m.get(0,0,b); b.asInstanceOf[Array[T]]
        case CV_Byte =>
          val b = Array.ofDim[Byte](size)
          m.get(0,0,b); b.asInstanceOf[Array[T]]
        case _ => throw new UnsupportedOperationException
      }

    def fillNewMat[T:CVDataType](buff: Array[T]) = {
      val mat = new Mat(m.size(),m.`type`())
      implicitly[CVDataType[T]] match {
        case CV_Int => mat.put(0,0,buff.asInstanceOf[Array[Int]])
        case CV_Short => mat.put(0,0,buff.asInstanceOf[Array[Short]])
        case CV_Float => mat.put(0,0,buff.asInstanceOf[Array[Float]])
        case CV_Byte => mat.put(0,0,buff.asInstanceOf[Array[Byte]])
        case _ => throw new UnsupportedOperationException
      }
      mat
    }

    def tget[T:CVDataType](row: Int, col: Int): Array[T] = m.get(row,col).asInstanceOf[Array[T]]

    def map[E: CVDataType](fn: E => E): Mat = {
      val size = (m.total() * m.channels()).toInt
      val buff = fillCVTypeBuffer[E]
      var i = 0
      while (i < size) {
        buff(i) = fn(buff(i))
        i += 1
      }
      fillNewMat[E](buff)
    }

    def colorDepthCoords(depthMapImgCoord: Mat)(implicit depthInfo: DepthImageInfo): Unit = {
      val imbuff = fillCVTypeBuffer[Byte]

      val depthbuff = depthMapImgCoord.fillCVTypeBuffer[Short]

      val filteredDepth = depthbuff.filter(s => depthInfo.trusted(s)).map(_.toDouble)
      val avgDepth = filteredDepth.reduce(_ + _) / filteredDepth.length
      val stdDev = math.sqrt((0.0 /: filteredDepth) {
                                                      (a,e) => a + math.pow(e - avgDepth, 2.0)
                                                    } / filteredDepth.size)
      def normalize(d: Short) = 1 / (1 + math.exp(-((d.toDouble - avgDepth)/stdDev)))

      var i = 0
      while (i < size) {
        val depth = depthbuff(i / m.channels())
        if (depthInfo.trusted(depth)) {
          val scaledDepth: Double = 255 - 255 * normalize(depth)
          imbuff(i) = 0.toByte
          imbuff(i + 1) = scaledDepth.toByte
          imbuff(i + 2) = 0.toByte
        }
        i += m.channels()
      }
      m.put(0,0,imbuff)
    }

    def depth2ImageCoord(uvMap: Mat, imgSize: Size, destMat: Mat): Unit = {
      require(m.`type`() == CV_16SC1, "Depth Mat has wrong type to convert")
      require(uvMap.`type`() == CV_32FC2, "UV Mat has wrong type to convert")
      // copy depth data
      val dsize = (m.total() * m.channels()).toInt
      val dbuff = Array.ofDim[Short](dsize)
      m.get(0,0,dbuff)

      // copy uv data
      val uvSize = (uvMap.total() * uvMap.channels()).toInt
      val uvbuff = Array.ofDim[Float](uvSize)
      uvMap.get(0,0,uvbuff)

      // init image coordinate depth data
      val imbuff = Array.fill[Short](imgSize.area().toInt)(-1.toShort)

      var i = 0
      while (i < uvSize) {
        val xx = (uvbuff(i) * imgSize.width + 0.5f).toInt
        val yy = (uvbuff(i+1) * imgSize.height + 0.5f).toInt
        if (xx >= 0 && xx < imgSize.width && yy >= 0 && yy < imgSize.height)
          imbuff(yy * imgSize.width.toInt + xx) = dbuff(i/2)
        i += 2
      }

      destMat.put(0,0,imbuff)
    }

    private def getBufferedImageDataBuffer(image: BufferedImage) = {
      getBufferedImageType() match {
        case BufferedImage.TYPE_3BYTE_BGR | BufferedImage.TYPE_BYTE_GRAY => image.getRaster.getDataBuffer.asInstanceOf[DataBufferByte].getData
        case BufferedImage.TYPE_USHORT_GRAY => image.getRaster.getDataBuffer.asInstanceOf[DataBufferUShort].getData
        case BufferedImage.TYPE_INT_BGR => image.getRaster.getDataBuffer.asInstanceOf[DataBufferInt].getData
      }
    }

    def toBufferedImage: BufferedImage = {
      val buff = fillCVBuffer
      val image = new BufferedImage(m.cols(), m.rows(), getBufferedImageType())
      val targetPixels = getBufferedImageDataBuffer(image)
      System.arraycopy(buff, 0, targetPixels, 0, buff.length)
      image
    }
  }
}
