package com.ptwales.sheets

import scala.util.{Try, Success, Failure}
import scala.collection.JavaConverters._
import scala.io.Source

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

  val defaultColSep: Char = ','
  val defaultRowSep: String = System.lineSeparator

  /** Create a normal csv file.
    *
    * Assumes comma as column separator and LF as row separator
    */

  def fromText(text: String,
      colSep: Char=defaultColSep,
      rowSep: String=defaultRowSep): DataSheet = {
    var format = CSVFormat.DEFAULT
    format = format.withDelimiter(colSep)
    format = format.withRecordSeparator(rowSep)
    fromText(text, format)
  }

  def fromText(text: String, format: CSVFormat): DataSheet = {
    new CSVSheet(text, format)
  }

  def fromSource(src: Source,
      colSep: Char=defaultColSep,
      rowSep: String=defaultRowSep): DataSheet = {
    val text = try src.mkString finally src.close
    fromText(text, colSep, rowSep)
  }
}
