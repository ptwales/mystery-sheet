package com.ptwales.sheets

/**
  */
class HeadedSheet(sheet: DataSheet, header: Header) extends DataSheet {
  assert(header.distinct.size == header.size)

  val rows = {
    if (sheet.rows.head == header) sheet.rows.tail
    else sheet.rows
  }

  /**
    */
  def indexOf(field: Field): Index = header.indexOf(field)

  /**
    */
  def colAt(field: Field): Row = colAt(indexOf(field))

  /**
    */
  def colsAt(subHeader: Header): Table = {
    colsAt(subHeader.map(indexOf(_)))
  }

  /**
    */
  def recordAt(index: Index): Record = {
    rowAt(index).zipWithIndex.map((ci) => (header(ci._2) -> ci._1)).toMap
  }
}

/** Factory object for [[HeadedSheet]]. */
object HeadedSheet {
  
  def apply(sheet: DataSheet, header: Header): HeadedSheet = {
    new HeadedSheet(sheet, header)
  }

  def apply(sheet: DataSheet, header: Seq[String]): HeadedSheet = {
    apply(sheet, header.toVector)
  }
}
