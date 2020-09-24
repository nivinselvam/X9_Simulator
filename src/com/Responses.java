//
/*
 * This file is used for generating the responses for the transaction requests.
 * Constructor of this class requires the request packet to be fed in form of string.
 * Identifies the MTI from the request packet and decides the response accordingly.
 */
//
package com;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import com.HexDecoder;

public class Responses {
	private String requestPacket, eHeader, requestMTI;
	Map<String, String> requestBitfieldsWithValues, responseBitfieldswithValue;
	TreeSet<Integer> elementsInTransaction;
	SimpleDateFormat sdf;
	Date date = new Date();
	HexDecoder decoder;

	public Responses(String requestPacket) {
		this.requestPacket = requestPacket;

	}

	public Map<String, String> getResponseBitfieldswithValue() {
		return responseBitfieldswithValue;
	}

	// ------------------------------------------------------------------------------------------------------------------
	/*
	 * This method identifies the type of transaction and generates the response
	 * based on it
	 */
	// -------------------------------------------------------------------------------------------------------------------
	public String getResponsePacket() {
		String responsePacket = "";
		decoder = new HexDecoder(this.requestPacket);
		requestMTI = decoder.getMTI();
		requestBitfieldsWithValues = decoder.getBitFieldwithValues();
		System.out.println("Request Packet");
		responseBitfieldswithValue = new TreeMap<>(new BitfieldComparator());

		if (requestMTI.equals(Constants.authorisationRequestMTI)) {
			responsePacket = authorizationMessageResponse();
		} else if (requestMTI.equals(Constants.financialSalesRequestMTI)
				|| requestMTI.equals(Constants.financialForceDraftRequestMTI)) {
			responsePacket = financialMessageResponse();
		} else if (requestMTI.equals(Constants.reversalRequestMTI)) {
			responsePacket = reversalMessageResponse();
		} else if (requestMTI.equals(Constants.reconsillationRequestMTI)) {
			responsePacket = reconciliationMessageResponse();
		} else {
			System.out.println("Provided MTI is invalid for creating the response packet.");
		}

		decoder = new HexDecoder(responsePacket);

		return responsePacket;
	}

	// ------------------------------------------------------------------------------------------------------------------
	/*
	 * This method is used to generate the response for a reversal request(1100)
	 * Authorization request could be of two types. 1) Preauth 2) Balance Inquiry In
	 * case of balance inquiry Bitfield3 would denote the transaction type.
	 */
	// ------------------------------------------------------------------------------------------------------------------
	public String authorizationMessageResponse() {

		elementsInTransaction = new TreeSet<>(Arrays.asList(Constants.elementsInAuthorisationTransaction));
		String transactionResult = "", responsePacket = "", bitmap, bitfield4 = "", responseMTI = "";
		generateResponseBitfieldswithValue(elementsInTransaction);
		// Setting the response MTI bases on the request MTI. Two conditions are checked
		// since financial transaction can have sales and force draft requests
		if (requestMTI.equals(Constants.authorisationRequestMTI)) {
			responseMTI = Constants.authorisationResponseMTI;
			if (Main.isGUI) {

				transactionResult = Main.window.cbxTransactionResult.getSelectedItem().toString();
				if (transactionResult.equals("Approve")) {

					if (Integer.parseInt(Main.window.txtApprovalAmount.getText()) > Integer
							.parseInt(requestBitfieldsWithValues.get(Constants.nameOfbitfield4).substring(4))) {

						transactionResult = "PartiallyApprove";
					}
				}

			} else {
				transactionResult = Constants.authorizationTransactionResponse;
			}

		}
		if (transactionResult.equals("PartiallyApprove")) {
			transactionResult = "PartiallyApprove";
		}

		switch (transactionResult) {
		case "Approve":
			if (!Main.isGUI) {
				responseBitfieldswithValue.put(Constants.nameOfbitfield39,
						setBitfieldValue(Constants.nameOfbitfield39, Constants.ValueOfBitfield39Approval));
			}
			break;
		case "Decline":
			if (!Main.isGUI) {
				responseBitfieldswithValue.put(Constants.nameOfbitfield39,
						setBitfieldValue(Constants.nameOfbitfield39, Constants.ValueOfBitfield39Decline));
			}
			break;
		case "PartiallyApprove":
			if (Main.isGUI) {
				bitfield4 = Main.window.txtApprovalAmount.getText();
			}
			bitfield4 = generateHalfAmountForPartialApproval(requestBitfieldsWithValues.get(Constants.nameOfbitfield4));
			responseBitfieldswithValue.put(Constants.nameOfbitfield4,
					requestBitfieldsWithValues.get(Constants.nameOfbitfield4).substring(0, 4) + bitfield4);
			if (!Main.isGUI) {
				responseBitfieldswithValue.put(Constants.nameOfbitfield39,
						setBitfieldValue(Constants.nameOfbitfield39, Constants.ValueOfBitfield39Partial));
			}
			break;
		}

		if (Main.isGUI) {
			responseBitfieldswithValue.put(Constants.nameOfbitfield39, Main.window.txtResponseCode.getText());
		}
		// Bitfields for which the values should be generated.
		if (transactionResult.equals("Approve") || transactionResult.equals("PartiallyApprove")) {
			responseBitfieldswithValue.put(Constants.nameOfbitfield38,
					setBitfieldValue(Constants.nameOfbitfield38, Constants.valueOfBitfield38));
			elementsInTransaction.add(38);
			
			String valueOf63 = requestBitfieldsWithValues.get(Constants.nameOfbitfield63);	
			if (null != valueOf63 && valueOf63.contains("\\950O01\\")){	
				responseBitfieldswithValue.put(Constants.nameOfbitfield62,	
						setBitfieldValue(Constants.nameOfbitfield62, Constants.valueOfBitfield62));	
				elementsInTransaction.add(62);	
			}
		}
		if (transactionResult.equals("Decline")) {
			responseBitfieldswithValue.put(Constants.nameOfbitfield44,
					setBitfieldValue(Constants.nameOfbitfield44, Constants.valueOfBitfield44));
			elementsInTransaction.add(44);
		}

		addFEPSpecificElements(Constants.authorisationResponseMTI, transactionResult);
		HexEncoder encoder = new HexEncoder(responseMTI);
		bitmap = encoder.tgenerateBinaryData(elementsInTransaction);
		encoder.setBitmap(bitmap);
		encoder.setResponseBitFieldsWithValue(responseBitfieldswithValue);
		encoder.encodeddata();
		responsePacket = encoder.getEncodedHexData();
		System.out.println(responsePacket);
		return responsePacket;
	}

