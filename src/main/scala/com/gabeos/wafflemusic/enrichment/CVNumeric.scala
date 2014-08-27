package com.gabeos.wafflemusic.enrichment

/**
 * Created by Gabriel Schubiner on 8/26/2014.
 */
trait CVNumeric {

  sealed trait CVDataType[@specialized(Int, Short, Long, Float, Double) T] {

    def plus(lhs: T, rhs: T): T
    def times(lhs: T, rhs: T): T
    def minus(lhs: T, rhs: T): T
    def divide(lhs: T, rhs: T): T
    def mod(lhs: T, rhs: T): T
    def power(lhs: T, rhs: T): T
    def eqls(lhs: T, rhs: T): Boolean
    def neql(lhs: T, rhs: T): Boolean
    def negate(rhs: T): T
    def inverse(rhs: T): T
    val zero: T
    val one: T
    def absolute(rhs: T): T
    def normal(rhs: T): Double
    def toDouble(rhs: T): Double
    def typed(rhs: Double): T

    class CVDataOps(lhs: T) {
      def +(rhs: T): T = plus(lhs, rhs)
      def *(rhs: T): T = times(lhs, rhs)
      def -(rhs: T): T = minus(lhs, rhs)
      def neg: T = negate(lhs)
      def %(rhs: T): T = mod(lhs, rhs)
      def eql(rhs: T): Boolean = eqls(lhs, rhs)
      def neq(rhs: T): Boolean = neql(lhs, rhs)
      def inv: T = inverse(lhs)
      def /(rhs: T) = divide(lhs, rhs)
      def pow(rhs: T) = power(lhs, rhs)
      def norm: Double = normal(lhs)
      def abs: T = absolute(lhs)
      def dbl: Double = toDouble(lhs)
    }

    implicit def mkCVOps(lhs: T): CVDataOps = new CVDataOps(lhs)
  }

  object CVDataType {
    implicit object CV_Double extends CVDataType[Double] {
      override def eqls(lhs: Double, rhs: Double): Boolean = lhs == rhs
      override def plus(lhs: Double, rhs: Double): Double = lhs + rhs
      override def divide(lhs: Double, rhs: Double): Double = lhs / rhs
      override def inverse(rhs: Double): Double = 1.0 / rhs
      override def neql(lhs: Double, rhs: Double): Boolean = lhs != rhs
      override def negate(rhs: Double): Double = -rhs
      override def power(lhs: Double, rhs: Double): Double = math.pow(lhs, rhs)
      override def times(lhs: Double, rhs: Double): Double = lhs * rhs
      override def minus(lhs: Double, rhs: Double): Double = lhs - rhs
      override def mod(lhs: Double, rhs: Double): Double = lhs % rhs
      override val zero: Double = 0.0
      override val one: Double = 1.0
      override def absolute(rhs: Double): Double = math.abs(rhs)
      override def normal(lhs: Double): Double = absolute(lhs)
      override def toDouble(rhs: Double): Double = rhs.toDouble
      override def typed(rhs: Double): Double = rhs
    }

    implicit object CV_Short extends CVDataType[Short] {
      override def plus(lhs: Short, rhs: Short): Short = (lhs + rhs).toShort
      override def divide(lhs: Short, rhs: Short): Short = (lhs / rhs).toShort
      override def eqls(lhs: Short, rhs: Short): Boolean = lhs == rhs
      override def inverse(rhs: Short): Short = (1.toShort / rhs).toShort
      override def neql(lhs: Short, rhs: Short): Boolean = lhs != rhs
      override def negate(rhs: Short): Short = rhs
      override def power(lhs: Short, rhs: Short): Short = math.pow(lhs, rhs).toShort
      override def times(lhs: Short, rhs: Short): Short = (lhs * rhs).toShort
      override def minus(lhs: Short, rhs: Short): Short = (lhs - rhs).toShort
      override def mod(lhs: Short, rhs: Short): Short = (lhs % rhs).toShort
      override val zero: Short = 0.toShort
      override val one: Short = 1.toShort
      override def absolute(rhs: Short): Short = math.abs(rhs).toShort
      override def normal(lhs: Short): Double = math.abs(lhs).toDouble
      override def toDouble(rhs: Short): Double = rhs.toDouble
      override def typed(rhs: Double): Short = rhs.toShort
    }

