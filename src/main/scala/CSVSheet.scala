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
  val defaultRowSep: Char = '\n'

  /** Create a normal csv file.
    *
    * Assumes comma as column separator and LF as row separator
    */
//  def apply(text: String): DataSheet = {
//    apply(text, defaultColSep, defaultRowSep)
//  }
//
//  def apply(text: String, colSep: Char): DataSheet = {
//    apply(text, colSep, defaultRowSep);
//  }

  def fromText(text: String, colSep: Char = ',', rowSep: Char = '\n'): DataSheet = {
    var format = CSVFormat.DEFAULT
    format = format.withDelimiter(colSep)
    format = format.withRecordSeparator(rowSep.toString)
    fromText(text, format)
  }

  def fromText(text: String, format: CSVFormat): DataSheet = {
    new CSVSheet(text, format)
  }

//  def apply(path: Path): DataSheet = {
//    apply(path, defaultColSep, defaultRowSep)
//  }
//
//  def apply(path: Path, colSep: Char): DataSheet = {
//    apply(path, colSep, defaultRowSep)
//  }

  def fromSource(src: Source, colSep: Char = ',', rowSep: Char = '\n'): DataSheet = {
    val text = try src.mkString finally src.close
    fromText(text, colSep, rowSep)
  }
}
