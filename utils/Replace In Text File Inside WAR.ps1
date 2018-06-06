param(
	[string] $warFileName,
	[string] $fileToEditRelativePath,
	[string] $replaceOld,
	[string] $replaceNew
)
$tempFolder = "_TEMP_EXTRACT_WAR_"

# Create temp folder
new-item $tempFolder -itemtype directory

# Extract WAR to temp folder
Add-Type -assembly  System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::ExtractToDirectory($warFileName, $tempFolder)

# File to edit
$file = "$tempFolder/$fileToEditRelativePath"

# Replace file contents
$fileContents = Get-Content $file
$fileContents = $fileContents.Replace($replaceOld, $replaceNew)
Out-File -FilePath $file -Encoding UTF8 -InputObject $fileContents

# Build WAR
cd $tempFolder
C:\IBM\was\java_1.7_64\bin\jar.exe -cvf ../$warFileName *
cd ..

# Delete temp folder
remove-item $tempFolder -recurse