	// ------------------------------------------------------------------------------------------------------------------
	/*
	 * This method is used to generate the response for a financial request(1200),
	 * (1220)
	 */
	// ------------------------------------------------------------------------------------------------------------------
	public String financialMessageResponse() {
		elementsInTransaction = new TreeSet<>(Arrays.asList(Constants.elementsInFinancialTransaction));
		String transactionResult = "", responsePacket = "", bitmap, bitfield4 = "", responseMTI = "";
		generateResponseBitfieldswithValue(elementsInTransaction);
		// Setting the response MTI bases on the request MTI. Two conditions are checked
		// since financial transaction can have sales and force draft requests
		if (requestMTI.equals(Constants.financialSalesRequestMTI)) {
			responseMTI = Constants.financialSalesResponseMTI;
			transactionResult = Constants.financialSalesTransactionResponse;
			if(transactionResult.equals("Approve")||transactionResult.equals("PartiallyApprove")){
				
				String valueOf63 = requestBitfieldsWithValues.get(Constants.nameOfbitfield63);	
				if (null != valueOf63 && valueOf63.contains("\\950O01\\")){	
					responseBitfieldswithValue.put(Constants.nameOfbitfield62,	
							setBitfieldValue(Constants.nameOfbitfield62, Constants.valueOfBitfield62));	
					elementsInTransaction.add(62);	
				}
			}
		} else if (requestMTI.equals(Constants.financialForceDraftRequestMTI)) {
			responseMTI = Constants.financialForceDraftResponseMTI;
			transactionResult = Constants.financialForceDraftTransactionResponse;

			if (transactionResult.equals("PartiallyApprove")) {
				transactionResult = "PartiallyApprove";
			}
		}

		switch (transactionResult) {
		case "Approve":
			responseBitfieldswithValue.put(Constants.nameOfbitfield39,
					setBitfieldValue(Constants.nameOfbitfield39, Constants.ValueOfBitfield39Approval));
			break;
		case "Decline":
			responseBitfieldswithValue.put(Constants.nameOfbitfield39,
					setBitfieldValue(Constants.nameOfbitfield39, Constants.ValueOfBitfield39Decline));
			break;
		case "PartiallyApprove":
			bitfield4 = generateHalfAmountForPartialApproval(requestBitfieldsWithValues.get(Constants.nameOfbitfield4));
			responseBitfieldswithValue.put(Constants.nameOfbitfield4,
					requestBitfieldsWithValues.get(Constants.nameOfbitfield4).substring(0, 4) + bitfield4);
			responseBitfieldswithValue.put(Constants.nameOfbitfield39,
					setBitfieldValue(Constants.nameOfbitfield39, Constants.ValueOfBitfield39Partial));
			break;
		}
		// Bitfields for which the values should be generated.

		if (transactionResult.equals("Approve") || transactionResult.equals("PartiallyApprove")) {
			responseBitfieldswithValue.put(Constants.nameOfbitfield38,
					setBitfieldValue(Constants.nameOfbitfield38, Constants.valueOfBitfield38));
			elementsInTransaction.add(38);
		}
		if (transactionResult.equals("Decline")) {
			responseBitfieldswithValue.put(Constants.nameOfbitfield44,
					setBitfieldValue(Constants.nameOfbitfield44, Constants.valueOfBitfield44));
			elementsInTransaction.add(44);
		}
		if (requestMTI.equals(Constants.financialSalesRequestMTI)) {
			addFEPSpecificElements(Constants.financialSalesRequestMTI, transactionResult);
		} else {
			addFEPSpecificElements(Constants.financialForceDraftRequestMTI, transactionResult);
		}

		HexEncoder encoder = new HexEncoder(responseMTI);
		bitmap = encoder.tgenerateBinaryData(elementsInTransaction);
		encoder.setBitmap(bitmap);
		encoder.setResponseBitFieldsWithValue(responseBitfieldswithValue);
		encoder.encodeddata();
		responsePacket = encoder.getEncodedHexData();
		System.out.println(responsePacket);
		return responsePacket;
	}

