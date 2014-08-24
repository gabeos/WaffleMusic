package com.gabeos.wafflemusic

import java.awt._
import java.awt.event.{MouseEvent, WindowEvent, WindowAdapter}
import java.awt.geom.AffineTransform
import java.awt.image._
import java.nio.ByteBuffer
import javax.swing.event.MouseInputAdapter
import javax.swing.{JPanel, JApplet, JFrame}
import com.gabeos.wafflemusic.data.TrustworthyImagePXCMPoint3D
import com.gabeos.wafflemusic.processing.BasicProcessor
import com.gabeos.wafflemusic.sounds.BasicSoundKit
import org.opencv.core.{Core, CvType, Mat}
import org.opencv.videoio.VideoCapture
import rx._
import rx.ops._
import de.sciss.synth._
import intel.pcsdk._
import Cfg._

import scala.collection.mutable

/**
 * Created by Gabriel Schubiner on 7/29/2014.
 */
class MonitorWindow extends JApplet {}

object WaffleMusic extends App {
  // Boot server
  val serverCxn = Server.boot
  serverCxn.addListener({ case ServerConnection.Aborted =>
    Server.default.quit()
  })

  // Init pipeline


  System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

  val pp: PXCUPipeline = new PXCUPipeline
  if (!pp.Init(PXCUPipelineJNI.COLOR_VGA | PXCUPipelineJNI.DEPTH_QVGA)) {
    System.out.print("Failed to initialize PXCUPipeline\n")
    serverCxn.abort()
    System.exit(3)
  }
  println(s"PXCUPipeline initialized.")

  // Init camera sizes
  val dsize: Array[Int] = new Array[Int](2)
  pp.QueryDepthMapSize(dsize)
  val csize: Array[Int] = new Array[Int](2)
  pp.QueryRGBSize(csize)

  // init monitor
  val frame: JFrame = new JFrame("Intel(R) Perceptual Computing SDK Java Sample")
  val monitor = new MonitorWindow
  val df = new MonitorImage(csize(0), csize(1))
  monitor.add(df)
  frame.addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent) {
      pp.Close
      serverCxn.abort()
      System.exit(0)
    }
  })
  frame.setSize(csize(0), csize(1))
  frame.add(monitor)
  frame.setVisible(true)
  println(s"JFrame Initialized")

  // get untrusted value range
  val untrusted: Array[Float] = new Array[Float](2)
  pp.QueryDeviceProperty(PXCMCapture.Device.PROPERTY_DEPTH_SATURATION_VALUE, untrusted)

  // initialize depthmap
  val depthmap: Array[Short] = new Array[Short](dsize(0) * dsize(1))
  val p3: Array[PXCMPoint3DF32] = Array.tabulate[PXCMPoint3DF32](dsize(0) * dsize(1))(xy => new PXCMPoint3DF32(xy % dsize(0), xy / dsize(0), 0))
  val p2: Array[PXCMPointF32] = new Array[PXCMPointF32](dsize(0) * dsize(1))
  val depthImagePoints = new Array[TrustworthyImagePXCMPoint3D](dsize(0) * dsize(1))
  val validDepths = Var(new Array[TrustworthyImagePXCMPoint3D](0))
  val validDepthsM = ??? //Rx {org.opencv.imgproc.Imgproc.warpAffine()) }
  val affine = Rx {
    val tl = df.trackingLine()
    val theta = math.atan2(tl.y2 - tl.y1, tl.x2 - tl.x1)

    val tx = new AffineTransform()
    tx.translate(tl.x1, tl.y1)
    tx.rotate(theta)
    new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR)
  }
  // init validColors
