package com.ptwales.sheets

import java.nio.file.{Path, Files, Paths}
import java.nio.charset.Charset

import scala.collection.JavaConverters._
import scala.util.Random

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RandomCSV extends FunSuite {

  val testsPerCharset = 10
  val maxColCount     = 10
  val maxRowCount     = 10
  val maxCellSize     = 10

  val charsetsToTest = Seq("UTF-8").map(Charset.forName(_))


  val tests = for { 
    charset <- charsetsToTest.toStream
    i <- (1 to testsPerCharset)
  } yield CSVTestCase(
    charset,
    Random.nextInt(maxColCount) + 1,
    Random.nextInt(maxRowCount) + 1,
    maxCellSize)
    
 

  tests foreach { testCase =>

    val charset = testCase.charset

    test("Raw Test: " + testCase.name) {
      val sheet = CSVSheet.fromText(
        testCase.text,
        testCase.colSep,
        testCase.quote)
      testCase.check(sheet)
    }

    test("File Test: " + testCase.name) {
      val safeName = testCase.name.replace("/", "SLASH")
      val dest = Paths.get(safeName + ".csv")
      try {
        val written = Files.write(dest, testCase.lines.asJava, charset)
        val sheet = CSVSheet.fromSource(
          io.Source.fromFile(written.toFile),
          testCase.colSep,
          testCase.quote)
        testCase.check(sheet)
      } finally {
        Files.delete(dest)
      }
    }
  }
}

/** Contains test conditons for a random CSV test.
  */
case class CSVTestCase(
  charset: Charset, 
  colCount: Int,
  rowCount: Int, 
  colSep: Char,
  quote: Char,
  maxCellSize: Int)
  extends FunSuite { //probably a better class to extend


  /** Formatted string showing test conditions */
  lazy val name: String = {
    s"charset:${charset.displayName}_" +
    s"rows:${rowCount}_cols:${colCount}_" +
    s"c:${colSep}_q:${quote}"
  }

  /***************\
   Generating Data
  \***************/

  lazy val data = Vector.fill(rowCount, colCount)(
    generateRandomCell(0.25)
  )

  /** Generates random text without column separator or quote characters. */
  lazy val textGen: TextGenerator = {
    val tg = TextGenerator(charset)
    tg.forbidden += colSep
    tg.forbidden += quote
    tg
  }

  /** Generates a random cell value which might be quoted. */
  private def generateRandomCell(qProb: Double): String = {
    val s = textGen.randomString(maxCellSize)
    if (math.random < qProb) quote + s + quote
    else s
  }

  /**************\
   Printing Data
  \**************/

  /** Lines of the random data */
  lazy val lines: Seq[String] = {
    data.map(_.mkString(colSep.toString))
  }

  /** The random data as a string */
  lazy val text: String = {
    lines.mkString(System.lineSeparator)
  }

  /****************\
   Validating data
  \****************/

  def check(sheet: DataSheet): Unit = {

    assert(sheet.rows.size == data.size,
      s"First rows, sheet=${sheet.rows.head} data=${data.head}")

    for (r <- (0 until data.size)) {

      val drow = data(r)
      val srow = sheet.rowAt(r)

      assert(srow.size == drow.size, s"Row $r of data=$drow")

      for (c <- (0 until srow.size)) {
        assert(srow(c) === stripQuote(drow(c)))
      }
    }
  }

  private def stripQuote(cell: Cell): Cell = {
    cell.dropWhile(_ == quote).takeWhile(_ != quote)
  }
}

/** Companion object for [[CSVTestCase]].
  */
object CSVTestCase {

  /** Generate a random [[CSVTestCase]] with size limitations. */
  def apply(charset: Charset, colCount: Int, rowCount: Int, maxCell: Int): CSVTestCase = {
    val tg = TextGenerator(charset)
    val colSep = tg.randomChar
    tg.forbidden += colSep
    val quote = tg.randomChar
    new CSVTestCase(charset, colCount, rowCount, colSep, quote, maxCell)
  }
}

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
