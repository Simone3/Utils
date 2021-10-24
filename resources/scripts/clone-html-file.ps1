param(
	[string] $targetDirectory
)

# Prompt user
$newFileName = Read-Host -Prompt 'New File Name (ideally no spaces!)'

# File
$htmlFile = "./skeleton.html"

# Copy sample project
Copy-Item $htmlFile "$targetDirectory/$newFileName.html"

# Edit the HTML file
(Get-Content "$targetDirectory/$newFileName.html").replace('#REPLACE_UTIL_NAME#', $newFileName) | Set-Content "$targetDirectory/$newFileName.html"

