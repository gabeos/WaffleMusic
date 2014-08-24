package com.gabeos.wafflemusic

import de.sciss.synth._
import de.sciss.synth.ugen._

/**
 * Created by Gabriel Schubiner on 8/6/2014.
 */
object SynthDefs {
  val sinSD = SynthDef("AnalogBubbles") {
    val f1 = "freq1".kr(0.4)
    val fs = "sinfreq".ar(220)
    //      val f2 = "freq2".kr(8)
    //      val d = "detune".kr(0.90375)
    //      val f = LFSaw.ar(f1).madd(24, LFSaw.ar(Seq(f2, f2 * d)).madd(3, 80)).midicps // glissando function
    //      val x = CombN.ar(SinOsc.ar(fs) * 0.04, 0.2, 0.2, 4) // echoing sine wave
    val x = SinOsc.ar(fs) // echoing sine wave
    Out.ar(0, x)
  }
}
