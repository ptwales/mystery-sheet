package com.ptwales.sheets

import scala.util.{Try, Success, Failure}
import scala.collection.JavaConverters._

import org.apache.commons.csv.{CSVParser, CSVFormat, CSVRecord}

/** Implementation of [[DataSheet]] for csv or any character delimited files.
  *
  * Later will wrap Apache Commons CSV class.
  */
private class CSVSheet(text: String, format: CSVFormat) 
extends DataSheet {

  val rows: Table = {
    val parser = CSVParser.parse(text, format)
    val lines = parser.getRecords.asScala
    lines.map(readRecord _).toVector
  }

  private def readRecord(line: CSVRecord): Row = {
    (0 until line.size) map { i => cellOf(line.get(i)) }
  }

  private def cellOf(el: String): Cell = {
    if (el.isEmpty) None
    else Some(Try(el.toInt).getOrElse(el))
  }
}

/** Factory object for [[CSVSheet]].
  *
  * The separate factory object for [[CSVSheet]] is exposed because to parse
  * text directly without loading it from a file.
  */
object CSVSheet {

  /** Create a normal csv file.
    *
    * Assumes comma as column separator and LF as row separator
    */
  def apply(text: String): DataSheet = {
    apply(text, ',', '\n')
  }

  def apply(text: String, colSep: Char): DataSheet = {
    apply(text, colSep, '\n');
  }

  def apply(text: String, colSep: Char, rowSep: Char): DataSheet = {
    var format = CSVFormat.DEFAULT
    format = format.withDelimiter(colSep)
    format = format.withRecordSeparator("\n")
    apply(text, format)
  }

  def apply(text: String, format: CSVFormat): DataSheet = {
    new CSVSheet(text, format)
  }
}