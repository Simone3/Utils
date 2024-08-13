import fs from 'node:fs';
import path from 'node:path';
import { Constants } from './util.js';

/**
 * Helper to parse all playlists in a Spotify export
 */
const parseAllPlaylists = (inputFolder, collections) => {
	const playlist1FilePath = path.join(inputFolder, Constants.Files.PLAYLIST1_FILE_NAME);

	const fileStringContent = fs.readFileSync(playlist1FilePath, { encoding: 'utf8' });
	if(fileStringContent.trim().length === 0) {
		throw Error('Playlists input file is empty');
	}

	const fileContent = JSON.parse(fileStringContent);
	if(fileContent.playlists && Array.isArray(fileContent.playlists)) {
		console.log(`Found ${fileContent.playlists.length} playlists`);

		for(const inputPlaylist of fileContent.playlists) {
			const collection = {
				name: inputPlaylist.name,
				tracks: []
			};
			collections.push(collection);

			if(inputPlaylist.items && Array.isArray(inputPlaylist.items)) {
				console.log(`  Found playlist "${collection.name}" with ${inputPlaylist.items.length} tracks`);

				for(const inputItem of inputPlaylist.items) {
					if(inputItem.track) {
						collection.tracks.push({
							id: inputItem.track.trackUri || '',
							type: 'TRACK',
							name: inputItem.track.trackName || '',
							artist: inputItem.track.artistName || '',
							album: inputItem.track.albumName || '',
							addedDate: inputItem.addedDate || ''
						});
					}
					else {
						console.log(`    Unrecognized playlist item type in "${collection.name}": ${JSON.stringify(inputItem)}`);
					}
				}
			}
			else {
				console.log(`  Found playlist "${collection.name}" with no tracks`);
			}
		}
	}
};

/**
 * Helper to parse the Liked / Your Library collection in a Spotify export
 */
const parseLikedCollection = (inputFolder, collections) => {
	const yourLibraryFilePath = path.join(inputFolder, Constants.Files.YOUR_LIBRARY_FILE_NAME);

	const fileStringContent = fs.readFileSync(yourLibraryFilePath, { encoding: 'utf8' });
	if(fileStringContent.trim().length === 0) {
		throw Error('Liked / Your Library input file is empty');
	}

	const fileContent = JSON.parse(fileStringContent);

	if(fileContent.tracks && Array.isArray(fileContent.tracks)) {
		console.log(`Found ${fileContent.tracks.length} tracks in Liked collection`);

		const collection = {
			name: 'Liked',
			tracks: []
		};
		collections.push(collection);

		for(const inputTrack of fileContent.tracks) {
			collection.tracks.push({
				id: inputTrack.uri || '',
				type: 'TRACK',
				name: inputTrack.track || '',
				artist: inputTrack.artist || '',
				album: inputTrack.album || '',
				addedDate: ''
			});
		}
	}
};

/**
 * Helper to parse all collections (playlists and the Liked / Your Library collection) in a Spotify export
 */
export const parseAllCollections = (inputFolder) => {
	const collections = [];
	parseAllPlaylists(inputFolder, collections);
	parseLikedCollection(inputFolder, collections);
	return collections;
};
