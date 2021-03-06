package com.ptwales.sheets

import scala.util.{Try, Success, Failure}
import scala.collection.JavaConverters._
import scala.io.Source

import org.apache.commons.csv.{CSVParser, CSVFormat, CSVRecord}

/** Implementation of [[DataSheet]] for csv or any character delimited files.
  *
  * Wrapper for Apache Commons CSVFormat file.
  */
private class CSVSheet(text: String, format: CSVFormat) 
extends Table {

  /** Returns new a parser for this csv file
    *
    * Allows direct access to the CSVParser but you should probably just use
    * apache commons csv yourself.
    */
  def parser(): CSVParser = {
    CSVParser.parse(text, format)
  }

  val rows: IndexedSeq[Row] = {
    val lines = parser.getRecords.asScala
    lines.map(readRecord _).toVector
  }

  private def readRecord(line: CSVRecord): Row = {
    (0 until line.size).map(line.get(_))
  }
}

/** Factory object for [[CSVSheet]].
  *
  * The separate factory object for [[CSVSheet]] is exposed because to parse
  * text directly without loading it from a file.
  */
object CSVSheet {

  /** Assumed column separator is the comma. */
  val defaultColSep: Char = ','

  /** Assumed quote character is the double quote. */
  val defaultQuote: Char = '"'

  /** Create a new [[Table]] from a string with given settings.
    *
    * @param  text    Raw CSV text.
    * @param  colSep  Charater used to separate columns.
    * @param  quote   Character used to quote fields.
    * @return  A new [[Table]].
    */
  def fromText(text: String, colSep: Char=defaultColSep, 
               quote: Char=defaultQuote): Table = {
    val format = makeCSVFormat(colSep, quote)
    fromText(text, format)
  }

  /** Create a new [[Table]] using a predefined CSVFormat.
    *
    * @param  text    Raw CSV text.
    * @param  format  CSVFormat from apache.commons.csv.
    * @return  A new [[Table]].
    */
  def fromText(text: String, format: CSVFormat): Table = {
    new CSVSheet(text, format)
  }

  /** Create a new [[Table]] using a Source object.
    *
    * This should be replaced with the Java equivalent for better interop with
    * java clients and according to the maintainer scala.io.Source isn't high
    * quality.
    *
    * @param  src     Source of CSV data.
    * @param  colSep  Charater used to separate columns.
    * @param  quote   Character used to quote fields.
    * @return  A new [[Table]].
    */
  def fromSource(src: Source, colSep: Char=defaultColSep,
                 quote: Char=defaultQuote): Table = {
    val format = makeCSVFormat(colSep, quote)
    fromSource(src, format)
  }

  /** Create a new [[Table]] using a Source object and a
    * predifined CSVFormat.
    *
    * This should be replaced with the Java equivalent for better interop with
    * java clients and according to the maintainer scala.io.Source isn't high
    * quality.
    *
    * @param  src     Source of CSV data.
    * @param  format  CSVFormat from apache.commons.csv.
    * @return  A new [[Table]].
    */
  def fromSource(src: Source, format: CSVFormat): Table = {
    val text = try src.mkString finally src.close
    fromText(text, format)
  }

  /** Centralized location to create a CSVFormat. */
  private def makeCSVFormat(colSep: Char, quote: Char): CSVFormat = {
    CSVFormat.DEFAULT
      .withQuote(null)
      .withDelimiter(colSep)
      .withQuote(quote)
      //.withIgnoreEmptyLines(false)
      //.withTrim // not supported in this version of commons-csv?
  }
}
