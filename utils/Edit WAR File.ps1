param(
	# The WAR file to edit
	[string] $warFileName,
	
	# The optional Java home path (if null, the JAVA_HOME environment variable is used)
	[string] $javaHome,	
	
	# Optional parameters to replace a string inside a text file inside the WAR
	# Multiple groups of file-search-replace are allowed, the 3 arrays MUST have the same length
	[string[]] $fileToEditRelativePathArray,
	[string[]] $replaceOldArray,
	[string[]] $replaceNewArray,
	
	# Optional parameters to copy an external file inside the WAR (if target folder does not exist, it will be created)
	# Multiple pairs of file-destination are allowed, the 2 arrays MUST have the same length
	[string[]] $fileToCopyArray,
	[string[]] $destinationRelativePathArray
)
$tempFolder = "_TEMP_EXTRACT_WAR_"

# Create temp folder
new-item $tempFolder -itemtype directory

# Extract WAR to temp folder
Add-Type -assembly  System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::ExtractToDirectory($warFileName, $tempFolder)

# Search & Replace in WAR files, if any
For($i=0; $i -lt $fileToEditRelativePathArray.Length; $i++) {

	# Full file path
	$file = "$tempFolder/" + $fileToEditRelativePathArray[$i]
	
	# Replace file contents
	$fileContents = Get-Content $file
	$fileContents = $fileContents.Replace($replaceOldArray[$i], $replaceNewArray[$i])
	Out-File -FilePath $file -Encoding UTF8 -InputObject $fileContents
}

# Copy external files into WAR, if any
For($i=0; $i -lt $fileToCopyArray.Length; $i++) {

	# Create destination if it doesn't exist
	$destination = "$tempFolder/" + $destinationRelativePathArray[$i]
	New-Item -ItemType Directory -Force -Path $destination

	# Copy file
	Copy-Item $fileToCopyArray[$i] -Destination $destination
}

# Get Java
If($javaHome) {

	$javaFolder = $javaHome
}
Else {
	
	$javaFolder = (Get-ChildItem Env:JAVA_HOME).Value
}

# Rebuild WAR
cd $tempFolder
& "$javaFolder\bin\jar.exe" -cvf ../$warFileName *
cd ..

# Delete temp folder
remove-item $tempFolder -recurse
