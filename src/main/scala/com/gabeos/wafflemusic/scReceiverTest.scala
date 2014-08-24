package com.gabeos.wafflemusic

import de.sciss.synth._
import ugen._
import Ops._

object scReceiverTest extends App {

  //  val cfg = Server.Config()
  //  cfg.program = "C:\\Program Files (x86)\\SuperCollider-3.6.6\\scsynth"
  //  val serv = Server.boot("testsc",cfg)

  val serverCxn = Server.boot
  serverCxn.addListener({ case ServerConnection.Running(_) =>
    val serv = Server.default

    val df = SynthDef("AnalogBubbles") {
      val f1 = "freq1".kr(0.4)
      val fs = "sinfreq".ar(220)
//      val f2 = "freq2".kr(8)
//      val d = "detune".kr(0.90375)
//      val f = LFSaw.ar(f1).madd(24, LFSaw.ar(Seq(f2, f2 * d)).madd(3, 80)).midicps // glissando function
//      val x = CombN.ar(SinOsc.ar(fs) * 0.04, 0.2, 0.2, 4) // echoing sine wave
      val x = SinOsc.ar(fs) // echoing sine wave
      Out.ar(0, x)
    }
//    df.load()

    val x = df.play()
    //  x.set("freq1" -> 0.1)
    //  x.set("freq2" -> 222.2)
    //  x.set("detune" -> 0.44)

    //  s.freeAll()

    for (i <- 120 to 320 by 5) {
      Thread.sleep(200)
    }
  })
}
