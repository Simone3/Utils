#!/bin/sh

targetDirectory="$1"

# Prompt user
read -p "Folder Name / Maven Artifact (no spaces!): " newProjectId
read -p "Application Name: " newProjectName
read -p "Application Description: " newProjectDescription

# Check input
if test -z "$targetDirectory" || test -z "$newProjectId" || test -z "$newProjectName" || test -z "$newProjectDescription"
then

	echo "Empty vars: targetDirectory = $targetDirectory, newProjectId = $newProjectId, newProjectName = $newProjectName, newProjectDescription = $newProjectDescription"

else

	# Folders
	sampleProjectFolder="./"
	newProjectFolder="$targetDirectory/$newProjectId"

	# Copy sample project
	cp -r "$sampleProjectFolder" "$newProjectFolder"

	# Remove the CLONE.ME scripts
	find "$newProjectFolder" -type f -name "CLONE\.ME.*" -exec rm -f {} \;

	# Edit pom.xml
	sed -i '' -e "s/#REPLACE_PROJECT_ID#/$newProjectId/g" "$newProjectFolder/pom.xml"
	sed -i '' -e "s/#REPLACE_PROJECT_NAME#/$newProjectName/g" "$newProjectFolder/pom.xml"
	sed -i '' -e "s/#REPLACE_PROJECT_DESCRIPTION#/$newProjectDescription/g" "$newProjectFolder/pom.xml"

fi
