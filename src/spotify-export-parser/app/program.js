
import { Command } from 'commander';
import { findDuplicates } from './logic/find-duplicates.js';
import { compareCollections, compareExports } from './logic/compare-collections.js';
import { convertToExcel } from './logic/convert-to-excel.js';

/**
 * Helper to run the command logic inside a custom try-catch
 */
const runCommandLogic = (options, logicFunction) => {
	try {
		logicFunction();
	}
	catch(e) {
		console.error(`An error occurred: ${e.message || JSON.stringify(e)}`);
		if(options.verbose) {
			console.error(e);
		}
	}
};

/**
 * Defines all program commands
 */
export const runCommand = () => {
	const program = new Command();

	program
		.command('convert-to-excel')
		.description('Prints all playlists (including the Liked / Your Library collection) as an Excel file')
		.argument('<input-folder>', 'Spotify export folder path')
		.argument('<output-folder>', 'Folder for the output file')
		.option('-v, --verbose', 'Diplay full console output')
		.action((inputFolder, outputFolder, options) => {
			runCommandLogic(options, () => {
				convertToExcel(inputFolder, outputFolder);
			});
		});

	program
		.command('find-duplicates')
		.description('Check for duplicate songs in each playlist (including the Liked / Your Library collection)')
		.argument('<input-folder>', 'Spotify export folder path')
		.argument('<output-folder>', 'Folder for the output file')
		.option('-v, --verbose', 'Diplay full console output')
		.action((inputFolder, outputFolder, options) => {
			runCommandLogic(options, () => {
				findDuplicates(inputFolder, outputFolder);
			});
		});

	program
		.command('compare-collections')
		.description('Compares two playlists')
		.argument('<input-folder>', 'Spotify export folder path')
		.argument('<output-folder>', 'Folder for the output file')
		.argument('<first-playlist>', 'The name of the first collection (use "Liked" for the Liked / Your Library playlist)')
		.argument('<second-playlist>', 'The name of the second collection (use "Liked" for the Liked / Your Library playlist)')
		.option('-v, --verbose', 'Diplay full console output')
		.action((inputFolder, outputFolder, firstPlaylist, secondPlaylist, options) => {
			runCommandLogic(options, () => {
				compareCollections(inputFolder, outputFolder, firstPlaylist, secondPlaylist);
			});
		});

	program
		.command('compare-exports')
		.description('Compares each playlist (including the Liked / Your Library collection) in two different exports')
		.argument('<first-input-folder>', 'The first Spotify export folder path')
		.argument('<second-input-folder>', 'The second Spotify export folder path')
		.argument('<output-folder>', 'Folder for the output file')
		.option('-v, --verbose', 'Diplay full console output')
		.action((firstInputFolder, secondInputFolder, outputFolder, options) => {
			runCommandLogic(options, () => {
				compareExports(firstInputFolder, secondInputFolder, outputFolder);
			});
		});

	program.parse();
};
