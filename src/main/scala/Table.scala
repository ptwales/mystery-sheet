package com.ptwales.sheets

import scala.util.{Try, Success, Failure}

import java.nio.file.Path
import java.net.URL
import java.io.InputStream


/** Simplified representation of tabular data.
  *
  */
trait Table extends IndexedSeq[Row] {

  /** The collection of values as a 2D vector. */
  val rows: IndexedSeq[Row]

  /** Returns the row at the given index.
    *
    * Literally the same as indexing [[rows]]
    *
    * @param  rowIndex  Index of the row.
    * @return the row at the provided index.
    */
  def rowAt(rowIndex: Index): Row = {
    rows(rowIndex)
  }

  /** Returns the column at the given index.
    *
    * Returns a [[Column]] built from elements of the same index from [[rows]].
    *
    * @param  colIndex  Index of the column.
    * @return The column at the given index.
    */
  def colAt(colIndex: Index): Row = {
    rows.map(cellAt(_, colIndex))
  }

  /** Returns a subtable made from the rows of the given indexes.
    *
    * Creates a new [[Table]] from the rows of the given indexes, preserving order.
    * For example, getting the rows at indexes 0, 1, and 4 will return a three
    * row table made from the first, second and fifth rows.
    *
    * @param  rows  Indexes of the desired rows.
    * @return A table made from the selected rows.
    */
  def rowsAt(rowIndexes: Iterable[Index]): Table = {
    Sheet(rowIndexes.toVector.map(rows.apply _))
  }

  /** Returns a subtable made from the columns of the given indexes.
    *
    * Identical to [[rowsAt]] but uses the columns of the given indexes not
    * the rows.  It should not transform the columns into rows.
    *
    * @param  cols  Indexes of the desired columns.
    * @return A table made from the selected columns.
    */
  def colsAt(colIndexes: Iterable[Index]): Table = {
    val cols = rows map { row => 
      colIndexes.map(cellAt(row, _)).toVector
    }
    Sheet(cols)
  }

  private def cellAt(row: Row, colIndex: Index): Cell = {
    if (row.isDefinedAt(colIndex)) row(colIndex)
    else ""
  }

  /**********\
   IndexedSeq
  \**********/

  /** Returns the row at the given index
    *
    * @param  rowIndex  Index of the desired row.
    * @return The row at rowIndex.
    */
  def apply(rowIndex: Index): Row = {
    rowAt(rowIndex)
  }

  // for IndexedSeq
  def length = {
    rows.length
  }
}

/** Simplest implementation of [[Table]]. */
case class Sheet(rows: IndexedSeq[Row]) extends Table

/** Factory object for [[Table]] */
object Table {

  /** Returns a [[Table]] from agiven file URL.
    * 
    * @param url  URL to a data file.
    * @return A new [[Table]] instance.
    */
  def apply(url: URL): Table = {
    val ext = url.toString.split('.').last
    val istream = url.openStream
    try {
      extFactory(ext)(istream)
    } catch {
      case (nsee: NoSuchElementException) => {
        val msg = s".$ext files are not a supported extension"
        throw new UnsupportedOperationException(msg, nsee)
      }
      case (e: Exception) => throw e
    } finally {
      istream.close
    }
  }

  /** Returns an [[Table]] from the given file path.
    *
    * @param  path  Path to a data file.
    * @return A new [[Table]] instance.
    */
  def apply(path: Path): Table = {
    apply(path.toUri.toURL)
  }

  def apply(table: Table): Table = {
    Sheet(table)
  }

  private type Factory = InputStream => Table
  private val extFactory = Map[String, Factory](
    "xlsx" -> ExcelSheet.fromXlsxInput(0),
    "xls"  -> ExcelSheet.fromXlsInput(0),
    "csv"  -> txt(','),
    "ttx"  -> txt('\t'),
    "txt"  -> txt('\t'), 
    "ods"  -> ODSSheet.fromInput(0)
  )

  private def txt(delim: Char)(istream: InputStream): Table = {
    import scala.io.Source
    CSVSheet.fromSource(Source.fromInputStream(istream), colSep=delim)
  }
}

