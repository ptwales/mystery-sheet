# Mystery Sheet

If you are recieving tabular files of inconsistent and varying formats, this is
the lib for you. DataSheet will determine the format of the file, read it, and 
expose it's contents in a unified format. All it does is bind several diffent
libraries for reading different tabular files together into one library.

## What is Supported

  - Delimited text files (tab, comma, etc) (Apache commons CSV)
  - Microsoft Excel files xlsx (Apache POI)
  - Microsoft Excel 97-03 files xls (also Apache POI)
  - Open Office Sheets files ods (Apache odftoolkit)

## What will be supported

 - Determining the format without trusting the file extension.
 - Allowing for variance in the header.
 - Deserialization of headed files.

