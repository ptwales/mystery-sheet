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

  val testsPerCharset = 10
  val maxColCount = 10
  val maxRowCount = 10
  val maxCellSize = 10

  val charsetsToTest = Seq("UTF-8").map(Charset.forName(_))

  val tests = for { 
    charset <- charsetsToTest.toStream
    i <- (1 to testsPerCharset)
  } yield  CSVTestData(charset, maxColCount, maxRowCount, maxCellSize)

  tests foreach { t =>

    val testCase = t.testCase
    val charset = testCase.charset

    test("Raw Test: " + testCase.name) {
      val sheet = CSVSheet.fromText(t.text, testCase.colSep)
      DataChecker.check(sheet, t.data)
    }

    test("File Test: " + testCase.name) {

      val dest = Paths.get(testCase.name + ".csv")

      try {
        val written = Files.write(dest, t.lines.asJava, charset)
        val src = io.Source.fromURL(written.toUri.toURL)
        val sheet = CSVSheet.fromSource(src, testCase.colSep)
        DataChecker.check(sheet, t.data)

      } finally {
        Files.delete(dest)
      }
    }
  }

  def randomSize(max: Int): Int = {
    Random.nextInt(max - 1) + 1
  }

  /** Contains test conditons for a random CSV. */
  case class CSVTestCase(charset: Charset, colCount: Int, rowCount: Int, colSep: Char) {

    /** Returns a [[CSVTestData]] object. */
    def createData(maxCellSize: Int): CSVTestData = {
      val tg = TextGenerator(charset)
      tg.forbidden += colSep
      tg.forbidden += '"'  // TODO: return quote as a test option
      val data = Vector.fill(rowCount, colCount)(tg.randomString(maxCellSize))
      CSVTestData(this, data)
    }

    /** Formatted string showing test conditions */
    lazy val name: String = {
      s"charset=${charset.displayName} " +
      s"rows=${rowCount} cols=${colCount} " +
      s"c=`${colSep}'"
    }
  }

  /** Companion object for [[CSVTestCase]]. */
  object CSVTestCase {

    /** Generate a random [[CSVTestCase]] with size limitations. */
    def apply(charset: Charset, maxCol: Int, maxRow: Int): CSVTestCase = {
      val colCount = randomSize(maxCol)
      val rowCount = randomSize(maxRow)
      val tg = TextGenerator(charset)
      tg.forbidden += '"'  // TODO: return quote as a test option
      val colSep = tg.randomChar
      new CSVTestCase(charset, colCount, rowCount, colSep)
    }
  }
  
  /** Contains a [[CSVTestCase]] and random data */
  case class CSVTestData(testCase: CSVTestCase, data: Data) {

    /** Lines of the random data */
    lazy val lines: Vector[String] = {
      data.map(_.mkString(testCase.colSep.toString))
    }

    /** The random data as a string */
    lazy val text: String = {
      lines.mkString(System.lineSeparator)
    }
  }

  /** Companion object for [[CSVTestData]] */
  object CSVTestData {
    
    /** Create a random [[CSVTestData]] with limitations. */
    def apply(charset: Charset, maxCol: Int, maxRow: Int, 
              maxCell: Int): CSVTestData = {
      CSVTestCase(charset, maxCol, maxRow).createData(maxCell)
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
      val size = Random.nextInt(maxSize - 1) + 1
      Seq.fill(size)(randomChar()).mkString
    }

  }

  /** Companion object for [[TextGenerator]]. */
  object TextGenerator {
    def apply(cs: Charset): TextGenerator = new TextGenerator(cs)
  }

  object DataChecker {

    def check(sheet: DataSheet, data: Data): Unit = {

      assert(sheet.rows.size == data.size,
        s"First rows, sheet=${sheet.rows.head} data=${data.head}")

      for (r <- (0 until data.size)) {

        val drow = data(r)
        val srow = sheet.rowAt(r)

        assert(srow.size == drow.size, s"Row $r of data=$drow")

        for (c <- (0 until srow.size)) {
          checkCell(srow(c), drow(c))
        }
      }
    }

    def checkCell(c: Option[Any], d: String): Unit = {
      c match {
        case Some(x) => checkValue(x, d)
        case None => assertEmpty(d)
      }
    }

    def assertEmpty(d: String): Unit = {
      checkValue("", d)
    }

    def checkValue(c: Any, d: String): Unit = {
      c match {
        case s: String => assert(s == d)
        case i: Int => assert(i == d.toInt)
        case n: Double => assert(n == d.toDouble)
        case _ => fail("Unsupported data type")
      }
    }
  }
}
