package com.gabeos.wafflemusic

import de.sciss.synth._
import ugen._
import Ops._

/**
 * Created by Gabriel Schubiner on 8/6/2014.
 */
object SCActors {

  val df = SynthDef("test") {
    val sig = Resonz.ar(WhiteNoise.ar(Seq(1, 1)), "freq".kr(400), 0.1)
    val env = EnvGen.ar(Env.perc(), doneAction = freeSelf)
    Out.ar(0, sig * env)
  }

  val sinsd = SynthDefs.sinSD.play()

  def normalize220_660(d: Double) =
    if (d > 660) 660.0
    else if (d < 440) 440.0
    else d

}
