param(
	[string] $targetDirectory
)

# Prompt user
$newFileName = Read-Host -Prompt 'New File Name'

# File
$htmlFile = "./skeleton.html"

# Copy sample project
Copy-Item $htmlFile "$targetDirectory/$newFileName.html"

# Edit the HTML file
(Get-Content "$targetDirectory/$newFileName.html").replace('#REPLACE_UTIL_NAME#', $newFileName) | Set-Content "$targetDirectory/$newFileName.html"

