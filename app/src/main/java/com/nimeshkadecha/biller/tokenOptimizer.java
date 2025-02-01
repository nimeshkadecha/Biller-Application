package com.nimeshkadecha.biller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class tokenOptimizer {

	/**
		* Converts the given input JSON into the desired output format.
		*
		* Expected output structure:
		* {
		*   "biz": {
		*     "prods": {
		*         "1": [productName, category, purchasePrice, sellingPrice, availableStock, GST],
		*         ...
		*     },
		*     "custs": {
		*         "1": [customerName, customerNumber],
		*         ...
		*     },
		*     "sales": [
		*         [date, billId, productId, customerId, quantity, price, subtotal, GST],
		*         ...
		*     ],
		*     "purchases": [
		*         [date, stockId, productId, purchasePrice, sellingPrice, quantity, GST],
		*         ...
		*     ]
		*   }
		* }
		*
		* If an expected key is missing from the input JSON, that section will be empty.
		*
		* @param inputJson the original JSON string.
		* @return the converted JSON string.
		* @throws JSONException if JSON parsing fails.
		*/
	public static String convertJson(String inputJson) throws JSONException {
		JSONObject inputObj = new JSONObject(inputJson);

		// Use empty arrays/objects if the expected key is missing
		JSONArray productData = inputObj.optJSONArray("productData");
		JSONArray stockData = inputObj.optJSONArray("stockData");
		JSONArray stockQuantityData = inputObj.optJSONArray("stockQuantityData");
		JSONArray customerData = inputObj.optJSONArray("customerData");
		JSONArray salesData = inputObj.optJSONArray("salesData");

		// --- Build a map for stockData (purchase entries) by productId if available ---
		JSONObject stockMap = new JSONObject(); // key: productId, value: stockData JSONObject
		if (stockData != null) {
			for (int i = 0; i < stockData.length(); i++) {
				JSONObject stockEntry = stockData.getJSONObject(i);
				String productId = stockEntry.optString("productId", "");
				if (!productId.isEmpty() && !stockMap.has(productId)) {
					stockMap.put(productId, stockEntry);
				}
			}
		}

		// --- Build a map for stockQuantityData (available stock) by productId if available ---
		JSONObject stockQuantityMap = new JSONObject(); // key: productId, value: stockQuantityData JSONObject
		if (stockQuantityData != null) {
			for (int i = 0; i < stockQuantityData.length(); i++) {
				JSONObject sqEntry = stockQuantityData.getJSONObject(i);
				String productId = sqEntry.optString("productId", "");
				if (!productId.isEmpty() && !stockQuantityMap.has(productId)) {
					stockQuantityMap.put(productId, sqEntry);
				}
			}
		}

		// --- Build "prods" object using productData ---
		JSONObject prods = new JSONObject();
		if (productData != null) {
			for (int i = 0; i < productData.length(); i++) {
				JSONObject prodEntry = productData.getJSONObject(i);
				String productId = prodEntry.optString("productId", "");
				String productName = prodEntry.optString("productName", "");
				String category = prodEntry.optString("category", "");

				// Get purchase info (if available) from stockData map
				int purchasePrice = 0;
				int sellingPrice = 0;
				int gst = 0;
				if (stockMap.has(productId)) {
					JSONObject stockEntry = stockMap.getJSONObject(productId);
					purchasePrice = parseIntSafe(stockEntry.optString("purchasePrice", "0"));
					sellingPrice = parseIntSafe(stockEntry.optString("sellingPrice", "0"));
					gst = parseIntSafe(stockEntry.optString("Gst", "0"));
				}

				// Get available stock quantity (if available)
				int availableStock = 0;
				if (stockQuantityMap.has(productId)) {
					JSONObject sqEntry = stockQuantityMap.getJSONObject(productId);
					availableStock = parseIntSafe(sqEntry.optString("quantity", "0"));
				}

				// Build the array for this product:
				// [productName, category, purchasePrice, sellingPrice, availableStock, GST]
				JSONArray prodArray = new JSONArray();
				prodArray.put(productName);
				prodArray.put(category);
				prodArray.put(purchasePrice);
				prodArray.put(sellingPrice);
				prodArray.put(availableStock);
				prodArray.put(gst);

				prods.put(productId, prodArray);
			}
		}

		// --- Build "custs" object from customerData (if available) ---
		JSONObject custs = new JSONObject();
		if (customerData != null) {
			for (int i = 0; i < customerData.length(); i++) {
				JSONObject custEntry = customerData.getJSONObject(i);
				String customerId = custEntry.optString("customerId", "");
				String customerName = custEntry.optString("customerName", "");
				String customerNumber = custEntry.optString("customerNumber", "");

				JSONArray custArray = new JSONArray();
				custArray.put(customerName);
				custArray.put(customerNumber);

				custs.put(customerId, custArray);
			}
		}

		// --- Build "sales" array from salesData (if available) ---
		JSONArray sales = new JSONArray();
		if (salesData != null) {
			for (int i = 0; i < salesData.length(); i++) {
				JSONObject saleEntry = salesData.getJSONObject(i);
				String date = saleEntry.optString("date", "");
				int billId = parseIntSafe(saleEntry.optString("billId", "0"));
				int productId = parseIntSafe(saleEntry.optString("productId", "0"));
				int customerId = parseIntSafe(saleEntry.optString("customerId", "0"));
				int quantity = parseIntSafe(saleEntry.optString("quantity", "0"));
				int price = parseIntSafe(saleEntry.optString("price", "0"));
				int subtotal = parseIntSafe(saleEntry.optString("subtotal", "0"));
				int saleGst = parseIntSafe(saleEntry.optString("Gst", "0"));

				// Build sale array: [date, billId, productId, customerId, quantity, price, subtotal, GST]
				JSONArray saleArray = new JSONArray();
				saleArray.put(date);
				saleArray.put(billId);
				saleArray.put(productId);
				saleArray.put(customerId);
				saleArray.put(quantity);
				saleArray.put(price);
				saleArray.put(subtotal);
				saleArray.put(saleGst);

				sales.put(saleArray);
			}
		}

		// --- Build "purchases" array from stockData (if available) ---
		JSONArray purchases = new JSONArray();
		if (stockData != null) {
			for (int i = 0; i < stockData.length(); i++) {
				JSONObject purchaseEntry = stockData.getJSONObject(i);
				String date = purchaseEntry.optString("date", "");
				int stockId = parseIntSafe(purchaseEntry.optString("stockId", "0"));
				int productId = parseIntSafe(purchaseEntry.optString("productId", "0"));
				int purchasePrice = parseIntSafe(purchaseEntry.optString("purchasePrice", "0"));
				int sellingPrice = parseIntSafe(purchaseEntry.optString("sellingPrice", "0"));
				int quantity = parseIntSafe(purchaseEntry.optString("quantity", "0"));
				int purchaseGst = parseIntSafe(purchaseEntry.optString("Gst", "0"));

				// Build purchase array: [date, stockId, productId, purchasePrice, sellingPrice, quantity, GST]
				JSONArray purchaseArray = new JSONArray();
				purchaseArray.put(date);
				purchaseArray.put(stockId);
				purchaseArray.put(productId);
				purchaseArray.put(purchasePrice);
				purchaseArray.put(sellingPrice);
				purchaseArray.put(quantity);
				purchaseArray.put(purchaseGst);

				purchases.put(purchaseArray);
			}
		}

		// --- Build final "biz" object ---
		JSONObject biz = new JSONObject();
		biz.put("prods", prods);
		biz.put("custs", custs);
		biz.put("sales", sales);
		biz.put("purchases", purchases);

		JSONObject outputObj = new JSONObject();
		outputObj.put("biz", biz);

		return outputObj.toString();
	}

	/**
		* Helper method to safely parse a string into an int.
		* If the number is in a floating-point format, it will be converted to int.
		* Returns 0 if parsing fails.
		*
		* @param s the string to parse.
		* @return the parsed integer.
		*/
	private static int parseIntSafe(String s) {
		try {
			Double d = Double.parseDouble(s);
			return d.intValue();
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
