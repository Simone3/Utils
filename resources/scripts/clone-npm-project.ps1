param(
	[string] $targetDirectory
)

# Prompt user
$newProjectId = Read-Host -Prompt 'Folder Name / package.json name (no spaces!)'
$newProjectName = Read-Host -Prompt 'Application Name (ideally no spaces!)'
$newProjectDescription = Read-Host -Prompt 'Application Description'

# Folders
$sampleProjectFolder = "./"
$newProjectFolder = "$targetDirectory/$newProjectId"

# Copy sample project
Copy-Item -Path $sampleProjectFolder -Recurse -Destination $newProjectFolder -Container

# Remove the CLONE.ME scripts
Remove-Item -Path $newProjectFolder -include CLONE.ME.* -recurse

# Edit package.json
(Get-Content "$newProjectFolder/package.json").replace('#REPLACE_PROJECT_ID#', $newProjectId).replace('#REPLACE_PROJECT_NAME#', $newProjectName).replace('#REPLACE_PROJECT_DESCRIPTION#', $newProjectDescription) | Set-Content "$newProjectFolder/package.json"

