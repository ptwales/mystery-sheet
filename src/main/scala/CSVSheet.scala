package com.ptwales.sheets

import scala.util.{Try, Success, Failure}

/** Implementation of [[DataSheet]] for csv or any character delimited files.
  *
  * Wraps Apache Commons CSV class.
  */
private class CSVSheet(text: String, colSep: Char, rowSep: Char) 
extends DataSheet {

  val rows: Table = {
    val lines = seqSplit(text, rowSep)
    lines map {
      (line: String) => seqSplit(line, colSep) map {
        (el: String) => cellOf(el)
      }
    }
  }

  private def seqSplit(text: String, sep: Char) = {
    text.split(sep).toVector
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
    new CSVSheet(text, colSep, rowSep)
  }
}
