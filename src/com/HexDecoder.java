package com;

import java.util.LinkedHashMap;
import java.util.Map;

public class HexDecoder {
	public static Converter converter = new Converter();
	private int dataLength, currentPosition;
	private String eHeader, MTI, primaryBitMap, secondaryBitmap, consolidatedBitmap;
	Map<String, String> bitFieldwithValues = new LinkedHashMap<String, String>();

	public HexDecoder(String hexData) {
		try {
			decodedData(hexData);
		} catch (NumberFormatException e) {
			System.out.println("Request packet Format error. Please make sure the Hex data is correct");
		}

	}

	public int getDataLength() {
		return dataLength;
	}

	public String geteHeader() {
		return eHeader;
	}

	public String getMTI() {
		return MTI;
	}

	public String getPrimaryBitMap() {
		return primaryBitMap;
	}

	public String getSecondaryBitmap() {
		return secondaryBitmap;
	}

	public String getConsolidatedBitmap() {
		return consolidatedBitmap;
	}

	public Map<String, String> getBitFieldwithValues() {
		return bitFieldwithValues;
	}

	// -----------------------------------------------------------------------------------------------------------
	/*
	 * Takes the hex array as input, splits the data and convets into ascii values
	 */
	// -----------------------------------------------------------------------------------------------------------
	public void decodedData(String hexData) {
		Boolean isSecondaryBitmapAvailable = false;
		String hexDataLengthValue, primaryBitmapValue, secondaryBitmapValue;

		MTI = hexData.substring(0, 4);
		System.out.println("MTI " + MTI);
		// Grep the primary bitmap from hexData
		primaryBitmapValue = hexData.substring(4, 20);
		primaryBitmapValue = converter.addSpacesToString(primaryBitmapValue);
		primaryBitMap = converter.hexToBinary(primaryBitmapValue);
		currentPosition = 20;
		System.out.println("Primary bitmap has been set as " + primaryBitmapValue);
		// While creating a bitfieldAndValueMapping spaces in the hexdata will be
		// removed.
		// Hence current position should be calculated by removing spaces.
		if (primaryBitMap.charAt(0) == '1') {
			isSecondaryBitmapAvailable = true;
			System.out.println("Secondary bitmap is available");
		}

		// Grep the secondary bitmap from hex Data if available
		if (isSecondaryBitmapAvailable) {
			secondaryBitmapValue = hexData.substring(20, 36);
			secondaryBitmapValue = converter.addSpacesToString(secondaryBitmapValue);
			secondaryBitmap = converter.hexToBinary(secondaryBitmapValue);
			currentPosition = 36;
			System.out.println("Secondary Bitmap is set as " + secondaryBitmapValue);
		}

		// Bitmap consolidation
		if (isSecondaryBitmapAvailable) {
			consolidatedBitmap = primaryBitMap.replaceAll("\\s", "") + secondaryBitmap.replaceAll("\\s", "");
		} else {
			consolidatedBitmap = primaryBitMap.replaceAll("\\s", "");
		}
		System.out.println("Consolidated bitmap is set as " + consolidatedBitmap);
		System.out.println("Trying to identify the bitfields with values");
		bitFieldwithValues = bitfieldAndValueMapping(consolidatedBitmap, hexData);
		// identify the bitfields involved in the transaction
		for (Map.Entry<String, String> currentEntry : bitFieldwithValues.entrySet()) {

			System.out.println(currentEntry.getKey() + ":" + currentEntry.getValue());
			
		}
		
	}

	// --------------------------------------------------------------------------------------------------
	/*
	 * This function is used to identify the bitfields involved in the transaction
	 * Takes the consolidated bitmap as input and returns a string with integers
	 * representing bitfields
	 */
	// ---------------------------------------------------------------------------------------------------
	public String getElementsInTransaction(String bitmap) {
		String elementsInTransaction = "";
		for (int i = 0; i < bitmap.length(); i++) {
			if (bitmap.charAt(i) == '1') {
				elementsInTransaction = elementsInTransaction + (i + 1) + " ";
			}
		}
		return elementsInTransaction;
	}

