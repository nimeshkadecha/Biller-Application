package com.nimeshkadecha.biller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class DBManager extends SQLiteOpenHelper {

	public DBManager(Context context) {
//        Creating database with name = Biller
		super(context, "Biller", null, 1);
	}

	// convert scientific notation to normal notation =================================================
	public static String convertScientificToNormal(double scientificNotation) {
		BigDecimal bd = new BigDecimal(scientificNotation);
		bd = bd.setScale(2, RoundingMode.HALF_UP);
		return bd.toPlainString();
	}

	@Override
	public void onUpgrade(SQLiteDatabase DB, int oldVersion, int newVersion) {

		DB.execSQL("DROP TABLE IF EXISTS users"); // Seller (User)

		DB.execSQL("DROP TABLE IF EXISTS display"); // bill record

		DB.execSQL("DROP TABLE IF EXISTS products"); // product data

		// Stock history and records
		DB.execSQL("DROP TABLE IF EXISTS stock"); // stock history
		DB.execSQL("DROP TABLE IF EXISTS stockQuantity"); // current stock quantity

		// customer data along with there purchase history
		DB.execSQL("DROP TABLE IF EXISTS customer"); // customer bill data
		DB.execSQL("DROP TABLE IF EXISTS customers"); // all customer data
	}

// TODO Users Table ================================================================================

	// Insertion --------------------------------------------------------------------------------------

	@Override
	public void onCreate(SQLiteDatabase DB) {

		// record of seller(user) ========================================================================
		DB.execSQL("CREATE TABLE users(" +
										           " userId INTEGER PRIMARY KEY AUTOINCREMENT," +
										           " name TEXT," +
										           " email TEXT UNIQUE," +
										           " password TEXT," +
										           " gst TEXT," +
										           " contact TEXT," +
										           " address TEXT," +
										           " apiKey TEXT)"
		          );

		// record of each bills ==========================================================================
		DB.execSQL("CREATE TABLE display(" +
										           " indexs INTEGER PRIMARY KEY AUTOINCREMENT," + // AUTO
										           " productId INTEGER," + // from Product
										           " quantity INTEGER," +
										           " price REAL," +
										           " subtotal REAL," +
										           " customerId INTEGER," + // from Customers
										           " sellerId INTEGER," + // from Users
										           " date DATE," +
										           " billId INTEGER," +
										           " Gst INTEGER," +
										           " FOREIGN KEY(productId) REFERENCES products(productId)," +
										           " FOREIGN KEY(customerId) REFERENCES customers(customerId)," +
										           " FOREIGN KEY(sellerId) REFERENCES users(userId))"
		          );

// Product records =================================================================================
		DB.execSQL("CREATE TABLE products(" +
										           "productId INTEGER PRIMARY KEY AUTOINCREMENT, " +
										           "productName TEXT, " +
										           "category TEXT)"
		          );

		// Customer data =================================================================================
		DB.execSQL("CREATE TABLE customer(" +
										           " billId INTEGER PRIMARY KEY," +
										           " customerId INTEGER," +
										           " date DATE," +
										           " total REAL," +
										           " sellerId INTEGER," +
										           " FOREIGN KEY(customerId) REFERENCES customers(customerId)," +
										           " FOREIGN KEY(sellerId) REFERENCES users(userId))"
		          );

		DB.execSQL("CREATE TABLE customers(" +
										           " customerId INTEGER PRIMARY KEY AUTOINCREMENT," +
										           " customerName TEXT," +
										           " customerNumber TEXT, " +
										           "sellerId INTEGER )"
		          );

		// Stock control =================================================================================
		DB.execSQL("CREATE TABLE stock(" +
										           " stockId INTEGER PRIMARY KEY AUTOINCREMENT," +
										           " productId INTEGER," +
										           " purchasePrice REAL," +
										           " sellingPrice REAL," +
										           " date DATE," +
										           " quantity TEXT," +
										           " sellerId INTEGER," +
										           " Gst REAL," +
										           " FOREIGN KEY(productId) REFERENCES products(productId)," +
										           " FOREIGN KEY(sellerId) REFERENCES users(userId))"
		          );

		DB.execSQL("CREATE TABLE stockQuantity(" +
										           " stickTrackId INTEGER PRIMARY KEY AUTOINCREMENT," +
										           " productId INTEGER," +
										           " quantity TEXT," +
										           " price REAL," +
										           " sellerId INTEGER," +
										           " Gst INTEGER," +
										           " FOREIGN KEY(sellerId) REFERENCES users(userId))"
		          );

		// GEMINI chat ===================================================================================

		DB.execSQL("CREATE TABLE messages (" +
										           "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
										           "message TEXT NOT NULL, " +
										           "is_sent_by_user INTEGER NOT NULL, " +
										           "timestamp INTEGER NOT NULL, " +
										           "seller_id INTEGER NOT NULL, " +
										           "FOREIGN KEY (seller_id) REFERENCES users(userId))");
	}
	// ------------------------------------------------------------------------------------------------

	// Update -----------------------------------------------------------------------------------------

	// Register User [Insert - user ] *****************************************************************
	public boolean RegisterUser(String name, String email, String password, String gst, String contact, String address) {
		SQLiteDatabase DB = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("name", name);
		contentValues.put("email", email);
		contentValues.put("password", password);
		contentValues.put("gst", gst);
		contentValues.put("contact", contact);
		contentValues.put("address", address);
		contentValues.put("apikey", "");
		return DB.insert("users", null, contentValues) != -1;
	}

	// inserting API key ******************************************************************************
	public boolean insertApiKey(String apiKey, String email) {
		SQLiteDatabase DB = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("apiKey", apiKey);
		return DB.update("users", contentValues, "userId =?", new String[]{String.valueOf(get_userId(email))}) != -1;
	}

	//    Update Data *********************************************************************************
	//    [SELECT * From users where email =?]
	public boolean UpdateUser(String name, String email, String password, String gst, String contact, String address) {
		SQLiteDatabase DB = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("name", name);
		contentValues.put("password", password);
		contentValues.put("gst", gst);
		contentValues.put("contact", contact);
		contentValues.put("address", address);

		@SuppressLint("Recycle") Cursor cursor = DB.rawQuery("SELECT * From users where email =?", new String[]{email});
		if (cursor.getCount() > 0) {
			cursor.close();
			return DB.update("users", contentValues, "email =?", new String[]{email}) != -1;
		} else {
			cursor.close();
			return false;
		}
	}

	// ------------------------------------------------------------------------------------------------

	// Selection --------------------------------------------------------------------------------------

	//    Reset Password! *****************************************************************************
	//    [select * from users where contact = ?]
	public boolean ResetPassword(String Email, String password) {
		SQLiteDatabase DB = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("password", password);
		@SuppressLint("Recycle") Cursor cursor = DB.rawQuery("select * from users where email = ?", new String[]{Email});
		if (cursor.getCount() > 0) {
			cursor.close();
			return DB.update("users", contentValues, "email =?", new String[]{Email}) != -1;
		} else {
			cursor.close();
			return false;
		}
	}

	//    Getting user info for testing purposes in register ******************************************
	//    [select * from users]
	public Cursor getData() {
		SQLiteDatabase DB = this.getReadableDatabase();
		return DB.rawQuery("select * from users", null);
	}

	//    Login Verification **************************************************************************
	//    [select * from users where email =? AND password = ?]
	public boolean LoginUser(String email, String password) {
		SQLiteDatabase DB = this.getReadableDatabase();
		@SuppressLint("Recycle") Cursor cursor = DB.rawQuery("select * from users where email =? AND password = ?", new String[]{email, password});
		boolean ans = cursor.getCount() > 0;
		cursor.close();
		return ans;
	}

	//    Validate user *******************************************************************************
	//    [select * from users where email =?]
	public boolean ValidateUser(String email) {
		SQLiteDatabase DB = this.getReadableDatabase();
		@SuppressLint("Recycle") Cursor cursor = DB.rawQuery("select * from users where email =?", new String[]{email});
		boolean ans = cursor.getCount() > 0;
		cursor.close();
		return ans;
	}

	// fetching API key *******************************************************************************
	//    [select * from users where email =?]
	public String getApiKey(String email) {
		SQLiteDatabase DB = this.getReadableDatabase();
		@SuppressLint("Recycle") Cursor cursor = DB.rawQuery("select * from users where email =?", new String[]{email});
		cursor.moveToFirst();
		@SuppressLint("Range") String apiKey = cursor.getString(cursor.getColumnIndex("apiKey"));
		cursor.close();
		DB.close();
		return apiKey;
	}

	//    Getting specific user all DATA  *************************************************************
	//    [select * from users where email=?]
	public Cursor GetUser(String email) {
		SQLiteDatabase DB = this.getReadableDatabase();
		return DB.rawQuery("select * from users where email=?", new String[]{email});
	}

	// checking if GST is available or not =========================================================
	@SuppressLint("Range")
	public Boolean CheckGstAvailability(String email) {
		Cursor cursor = GetUser(email);
		cursor.moveToFirst();
		return !cursor.getString(cursor.getColumnIndex("gst")).equals("-1");
	}

	// ------------------------------------------------------------------------------------------------

	// Deletion ---------------------------------------------------------------------------------------

	// Get userID *************************************************************************************
	// [SELECT userId FROM users WHERE email = ?]
	@SuppressLint("Range")
	public int get_userId(String email) {
		SQLiteDatabase DB = this.getReadableDatabase();
		Cursor userId_C = DB.rawQuery("SELECT userId FROM users WHERE email = ?", new String[]{email});

		userId_C.moveToFirst();
		if (userId_C.getCount() > 0) {
			int ID = userId_C.getInt(userId_C.getColumnIndex("userId"));
			userId_C.close();
			return ID;
		}
		userId_C.close();
		return -1;
	}
	// ------------------------------------------------------------------------------------------------

// =================================================================================================

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// TODO Display table ==============================================================================

	// Insertion --------------------------------------------------------------------------------------

	//    Deleting User *******************************************************************************
	//    [select * from users where email = ?]
	public boolean DeleteUser(String email) {
		SQLiteDatabase DB = this.getWritableDatabase();

		String sellerId = String.valueOf(get_userId(email));

		@SuppressLint("Recycle") Cursor cursor = DB.rawQuery("select * from users where email = ?", new String[]{email});

		if (cursor.getCount() > 0) {
			cursor.close();
			long check, check1, check2 , check3 , check4 , check5, check6;
			check = DB.delete("users", "email = ?", new String[]{email});
			check1 = DB.delete("display", "sellerId = ?", new String[]{sellerId});
			check2 = DB.delete("customer", "sellerId = ?", new String[]{sellerId});
			check3 = DB.delete("customers", "sellerId = ?", new String[]{sellerId});
			check4 = DB.delete("stock", "sellerId = ?", new String[]{sellerId});
			check5 = DB.delete("stockQuantity", "sellerId = ?", new String[]{sellerId});
			check6 = DB.delete("messages", "seller_id = ?", new String[]{sellerId});
			return check != -1 && check1 != -1 && check2 != -1 && check3 != -1 && check4 != -1 && check5 != -1 && check6 != -1;
		} else {
			cursor.close();
			return false;
		}
	}

	// ------------------------------------------------------------------------------------------------

	// Update -----------------------------------------------------------------------------------------

	// inserting data in to display table *************************************************************
	// if the product exist then update the quantity
	@SuppressLint("Range")
	public boolean InsertList(String name, String price, String quantity, String cName, String cNumber, String date, int billId, String email, int state, String Gst) {

		double priceInt = Double.parseDouble(price);

		int quantityInt = Integer.parseInt(quantity);

		if (Gst.isEmpty()) Gst = "0";

		double tax = (priceInt * quantityInt) * (Double.parseDouble(Gst) / 100f);

		double subtotal = ((priceInt * quantityInt) + tax);

		SQLiteDatabase DB = this.getWritableDatabase();

		ContentValues contentValues = new ContentValues();

		//  Checking if the product is same them update the quantity
		Cursor displayTableData = DisplayList(billId); // display

		displayTableData.moveToFirst();

		int p_id = get_productId(name, "all");

		boolean check_if_already_added = false;
		int number_of_product = 0;
		String Index = null;

		if (displayTableData.getCount() > 0) {
			do {

				if (displayTableData.getString(displayTableData.getColumnIndex("productId")).equals(String.valueOf(p_id))) {
					check_if_already_added = true;
					Index = displayTableData.getString(displayTableData.getColumnIndex("indexs")); // getting index
					number_of_product = displayTableData.getInt(displayTableData.getColumnIndex("quantity"));
				}
			} while (displayTableData.moveToNext());
		}
		displayTableData.close();

		int s_id = get_userId(email);
		int c_id = get_customersId(cName, cNumber, s_id);

		if (check_if_already_added) {

			number_of_product += 1;
			double taxUpdate = (priceInt * number_of_product) * (Double.parseDouble(Gst) / 100f);
			subtotal = ((priceInt * number_of_product) + taxUpdate);

			String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");

			contentValues.put("productId", p_id); // product name and category
			contentValues.put("quantity", number_of_product);
			contentValues.put("price", price);
			contentValues.put("subtotal", subtotal);
			contentValues.put("customerId", c_id); // customer name and number
			contentValues.put("sellerId", s_id); // seller email
			contentValues.put("date", formattedDate);
			contentValues.put("billId", billId); // bill ID
			contentValues.put("Gst", Gst);

			return DB.update("display", contentValues, "indexs=?", new String[]{Index}) != -1;
		} else {

			String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");

			contentValues.put("productId", p_id);
			contentValues.put("quantity", Integer.parseInt(quantity));
			contentValues.put("price", price);
			contentValues.put("subtotal", subtotal);
			contentValues.put("customerId", c_id);
			contentValues.put("sellerId", s_id);
			contentValues.put("date", formattedDate);
			contentValues.put("billId", billId);
			contentValues.put("Gst", Gst);

			return DB.insert("display", null, contentValues) != -1;
		}
	}

	// ------------------------------------------------------------------------------------------------

	// Updating data in display table *****************************************************************
	// [Update Quantity in display where index =? ]
	public boolean UpdateQuantity(int quantity, double subtotal, int index) {
		SQLiteDatabase DB = this.getWritableDatabase();

		ContentValues contentValues = new ContentValues();
		contentValues.put("quantity", String.valueOf(quantity));
		contentValues.put("subtotal", subtotal);

		return DB.update("display", contentValues, "indexs =?", new String[]{String.valueOf(index)}) != -1;
	}

	// Selection --------------------------------------------------------------------------------------
	// Getting sum of total in display table with bill id *********************************************
	// [select * from display where billId =? ]
	@SuppressLint("Range")
	public String CheckTotal(int billID) {
		Cursor displayList_subtotal = DisplayList(billID);
		double total = 0d;
		displayList_subtotal.moveToFirst();
		if (displayList_subtotal.getCount() == 0) {
			displayList_subtotal.close();
			return "-1";
		}
		do {
			total += displayList_subtotal.getDouble(displayList_subtotal.getColumnIndex("subtotal"));
		} while (displayList_subtotal.moveToNext());
		displayList_subtotal.close();
		return convertScientificToNormal(total);
	}

	// Getting sub total from billID*******************************************************************
	//    [ select * from display where billId = ? ]
	public Cursor GetSubTotal(int billID) {
		SQLiteDatabase DB = this.getReadableDatabase();

		return DB.rawQuery("select * from display where billId = ? ", new String[]{String.valueOf(billID)});
	}

	// confirming that data is entered ****************************************************************
	public boolean ConfirmSale(int billId) {
		Cursor cursor = GetSubTotal(billId);
		boolean checkResult = cursor.getCount() > 0;
		cursor.close();
		return checkResult;
	}

	// ------------------------------------------------------------------------------------------------

	// Deletion ---------------------------------------------------------------------------------------

	// Sales data of a particular product *************************************************************
	// =[Select SUM(quantity), SUM(price) ,AVG(price) from display where product = ? AND sellerId = ?]
	public Cursor ViewSaleProductHistory(String seller, String product) {
		SQLiteDatabase db = this.getReadableDatabase();

		return db.rawQuery("Select SUM(quantity), SUM(price) ,AVG(price) " +
										                   "from display " +
										                   "where productId = (SELECT productId FROM products WHERE productName = ?) " +
										                   "AND sellerId = ?",
		                   new String[]{product, String.valueOf(get_userId(seller))});
	}

	// ------------------------------------------------------------------------------------------------

// =================================================================================================

// TODO Product table ==============================================================================
	// Insertion --------------------------------------------------------------------------------------

	//    Remove from list ****************************************************************************
	//    [delete from display where index =? ]
	@SuppressLint("Recycle")
	public boolean RemoveItem(String id) {
		SQLiteDatabase DB = this.getWritableDatabase();
		return DB.rawQuery("DELETE FROM display WHERE indexs = ?", new String[]{id}).getCount() > -1;
	}

	// ------------------------------------------------------------------------------------------------

	// Update -----------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------

	// Selection --------------------------------------------------------------------------------------

	// fetch product Id *******************************************************************************
	// if nor available then create it
	// if available but in 'all' category then can update it
	@SuppressLint("Range")
	public int get_productId(String p_name, String category) {
		SQLiteDatabase DB = this.getReadableDatabase();
		Cursor product_Id;
		product_Id = DB.rawQuery("SELECT productId FROM products WHERE productName = ?", new String[]{p_name});
		String categoryLocal = "all";
		product_Id.moveToFirst();
		if (product_Id.getCount() > 0) {
			int id = product_Id.getInt(product_Id.getColumnIndex("productId"));

			Cursor cursor = DB.rawQuery("SELECT category FROM products WHERE productId = ?", new String[]{String.valueOf(id)});
			cursor.moveToFirst();
			if (cursor.getCount() > 0) {
				categoryLocal = cursor.getString(cursor.getColumnIndex("category"));
			}
			cursor.close();

			if (categoryLocal.equals("all") && !category.equals("all")) {
				@SuppressLint("Recycle") Cursor cursor1 = DB.rawQuery("UPDATE products SET category = ? WHERE productId = ?", new String[]{category, String.valueOf(id)});

				if (cursor1.getCount() >= 0) {
					return id;
				} else {
					return -2;
				}
			}

			product_Id.close();
			return id;
		}

		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("productName", p_name);
		contentValues.put("category", category);

		if (db.insert("products", null, contentValues) != -1) {
			product_Id = DB.rawQuery("SELECT productId FROM products WHERE productName = ? AND category = ? ", new String[]{p_name, category});
			product_Id.moveToFirst();
			if (product_Id.getCount() > 0) {
				int id = product_Id.getInt(product_Id.getColumnIndex("productId"));
				product_Id.close();
				return id;
			} else {
				product_Id.close();
				return -1;
			}
		}
		product_Id.close();
		return -1;
	}

	// ------------------------------------------------------------------------------------------------

	// Deletion ---------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------

//==================================================================================================

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// TODO Customer table =============================================================================
	// Insertion --------------------------------------------------------------------------------------

	// fetch productName ******************************************************************************
	// [select * from products where productId =? ]
	@SuppressLint("Range")
	public String getProductName(Integer ID) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("Select * from products where productId = ?", new String[]{String.valueOf(ID)});
		cursor.moveToFirst();
		String name = cursor.getString(cursor.getColumnIndex("productName"));
		cursor.close();
		return name;
	}

	// ------------------------------------------------------------------------------------------------

	// Update -----------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------

	// Selection --------------------------------------------------------------------------------------

	//    Insert customer info in customer Table ******************************************************
	//    [insert into customer values(?, ?, ?, ?, ?) ]
	@SuppressLint("Range")
	public boolean InsertCustomer(int billId, String name, String number, String date, String email, int state) {

		double total = 0d;

		Cursor cursor = GetSubTotal(billId);

		cursor.moveToFirst();
		do {
			total += cursor.getDouble(cursor.getColumnIndex("subtotal"));
		} while (cursor.moveToNext());

		cursor.close();

		String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");

		ContentValues contentValues = new ContentValues();

		int s_id = get_userId(email);
		int c_id = get_customersId(name, number, s_id);

		contentValues.put("billId", billId);
		contentValues.put("customerId", c_id);
		contentValues.put("date", formattedDate);
		contentValues.put("total", total);
		contentValues.put("sellerId", s_id);

		SQLiteDatabase DB = this.getWritableDatabase();

		return DB.insert("customer", null, contentValues) != -1;
	}

	//   Generating BILL ID ***************************************************************************
	//    [select * from customer]
	public int GetBillId() {
		SQLiteDatabase DB = this.getReadableDatabase();

		int id = 0;
		try {
			@SuppressLint("Recycle") Cursor customer_billId_list_cursor = DB.rawQuery("select * from customer ORDER BY billId ASC", null);
			if (customer_billId_list_cursor.getCount() > 0) {
				customer_billId_list_cursor.moveToLast();
				id = Integer.parseInt(customer_billId_list_cursor.getString(0));
			}
			id++;
			customer_billId_list_cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}

	// ------------------------------------------------------------------------------------------------

	// Deletion ---------------------------------------------------------------------------------------

	//    Fetching all customer data ******************************************************************
	//    [select * from customer where seller =?]
	public Cursor CustomerInformation(String email) {
		SQLiteDatabase DB = this.getReadableDatabase();

		String sellerId = String.valueOf(get_userId(email));

		return DB.rawQuery("select * from customers where sellerId =?", new String[]{sellerId});
	}

	// Deleting bills with bill id ********************************************************************
	// delete bill from display and customer table
	public boolean DeleteBillWithBillID(String billId, String email) {
		SQLiteDatabase db = getWritableDatabase();
		long delete_customer, delete_display;
		String sellerId = String.valueOf(get_userId(email));
		delete_customer = db.delete("customer", "billId = ? and sellerId = ?", new String[]{billId, sellerId});
		delete_display = db.delete("display", "billId = ? and sellerId = ?", new String[]{billId, sellerId});
		return delete_customer != -1 && delete_display != -1;
	}

	// Delete Bill With Customer Number ***************************************************************
	public boolean DeleteBillWithCustomerNumber(String number, String email) {
		SQLiteDatabase db = getWritableDatabase();
		long delete_customer, delete_display;
		String sellerId = String.valueOf(get_userId(email));

		String getCustomerId = String.valueOf(get_customersId("", number, get_userId(email)));

		delete_customer = db.delete("customer", "customerId = ? and sellerId = ?", new String[]{getCustomerId, sellerId});
		delete_display = db.delete("display", "customerId = ? and sellerId = ?", new String[]{getCustomerId, sellerId});
		return delete_customer != -1 && delete_display != -1;
	}

	// Deleting bills with Customer Name **************************************************************
	public boolean DeleteBillWithCustomerName(String name, String email) {
		SQLiteDatabase db = getWritableDatabase();
		long delete_customer, delete_display;
		String sellerId = String.valueOf(get_userId(email));
		delete_customer = db.delete("customer", "customerName = ? and sellerId = ?", new String[]{name, sellerId});
		delete_display = db.delete("display", "customerName = ? and sellerId = ?", new String[]{name, sellerId});
		return delete_customer != -1 && delete_display != -1;
	}

	// ------------------------------------------------------------------------------------------------

//==================================================================================================

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// TODO Customers table ============================================================================

	// Deleting bills with Date ***********************************************************************
	@SuppressLint("Range")
	public boolean DeleteBillWithDate(Cursor data, String email) {
		data.moveToFirst();
		do {
			if (!DeleteBillWithBillID(data.getString(data.getColumnIndex("billId")), email))
				return false;
		} while (data.moveToNext());
		return true;
	}

	// Selection // Insertion -------------------------------------------------------------------------
	// Get customerId *********************************************************************************
	// [SELECT customerId FROM customers WHERE customerNumber = ?]
	@SuppressLint("Range")
	public int get_customersId(String c_name, String c_number, int sid) {
		SQLiteDatabase DB = this.getReadableDatabase();
		Cursor customerId_C = DB.rawQuery("SELECT customerId FROM customers WHERE customerNumber = ? ", new String[]{c_number});

		customerId_C.moveToFirst();
		if (customerId_C.getCount() > 0) {
			int id = customerId_C.getInt(customerId_C.getColumnIndex("customerId"));
			customerId_C.close();
			return id;
		} else {
			customerId_C.close();

			SQLiteDatabase db = getWritableDatabase();
			ContentValues contentValues = new ContentValues();
			contentValues.put("customerName", c_name);
			contentValues.put("customerNumber", c_number);
			contentValues.put("sellerId", sid);

			if (db.insert("customers", null, contentValues) != -1) {
				return get_customersId(c_name, c_number, sid);
			} else {

				return -2;
			}
		}
	}
	// ------------------------------------------------------------------------------------------------

	// Selection --------------------------------------------------------------------------------------

	// Selection // Update ----------------------------------------------------------------------------
	// validate name and number **********************************************************************
	// [select * from customers where customerNumber = ? AND sellerId = ? ]
	@SuppressLint("Range")
	public void validateNameAndNumberConnection(final String name, final String number, final String email, final Context context, final Handler handler) {
		new Thread(() -> {
			SQLiteDatabase DB = getWritableDatabase();
			String sellerId = String.valueOf(get_userId(email));

			Cursor cursor = DB.rawQuery("SELECT customerName FROM customers WHERE customerNumber = ? AND sellerId = ? ", new String[]{number, sellerId});
			int count = cursor.getCount();

			if (count == 0) {
				cursor.close();
				Message message = handler.obtainMessage(1, true);
				handler.sendMessage(message);
			} else {
				cursor.moveToFirst();
				final String existingCustomerName = cursor.getString(cursor.getColumnIndex("customerName"));
				if (existingCustomerName.equals(name)) {
					cursor.close();
					Message message = handler.obtainMessage(1, true);
					handler.sendMessage(message);
				} else {
					cursor.close();
					Handler mainHandler = new Handler(Looper.getMainLooper());
					mainHandler.post(() -> {
						AlertDialog.Builder alert = new AlertDialog.Builder(context);
						alert.setTitle("Note!");
						alert.setMessage("Customer :\"" + existingCustomerName + "\" already exist with this number.\nDo you want to update it's name to : \"" + name + "\"");
						alert.setPositiveButton("Yes", (dialogInterface, i) -> {
							new Thread(() -> {
								ContentValues contentValues = new ContentValues();
								contentValues.put("customerName", name);

								boolean isUpdated = DB.update("customers", contentValues, "customerNumber = ? AND sellerId = ?", new String[]{number, sellerId}) != -1;
								Message message = handler.obtainMessage(1, isUpdated);
								handler.sendMessage(message);
							}).start();
							dialogInterface.dismiss();
						});
						alert.setNegativeButton("No", (dialogInterface, i) -> {
							Message message = handler.obtainMessage(1, false);
							handler.sendMessage(message);
							dialogInterface.dismiss();
						});
						alert.show();
					});
				}
			}
		}).start();
	}

	// Fetching single customer ***********************************************************************
	// [select * from customers where sellerId = ? and customerName = ? ]
	public Cursor ParticularCustomerInformation(String email, String name) {
		SQLiteDatabase DB = this.getReadableDatabase();

		String sellerId = String.valueOf(get_userId(email));

		return DB.rawQuery("select * from customers where sellerId = ? and customerName = ? ", new String[]{sellerId, name});
	}


	// ------------------------------------------------------------------------------------------------

	// Deletion ---------------------------------------------------------------------------------------


	// ------------------------------------------------------------------------------------------------

//==================================================================================================

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// TODO stock table ================================================================================

	// insert -----------------------------------------------------------------------------------------

	//   Fetching all customer names ******************************************************************
	//    [select * from customers where sellerId = ? ]
	public String[] customersName_arr(String email) {
		SQLiteDatabase DB = this.getReadableDatabase();

		String sellerId = String.valueOf(get_userId(email));

		@SuppressLint("Recycle") Cursor nameCursor = DB.rawQuery("SELECT DISTINCT customerName FROM customers WHERE sellerId = ?;", new String[]{sellerId});

		String[] name = new String[nameCursor.getCount()];

		nameCursor.moveToFirst();
		if (nameCursor.getCount() > 0) {
			int count = 0;
			do {
				name[count] = nameCursor.getString(0);
				count++;
			} while (nameCursor.moveToNext());
		}
		return name;
	}

	// ------------------------------------------------------------------------------------------------

// =================================================================================================

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// TODO StockQuantity table ========================================================================

	// Insertion // update ----------------------------------------------------------------------------

	// Adding Stock to stock table ********************************************************************
	// insert stock and update/insert in stockQuantity
	public boolean AddStock(String name, String category, String pPrice, String sPrice, String date, String quantity, String seller, String gst) {

		SQLiteDatabase db = this.getWritableDatabase();

		String sellerId = String.valueOf(get_userId(seller));

		String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");

		ContentValues cv = new ContentValues();
		cv.put("productId", get_productId(name, category));
		cv.put("purchasePrice", Double.parseDouble(pPrice));
		cv.put("sellingPrice", Double.parseDouble(sPrice));
		cv.put("date", formattedDate);
		cv.put("quantity", quantity);
		cv.put("sellerId", sellerId);
		cv.put("Gst", gst);

		long check;

		check = db.insert("stock", null, cv);

		if (check == -1) {
			return false;
		} else {
			Cursor cursor = GetProductQuantity(name, seller);
			cursor.moveToFirst();
			if (cursor.getCount() > 0) {

				@SuppressLint("Range") int qty = Integer.parseInt(cursor.getString(cursor.getColumnIndex("quantity")));
				int newQty = Integer.parseInt(quantity) + qty;

				ContentValues contentValues = new ContentValues();

				contentValues.put("productId", get_productId(name, category));
				contentValues.put("quantity", newQty);
				contentValues.put("price", Double.parseDouble(sPrice));
				contentValues.put("sellerId", sellerId);
				contentValues.put("Gst", gst);

				long result;
				result = db.update("stockQuantity", contentValues, "sellerId = ? and productId = ? ", new String[]{sellerId, String.valueOf(get_productId(name, category))});

				return result != -1;

			} else {
				ContentValues contentValues = new ContentValues();
				contentValues.put("productId", get_productId(name, category));
				contentValues.put("quantity", quantity);
				contentValues.put("price", Double.parseDouble(sPrice));
				contentValues.put("sellerId", sellerId);
				contentValues.put("Gst", gst);
				long result;

				result = db.insert("stockQuantity", null, contentValues);

				return result != -1;
			}
		}
	}

	// ------------------------------------------------------------------------------------------------

	// Update -----------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------

	// Selection --------------------------------------------------------------------------------------

	// removing the sold product Quantity *************************************************************
	// [update stockQuantity set quantity = ? where productId = ? AND sellerId = ? ]
	@SuppressLint("Range")
	public Boolean RemoveSell(int billID, String seller) {
		String productID, quantity, productName;

		String sellerId = String.valueOf(get_userId(seller));

		SQLiteDatabase db = this.getWritableDatabase();

		Cursor displayListCursor = DisplayList(billID); // getting all detail of product from BillID

		displayListCursor.moveToFirst();

		long result;

		do {
			productID = String.valueOf(Integer.valueOf(displayListCursor.getString(displayListCursor.getColumnIndex("productId"))));
			productName = getProductName(Integer.valueOf(displayListCursor.getString(displayListCursor.getColumnIndex("productId"))));
			quantity = displayListCursor.getString(displayListCursor.getColumnIndex("quantity"));

			Cursor stockQuentityCursor = GetProductQuantity(productName, seller);

			stockQuentityCursor.moveToFirst();
			if (stockQuentityCursor.getCount() > 0) { // Updating Quantity record
				int qty = Integer.parseInt(stockQuentityCursor.getString(stockQuentityCursor.getColumnIndex("quantity")));

				int newQty = qty - Integer.parseInt(quantity);

				ContentValues contentValues = new ContentValues();
				contentValues.put("productId", productID);
				contentValues.put("quantity", newQty);
				contentValues.put("price", String.valueOf(Double.parseDouble(displayListCursor.getString(displayListCursor.getColumnIndex("price")))));
				contentValues.put("sellerId", sellerId);
				contentValues.put("Gst", Double.parseDouble(displayListCursor.getString(displayListCursor.getColumnIndex("Gst"))));

				result = db.update("stockQuantity", contentValues, "sellerId = ? and productId = ? ", new String[]{sellerId, productID});

			} else { // Insert new Record in stockQuantity
				int Sell_qty = -1 * (Integer.parseInt(quantity)); // Making quantity negative so it's easy to add and we can know that user is not managing this stock !

				ContentValues contentValues = new ContentValues();
				contentValues.put("productId", productID);
				contentValues.put("quantity", String.valueOf(Sell_qty));
				contentValues.put("price", String.valueOf(Double.parseDouble(displayListCursor.getString(displayListCursor.getColumnIndex("price")))));
				contentValues.put("sellerId", sellerId);
				contentValues.put("Gst", Double.parseDouble(displayListCursor.getString(displayListCursor.getColumnIndex("Gst"))));

				try {
					result = db.insert("stockQuantity", null, contentValues);
					if (result == -1) {
						displayListCursor.close();
						return false;
					}
				} catch (Exception e) {
					e.printStackTrace();
					displayListCursor.close();
					return false;
				}
			}

		} while (displayListCursor.moveToNext());

		displayListCursor.close();
		return result != -1;
	}
	// ------------------------------------------------------------------------------------------------

	// Deletion ---------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------

//==================================================================================================


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// TODO messages table =============================================================================

	// Insertion --------------------------------------------------------------------------------------

	// setting current quantity ***********************************************************************
	// [Select * from stockQuantity where sellerId = ? AND productName = ?]
	public Cursor GetProductQuantity(String name, String seller) {
		SQLiteDatabase db = this.getReadableDatabase();
		return db.rawQuery("Select * from stockQuantity where sellerId = ? AND productId = ?",
		                   new String[]{String.valueOf(get_userId(seller)),
										                   String.valueOf(get_productId(name, "all"))});
	}

	// ------------------------------------------------------------------------------------------------

	// Update -----------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------

	// Selection --------------------------------------------------------------------------------------

	// Inserting message in messages table ************************************************************
	public void insertMessage(ChatMessage message) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("message", message.getMessage());
		values.put("is_sent_by_user", message.isSentByUser() ? 1 : 0);
		values.put("timestamp", message.getTimestamp());
		values.put("seller_id", message.getSellerId());
		db.insert("messages", null, values);
	}

	// getChatData ************************************************************************************
	// [select * from messages where seller_id = ? ]
	public List<ChatMessage> getChatBySeller(int sellerId) {
		SQLiteDatabase db = this.getReadableDatabase();
		List<ChatMessage> messages = new ArrayList<>();
		Cursor cursor = null;

		try {
			String[] selectionArgs = {String.valueOf(sellerId)};
			cursor = db.query("messages",
			                  new String[]{"_id", "message", "is_sent_by_user", "timestamp", "seller_id"},
			                  "seller_id = ?", selectionArgs, null, null, "timestamp ASC");

			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
				String message = cursor.getString(cursor.getColumnIndexOrThrow("message"));
				boolean isSentByUser = cursor.getInt(cursor.getColumnIndexOrThrow("is_sent_by_user")) == 1;
				long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));
				int seller = cursor.getInt(cursor.getColumnIndexOrThrow("seller_id"));

				messages.add(new ChatMessage(id, message, isSentByUser, timestamp, seller));
			}
		} catch (Exception e) {
			// Handle exception
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return messages;
	}

	// getChatOfUser **********************************************************************************
	// [select * from messages where seller_id = ? AND timestamp BETWEEN ? AND ? ]
	public List<ChatMessage> getChatBySellerAndDate(int sellerId, String date) {
		SQLiteDatabase db = this.getReadableDatabase();
		List<ChatMessage> chatMessages = new ArrayList<>();
		long startTime = getStartOfDayInMillis(date);
		long endTime = getEndOfDayInMillis(date);

		String sql = "SELECT * FROM messages WHERE seller_id = ? AND timestamp BETWEEN ? AND ?";
		Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(sellerId), String.valueOf(startTime), String.valueOf(endTime)});

		if (cursor.moveToFirst()) {
			do {
				int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
				String message = cursor.getString(cursor.getColumnIndexOrThrow("message"));
				boolean isUser = cursor.getInt(cursor.getColumnIndexOrThrow("is_sent_by_user")) == 1;
				long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));
				ChatMessage chatMessage = new ChatMessage(id, message, isUser, timestamp, sellerId);
				chatMessages.add(chatMessage);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return chatMessages;
	}

	// get start of the day in ms to get to days chat *************************************************
	private long getStartOfDayInMillis(String date) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			Date parsedDate = sdf.parse(date);
			if (parsedDate != null) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(parsedDate);
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				return calendar.getTimeInMillis();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	// ------------------------------------------------------------------------------------------------

	// Deletion ---------------------------------------------------------------------------------------

	// get end of the day in ms to get to days chat ***************************************************
	private long getEndOfDayInMillis(String date) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			Date parsedDate = sdf.parse(date);
			if (parsedDate != null) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(parsedDate);
				calendar.set(Calendar.HOUR_OF_DAY, 23);
				calendar.set(Calendar.MINUTE, 59);
				calendar.set(Calendar.SECOND, 59);
				calendar.set(Calendar.MILLISECOND, 999);
				return calendar.getTimeInMillis();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	// ------------------------------------------------------------------------------------------------