	// ------------------------------------------------------------------------------------------------------------------
	/*
	 * This method is used to generate the response for a reversal request(1420)
	 */
	// ------------------------------------------------------------------------------------------------------------------
	public String reversalMessageResponse() {
		String responsePacket = "", bitmap, bitfield39 = "";
		elementsInTransaction = new TreeSet<>(Arrays.asList(Constants.elementsInReversalTransaction));
		generateResponseBitfieldswithValue(elementsInTransaction);
		String transactionResult = "", bitfield4 = "", responseMTI = "";
		if (requestMTI.equals(Constants.reversalRequestMTI)) {
			responseMTI = Constants.reversalResponseMTI;
			transactionResult = Constants.reversalTransactionResponse;
		}

		// Bitfields for which the values should be generated.

		switch (transactionResult) {
		case "Approve":
			responseBitfieldswithValue.put(Constants.nameOfbitfield39,
					setBitfieldValue(Constants.nameOfbitfield39, Constants.ValueOfBitfield39Reversal));
			break;
		case "Decline":
			responseBitfieldswithValue.put(Constants.nameOfbitfield39,
					setBitfieldValue(Constants.nameOfbitfield39, Constants.ValueOfBitfield39ReversalDecline));
			responseBitfieldswithValue.put(Constants.nameOfbitfield44,
					setBitfieldValue(Constants.nameOfbitfield44, Constants.valueOfBitfield44));
			elementsInTransaction.add(44);
			break;
		}

		addFEPSpecificElements(Constants.reversalRequestMTI, transactionResult);
		HexEncoder encoder = new HexEncoder(responseMTI);
		bitmap = encoder.tgenerateBinaryData(elementsInTransaction);
		encoder.setBitmap(bitmap);
		encoder.setResponseBitFieldsWithValue(responseBitfieldswithValue);
		encoder.encodeddata();
		responsePacket = encoder.getEncodedHexData();
		System.out.println(responsePacket);
		return responsePacket;
	}

