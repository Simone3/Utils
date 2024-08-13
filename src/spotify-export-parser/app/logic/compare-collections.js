import { Constants, sortCollectionsAndTracks, findCollection, trackComparator, collectionComparator } from '../common/util.js';
import { checkInputFolder, checkOutputFolder } from '../common/validation.js';
import { parseAllCollections } from '../common/parsing.js';
import { generateExcelFile, getTrackOutputFileHeader, getTrackOutputFileRow } from '../common/excel.js';

/**
 * Helper that applies the comparison logic between two lists of tracks
 */
const doTrackLogic = (firstTracks, secondTracks) => {
	const outputTracks = [];

	// Loop both lists of tracks, each with its own index
	let t1 = 0;
	let t2 = 0;
	while(t1 < firstTracks.length || t2 < secondTracks.length) {
		// Compare the two current tracks (this requires that both lists have the same ordering logic!)
		let trackComparison;
		if(t1 < firstTracks.length && t2 < secondTracks.length) {
			trackComparison = trackComparator(firstTracks[t1], secondTracks[t2]);
		}
		else if(t1 < firstTracks.length) {
			trackComparison = -1;
		}
		else {
			trackComparison = 1;
		}

		if(trackComparison === 0) {
			// Track is the same: mark it as present in both lists
			outputTracks.push({
				...firstTracks[t1],
				state: Constants.CompareState.BOTH,
				firstAddedDate: firstTracks[t1].addedDate,
				secondAddedDate: secondTracks[t2].addedDate
			});
			t1 += 1;
			t2 += 1;
		}
		else if(trackComparison < 0) {
			// Track is only in the first list
			outputTracks.push({
				...firstTracks[t1],
				state: Constants.CompareState.FIRST,
				firstAddedDate: firstTracks[t1].addedDate
			});
			t1 += 1;
		}
		else {
			// Track is only in the second list
			outputTracks.push({
				...secondTracks[t2],
				state: Constants.CompareState.SECOND,
				secondAddedDate: secondTracks[t2].addedDate
			});
			t2 += 1;
		}
	}

	return outputTracks;
};

/**
 * Helper that applies the comparison logic between two lists of collections
 */
const doCollectionLogic = (firstCollections, secondCollections) => {
	const outputCollections = [];

	// Loop both lists of collections, each with its own index
	let c1 = 0;
	let c2 = 0;
	while(c1 < firstCollections.length || c2 < secondCollections.length) {
		// Compare the two current collections (this requires that both lists have the same ordering logic!)
		let collectionComparison;
		if(c1 < firstCollections.length && c2 < secondCollections.length) {
			collectionComparison = collectionComparator(firstCollections[c1], secondCollections[c2]);
		}
		else if(c1 < firstCollections.length) {
			collectionComparison = -1;
		}
		else {
			collectionComparison = 1;
		}

		if(collectionComparison === 0) {
			// Collection is the same: need to apply the comparison logic to the contained tracks
			outputCollections.push({
				...firstCollections[c1],
				tracks: doTrackLogic(firstCollections[c1].tracks, secondCollections[c2].tracks)
			});
			c1 += 1;
			c2 += 1;
		}
		else if(collectionComparison < 0) {
			// Collection is only in the first list: all tracks are by definition only in the first list
			outputCollections.push({
				...firstCollections[c1],
				tracks: firstCollections[c1].tracks.map((track) => {
					return {
						...track,
						state: Constants.CompareState.FIRST,
						firstAddedDate: track.addedDate
					};
				})
			});
			c1 += 1;
		}
		else {
			// Collection is only in the second list: all tracks are by definition only in the second list
			outputCollections.push({
				...secondCollections[c2],
				tracks: secondCollections[c2].tracks.map((track) => {
					return {
						...track,
						state: Constants.CompareState.SECOND,
						secondAddedDate: track.addedDate
					};
				})
			});
			c2 += 1;
		}
	}

	return outputCollections;
};

/**
 * Helper to write the comparison results as an Excel
 */
const writeOutput = (outputFolder, outputCollections, writeCollectionName, firstName, secondName) => {
	const trackColumns = {
		collection: writeCollectionName,
		number: false,
		id: true,
		artist: true,
		name: true,
		album: true,
		addedDate: false
	};

	const xlsxRows = [];

	// Excel header (column titles)
	const header = getTrackOutputFileHeader(trackColumns);
	header.push({
		value: 'Result',
		fontWeight: 'bold'
	});
	header.push({
		value: `${firstName} Added Date`,
		fontWeight: 'bold'
	});
	header.push({
		value: `${secondName} Added Date`,
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
					value: trackOutput.firstAddedDate
				});
				data.push({
					type: String,
					value: trackOutput.secondAddedDate
				});
				xlsxRows.push(data);
			}
		}
		else {
			xlsxRows.push(getTrackOutputFileRow(trackColumns, collectionOutput, undefined, undefined));
		}
	}

	// Write the output Excel
	generateExcelFile('compare', outputFolder, xlsxRows);
};

/**
 * Main entry point for the logic of comparing two different collections from the same export
 */
export const compareCollections = (inputFolder, outputFolder, firstCollectionName, secondCollectionName) => {
	// Validation
	checkInputFolder(inputFolder);
	checkOutputFolder(outputFolder);

	// Get all collections and tracks from the Spotify Export
	const collections = parseAllCollections(inputFolder);
	sortCollectionsAndTracks(collections);

	// Find the two collections in the parsed input
	const firstCollection = findCollection(collections, firstCollectionName);
	const secondCollection = findCollection(collections, secondCollectionName);

	// Create two 1-element lists whose collections have the same name to reuse the common logic function
	const commonNameCompare = `Merge of ${firstCollection.name} and ${secondCollection.name}`;
	const firstCollectionsCompare = [{
		...firstCollection,
		name: commonNameCompare
	}];
	const secondCollectionsCompare = [{
		...secondCollection,
		name: commonNameCompare
	}];

	// Apply the main logic
	const outputCollections = doCollectionLogic(firstCollectionsCompare, secondCollectionsCompare);

	// Write the output Excel file
	writeOutput(outputFolder, outputCollections, false, `"${firstCollection.name}"`, `"${secondCollection.name}"`);
};

/**
 * Main entry point for the logic of comparing two different exports
 */
export const compareExports = (firstInputFolder, secondInputFolder, outputFolder) => {
	// Validation
	checkInputFolder(firstInputFolder);
	checkInputFolder(secondInputFolder);
	checkOutputFolder(outputFolder);

	// Get all collections and tracks from the first Spotify Export
	const firstExportCollections = parseAllCollections(firstInputFolder);
	sortCollectionsAndTracks(firstExportCollections);

	// Get all collections and tracks from the second Spotify Export
	const secondExportCollections = parseAllCollections(secondInputFolder);
	sortCollectionsAndTracks(secondExportCollections);

	// Apply the main logic
	const outputCollections = doCollectionLogic(firstExportCollections, secondExportCollections);

	// Write the output Excel file
	writeOutput(outputFolder, outputCollections, true, 'First Export', 'Second Export');
};