//==================================================================================================

//TODO  Backup Working =============================================================================

	//    Delete all messages *************************************************************************
	// [DELETE FROM messages WHERE seller_id = ?]
	public boolean clearChat(String sellerId) {
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "DELETE FROM messages WHERE seller_id = ?";
		db.execSQL(sql, new String[]{sellerId});
		return true;
	}

	// checking for permission
	private boolean isPermissionGranted(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			return Environment.isExternalStorageManager();
		} else {
			int writePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
			int readPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
			return writePermission == PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED;
		}
	}

	// Generate secret key from password and salt
	private SecretKey generateKey(char[] password, byte[] salt) throws GeneralSecurityException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		PBEKeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
		SecretKey tmp = factory.generateSecret(spec);
		return new SecretKeySpec(tmp.getEncoded(), "AES");
	}

	// downloading db ---------------------------------------------------------------------------------
	@SuppressLint("Range")
	public String DownloadBackup(Context context, String email) {

		// Get current date and time using Calendar
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();

		// Format date and time
		@SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("_dd-MM-yyyy_HH:mm_");
		String currentDateTime = sdf.format(date);

		String password = "963258741";
		Cursor user = getData();
		user.moveToFirst();
		boolean passwordFetched = false;
		do {
			if (user.getString(user.getColumnIndex("email")).equals(email)) {
				password = user.getString(user.getColumnIndex("password"));
				passwordFetched = true;
				break;
			}
		} while (user.moveToNext());

		if (!passwordFetched) return "ERROR Password not fetched";

		if (!isPermissionGranted(context)) return "Permission Denied";

		try {
			// Get the path to the app's internal database
			String internalDatabasePath = context.getDatabasePath("Biller").getPath();
			File databaseFile = new File(internalDatabasePath);
			FileInputStream fis = new FileInputStream(databaseFile);

			// Create the backup file on external storage or other location
			String backupPath = context.getExternalFilesDir(null) + "/Backups/";
			File backupFolder = new File(backupPath);

			if (!backupFolder.exists()) if (!backupFolder.mkdirs()) return "Error Creating Directory";

			String backupDatabasePath = backupPath + "Biller_Backup" + currentDateTime + ".db";
			// Generate salt and secret key
			byte[] salt = new byte[16];
			SecureRandom secureRandom = new SecureRandom();
			secureRandom.nextBytes(salt);
			SecretKey secretKey = generateKey(password.toCharArray(), salt);

			// Generate IV
			byte[] iv = new byte[16];
			secureRandom.nextBytes(iv);
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

			// Encrypt the database file
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

			FileOutputStream fos = new FileOutputStream(backupDatabasePath);
			CipherOutputStream cos = new CipherOutputStream(fos, cipher);

			// Write salt and IV to the backup file
			fos.write(salt);
			fos.write(iv);

			// Copy the database file to the backup location
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = fis.read(buffer)) != -1) {
				cos.write(buffer, 0, bytesRead);
			}

			// Close the streams
			cos.close();
			fos.close();
			fis.close();

			return backupDatabasePath; // Backup successful
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
			return "Error";
		}
	}