	// ------------------------------------------------------------------------------------------------------------------
	/*
	 * This method is used to generate the response for a reconcillation request
	 */
	// ------------------------------------------------------------------------------------------------------------------
	public String reconciliationMessageResponse() {
		String responsePacket = "", bitmap, transactionResult = "", responseMTI = "";
		elementsInTransaction = new TreeSet<>(Arrays.asList((Constants.elementsInReconsillationTransaction)));
		generateResponseBitfieldswithValue(elementsInTransaction);
		// Bitfields for which the values should be generated.

		if (requestMTI.equals(Constants.reconsillationRequestMTI)) {
			responseMTI = Constants.reconsillationResponseMTI;
			transactionResult = "Approve";
		}

		switch (transactionResult) {
		case "Approve":
			responseBitfieldswithValue.put(Constants.nameOfbitfield39,
					setBitfieldValue(Constants.nameOfbitfield39, Constants.ValueOfBitfield39Reconsillation));
			break;
		case "Decline":
			responseBitfieldswithValue.put(Constants.nameOfbitfield39,
					setBitfieldValue(Constants.nameOfbitfield39, Constants.ValueOfBitfield39Reconsillation));
			responseBitfieldswithValue.put(Constants.nameOfbitfield123,
					setBitfieldValue(Constants.nameOfbitfield123, Constants.valueOfBitfield123));
			break;
		}

		addFEPSpecificElements(Constants.reconsillationRequestMTI, transactionResult);
		HexEncoder encoder = new HexEncoder(responseMTI);
		bitmap = encoder.tgenerateBinaryData(elementsInTransaction);
		encoder.setBitmap(bitmap);
		encoder.setResponseBitFieldsWithValue(responseBitfieldswithValue);
		encoder.encodeddata();
		responsePacket = encoder.getEncodedHexData();
		return responsePacket;
	}

	// ------------------------------------------------------------------------------------------------------------------
	/*
	 * This method is used to generate the response bitfield value. When we receive
	 * the request packet, bitfields will have the length prefixed. This has to be
	 * removed for the HexEncoder to generate the correct value.
	 */
	// ------------------------------------------------------------------------------------------------------------------
	public String removeLLVAR(String bitfield, String bitfieldValue) {
		StringBuffer updatedValue = new StringBuffer(bitfieldValue);
		BitFieldData bitfieldLength = new BitFieldData();
		if (bitfieldLength.bitfieldLength.get(bitfield) == -2) {
			updatedValue.delete(0, 2);
		} else if (bitfieldLength.bitfieldLength.get(bitfield) == -3) {
			updatedValue.delete(0, 3);
		}
		return updatedValue.toString();
	}

//	public String generateBitfield2(Map<String, String> requestPacketBitfields) {
//		int endPoint = 0;
//		String bitfield2Value = "", bitfield2Length = "";
//		if (requestPacketBitfields.containsKey(Constants.nameOfbitfield2)) {
//			return requestPacketBitfields.get(Constants.nameOfbitfield2);
//		} else if (requestPacketBitfields.containsKey(Constants.nameOfbitfield35)) {
//			endPoint = requestPacketBitfields.get(Constants.nameOfbitfield35).indexOf('=');
//			bitfield2Value = requestPacketBitfields.get(Constants.nameOfbitfield35).substring(2, endPoint);
//			bitfield2Length = Integer.toString(bitfield2Value.length());
//			if (bitfield2Length.length() < 2) {
//				return "0" + bitfield2Value.length() + bitfield2Value;
//			} else {
//				return bitfield2Value.length() + bitfield2Value;
//			}
//
//		} else if (requestPacketBitfields.containsKey(Constants.nameOfbitfield45)) {
//			endPoint = requestPacketBitfields.get(Constants.nameOfbitfield45).indexOf('^');
//			bitfield2Value = requestPacketBitfields.get(Constants.nameOfbitfield45).substring(3, endPoint);
//			bitfield2Length = Integer.toString(bitfield2Value.length());
//			if (bitfield2Length.length() < 2) {
//				return "0" + bitfield2Value.length() + bitfield2Value;
//			} else {
//				return bitfield2Value.length() + bitfield2Value;
//			}
//		}
//		return "";
//	}
//
	// ------------------------------------------------------------------------------------------------------------------
	/*
	 * This method is used to create a responsebitfield treemap. Treemap is used to
	 * make sure the bitfield values are sorted. Numbers of bitfields that are to be
	 * sent in response should be passed to the method.
	 */
	// ------------------------------------------------------------------------------------------------------------------
	public void generateResponseBitfieldswithValue(TreeSet<Integer> elementsInTransaction) {
		for (Integer currentEntry : elementsInTransaction) {
			String key = "BITFIELD" + currentEntry;
			responseBitfieldswithValue.put(key, requestBitfieldsWithValues.get(key));
			System.out.println(key + ":" + requestBitfieldsWithValues.get(key) + " added to the response bitfield map");
		}
	}

