import fs from 'node:fs';
import path from 'node:path';
import { Constants } from './util.js';

/**
 * Checks for a valid input folder (Sporify export)
 */
export const checkInputFolder = (inputFolder) => {
	if(!fs.existsSync(inputFolder)) {
		throw Error(`Input folder "${inputFolder}" does not exist!`);
	}

	if(!fs.lstatSync(inputFolder).isDirectory()) {
		throw Error(`Input folder "${inputFolder}" is not a folder!`);
	}

	const userDataFilePath = path.join(inputFolder, Constants.Files.USER_DATA_FILE_NAME);
	if(!fs.existsSync(userDataFilePath)) {
		throw Error(`Input folder "${inputFolder}" does not seem to contain a valid Sporify export!`);
	}
};

/**
 * Checks for a valid output folder
 */
export const checkOutputFolder = (outputFolder) => {
	if(!fs.existsSync(outputFolder)) {
		throw Error(`Output folder "${outputFolder}" does not exist!`);
	}

	if(!fs.lstatSync(outputFolder).isDirectory()) {
		throw Error(`Output folder "${outputFolder}" is not a folder!`);
	}
};
