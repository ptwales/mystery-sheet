package com.ptwales.sheets

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.{Workbook, Sheet}

import scala.collection.JavaConverters._

/** Implementation of [[DataSheet]] for excel workbooks.
  *
  * Implementation of [[DataSheet]] for excel workbooks using apache poi library.
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
