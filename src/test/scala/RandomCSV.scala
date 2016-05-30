package com.ptwales.sheets

import java.nio.file.{Path, Files, Paths}
import java.nio.charset.Charset

import scala.collection.JavaConverters._
import scala.util.Random

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RandomCSVGenerator extends FunSuite {

  type Data = Vector[Vector[String]]

  val charsetsToTest = {
    val allCharsets = Charset.availableCharsets.asScala.values
    val encodeable = allCharsets.filter(_.canEncode)
    encodeable.map({
        (cs: Charset) => cs -> allCharsInCharset(cs)
      }).toMap
  }

  val testsPerCharset = 1
  for ((charset, chars) <- charsetsToTest; i <- (1 to testsPerCharset)) {

    val name = charset.displayName
    var availableChars = chars

    val col = randomDelim(availableChars)
    availableChars -= col

    val row = randomDelim(availableChars)
    availableChars -= row

    val quote = '"' // randomDelim(availableChars)
    availableChars -= quote

    val data = randomData(availableChars, quote)
    val text = data.map(_.mkString(col.toString))

    test(s"Test #$i in $name: with c=$col, r=$row, q=$quote") {
      // write text to file in charset
      val dest = Paths.get(getClass.getResource("/random-csv/" + name).toURI)
      val written = Files.write(dest, text.asJava, charset)
      // read as CSVSheet
      val sheet: DataSheet = CSVSheet(written, col, row)
      // check that data matches
      //???
      // delete file
      Files.delete(written)
    }
  }

  private def allCharsInCharset(charset: Charset): Set[Char] = {
    val encoder = charset.newEncoder
    var result = Set[Char]()
    for (c <- (Char.MinValue to Char.MaxValue) if encoder.canEncode(c)) {
      result += c
    }
    return result
  }

  private def randomDelim(chars: Set[Char]): Char = {
    Random.shuffle(chars.toList).head
  }

  private def randomData(chars: Set[Char], quote: Char): Data = {

    val rows = Random.nextInt(256)
    val cols = Random.nextInt(256)

    def randomCell(quoteProb: Double): String = {
      val cellSize = Random.nextInt(chars.size)
      val cell = Random.shuffle(chars.toList).take(cellSize).mkString
      if (math.random < quoteProb) quote + cell + quote
      else cell
    }

    Vector.fill(rows, cols)(randomCell(0.25))
  }
}

