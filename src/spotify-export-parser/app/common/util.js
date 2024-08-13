
/**
 * Some helper constants
 */
export const Constants = Object.freeze({
	Files: {
		USER_DATA_FILE_NAME: 'Userdata.json',
		PLAYLIST1_FILE_NAME: 'Playlist1.json',
		YOUR_LIBRARY_FILE_NAME: 'YourLibrary.json'
	},
	DuplicateState: {
		NOT_DUPLICATE: 'NOT_DUPLICATE',
		EXACT_DUPLICATE: 'EXACT_DUPLICATE',
		POTENTIAL_DUPLICATE: 'POTENTIAL_DUPLICATE'
	},
	CompareState: {
		FIRST: 'FIRST',
		SECOND: 'SECOND',
		BOTH: 'BOTH'
	}
});

/**
 * Locale-aware case-insensitive string comparator
 */
export const stringComparator = new Intl.Collator(undefined, { caseFirst: false });

/**
 * Comparator for collections
 */
export const collectionComparator = (a, b) => {
	return stringComparator.compare(a.name, b.name);
};

/**
 * Comparator for tracks
 */
export const trackComparator = (a, b) => {
	// Note: duplicates/comparison algorithms rely on sorting logic, be careful when changing it
	return stringComparator.compare(a.artist, b.artist) ||
		stringComparator.compare(a.name, b.name) ||
		stringComparator.compare(a.album, b.album) ||
		stringComparator.compare(a.id, b.id);
};

/**
 * Sorts all collections and their lists of contained tracks
 */
export const sortCollectionsAndTracks = (collections) => {
	collections.sort(collectionComparator);
	for(const collection of collections) {
		collection.tracks.sort(trackComparator);
	}
};

/**
 * Helper to replace special characters in strings
 */
export const replaceSpecialCharsAndSpaces = (string) => {
	return string.replace(/[!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~]|\s/g, '');
};

/**
 * Finds a collection by name in a list of collections
 */
export const findCollection = (collections, collectionName) => {
	if(!collectionName) {
		throw Error('Emtpy collection name');
	}
	const findValue = collectionName.toLowerCase();
	const results = collections.filter((v) => v.name && findValue === v.name.toLowerCase());
	if(results.length === 0) {
		throw Error(`No collection with name "${collectionName}" was found`);
	}
	else if(results.length === 1) {
		return results[0];
	}
	else {
		throw Error(`There are ${results.length} collections with name "${collectionName}"`);
	}
};