//  val validRaster = Var(df.image.getData)

  // init reactive vars
  val samplesTaken = Var(0)
  val rxDistanceToLine = Rx { (x: Int, y: Int) => {
    val (x1, y1, x2, y2) = df.trackingLine().points
    val length = df.trackingLine().length
    ((x2 - x1) * (y1 - y) - (y2 - y1) * (x1 - x)) / length
  }
  }

  val imgMat = Var(new Mat())
  val depthMat = Var(new Mat())
  // Instantiate reactively activated sound synth
  val synth = ??? //new BasicSoundKit(imgMat, depthMat, validDepths, validRaster, samplesTaken) with BasicProcessor


  // main loop
  while (true) {
    if (pp.AcquireFrame(true)) {
      if (pp.QueryRGB(df.image) && pp.QueryDepthMap(depthmap)) {
        //fill depth map
        for (xy <- 0 until p3.length)
          p3(xy).z = depthmap(xy).asInstanceOf[Float]

        // update depth map array
        if (pp.MapDepthToColorCoordinates(p3, p2)) {


          // update samples and send message to process data

          samplesTaken() = samplesTaken() + 1
          imgMat() = {
            val rows = df.image.getWidth()
            val cols = df.image.getHeight()
            val m = new Mat(rows, cols, CvType.CV_8UC3)
            val data = df.image.getRaster.getDataBuffer.asInstanceOf[DataBufferInt].getData
            val bBuff = ByteBuffer.allocate(data.length * 4)
            val intB = bBuff.asIntBuffer()
            intB.put(data)
            m.put(0, 0, bBuff.array())
            m
          }
          depthMat() = {
            val validBldr = mutable.ArrayBuilder.make[TrustworthyImagePXCMPoint3D]
            val m = new Mat(dsize(0), dsize(1), CvType.CV_32FC1)
            var xy = 0
            while (xy < p2.length) {
              if (depthmap(xy) != untrusted(0) &&
                depthmap(xy) != untrusted(1) &&
                p2(xy).x != -1.0f && p2(xy).y != -1.0f)
                m.put(p2(xy).x.asInstanceOf[Int], p2(xy).y.asInstanceOf[Int], Array(depthmap(xy).asInstanceOf[Float]))
              else
                m.put(p2(xy).x.asInstanceOf[Int], p2(xy).y.asInstanceOf[Int], Array(-1.0f))
              xy += 1
              //                val dip = depthImagePoints(xy)
              //                dip.x = p2(xy).x.asInstanceOf[Int]
              //                dip.y = p2(xy).y.asInstanceOf[Int]
              //                dip.trusted = depthmap(xy) != untrusted(0) &&
              //                  depthmap(xy) != untrusted(1) &&
              //                  p2(xy).x != -1.0f && p2(xy).y != -1.0f
              //                dip.inTrackingScope = rxDistanceToLine()(dip.x, dip.y) < trackingWidth

              //                if (dip.trusted && dip.inTrackingScope)
              //                  validBldr += dip
            }
            m
          }
        }
//        validDepths() = validBldr.result()
//        val nRaster = df.image.getData.createCompatibleWritableRaster()
//        affine().filter(df.image.getData, nRaster)
//        validRaster() = nRaster
      }
      df.repaint()
    }
    pp.ReleaseFrame()
  }
}

class MonitorImage(width: Int, height: Int) extends JPanel {
  val image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

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

  val lineColor = Var(Color.BLUE)
  val lineVals = Var((0, 0, 0, 0))
  val trackingLine = lineVals map { case (x1, y1, x2, y2) => TrackingLine(x1, y1, x2, y2, lineColor())}
  val redrawTL = Obs(trackingLine) {
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
    val edge = Var[Point](new Point(0, 0))

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


/* Init depthmap
  var xy: Int = 0
  var y: Int = 0
  while (y < dsize(1)) {
    var x: Int = 0
    while (x < dsize(0)) {
      p3old(xy) = new PXCMPoint3DF32(x, y, 0)
      x += 1
      xy += 1
    }
    y += 1
  }
 */

/*
//          depthImagePoints.map(p => {
//            val x = p2(xy).x.asInstanceOf[Int]
//            val y = p2(xy).y.asInstanceOf[Int]
//            val z = depthmap(xy).asInstanceOf[Float]
//            new TrustworthyImagePXCMPoint3D(
//              x, y, z,
//              trusted = depthmap(xy) != untrusted(0) &&
//                depthmap(xy) != untrusted(1) &&
//                p2(xy).x != -1.0f && p2(xy).y != -1.0f,
//              inTrackingScope = rxDistanceToLine()(x, y) < trackingWidth
//            )})

 */

/*
//          for (xy <- 0 until p2.length) {
          //            new TrustworthyImagePXCMPoint3D(p2(xy).x.asInstanceOf[Int],p2(xy).y.asInstanceOf[Int],p3(xy).z,depthmap(xy) != untrusted(0) && depthmap(xy) != untrusted(1))


          // filter untrusted depths
          //            if (depthmap(xy) != untrusted(0) && depthmap(xy) != untrusted(1)) {
          // get (x,y) coords in image space
          //              val x1: Int = p2(xy).x.asInstanceOf[Int]
          //              val y1: Int = p2(xy).y.asInstanceOf[Int]
          //              totDepth += p3(xy).z.toDouble
          //              if (x1 >= 0 && x1 < csize(0) && y1 >= 0 && y1 < csize(1)) {
          ////                println(s"(x,y): ${(x1,y1)}")
          ////                df.image().setRGB(x1, y1, 0xff00ff00.asInstanceOf[Int])
          //              }
          //            }
          //          }
 */

/*
  //  val serverCfg = Server.Config()
  //  serverCfg.deviceNames match {
  //    case Some((s1,s2)) => println(s"device names: $s1,$s2")
  //    case None => println(s"No device name!")
  //  }
  //  serverCfg.deviceName

 */