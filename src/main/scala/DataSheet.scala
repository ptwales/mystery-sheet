package com.ptwales.sheets

import scala.util.{Try, Success, Failure}

import java.nio.file.Path
import java.net.URL

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.{Workbook, Sheet}

/** Simplified version of a workbook as a Table of values.
  *
  * DataSheet unifies the apache poi Sheet object with other common 
  * datasheets to give a simplified representation of the data.
  *
  */
trait DataSheet {

  /** The collection of values as a 2D vector */
  val rows: Table

  /** Returns the row at the given index.
    *
    * Literally the same as indexing [[rows]]
    *
    * @param  rowIndex  index of the row.
    * @return the row at the provided index.
    */
  def rowAt(rowIndex: Int): Row = rows(rowIndex)

  /** Returns the column at the given index.
    *
    * Returns a [[Column]] built from elements of the same index from [[rows]].
    *
    * @param  colIndex  index of the column.
    * @return the column at the given index.
    */
  def colAt(colIndex: Int): Row = rows.map(cellAt(colIndex))

  /** Returns a subtable made from the rows of the given indexes.
    *
    * Creates a new [[Table]] from the rows of the given indexes, preserving order.
    * For example, getting the rows at indexes 0, 1, and 4 will return a three
    * row table made from the first, second and fifth rows.
    *
    * @param  rows  Indexes of the desired rows.
    * @return a table made from the selected rows.
    */
  def rowsAt(rowIndexes: Iterable[Int]): Table = {
    rowIndexes.toVector.map(rows.apply _)
  }

  /** Returns a subtable made from the columns of the given indexes.
    *
    * Identical to [[rowsAt]] but uses the columns of the given indexes not
    * the rows.  It should not transform the columns into rows.
    *
    * @param  cols  Indexes of the desired columns.
    * @return a table made from the selected columns.
    */
  def colsAt(colIndexes: Iterable[Int]): Table = {
    val colVec = colIndexes.toVector
    rows map { row => 
      colVec map { colIndex =>
        cellAt(colIndex)(row)
      }
    }
  }

  /** Returns the row at the given index
    *
    * @param  rowIndex  Index of the desired row.
    * @return The row at rowIndex.
    */
  def apply(rowIndex: Int): Row = rowAt(rowIndex)

  private def cellAt(colIndex: Int)(row: Row): Cell = {
    if (row.isDefinedAt(colIndex)) row(colIndex)
    else None
  }
}

/** Factory object for [[DataSheet]] */
object DataSheet {

  /** Returns a [[DataSheet]] from agiven file URL.
    * 
    * @param url  URL to a data file.
    * @return A new [[DataSheet]] instance.
    */
  def apply(url: URL): DataSheet = {
    val ext = url.toString.split('.').last
    Try(factory(ext)(url)).get
  }

  /** Returns an [[DataSheet]] from the given file path.
    *
    * @param  path  Path to a data file.
    * @return A new [[DataSheet]] instance.
    */
  def apply(path: Path): DataSheet = {
    apply(path.toUri.toURL)
  }

  private type Factory = URL => DataSheet
  private val factory = Map[String, Factory](
    "xlsx" -> (excel _ compose xssf _ ),
    "xls"  -> (excel _ compose hssf _ ),
    "csv"  -> txt(','),
    "ttx"  -> txt('\t'),
    "txt"  -> txt('\t')
  )

  private def excel(book: Workbook): DataSheet = {
    new ExcelTable(book.getSheetAt(0))
  }

  private def xssf(url: URL): Workbook = {
    val inStream = url.openStream
    new XSSFWorkbook(inStream)
  }

  private def hssf(url: URL): Workbook = {
    val inStream = url.openStream
    new HSSFWorkbook(inStream)
  }

  private def txt(delim: Char)(url: URL): DataSheet = {
    val src = io.Source.fromURL(url)
    val data = try src.mkString.replace("\r", "") finally src.close
    CSVSheet(data, delim, '\n')
  }

}
