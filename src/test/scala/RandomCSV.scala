package com.ptwales.sheets

import java.nio.file.Path
import java.nio.charset.Charset

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RandomCSVGenerator extends FunSuite {

  type Data = Vector[Vector[String]]

  def randomColDelim(chars: String): Char = ???
  def randomRowDelim(chars: String): Char = ???
  def randomData(chars: String): Data = ???
  def randomCharSet(): Charset = ???
  def makeFileText(data: Data, c: Char, r: Char): String = ???
  def writeToFile(text: String): Path = ???
  def readFromFile(path: Path): CSVSheet = ???

}
