package com.ptwales.DataSheet

import scala.collection.JavaConverters._
import scala.util.{Try, Success, Failure}

import java.nio.file.{Path, Files}

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

  /** Returns an [[DataSheet]] from the given file.
    *
    * @param  path  Path to a data file.
    * @return A new [[DataSheet]] instance.
    */
  def apply(path: Path): DataSheet = {
    val ext = path.toString.split('.').last
    Try(factory(ext)(path)).get
  }

  private type Factory = Path => DataSheet
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

  private def xssf(path: Path): Workbook = {
    new XSSFWorkbook(path.toFile)
  }

  private def hssf(path: Path): Workbook = {
    new HSSFWorkbook(Files.newInputStream(path))
  }

  private def txt(delim: Char)(path: Path): DataSheet = {
    val src = io.Source.fromFile(path.toFile)
    val data = try src.mkString("\n") finally src.close
    new CSVTable(data.replace("\r", ""), delim, '\n')
  }

}

/** Implementation of DataSheet for excel workbooks.
  *
  * Implementation of DataSheet for excel workbooks using apache poi library.
  *
  * @constructor  Create a POITable instance.
  * @param  sheet POI Sheet used to create a table.
  */
private class ExcelTable(sheet: Sheet) extends DataSheet {

  val rows: Table = {
    val rowIter = sheet.rowIterator.asScala
    val allRows = rowIter.map(cellsOfRow _)
    val definedRows = allRows takeWhile { (row: Row) =>
      (row.size > 1) || (row(0).getOrElse("").toString.trim.size > 0)
    }
    definedRows.toVector
  }

  import org.apache.poi.ss.usermodel.{Row => POIRow}
  import org.apache.poi.ss.usermodel.{Cell => POICell}
  import org.apache.poi.ss.usermodel.DateUtil

  /** Returns the values of each cell in a row. */
  private def cellsOfRow(row: POIRow): Row = {
    val first = row.getFirstCellNum
    val last = row.getLastCellNum
    val range = first.until(last)
    range map {
      (i: Int) => Option(row.getCell(i)) flatMap {
        (c: POICell) => valueOfCell(c)
      }
    }
  }

  /** Returns the value of a cell as an Option[Any] */
  private def valueOfCell(cell: POICell): Option[Any] = cell.getCellType match {
    case POICell.CELL_TYPE_NUMERIC => Option(
      if (DateUtil.isCellDateFormatted(cell)) cell.getDateCellValue
      else cell.getNumericCellValue
    )
    case POICell.CELL_TYPE_STRING  => Option(cell.getStringCellValue)
    case POICell.CELL_TYPE_FORMULA => Option(cell.getStringCellValue)
    case POICell.CELL_TYPE_BLANK   => None
    case POICell.CELL_TYPE_BOOLEAN => Option(cell.getBooleanCellValue)
    case POICell.CELL_TYPE_ERROR   => None
  }
}

/** Implementation of DataSheet for csv files.
  *
  */
private class CSVTable(text: String, colSep: Char, rowSep: Char) 
extends DataSheet {

  val rows: Table = {
    val lines = seqSplit(text, rowSep)
    lines map {
      (row: String) => seqSplit(row, colSep) map {
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
