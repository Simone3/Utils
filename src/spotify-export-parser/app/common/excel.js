import path from 'node:path';
import writeXlsxFile from 'write-excel-file/node';

/**
 * Returns the standard Excel header row (column titles) for the tracks table
 */
export const getTrackOutputFileHeader = (columns) => {
	const header = [];
	if(columns.collection) {
		header.push({
			value: 'Playlist',
			fontWeight: 'bold'
		});
	}
	if(columns.number) {
		header.push({
			value: '#',
			fontWeight: 'bold'
		});
	}
	if(columns.id) {
		header.push({
			value: 'ID',
			fontWeight: 'bold'
		});
	}
	if(columns.artist) {
		header.push({
			value: 'Artist',
			fontWeight: 'bold'
		});
	}
	if(columns.name) {
		header.push({
			value: 'Title',
			fontWeight: 'bold'
		});
	}
	if(columns.album) {
		header.push({
			value: 'Album',
			fontWeight: 'bold'
		});
	}
	if(columns.addedDate) {
		header.push({
			value: 'Added Date',
			fontWeight: 'bold'
		});
	}
	return header;
};

/**
 * Returns the standard Excel row for a track
 */
export const getTrackOutputFileRow = (columns, collection, track, trackIndex) => {
	const data = [];
	if(columns.collection) {
		data.push({
			type: String,
			value: collection.name
		});
	}
	if(columns.number) {
		data.push({
			type: Number,
			value: trackIndex >= 0 ? trackIndex + 1 : undefined
		});
	}
	if(columns.id) {
		data.push({
			type: String,
			value: track ? track.id : ''
		});
	}
	if(columns.artist) {
		data.push({
			type: String,
			value: track ? track.artist : ''
		});
	}
	if(columns.name) {
		data.push({
			type: String,
			value: track ? track.name : ''
		});
	}
	if(columns.album) {
		data.push({
			type: String,
			value: track ? track.album : ''
		});
	}
	if(columns.addedDate) {
		data.push({
			type: String,
			value: track ? track.addedDate : ''
		});
	}
	return data;
};

/**
 * Writes an Excel file in the output folder with the given rows
 */
export const generateExcelFile = (outputFileName, outputFolder, rows) => {
	const filePath = path.join(outputFolder, `spotifyexport_${outputFileName}_${new Date().toISOString().replace(/[-:.ZT]/g, '')}.xlsx`);

	writeXlsxFile(
		rows,
		{
			undefined,
			filePath: filePath
		}
	).then(() => {
		console.log(`Successfully written output file "${filePath}"!`);
	})
	.catch((e) => {
		console.error(`Error writing output file "${filePath}": ${e}`);
	});
};