// =================================================================================================

	// Upload local backup method with decryption and validation --------------------------------------
	public boolean UploadLocalBackup(Context context, File selectedFile, char[] password) {
		File dbFile = context.getDatabasePath("Biller");
		try {
			FileInputStream fis = new FileInputStream(selectedFile);

			// Read salt and IV from the backup file
			byte[] salt = new byte[16];
			byte[] iv = new byte[16];
			if (fis.read(salt) != salt.length || fis.read(iv) != iv.length) {
				throw new IOException("Failed to read salt or IV from the backup file");
			}
			SecretKey secretKey = generateKey(password, salt);
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

			// Decrypt the backup file
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

			CipherInputStream cis = new CipherInputStream(fis, cipher);
			FileOutputStream fos = new FileOutputStream(dbFile);

			// Copy the decrypted file to the database location
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = cis.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
			}

			// Close the streams
			cis.close();
			fos.close();
			fis.close();

			return true; // Restore successful
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
			return false;
		}
	}


// TODO ALL JOIN Query =============================================================================

	// Focus on Display Table -------------------------------------------------------------------------

	// Work for GEMINI ================================================================================
	// Method to fetch data from a specific table
	public JSONArray getTableData(String tableName, int sellerId) {
		SQLiteDatabase db = null;
		JSONArray resultSet = new JSONArray();
		Cursor cursor = null;

		try {
			db = this.getReadableDatabase();
			String query;
			if (tableName.equals("users")) {
				query = "SELECT * FROM " + tableName + " WHERE userId = ?";
			} else if (tableName.equals("products")) {
				query = "SELECT p.* FROM " + tableName + " p JOIN stockQuantity sq ON sq.productId = p.productId WHERE sq.sellerId = ?";
			} else {
				query = "SELECT * FROM " + tableName + " WHERE sellerId = ?";
			}

			cursor = db.rawQuery(query, new String[]{String.valueOf(sellerId)});

			if (cursor.moveToFirst()) {
				do {
					JSONObject rowObject = new JSONObject();
					for (int i = 0; i < cursor.getColumnCount(); i++) {
						try {
							rowObject.put(cursor.getColumnName(i), cursor.getString(i));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					resultSet.put(rowObject);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			// Close the database connection if it's open
			if (db != null && db.isOpen()) {
				db.close();
			}
		}

		return resultSet;
	}

	//    Bill id is unique every time so no need of email ********************************************
	//    [select * from display where billId =? ]
	public Cursor DisplayList(int billId) {
		SQLiteDatabase DB = this.getReadableDatabase();

		// Gst | price | quantity | subtotal | product(name)[productTable] | indexs
		return DB.rawQuery("SELECT d.indexs,d.productId, p.productName product, d.quantity, d.subtotal, d.Gst, d.price " +
										                   "FROM display d " +
										                   "JOIN products p ON d.productId = p.productId " +
										                   "WHERE d.billId = ?", new String[]{String.valueOf(billId)});
	}

	//    search based on customer name ***************************************************************
	//    [select * from display where customerName = ? and seller=?]
	public Cursor CustomerNameBill(String Name, String email) {
		SQLiteDatabase DB = this.getReadableDatabase();

		// billId | customerName | customerNumber | product | price | quantity | subtotal | date
		return DB.rawQuery("SELECT d.indexs, p.productName product, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst ,d.price " +
										                   "FROM display d " +
										                   "JOIN products p ON d.productId = p.productId " +
										                   "JOIN customers c ON d.customerId = c.customerId " +
										                   "WHERE c.customerName = ? AND d.sellerId = ?", new String[]{Name, String.valueOf(get_userId(email))}
		                  );
	}

	//    Search Based on single date *****************************************************************
	//    [select * from display where date = ? and email = ?]
	public Cursor CustomerDateBill(String date, String email) {
		String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");

		SQLiteDatabase DB = this.getReadableDatabase();

		// billId | customerName | customerNumber | product | price | quantity | subtotal | date
		return DB.rawQuery("SELECT d.indexs, p.productName product, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst ,d.price " +
										                   "FROM display d " +
										                   "JOIN products p ON d.productId = p.productId " +
										                   "JOIN customers c ON d.customerId = c.customerId " +
										                   "WHERE d.date = ? AND  d.sellerId = ?", new String[]{formattedDate, String.valueOf(get_userId(email))}
		                  );
	}

	//    Search Based on number **********************************************************************
	//    [select * from display where customerNumber = ? and seller = ? ]
	public Cursor CustomerNumberBill(String Number, String email) {
		SQLiteDatabase DB = this.getReadableDatabase();

		// indexs | billId | customerName | customerNumber | product | price | quantity | subtotal | date
		return DB.rawQuery("SELECT d.indexs, p.productName product, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst ,d.price " +
										                   "FROM display d " +
										                   "JOIN products p ON d.productId = p.productId " +
										                   "JOIN customers c ON d.customerId = c.customerId " +
										                   "JOIN users u ON d.sellerId = u.userId " +
										                   "WHERE c.customerNumber = ? AND u.email = ?", new String[]{Number, email}
		                  );

	}

	//    Search based on billID **********************************************************************
	//    [select * from display where billId = ? and email = ? ]
	public Cursor CustomerBillID(int billID, String email) {
		SQLiteDatabase DB = this.getReadableDatabase();

		// indexs | billId | customerName | customerNumber | product | price | quantity | subtotal | date
		return DB.rawQuery("SELECT d.indexs, p.productName product, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst ,d.price " +
										                   "FROM display d " +
										                   "JOIN products p ON d.productId = p.productId " +
										                   "JOIN customers c ON d.customerId = c.customerId " +
										                   "JOIN users u ON d.sellerId = u.userId " +
										                   "WHERE d.billId = ? AND u.email = ?", new String[]{String.valueOf(billID), email}
		                  );
	}

	// focus on customer ------------------------------------------------------------------------------

	//Searching in Date Range *************************************************************************
	// [Select * from display where seller =? AND  date  BETWEEN ? AND ? ]
	public Cursor RangeSearch(String date, String toDate, String email) {
		String startDate_formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");
		String endDate_formattedDate = date_convertor.convertDateFormat(toDate, "dd/MM/yyyy", "yyyy-MM-dd");
		SQLiteDatabase DB = this.getReadableDatabase();

		// indexs | billId | customerName | customerNumber | product | price | quantity | subtotal | date
		return DB.rawQuery("SELECT d.indexs, p.productName product, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst, d.price " +
										                   "FROM display d " +
										                   "JOIN products p ON d.productId = p.productId " +
										                   "JOIN customers c ON d.customerId = c.customerId " +
										                   "JOIN users u ON d.sellerId = u.userId " +
										                   "WHERE u.email = ? AND d.date BETWEEN ? AND ?",
		                   new String[]{email, startDate_formattedDate, endDate_formattedDate}
		                  );
	}

	// Focus on stockQuantity table -------------------------------------------------------------------

	//    Getting Bill TOTAL **************************************************************************
	//    [select * from customer where billid = ?]
	public Cursor BillTotal(int billID) {
		SQLiteDatabase DB = this.getReadableDatabase();

		//     billId   |   customerName   |   CustomerNumber    |    date    |    total    |
		return DB.rawQuery("select c.billId, cs.customerName, cs.customerNumber , c.date , c.total  " +
										                   "from customer c " +
										                   "JOIN customers cs ON c.customerId = cs.customerId " +
										                   "where billId = ? ", new String[]{String.valueOf(billID)});
	}

	// getting current stock quantity *****************************************************************
	// [Select * from stockQuantity where sellerId = ?]
	public Cursor GetInventory(String seller) {
		SQLiteDatabase db = this.getReadableDatabase();

		return db.rawQuery("Select * " +
										                   "from stockQuantity sq " +
										                   "JOIN products p ON sq.productId = p.productId " +
										                   "where sellerId = ?", new String[]{String.valueOf(get_userId(seller))});
	}

	// focus on stock table ---------------------------------------------------------------------------

	// View current Stock Quantity ********************************************************************
	// [Select * from stockQuantity where seller =?]
	public Cursor ViewStock(String seller) {
		SQLiteDatabase db = this.getReadableDatabase();

		return db.rawQuery("Select * " +
										                   "from stockQuantity sq " +
										                   "JOIN products p ON sq.productId = p.productId " +
										                   "where sellerId =?", new String[]{String.valueOf(get_userId(seller))});
	}

	// getting the cursor of Category *****************************************************************
	// [Select * from stock where sellerId = ?]
	// category
	public Cursor GetCategory(String seller) {
		SQLiteDatabase db = this.getReadableDatabase();

		return db.rawQuery("Select * " +
										                   "from stock s " +
										                   "JOIN products p ON s.productId = p.productId " +
										                   "where s.sellerId = ?", new String[]{String.valueOf(get_userId(seller))});
	}

	// view when stock is entered in table ************************************************************
	// [Select * from stock where seller =? AND productName=?]
	public Cursor ViewProductHistory(String seller, String product) {
		SQLiteDatabase db = this.getReadableDatabase();

		return db.rawQuery("SELECT * FROM stock " +
										                   "JOIN products ON stock.productId = products.productId " +
										                   "WHERE sellerId = ? AND products.productName = ?", new String[]{String.valueOf(get_userId(seller)), product});
	}

	// view stock history but category wise ***********************************************************
	// [Select * from stock where seller =? AND category=?]
	public Cursor ViewCategoryHistory(String seller, String category) {

		SQLiteDatabase db = this.getReadableDatabase();

		return db.rawQuery("SELECT * " +
										                   "FROM stock JOIN products ON stock.productId = products.productId " +
										                   "WHERE sellerId = ? AND products.category = ?",
		                   new String[]{String.valueOf(get_userId(seller)), category});
	}

// =================================================================================================

	// Sales data of a particular category ************************************************************
	//[Select SUM(quantity), SUM(price) ,AVG(price) from display where product = ? AND category = ?]
	public Cursor ViewSaleCategoryHistory(String seller, String category) {
		SQLiteDatabase db = this.getReadableDatabase();

		return db.rawQuery("Select DISTINCT productName " +
										                   "FROM stock " +
										                   "JOIN products ON stock.productId = products.productId " +
										                   "WHERE sellerId = ? " +
										                   "AND products.category = ? ",
		                   new String[]{String.valueOf(get_userId(seller)), category});
	}
	// ================================================================================================


	// inserting Demo data ----------------------------------------------------------------------------
	public boolean insertDemoData() {
		SQLiteDatabase DB = this.getWritableDatabase();
		try {
			// Insert data into 'users' (Sellers)
			DB.execSQL("INSERT INTO users (name, email, password, gst, contact, address, apiKey) VALUES " +
											           "('FastBites', 'contact@fastbites.com', '1234567890', '29ABCDE1234F2Z5', '9876543210', '123 Food Street, Tasty Town', '');");

			// Insert data into 'products' (Product Records)
			DB.execSQL("INSERT INTO products (productName, category) VALUES " +
											           "('Cheeseburger', 'Burgers')," +
											           "('Veggie Pizza', 'Pizzas')," +
											           "('Chicken Nuggets', 'Snacks')," +
											           "('French Fries', 'Sides')," +
											           "('Soft Drink', 'Beverages')," +
											           "('Grilled Chicken Sandwich', 'Burgers')," +
											           "('Pepperoni Pizza', 'Pizzas')," +
											           "('Onion Rings', 'Sides')," +
											           "('Milkshake', 'Beverages')," +
											           "('Fish & Chips', 'Meals')," +
											           "('Hot Dog', 'Snacks')," +
											           "('Caesar Salad', 'Salads')," +
											           "('Ice Cream', 'Desserts')," +
											           "('Spaghetti', 'Meals')," +
											           "('Apple Pie', 'Desserts');");

			// Insert data into 'customers' (Customer Data)
			DB.execSQL("INSERT INTO customers (customerName, customerNumber, sellerId) VALUES " +
											           "('John Doe', '9876543211', 1)," +
											           "('Jane Smith', '9876543212', 1)," +
											           "('Alice Johnson', '9876543213', 1)," +
											           "('Bob Brown', '9876543214', 1)," +
											           "('Charlie Davis', '9876543215', 1)," +
											           "('Emily Clark', '9876543216', 1)," +
											           "('David Wilson', '9876543217', 1)," +
											           "('Sophia Lewis', '9876543218', 1)," +
											           "('Lucas Hall', '9876543219', 1)," +
											           "('Olivia Young', '9876543220', 1)," +
											           "('James Walker', '9876543221', 1)," +
											           "('Amelia Turner', '9876543222', 1)," +
											           "('Liam King', '9876543223', 1)," +
											           "('Mia Scott', '9876543224', 1)," +
											           "('Isabella Green', '9876543225', 1);");

			// Insert data into 'stock' (Stock Control)
			for (int i = 1; i <= 15; i++) {
				String date = "2024-08-" + ((i % 29) + 1); // Varying date between 2024-08-01 and 2024-08-29

				DB.execSQL("INSERT INTO stock (productId, purchasePrice, sellingPrice, date, quantity, sellerId, Gst) VALUES " + "(" + i + ", " + (i * 2 + 18) + ".00, " + (i * 5 + 45) + ".00, '" + date + "', '" + (i * 10 + 40) + "', 1, " + i * 1.0 + "00);");
			}

			// Insert data into 'display' (Bill Records)
			for (int i = 1; i <= 80; i++) {
				int productId = (i % 15) + 1; // Cycle through product IDs
				int customerId = (i % 15) + 1; // Cycle through customer IDs
				int quantity = (i % 5) + 1; // Random quantity between 1 and 5
				double price = (productId <= 5 ? 50.00 : productId <= 10 ? 30.00 : 20.00); // Pricing based on product category
				double subtotal = price * quantity;
				int billId = 100 + i;
				double gst = subtotal * 0.05; // 5% GST
				String date = "2024-08-" + ((i % 29) + 1); // Varying date between 2024-08-01 and 2024-08-29

				DB.execSQL("INSERT INTO display (productId, quantity, price, subtotal, customerId, sellerId, date, billId, Gst) VALUES " +
												           "(" + productId + ", " + quantity + ", " + price + ", " + subtotal + ", " + customerId + ", 1, '" + date + "', " + billId + ", " + gst + ");");
			}

			// Insert data into 'customer' (Customer Bill Data)
			for (int i = 1; i <= 80; i++) {
				int customerId = (i % 15) + 1; // Cycle through customer IDs
				int billId = 100 + i;
				double total = (i % 100) + 50.00; // Random total between 50 and 149.99
				String date = "2024-08-" + ((i % 29) + 1); // Varying date between 2024-08-01 and 2024-08-29

				DB.execSQL("INSERT INTO customer (billId, customerId, date, total, sellerId) VALUES " +
												           "(" + billId + ", " + customerId + ", '" + date + "', " + total + ", 1);");
			}

			// Insert data into 'stockQuantity' (Stock Quantity Tracking)
			for (int i = 1; i <= 15; i++) {
				DB.execSQL("INSERT INTO stockQuantity (productId, quantity, price, sellerId, Gst) VALUES " +
												           "(" + i + ", '50', " + (i <= 5 ? 50.00 : i <= 10 ? 30.00 : 20.00) + ", 1, 5);");
			}

			System.out.println("Demo data inserted successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error inserting demo data: " + e.getMessage());
			return false;
		}
		return true;
	}


	// ================================================================================================
}