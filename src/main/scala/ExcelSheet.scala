package com.ptwales.sheets

import java.util.Date
import java.text.SimpleDateFormat

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.{Sheet => POISheet}
import org.apache.poi.ss.usermodel.{Row   => POIRow}
import org.apache.poi.ss.usermodel.{Cell  => POICell}
import org.apache.poi.ss.usermodel.DateUtil

import scala.collection.JavaConverters._
import java.io.InputStream

/** Implementation of [[DataSheet]] for excel workbooks.
  *
  * Implementation of [[DataSheet]] for excel workbooks using apache poi library.
  *
  * @constructor  Create a ExcelTable instance.
  * @param  sheet POI Sheet used to create a table.
  */
private class ExcelSheet(sheet: POISheet) extends DataSheet {

  val rows: Table = {
    val rowIter = sheet.rowIterator.asScala
    val allRows = rowIter.map(cellsOfRow _)
    val definedRows = allRows takeWhile { (row: Row) =>
      (row.size > 1) || (row(0).trim.size > 0)
    }
    definedRows.toVector
  }


  /** Returns the values of each cell in a row. */
  private def cellsOfRow(row: POIRow): Row = {
    val first = 0 // row.getFirstCellNum
    val last = row.getLastCellNum
    val range = first.until(last)
    range map { valueOfCell _ compose row.getCell _ }
  }

  /** Returns the value of a cell as a String. */
  private def valueOfCell(cell: POICell): Cell = cell.getCellType match {
    case POICell.CELL_TYPE_NUMERIC => {
      if (DateUtil.isCellDateFormatted(cell)) {
        dateFormat.format(cell.getDateCellValue)
      } else {
        cell.getNumericCellValue.toString
      }
    }
    case POICell.CELL_TYPE_STRING  => cell.getStringCellValue
    case POICell.CELL_TYPE_FORMULA => cell.getStringCellValue
    case POICell.CELL_TYPE_BLANK   => ""
    case POICell.CELL_TYPE_BOOLEAN => cell.getBooleanCellValue.toString
    case POICell.CELL_TYPE_ERROR   => "#ERROR" 
  }

  lazy val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  
}

object ExcelSheet {

  def apply(sheet: POISheet): DataSheet = {
    new ExcelSheet(sheet)
  }

  def fromXlsxInput(tab: Int)(istream: InputStream): DataSheet = {
    apply((new XSSFWorkbook(istream)).getSheetAt(tab))
  }

  def fromXlsInput(tab: Int)(istream: InputStream): DataSheet = {
    apply((new HSSFWorkbook(istream)).getSheetAt(tab))
  }
}
