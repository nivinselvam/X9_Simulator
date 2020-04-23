package com;

public class Main {

	static String fepName = "X9";
	//static String elementsInTransaction = "3 4 11 12 22 24 26 35 41 42";
	public static void main(String[] args) {
		
		String requestpacket = "2200 3030054020C00002 200030 8402000000000099 023576189037 2003103117423308000000200000004000000000000000 200 5499 376357890012348779=99120001234567890123 00000000C123X3451500346782ARST119 011O01461O\\\\99";
		requestpacket = requestpacket.replaceAll(" ", "");
		HexDecoder decoder = new HexDecoder(requestpacket);
		//HexEncoder encoder = new HexEncoder();
		//String binarystring =  encoder.generateBinaryData(elementsInTransaction);
		//Converter conv = new Converter();
		//System.out.println(conv.binaryToHex(binarystring));
	}

}
