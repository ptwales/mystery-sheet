package com.ptwales.sheets

import scala.util.{Try, Success, Failure}

import java.nio.file.Path
import java.net.URL
import java.io.InputStream


/** Simplified representation of tabular data.
  *
  */
trait DataSheet extends Table {

  /** The collection of values as a 2D vector. */
  val rows: Table

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
    rows.map(cellAt(colIndex))
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
  def rowsAt(rowIndexes: Iterable[Index]): DataSheet = {
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
  def colsAt(colIndexes: Iterable[Index]): DataSheet = {
    val colVec = colIndexes.toVector
    val cols = rows map { row => 
      colVec map { colIndex =>
        cellAt(colIndex)(row)
      }
    }
    Sheet(cols)
  }

  private def cellAt(colIndex: Index)(row: Row): Cell = {
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

/** Simplest implementation of [[DataSheet]]. */
case class Sheet(rows: Table) extends DataSheet

/** Factory object for [[DataSheet]] */
object DataSheet {

  /** Returns a [[DataSheet]] from agiven file URL.
    * 
    * @param url  URL to a data file.
    * @return A new [[DataSheet]] instance.
    */
  def apply(url: URL): DataSheet = {
    val ext = url.toString.split('.').last
    val istream = url.openStream
    try {
      extFactory(ext)(istream)
    } catch {
      case (nsee: NoSuchElementException) => {
        val msg = s".$ext files are not a supported extension"
        throw new UnsupportedOperationException(msg)
      }
      case (e: Exception) => throw e
    } finally {
      istream.close
    }
  }

  /** Returns an [[DataSheet]] from the given file path.
    *
    * @param  path  Path to a data file.
    * @return A new [[DataSheet]] instance.
    */
  def apply(path: Path): DataSheet = {
    apply(path.toUri.toURL)
  }

  def apply(table: Table): DataSheet = {
    Sheet(table)
  }

  private type Factory = InputStream => DataSheet
  private val extFactory = Map[String, Factory](
    "xlsx" -> ExcelSheet.fromXlsxInput(0),
    "xls"  -> ExcelSheet.fromXlsInput(0),
    "csv"  -> txt(','),
    "ttx"  -> txt('\t'),
    "txt"  -> txt('\t'), 
    "ods"  -> ODSSheet.fromInput(0)
  )

  private def txt(delim: Char)(istream: InputStream): DataSheet = {
    import scala.io.Source
    CSVSheet.fromSource(Source.fromInputStream(istream), colSep=delim)
  }
}

