import { sortCollectionsAndTracks } from '../common/util.js';
import { checkInputFolder, checkOutputFolder } from '../common/validation.js';
import { parseAllCollections } from '../common/parsing.js';
import { generateExcelFile, getTrackOutputFileHeader, getTrackOutputFileRow } from '../common/excel.js';

/**
 * Helper to write the list of collections/tracks as an Excel
 */
const generateSimpleExcelFile = (outputFolder, collections) => {
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
	xlsxRows.push(header);

	// Excel data rows (for each collection/track)
	for(let c = 0; c < collections.length; c++) {
		const collection = collections[c];
		if(collection.tracks.length > 0) {
			for(let t = 0; t < collection.tracks.length; t++) {
				const track = collection.tracks[t];
				const data = getTrackOutputFileRow(trackColumns, collection, track, t);
				xlsxRows.push(data);
			}
		}
		else {
			xlsxRows.push(getTrackOutputFileRow(trackColumns, collection, undefined, undefined));
		}
	}

	// Write the output Excel
	generateExcelFile('converted', outputFolder, xlsxRows);
};

/**
 * Main entry point for the logic of converting the list of collections/tracks to Excel
 */
export const convertToExcel = (inputFolder, outputFolder) => {
	// Validation
	checkInputFolder(inputFolder);
	checkOutputFolder(outputFolder);

	// Get all collections and tracks from the Spotify Export
	const collections = parseAllCollections(inputFolder);
	sortCollectionsAndTracks(collections);

	// Write the output Excel file
	generateSimpleExcelFile(outputFolder, collections);
};