	// ------------------------------------------------------------------------------------------------------------------
	/*
	 * This method is used to generate a dynamic amount for partial approval. This
	 * takes transaction amount as input and returns half of it.
	 */
	// ------------------------------------------------------------------------------------------------------------------
	public String generateHalfAmountForPartialApproval(String transactionAmount) {
		// for the X9 last 12 digit of bitfield 4 signifies the amount.
		transactionAmount = transactionAmount.substring(4);
		String bitfield4 = Integer.toString(Integer.parseInt(transactionAmount) / 2);
		// Bitfield4 has a fixed length of 12 digits and has to have 0's for
		// the digits missing.
		int length = bitfield4.length();
		String tempString = "";
		for (int i = 0; i < 12 - length; i++) {
			tempString = tempString + "0";
		}
		return tempString + bitfield4;
	}

	// ------------------------------------------------------------------------------------------------------------------
	/*
	 * This method is used to add HPS specific data elements Transaction type i.e.,
	 * request MTI should be passed as argument
	 */
	// ------------------------------------------------------------------------------------------------------------------
	public void addFEPSpecificElements(String transactionType, String transactionResult) {
		if (Main.fepName.equals("X9")) {

			if (!requestMTI.equals("2420")) {

				responseBitfieldswithValue.put(Constants.nameOfbitfield7,
						setBitfieldValue(Constants.nameOfbitfield7, generateBitField7Value()));
			}

			if (requestBitfieldsWithValues.containsKey(Constants.nameOfbitfield68)) {

				responseBitfieldswithValue.put(Constants.nameOfbitfield68,
						setBitfieldValue(Constants.nameOfbitfield68,
								genrateBitFiled68Value(requestBitfieldsWithValues.get(Constants.nameOfbitfield68),
										requestBitfieldsWithValues.get(Constants.nameOfbitfield69))));
				elementsInTransaction.add(68);

			}

		}
	}
	// ------------------------------------------------------------------------------------------------------------------
	/*
	 * This method is used to identify the bitfield and add length of bitfield if
	 * required.
	 */
	// ------------------------------------------------------------------------------------------------------------------

	public static String setBitfieldValue(String bitfieldName, String bitfieldValue) {
		int variableLengthValue;
		String bitfieldLength;
		BitFieldData bitfieldData = new BitFieldData();
		variableLengthValue = bitfieldData.bitfieldLength.get(bitfieldName);
		if (variableLengthValue > 0) {
			return bitfieldValue;
		} else if (variableLengthValue == -2) {
			bitfieldLength = Integer.toString(bitfieldValue.length());
			if (bitfieldLength.length() < 2) {
				bitfieldLength = "0" + bitfieldLength;
			}
			return bitfieldLength + bitfieldValue;
		} else if (variableLengthValue == -3) {
			bitfieldLength = Integer.toString(bitfieldValue.length());
			if (bitfieldLength.length() < 3) {
				if (bitfieldLength.length() < 2) {
					bitfieldLength = "00" + bitfieldLength;
				}

				else {
					bitfieldLength = "0" + bitfieldLength;
				}

			}
			return bitfieldLength + bitfieldValue;

		} else if (variableLengthValue == -4) {
			bitfieldLength = Integer.toString(bitfieldValue.length());
			if (bitfieldLength.length() < 4) {
				if (bitfieldLength.length() < 3) {
					if (bitfieldLength.length() < 2) {
						bitfieldLength = "000" + bitfieldLength;
					} else {
						bitfieldLength = "00" + bitfieldLength;
					}

				}

				else {
					bitfieldLength = "0" + bitfieldLength;
				}

			}
			return bitfieldLength + bitfieldValue;

		} else {
			return bitfieldValue;
		}

	}

	/*
	 * to genrate bitfield 7 (date and time transmission)
	 */
	public String generateBitField7Value() {

		String bitfield7Value = "";

		SimpleDateFormat sdf = new SimpleDateFormat("MMddhhmmss");
		bitfield7Value = sdf.format(Calendar.getInstance().getTime());
		return bitfield7Value;

	}

	public String genrateBitFiled68Value(String bitField68, String bitField69) {

		StringBuffer value68 = new StringBuffer(bitField68);
		String value69 = bitField69.substring(0, 8);
		String convertedValueof69 = String.valueOf(Integer.parseInt(value69));
		int numberOfPlaces = convertedValueof69.length();
		value68.replace(0, numberOfPlaces, convertedValueof69);
		System.out.println(value68);
		return value68.toString();
	}

}