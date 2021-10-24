param(
	[string] $zipFileName,
	[string] $fileToEdit,
	[string] $replaceOld,
	[string] $replaceNew
)

# Open zip and find the particular file (assumes only one inside the ZIP file)
Add-Type -assembly  System.IO.Compression.FileSystem
$zip =  [System.IO.Compression.ZipFile]::Open($zipFileName, "Update")
$file = $zip.Entries.Where({$_.name -eq $fileToEdit})

# Read the contents of the file
$filePointer = [System.IO.StreamReader]($file).Open()
$fileContents = $filePointer.ReadToEnd()
$filePointer.Close()

# Replace file contents
$fileContents = $fileContents.Replace($replaceOld, $replaceNew)

# Update the contents of the file
$filePointer = [System.IO.StreamWriter]($file).Open()
$filePointer.BaseStream.SetLength(0)
$filePointer.Write($fileContents)
$filePointer.Flush()
$filePointer.Close()

# Write the changes and close the zip file
$zip.Dispose()

