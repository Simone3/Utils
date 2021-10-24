
param(

	# The source folder (folder to clone)
	[string] $sourceFolder,
	
	# The destination folder (will be deleted and replaced with a new copy of the source folder)
	[string] $destinationFolder,
	
	# Replacements to apply to all file names, folder names and file contents (case SENSITIVE) in the new clone (destination folder)
	# The 2 arrays MUST have the same length
	# Strings should not contain regex characters or invalid characters for filenames
	[string[]] $replaceCaseSensitiveFrom,
	[string[]] $replaceCaseSensitiveTo
)

# Helper variables
$matchRegex = $replaceCaseSensitiveFrom -join '|'

# Delete old clone (if any)
Write-Host "`n`n[DELETE] Deleting Destination Folder"
Remove-Item $destinationFolder -Recurse -ErrorAction Ignore

# Create new clone
Write-Host "`n`n[CLONE] Cloning Source Folder"
Copy-Item $sourceFolder $destinationFolder -recurse

# Rename all files/folders inside new clone
Write-Host "`n`n[RENAME_FILES] Loop Start"
while($true) {

	$files = Get-ChildItem $destinationFolder -Recurse | Where-Object -FilterScript {$_.name -match $matchRegex}
	
	Write-Host '[RENAME_FILES] Iteration - Found' $files.Count 'Matching Files'
	
    if($files.Length -eq 0) {
	
         break
    }

	foreach($file in $files) {
		
		if(Test-Path $file.FullName) {
		
			$newName = $file.name;
			for($i=0; $i -lt $replaceCaseSensitiveFrom.Length; $i++) {
			
				$newName = $newName.Replace($replaceCaseSensitiveFrom[$i], $replaceCaseSensitiveTo[$i])
			}
			
			Write-Host '[RENAME_FILES] Renaming from ' $file.name ' to ' $newName
			
			Rename-Item -Path $file.FullName -NewName $newName
		}
	}
}
Write-Host "[RENAME_FILES] Loop End"

# Replace in all files inside new clone
$files = Get-ChildItem $destinationFolder -File -Recurse
Write-Host "`n`n[REPLACE_IN_FILES] Loop Start - Found "$files.Count' Total Files'
foreach($file in $files) {

	$content = Get-Content $file.FullName
	if($content -match $matchRegex) {
		
		Write-Host '[REPLACE_IN_FILES] Replacing in '$file
		
		$newContent = $content;
		for($i=0; $i -lt $replaceCaseSensitiveFrom.Length; $i++) {
		
			$newContent = $newContent.Replace($replaceCaseSensitiveFrom[$i], $replaceCaseSensitiveTo[$i])
		}
		
		Set-Content $file.FullName $newContent
	}
}
Write-Host "[REPLACE_IN_FILES] Loop End"



