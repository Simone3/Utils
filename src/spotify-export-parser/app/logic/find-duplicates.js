import { Constants, replaceSpecialCharsAndSpaces, sortCollectionsAndTracks } from '../common/util.js';
import { checkInputFolder, checkOutputFolder } from '../common/validation.js';
import { parseAllCollections } from '../common/parsing.js';
import { generateExcelFile, getTrackOutputFileHeader, getTrackOutputFileRow } from '../common/excel.js';

/**
 * Helper to "guess" if two strings are potentially related and therefore "duplicates"
 */
const isPotentialDuplicate = (string1, string2) => {
	// This could be done better (e.g. string similarity) but good enough for now...
	const normalized1 = replaceSpecialCharsAndSpaces(string1).toLowerCase();
	const normalized2 = replaceSpecialCharsAndSpaces(string2).toLowerCase();
	return normalized1.includes(normalized2) || normalized2.includes(normalized1);
};

/**
 * Helper that applies the duplicates logic
 */
const doLogic = (collections) => {
	const outputCollections = [];

	for(const collection of collections) {
		const collectionOutput = {
			name: collection.name,
			tracks: []
		};
		outputCollections.push(collectionOutput);

		// Loop forwards all collection tracks
		for(let i = 0; i < collection.tracks.length; i++) {
			const currentTrack = collection.tracks[i];
			const currentOutputTrack = {
				...currentTrack,
				state: Constants.DuplicateState.NOT_DUPLICATE,
				duplicateOf: []
			};
			collectionOutput.tracks.push(currentOutputTrack);

			// Loop backwards to find track duplicates
			for(let j = i - 1; j >= 0; j--) {
				const prevTrack = collection.tracks[j];
				const prevOutputTrack = collectionOutput.tracks[j];

				// Duplicates must have the exact same artist, stop going backwards if artist changes (this requires that tracks are always sorted by artist first!)
				if(currentTrack.artist !== prevTrack.artist) {
					break;
				}

				if(currentTrack.id === prevTrack.id) {
					// Exact duplicate (same ID)
					currentOutputTrack.state = Constants.DuplicateState.EXACT_DUPLICATE;
					currentOutputTrack.duplicateOf.push(j);
					prevOutputTrack.state = Constants.DuplicateState.EXACT_DUPLICATE;
					prevOutputTrack.duplicateOf.push(i);
				}
				else if(isPotentialDuplicate(currentTrack.name, prevTrack.name)) {
					// Potential duplicate (same artist and the names are "similar")
					if(currentOutputTrack.state === Constants.DuplicateState.NOT_DUPLICATE) {
						currentOutputTrack.state = Constants.DuplicateState.POTENTIAL_DUPLICATE;
					}
					currentOutputTrack.duplicateOf.push(j);
					if(prevOutputTrack.state === Constants.DuplicateState.NOT_DUPLICATE) {
						prevOutputTrack.state = Constants.DuplicateState.POTENTIAL_DUPLICATE;
					}
					prevOutputTrack.duplicateOf.push(i);
				}
			}
		}
	}

	return outputCollections;
};

/**
 * Helper to write the duplicates results as an Excel
 */
const writeOutput = (outputFolder, outputCollections) => {
	const trackColumns = {
		collection: true,
		number: true,
		id: true,
		artist: true,
		name: true,
		album: true,
		addedDate: true
	};

	const xlsxRows = [];

	// Excel header (column titles)
	const header = getTrackOutputFileHeader(trackColumns);
	header.push({
		value: 'Duplicate check',
		fontWeight: 'bold'
	});
	header.push({
		value: 'Duplicate notes',
		fontWeight: 'bold'
	});
	xlsxRows.push(header);

	// Excel data rows (for each collection/track)
	for(let c = 0; c < outputCollections.length; c++) {
		const collectionOutput = outputCollections[c];
		if(collectionOutput.tracks.length > 0) {
			for(let t = 0; t < collectionOutput.tracks.length; t++) {
				const trackOutput = collectionOutput.tracks[t];
				const data = getTrackOutputFileRow(trackColumns, collectionOutput, trackOutput, t);
				data.push({
					type: String,
					value: trackOutput.state
				});
				data.push({
					type: String,
					value: trackOutput.duplicateOf.length > 0 ? `Duplicate of ${trackOutput.duplicateOf.map((v) => `#${v + 1}`).join(', ')}` : ''
				});
				xlsxRows.push(data);
			}
		}
		else {
			xlsxRows.push(getTrackOutputFileRow(trackColumns, collectionOutput, undefined, undefined));
		}
	}

	// Write the output Excel
	generateExcelFile('duplicates', outputFolder, xlsxRows);
};

/**
 * Main entry point for the logic of finding duplicates in all collections
 */
export const findDuplicates = (inputFolder, outputFolder) => {
	// Validation
	checkInputFolder(inputFolder);
	checkOutputFolder(outputFolder);

	// Get all collections and tracks from the Spotify Export
	const collections = parseAllCollections(inputFolder);
	sortCollectionsAndTracks(collections);

	// Apply the main logic
	const outputCollections = doLogic(collections);

	// Write the output Excel file
	writeOutput(outputFolder, outputCollections);
};
