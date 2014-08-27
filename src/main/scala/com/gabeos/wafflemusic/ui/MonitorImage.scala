package com.gabeos.wafflemusic.ui

import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.awt.{Color, Graphics, Graphics2D, Point}
import javax.swing.JPanel
import javax.swing.event.MouseInputAdapter

import com.gabeos.wafflemusic.ImageData
import com.gabeos.wafflemusic.camera.{ImageInfo, DepthImageInfo}
import com.gabeos.wafflemusic.enrichment._
import org.opencv.core.Mat
import org.opencv.core.CvType._
import org.opencv.imgproc.Imgproc._
import rx._
import rx.ops._

/**
 * Created by Gabriel Schubiner on 8/25/2014.
 */
class MonitorImage(dataVar: Var[ImageData])(implicit depthInfo: DepthImageInfo, imgInfo: ImageInfo) extends JPanel {
  private var image   = new BufferedImage(imgInfo.width, imgInfo.height, BufferedImage.TYPE_3BYTE_BGR)
  private val cvImage = new Mat(imgInfo.width, imgInfo.height, imgInfo.cvType)


  override def getWidth: Int = imgInfo.width
  override def getHeight: Int = imgInfo.height

  val updateImg = Obs(dataVar) {
                                 dataVar() match {
                                   case ImageData(img, _, imgDepth, _, _) =>
                                     img.copyTo(cvImage)
                                     cvImage.colorDepthCoords(imgDepth)
                                     image = cvImage.toBufferedImage
                                     repaint()
                                 }
                               }

  case class TrackingLine(x1: Int, y1: Int, x2: Int, y2: Int, clr: Color = Color.WHITE) {
    val length = math.sqrt(math.pow(x2 - x1, 2) + math.pow(y2 - y1, 2))
    val points = (x1, y1, x2, y2)
  }

  implicit class RichGraphics(g: Graphics) {
    def drawLine(tl: TrackingLine) = {
      val oc = g.getColor
      g.setColor(tl.clr)
      g.drawLine(tl.x1, tl.y1, tl.x2, tl.y2)
      g.setColor(oc)
    }
  }

  val lineColor    = Var(Color.BLUE)
  val lineVals     = Var((0, 0, 0, 0))
  val trackingLine = lineVals map { case (x1, y1, x2, y2) => TrackingLine(x1, y1, x2, y2, lineColor())}
  val redrawTL     = Obs(trackingLine) {
                                         repaint()
                                       }

  addMouseListener(MouseHandler)
  addMouseMotionListener(MouseHandler)

  override def paintComponent(g: Graphics) {
    super.paintComponent(g)
    g.asInstanceOf[Graphics2D].drawImage(image, 0, 0, null)
    g.drawLine(trackingLine())
  }

  object MouseHandler extends MouseInputAdapter {
    val center = Var[Point](new Point(0, 0))
    val edge   = Var[Point](new Point(0, 0))

    override def mousePressed(e: MouseEvent): Unit = {
      center() = e.getPoint
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      edge() = e.getPoint
    }

    override def mouseDragged(e: MouseEvent): Unit = {
      lineVals() = (center().x, center().y, e.getX, e.getY)
    }
  }

}

class DataImage(width: Int, height: Int, img: Rx[Mat]) extends JPanel {
  private var image = new BufferedImage(width, height,BufferedImage.TYPE_3BYTE_BGR)

  override def getWidth: Int = width
  override def getHeight: Int = height

  val mObs = Obs(img) {
                        image = img().toBufferedImage
                        repaint()
                      }
  def update(m: Mat) = {
    image = m.toBufferedImage
    repaint()
  }

  override def paintComponent(g: Graphics) {
    super.paintComponent(g)
    g.asInstanceOf[Graphics2D].drawImage(image, 0, 0, null)
  }
}
