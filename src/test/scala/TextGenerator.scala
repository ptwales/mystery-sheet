package com.ptwales.sheets

import java.nio.charset.Charset
import scala.util.Random

/** Generates random characters that are in a given Charset.
  *
  * Can randomly and efficiently non-control latin characters that are
  * encodeable by a charset.
  *
  * It also has a mutable set [[forbidden]] who's characters will not be 
  * generated.
  */
class TextGenerator(cs: Charset) {

  /** The [[java.nio.charset.Charset]] characters are generated from. */
  val charset: Charset = cs

  /** Characters in this set will never be generated. */
  var forbidden: Set[Char] = Set[Char]()

  /** All chars this can generate, not omitting [[forbidden]]. 
    * 
    * TODO: replace with a Stream[Char] for efficiency.
    */
  lazy val allChars: String = {
    val encoder = charset.newEncoder
    val charRange = (Char.MinValue.toChar to Char.MaxValue.toChar).mkString
    val encodeable: String = charRange.filter(encoder.canEncode(_))
    val noControl = encodeable.replaceAll("[\\p{C}]", "")
    val noExtension = noControl.replaceAll("[^\\p{InBasicLatin}]", "")
    noExtension
  }

  /** Generate a random char from [[allChars]] that isn't in [[forbidden]].*/
  def randomChar(): Char = {
    var result: Char = '.'
    do {
      result = allChars(Random.nextInt(allChars.size))
    } while (forbidden(result))
    result
  }

  /** Generate a string of random characters between 1 to maxSize characters 
    * long.
    *
    * Characters are drawn from [[randomChar]] and repeats are allowed.
    *
    * @param  maxSize Maximum size the random string may be.
    * @returns  A random string no longer than maxSize
    */
  def randomString(maxSize: Int): String = {
    val size = Random.nextInt(maxSize)
    Seq.fill(size)(randomChar()).mkString
  }
}

/** Companion object for [[TextGenerator]]. */
object TextGenerator {
  def apply(cs: Charset): TextGenerator = new TextGenerator(cs)
}
