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

  val testsPerCharset = 3
  val maxColCount = 4
  val maxRowCount = 4
  val maxCellSize = 10

  val charsetsToTest = Seq("UTF-8").map(Charset.forName(_))

  for (charset <- charsetsToTest; i <- (1 to testsPerCharset)) {

    val tg = TextGenerator(charset)
    val name = tg.charset.displayName
    tg.forbidden += '"'

    val col = tg.randomChar
    tg.forbidden += col

    val rowCount = randomSize(maxRowCount)
    val colCount = randomSize(maxColCount)

    val testCase = CSVTestCase(charset, colCount, rowCount, col)
    val testData = testCase.createData(tg)

    rawTextTest(testData)
    fileReadTest(testData)
  }

  def rawTextTest(testData: CSVTestData): Unit = {
    val testCase = testData.testCase
    test("Raw Test: " + testData.name) {
      val sheet = CSVSheet.fromText(testData.text, testCase.colSep)
      DataChecker.check(sheet, testData.data)
    }
  }

  def fileReadTest(testData: CSVTestData): Unit = {
    val testCase = testData.testCase
    val charset = testCase.charset
    test("File Test: " + testData.name) {
      val dest = Paths.get(charset.displayName + ".csv")
      try {
        val written = Files.write(dest, testData.lines.asJava, charset)
        val src = io.Source.fromURL(written.toUri.toURL)
        val sheet = CSVSheet.fromSource(src, testCase.colSep)
        DataChecker.check(sheet, testData.data)
      } finally {
        Files.delete(dest)
      }
    }
  }

  def randomSize(max: Int): Int = {
    Random.nextInt(max - 1) + 1
  }

  case class CSVTestCase(charset: Charset, colCount: Int, rowCount: Int, colSep: Char) {
    def createData(tg: TextGenerator): CSVTestData = {
      tg.forbidden += colSep
      val data = Vector.fill(rowCount, colCount)(tg.randomString(maxCellSize))
      CSVTestData(this, data)
    }
  }

  case class CSVTestData(testCase: CSVTestCase, data: Data) {

    lazy val text: String = {
      val rows = data.map(_.mkString(testCase.colSep.toString))
      rows.mkString(System.lineSeparator)
    }

    lazy val lines: Vector[String] = {
      text.split(System.lineSeparator).toVector
    }

    lazy val name: String = {
      val csName = testCase.charset.displayName
      s"charset=$csName " +
      s"rows=${testCase.rowCount} cols=${testCase.colCount} " +
      s"c=`${testCase.colSep}'"
    }
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
        case Some(x) => checkValue(x.toString, d)
        case None => assertEmpty(d)
      }
    }

    def assertEmpty(s: String): Unit = {
      checkValue("", s)
    }

    def checkValue(c: String, s: String): Unit = {
      assert(c == s)
    }
  }
}

class TextGenerator(cs: Charset) {

  val charset: Charset = cs
  var forbidden: Set[Char] = Set[Char]()

  val allChars: String = {
    val encoder = charset.newEncoder
    val charRange = (Char.MinValue.toChar to Char.MaxValue.toChar).mkString
    val encodeable: String = charRange.filter(encoder.canEncode(_))
    val noControl = encodeable.replaceAll("[\\p{C}]", "")
    val noExtension = noControl.replaceAll("[^\\p{InBasicLatin}]", "")
    noExtension
  }

  def randomChar(): Char = {
    var result: Char = '.'
    do {
      result = allChars(Random.nextInt(allChars.size))
    } while (forbidden(result))
    result
  }

  def randomString(maxSize: Int): String = {
    val size = Random.nextInt(maxSize - 1) + 1
    Seq.fill(size)(randomChar()).mkString
  }

}

object TextGenerator {
  def apply(cs: Charset): TextGenerator = new TextGenerator(cs)
}