    implicit object CV_Int extends CVDataType[Int] {
      override def plus(lhs: Int, rhs: Int): Int = lhs + rhs
      override def divide(lhs: Int, rhs: Int): Int = lhs / rhs
      override def eqls(lhs: Int, rhs: Int): Boolean = lhs == rhs
      override def inverse(rhs: Int): Int = 1 / rhs
      override def neql(lhs: Int, rhs: Int): Boolean = lhs != rhs
      override def negate(rhs: Int): Int = -rhs
      override def power(lhs: Int, rhs: Int): Int = math.pow(lhs, rhs).toInt
      override def times(lhs: Int, rhs: Int): Int = lhs * rhs
      override def minus(lhs: Int, rhs: Int): Int = lhs - rhs
      override def mod(lhs: Int, rhs: Int): Int = lhs % rhs
      override val zero: Int = 0
      override val one: Int = 1
      override def absolute(rhs: Int): Int = math.abs(rhs)
      override def normal(lhs: Int): Double = absolute(lhs).toDouble
      override def toDouble(rhs: Int): Double = rhs.toDouble
      override def typed(rhs: Double): Int = rhs.toInt
    }

    implicit object CV_Float extends CVDataType[Float] {
      override def plus(lhs: Float, rhs: Float): Float = lhs + rhs
      override def divide(lhs: Float, rhs: Float): Float = lhs / rhs
      override def eqls(lhs: Float, rhs: Float): Boolean = lhs == rhs
      override def inverse(rhs: Float): Float = 1.0f / rhs
      override def neql(lhs: Float, rhs: Float): Boolean = lhs != rhs
      override def negate(rhs: Float): Float = -rhs
      override def power(lhs: Float, rhs: Float): Float = math.pow(lhs, rhs).toFloat
      override def times(lhs: Float, rhs: Float): Float = lhs * rhs
      override def minus(lhs: Float, rhs: Float): Float = lhs - rhs
      override def mod(lhs: Float, rhs: Float): Float = lhs % rhs
      override val zero: Float = 0.0f
      override val one: Float = 1.0f
      override def absolute(rhs: Float): Float = math.abs(rhs)
      override def normal(rhs: Float): Double = absolute(rhs).toDouble
      override def toDouble(rhs: Float): Double = rhs.toDouble
      override def typed(rhs: Double): Float = rhs.toFloat
    }

    implicit object CV_Byte extends CVDataType[Byte] {
      override def plus(lhs: Byte, rhs: Byte): Byte = (lhs + rhs).toByte
      override def divide(lhs: Byte, rhs: Byte): Byte = (lhs / rhs).toByte
      override def eqls(lhs: Byte, rhs: Byte): Boolean = lhs == rhs
      override def inverse(rhs: Byte): Byte = (1 / rhs).toByte
      override def neql(lhs: Byte, rhs: Byte): Boolean = lhs != rhs
      override def negate(rhs: Byte): Byte = (-rhs).toByte
      override def power(lhs: Byte, rhs: Byte): Byte = math.pow(lhs, rhs).toByte
      override def times(lhs: Byte, rhs: Byte): Byte = (lhs * rhs).toByte
      override def minus(lhs: Byte, rhs: Byte): Byte = (lhs - rhs).toByte
      override def mod(lhs: Byte, rhs: Byte): Byte = (lhs % rhs).toByte
      override val zero: Byte = 0.toByte
      override val one: Byte = 1.toByte
      override def absolute(rhs: Byte): Byte = math.abs(rhs).toByte
      override def normal(rhs: Byte): Double = math.abs(rhs).toDouble
      override def toDouble(rhs: Byte): Double = rhs.toDouble
      override def typed(rhs: Double): Byte = rhs.toByte
    }
  }
}