	// -----------------------------------------------------------------------------------------------------
	/*
	 * This function is used to process the bitmap and identify the values
	 * associated with bitfields Takes consolidated bitmap involved in the
	 * transaction as input. Returns a linked hashmap which has bitfields as key and
	 * corresponding values as hashmap value
	 */
	// ------------------------------------------------------------------------------------------------------
	
	public Map<String, String> bitfieldAndValueMapping(String consolidatedBitmap, String hexData) {
		Map<String, String> bitfieldAndValueMap = new LinkedHashMap<String, String>();
		String tempString = getElementsInTransaction(consolidatedBitmap);
		String tempHexData = hexData.replaceAll("\\s", "");
		BitFieldData bitfieldLength = new BitFieldData();
		String[] elements = tempString.split(" ");
		for (String element : elements) {
			String currentBitField = "BITFIELD" + element, currentBitFieldValue;
			System.out.println("Trying to add " + currentBitField + " and its values to the map");
			int currentBitfieldLength = (bitfieldLength.bitfieldLength.get(currentBitField));
			if (currentBitfieldLength > 0 && (currentBitField.equals("BITFIELD1")) == false) {
				currentBitFieldValue = tempHexData.substring(currentPosition, currentPosition + currentBitfieldLength);
				bitfieldAndValueMap.put(currentBitField, currentBitFieldValue);
				System.out.println(currentBitField + " with value " + currentBitFieldValue + " successfully added");
				currentPosition = currentPosition + currentBitfieldLength;
			} else if (currentBitfieldLength == -2) {
				currentBitfieldLength = Integer.parseInt(tempHexData.substring(currentPosition, currentPosition + 2));
				currentPosition = currentPosition + 2;
				currentBitFieldValue = tempHexData.substring(currentPosition, currentPosition + currentBitfieldLength);
				tempString = (Integer.toString(currentBitfieldLength));
				if (tempString.length() < 2) {
					tempString = "0" + tempString;
				}
				currentBitFieldValue = tempString + currentBitFieldValue;
				bitfieldAndValueMap.put(currentBitField, currentBitFieldValue);
				System.out.println(currentBitField + " with value " + currentBitFieldValue + " successfully added");
				currentPosition = currentPosition + currentBitfieldLength;
			} else if (currentBitfieldLength == -3) {

				currentBitfieldLength = Integer.parseInt(tempHexData.substring(currentPosition, currentPosition + 3));
				currentPosition = currentPosition + 3;
				currentBitFieldValue = tempHexData.substring(currentPosition, currentPosition + currentBitfieldLength);
				tempString = (Integer.toString(currentBitfieldLength));
				if (tempString.length() < 3) {
					if (tempString.length() < 2) {
						tempString = "00" + tempString;
					} else {
						tempString = "0" + tempString;
					}
				}
				currentBitFieldValue = tempString + currentBitFieldValue;
				bitfieldAndValueMap.put(currentBitField, currentBitFieldValue);
				System.out.println(currentBitField + " with value " + currentBitFieldValue + " successfully added");
				currentPosition = currentPosition + currentBitfieldLength;
			} else if (currentBitfieldLength == -4) {

				currentBitfieldLength = Integer.parseInt(tempHexData.substring(currentPosition, currentPosition + 4));
				currentPosition = currentPosition + 4;
				currentBitFieldValue = tempHexData.substring(currentPosition, currentPosition + currentBitfieldLength);
				tempString = (Integer.toString(currentBitfieldLength));
				if (tempString.length() < 4) {
					if (tempString.length() < 3) {
						tempString = "000" + tempString;
					} else if (tempString.length() < 2) {
						tempString = "00" + tempString;
					} else {
						tempString = "0" + tempString;
					}
				}
				currentBitFieldValue = tempString + currentBitFieldValue;
				bitfieldAndValueMap.put(currentBitField, currentBitFieldValue);
				System.out.println(currentBitField + " with value " + currentBitFieldValue + " successfully added");
				currentPosition = currentPosition + currentBitfieldLength;

			}

		}

		return bitfieldAndValueMap;
	}

}