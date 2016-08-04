package com.ptwales.sheets

trait HeadedTable extends Table {

  /** Return this table as a [[Table]] with no header. */
  def sheet: Table

  /** Return the headers of this HeadedTable. */
  def header: Header

  val rows = {
    if (sheet.rows.head == header) sheet.rows.tail
    else sheet.rows
  }

  /** Return the index of a field in the header.
    *
    * @param  field   The name of the column header.
    * @return         The index of the given column header.
    */
  def indexOf(field: Field): Index = header.indexOf(field)

  /** Return the column specified by the given header name.
    * 
    * Overloads [[Table.colAt]].
    *
    * @param  field   The name of a the column header.
    * @return         The column under the given field.
    */
  def colAt(field: Field): Row = colAt(indexOf(field))

  /** Return a sub table with the given headers.
    *
    * Overloads [[Table.colsAt]].
    * All given fields must be contained in the header.
    *
    * @param  subHeaders  The fields of the selected columns.
    * @return A table made from the selected columns.
    */
  def colsAt(subHeaders: Header): Table = {
    colsAt(subHeaders.map(indexOf(_)))
  }

  /** Return a Map where the keys are the header and the values are the
    * cells at the given index.
    *
    * @param  index   Index of the record.
    * @return         A Map of [[Field]] to [[Cell]].
    */
  def recordAt(index: Index): Record = {
    rowAt(index).zipWithIndex.map((ci) => (header(ci._2) -> ci._1)).toMap
  }

}

/** Factory object for [[HeadedTable]]. */
object HeadedTable {
  
  def apply(sheet: Table, header: Header): HeadedTable = {
    new LabeledSheet(sheet, header)
  }

  def apply(sheet: Table, header: Seq[String]): HeadedTable = {
    apply(sheet, header.toVector)
  }

  //def apply(sheet: Table): HeadedTable
}

/** Basic implementation of [[HeadedTable]]. */
private case class LabeledSheet(sheet: Table, header: Header)
extends HeadedTable {
  assert(header.distinct.size == header.size)
}
