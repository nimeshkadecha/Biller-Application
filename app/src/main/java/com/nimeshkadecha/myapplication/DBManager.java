package com.nimeshkadecha.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class DBManager extends SQLiteOpenHelper {
	// Encryption parameters ==========================================================================
	private static final String ALGORITHM = "AES";
	private static final int KEY_SIZE = 256;
	private static final int ITERATIONS = 65536;
	private static final int SALT_LENGTH = 16;

	public DBManager(Context context) {
//        Creating database with name = Biller
		super(context, "Biller", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase DB) {
//        Creating table name as user and column like
//        Name | TEXT
//        E-Mail | TEXT
//        Password | TEXT
//        GST | TEXT
//        Contact | TEXT
//        Address | TEXT

//        DB.execSQL("CREATE TABLE users(name TEXT,email TEXT primary key,password TEXT ,gst TEXT,contact TEXT ,address TEXT)");

		// record of seller(user) ===================================================
		DB.execSQL("CREATE TABLE users(" +
										           " userId INTEGER PRIMARY KEY AUTOINCREMENT," +
										           " name TEXT," +
										           " email TEXT UNIQUE," +
										           " password TEXT," +
										           " gst TEXT," +
										           " contact TEXT," +
										           " address TEXT)"
		          );

		// record of each bills ================================================================
		DB.execSQL("CREATE TABLE display(" +
										           " indexs INTEGER PRIMARY KEY AUTOINCREMENT," + // AUTO
										           " productId INTEGER," + // from Product
										           " quantity INTEGER," +
										           " price INTEGER," +
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

//		DB.execSQL("Create table display(indexs Integer primary key autoincrement," +  // 0
//										           "product TEXT ," + //1
//										           "price TEXT," + //2
//										           "quantity TEXT," + //3
//										           "subtotal Float," +//4
//										           "customerName TEXT," +//5
//										           "customerNumber TEXT," +//6
//										           "date Date," +//7
//										           "billId Integer ," +//8
//										           "seller TEXT," +//9
//										           "backup Integer," +//10
//										           "Gst Integer)"); //11

// Product records ============================================================================
		DB.execSQL("CREATE TABLE products(" +
										           "productId INTEGER PRIMARY KEY AUTOINCREMENT, " +
										           "productName TEXT, " +
										           "category TEXT)"
		          );

//        Customer table
//		DB.execSQL("Create TABLE customer(billId Integer primary key," + // 0
//										           "customerName TEXT," + // 1
//										           "customerNumber TEXT," + // 2
//										           "date Date," + // 3
//										           "total TEXT," + // 4
//										           "seller TEXT," + // 5
//										           "backup Integer)"); // 6
		//        stock table


		// Customer data =====================================================================

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
										           " customerNumber TEXT UNIQUE, " +
										           "sellerId INTEGER )"
		          );

		// Stock control =====================================================================
		DB.execSQL("CREATE TABLE stock(" +
										           " stockId INTEGER PRIMARY KEY AUTOINCREMENT," +
										           " productId INTEGER," +
										           " purchasePrice TEXT," +
										           " sellingPrice TEXT," +
										           " date DATE," +
										           " quantity TEXT," +
										           " sellerId INTEGER," +
										           " Gst INTEGER," +
										           " FOREIGN KEY(productId) REFERENCES products(productId)," +
										           " FOREIGN KEY(sellerId) REFERENCES users(userId))"
		          );

		DB.execSQL("CREATE TABLE stockQuantity(" +
										           " stickTrackId INTEGER PRIMARY KEY AUTOINCREMENT," +
										           " productId INTEGER," +
										           " quantity TEXT," +
										           " price TEXT," +
										           " sellerId INTEGER," +
										           " Gst INTEGER," +
										           " FOREIGN KEY(sellerId) REFERENCES users(userId))"
		          );

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

	//    Register User ============================================================================
	public boolean RegisterUser(String name, String email, String password, String gst, String contact, String address) {
		SQLiteDatabase DB = this.getWritableDatabase();

//        Getting all values in
		ContentValues contentValues = new ContentValues();
		contentValues.put("name", name);
		contentValues.put("email", email);
		contentValues.put("password", password);
		contentValues.put("gst", gst);
		contentValues.put("contact", contact);
		contentValues.put("address", address);

//        Checking
//		long result;
//
//		result = DB.insert("users", null, contentValues);

		return DB.insert("users", null, contentValues) != -1;
	}

	//    Login Verification ==================[select * from users where email =? AND password = ?]
	public boolean LoginUser(String email, String password) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        Creating a cursor to check password;
		SQLiteDatabase DB = this.getReadableDatabase();
		@SuppressLint("Recycle") Cursor cursor = DB.rawQuery("select * from users where email =? AND password = ?", new String[]{email, password});
		boolean ans = cursor.getCount() > 0;
		cursor.close();
		return ans;
	}

	//    Validate user ========================================[select * from users where email =?]
	public boolean ValidateUser(String email) {
		SQLiteDatabase DB = this.getReadableDatabase();
		@SuppressLint("Recycle") Cursor cursor = DB.rawQuery("select * from users where email =?", new String[]{email});
		boolean ans = cursor.getCount() > 0;
		cursor.close();
		return ans;
	}

	//    Getting user info for texting purposes in register ==================[select * from users]
	public Cursor getdata() {
		SQLiteDatabase DB = this.getReadableDatabase();
		return DB.rawQuery("select * from users", null);
	}

//------------------------------------- Working on customer tables ---------------------------------

	//    Getting specific user all DATA ========================[select * from users where email=?]
	public Cursor GetUser(String email) {
		SQLiteDatabase DB = this.getReadableDatabase();
		return DB.rawQuery("select * from users where email=?", new String[]{email});
	}

	//    Reset Password! ===================================[select * from users where contact = ?]
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

	//    Update Data ==========================================[SELECT * From users where email =?]
	public boolean UpdateUser(String name, String email, String password, String gst, String contact, String address) {
		SQLiteDatabase DB = this.getWritableDatabase();

		//        Getting all values in
		ContentValues contentValues = new ContentValues();
		contentValues.put("name", name);
//        contentValues.put("email", email);
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

	//    Deleting User =======================================[select * from users where email = ?]
	public boolean DeleteUser(String email) {
		SQLiteDatabase DB = this.getWritableDatabase();

		String sellerId = String.valueOf(get_userId(email));

		@SuppressLint("Recycle") Cursor cursor = DB.rawQuery("select * from users where email = ?", new String[]{email});

		if (cursor.getCount() > 0) {
			cursor.close();
			long check, check1, check2;
			check = DB.delete("users", "email = ?", new String[]{email});
			check1 = DB.delete("display", "sellerId = ?", new String[]{sellerId});
			check2 = DB.delete("customer", "sellerId = ?", new String[]{sellerId});
			return check != -1 && check1 != -1 && check2 != -1;
		} else {
			cursor.close();
			return false;
		}
	}

	//    ADDING ITEM in list/ in recyclerview / in display insert TABLE ==================================
	@SuppressLint("Range")
	public boolean InsertList(String name, String price, String quantity, String cName, String cNumber, String date, int billId, String email, int state, String Gst) {

		Log.d("ENimesh", "inside Insert List = " + price);

		int priceInt = Integer.parseInt(price);

		int quantityInt = Integer.parseInt(quantity);

		if (Gst.isEmpty()) Gst = "0";

		float tax = (priceInt * quantityInt) * (Integer.parseInt(Gst) / 100f);

		float subtotal = ((priceInt * quantityInt) + tax);

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

//		int p_id = get_productId(name, "all");
		int s_id = get_userId(email);
		int c_id = get_customersId(cName, cNumber, s_id);

		if (check_if_already_added) {

			number_of_product += 1;
			float taxUpdate = (priceInt * number_of_product) * (Integer.parseInt(Gst) / 100f);
			subtotal = ((priceInt * number_of_product) + taxUpdate);

			String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");

//			contentValues.put("Product", name);
//			contentValues.put("quantity", number_of_product);
//			contentValues.put("subtotal", subtotal);
//			contentValues.put("customerName", cName);
//			contentValues.put("customerNumber", cNumber);
//			contentValues.put("date", formattedDate);
//			contentValues.put("billId", billId);
//			contentValues.put("seller", email);
//			contentValues.put("backup", state);
//			contentValues.put("Gst", Gst);

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


//			contentValues.put("Product", name);
//			contentValues.put("quantity", quantity);
//			contentValues.put("subtotal", subtotal);
//			contentValues.put("customerName", cName);
//			contentValues.put("customerNumber", cNumber);
//			contentValues.put("date", formattedDate);
//			contentValues.put("billId", billId);
//			contentValues.put("seller", email);
//			contentValues.put("backup", state);
//			contentValues.put("Gst", Gst);

			return DB.insert("display", null, contentValues) != -1;
		}
	}

	// Get userID =======================
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

	// Get productId ======================================================
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
				Cursor cursor1 = DB.rawQuery("UPDATE products SET category = ? WHERE productId = ?", new String[]{category, String.valueOf(id)});

				if (cursor1.getCount() > 0) {
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

	// Get customerId ==================================================
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

	//    Bill id is unique every time so no need of email =[select * from display where billId =? ]
	// Gst | price | quantity | subtotal | product(name)[productTable] | indexs
	public Cursor DisplayList(int billId) {
		SQLiteDatabase DB = this.getReadableDatabase();
		String bID = String.valueOf(billId);
		return DB.rawQuery("SELECT d.indexs,d.productId, p.productName product, d.quantity, d.subtotal, d.Gst, d.price " +
										                   "FROM display d " +
										                   "JOIN products p ON d.productId = p.productId " +
										                   "WHERE d.billId = ?", new String[]{bID});

//		return DB.rawQuery("select * from display where billId =? ", new String[]{bID});
	}

	//    Remove from list ====================================[delete from display where index =? ]
	@SuppressLint("Recycle")
	public boolean RemoveItem(String id) {
		SQLiteDatabase DB = this.getWritableDatabase();
//		Log.d("ENimesh","index =" + id +"count test" + DB.rawQuery("DELETE FROM display WHERE indexs = ?", new String[]{id}).getCount());
		return DB.rawQuery("DELETE FROM display WHERE indexs = ?", new String[]{id}).getCount() > -1;
	}

	//    Remove from list =============================[Update Quantity in display where index =? ]
	public boolean UpdateQuantity(int quantity, float subtotal, int index) {

		SQLiteDatabase DB = this.getWritableDatabase();

		//        Getting all values in
		ContentValues contentValues = new ContentValues();
		contentValues.put("quantity", String.valueOf(quantity));
		contentValues.put("subtotal", subtotal);

		return DB.update("display", contentValues, "indexs =?", new String[]{String.valueOf(index)}) != -1;
	}

	// Checking Total without saving ===============================================================
	@SuppressLint("Range")
	public int CheckTotal(int billID) {
		Cursor displayList_subtotal = DisplayList(billID);
		int total = 0;
		displayList_subtotal.moveToFirst();
		if (displayList_subtotal.getCount() == 0) {
			displayList_subtotal.close();
			return -1;
		}
		do {
			total += displayList_subtotal.getInt(displayList_subtotal.getColumnIndex("subtotal"));
		} while (displayList_subtotal.moveToNext());
		displayList_subtotal.close();
		return total;
	}

	//    Generating BILL ID ===============================================[select * from customer]
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

	//    Fetching all customer ============================[select * from customer where seller =?]
	public Cursor CustomerInformation(String email) {
		SQLiteDatabase DB = this.getReadableDatabase();

		String sellerId = String.valueOf(get_userId(email));

		Log.d("ENimesh", "Seller id - " + sellerId);

		return DB.rawQuery("select * from customers where sellerId =?", new String[]{sellerId});
	}

	// Fetching single customer =======[select * from customer where seller =? and customerName = ?]
	public Cursor ParticularCustomerInformation(String email, String name) {
		SQLiteDatabase DB = this.getReadableDatabase();

		String sellerId = String.valueOf(get_userId(email));

		return DB.rawQuery("select * from customers where sellerId = ? and customerName = ? ", new String[]{sellerId, name});
	}

	//    search based on customer name =[select * from display where customerName = ? and seller=?]
	// billId | customerName | customerNumber | product | price | quantity | subtotal | date
	public Cursor CustomerNameBill(String Name, String email) {
		SQLiteDatabase DB = this.getReadableDatabase();

		String sellerId = String.valueOf(get_userId(email));

		return DB.rawQuery("SELECT d.indexs, p.productName product, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst ,d.price " +
										                   "FROM display d " +
										                   "JOIN products p ON d.productId = p.productId " +
										                   "JOIN customers c ON d.customerId = c.customerId " +
										                   "WHERE c.customerName = ? AND d.sellerId = ?", new String[]{Name, sellerId}
		                  );

//		return DB.rawQuery("select * from display where customerName = ? and sellerId=?", new String[]{Name, sellerId});
	}

	//    Search Based on single date =========[select * from display where date = ? and sellerId = ?]
	// billId | customerName | customerNumber | product | price | quantity | subtotal | date
	public Cursor CustomerDateBill(String date, String email) {

		String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");

		SQLiteDatabase DB = this.getReadableDatabase();

		String sellerId = String.valueOf(get_userId(email));

		return DB.rawQuery("SELECT d.indexs, p.productName product, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst ,d.price " +
										                   "FROM display d " +
										                   "JOIN products p ON d.productId = p.productId " +
										                   "JOIN customers c ON d.customerId = c.customerId " +
										                   "WHERE d.date = ? AND  d.sellerId = ?", new String[]{formattedDate, sellerId}
		                  );
//		return DB.rawQuery("select * from display where date = ? and sellerId = ?", new String[]{formattedDate, sellerId});
	}

	//    Search Based on number =====[select * from display where customerNumber = ? and seller=? ]
	public Cursor CustomerNumberBill(String Number, String email) {
		SQLiteDatabase DB = this.getReadableDatabase();

//		String sellerId = String.valueOf(get_userId(email));

		return DB.rawQuery("SELECT d.indexs, p.productName product, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst ,d.price " +
										                   "FROM display d " +
										                   "JOIN products p ON d.productId = p.productId " +
										                   "JOIN customers c ON d.customerId = c.customerId " +
										                   "JOIN users u ON d.sellerId = u.userId " +
										                   "WHERE c.customerNumber = ? AND u.email = ?", new String[]{Number, email}
		                  );
//		return DB.rawQuery("select * from display where customerNumber = ? and sellerId=? ", new String[]{Number, sellerId});
	}

	//    Search based on billID ===========[select * from display where billId = ? and sellerId = ? ]
	public Cursor CustomerBillID(int billID, String email) {
		SQLiteDatabase DB = this.getReadableDatabase();

		String billId = String.valueOf(billID);

//		String sellerId = String.valueOf(get_userId(email));

		return DB.rawQuery("SELECT d.indexs, p.productName product, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst ,d.price " +
										                   "FROM display d " +
										                   "JOIN products p ON d.productId = p.productId " +
										                   "JOIN customers c ON d.customerId = c.customerId " +
										                   "JOIN users u ON d.sellerId = u.userId " +
										                   "WHERE d.billId = ? AND u.email = ?", new String[]{billId, email}
		                  );

//		return DB.rawQuery("select * from display where billId = ? and sellerId = ? ", new String[]{billId, sellerId});
	}

	//    Getting Bill TOTAL ==============================[select * from customer where billid = ?]
	public Cursor BillTotal(int billID) {
		SQLiteDatabase DB = this.getReadableDatabase();

		//     billId   |   customerName   |   CustomerNumber    |    date    |    total    |
		return DB.rawQuery("select c.billId, cs.customerName, cs.customerNumber , c.date , c.total  " +
										                   "from customer c " +
										                   "JOIN customers cs ON c.customerId = cs.customerId " +
										                   "where billId = ? ", new String[]{String.valueOf(billID)});
	}

	//    Getting sub total from billID ==================[ select * from display where billId = ? ]
	public Cursor GetSubTotal(int billID) {
		SQLiteDatabase DB = this.getReadableDatabase();

		return DB.rawQuery("select * from display where billId = ? ", new String[]{String.valueOf(billID)});
	}

	//Searching in Date Range == [Select * from display where seller =? AND  date  BETWEEN ? AND ? ]
	public Cursor RangeSearch(String date, String toDate, String email) {
		String startDate_formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");
		String endDate_formattedDate = date_convertor.convertDateFormat(toDate, "dd/MM/yyyy", "yyyy-MM-dd");
		SQLiteDatabase DB = this.getReadableDatabase();

//		String sellerId = String.valueOf(get_userId(email));

		Cursor cursor = DB.rawQuery("SELECT d.indexs, p.productName product, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst, d.price " +
										                            "FROM display d " +
										                            "JOIN products p ON d.productId = p.productId " +
										                            "JOIN customers c ON d.customerId = c.customerId " +
										                            "JOIN users u ON d.sellerId = u.userId " +
										                            "WHERE u.email = ? AND d.date BETWEEN ? AND ?",
		                            new String[]{email, startDate_formattedDate, endDate_formattedDate}
		                           );
//		cursor = DB.rawQuery("Select * from display where sellerId =? AND  date  BETWEEN ? AND ? ", new String[]{sellerId, startDate_formattedDate, endDate_formattedDate});

		return cursor;
	}

	//    Insert customer info in customer Table ===================================================
	@SuppressLint("Range")
	public boolean InsertCustomer(int billId, String name, String number, String date, String email, int state) {

		int total = 0;

		Cursor cursor = GetSubTotal(billId);

		cursor.moveToFirst();
		do {
			total += cursor.getInt(cursor.getColumnIndex("subtotal"));
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

	// confirming that data is entered =============================================================
	public boolean ConfirmSale(int billId) {
		Cursor cursor = GetSubTotal(billId);
		boolean checkResult = cursor.getCount() > 0;
		cursor.close();
		return checkResult;
	}

	// Deleting bills with bill id =================================================================
	public boolean DeleteBillWithBillID(String billId, String email) {
		SQLiteDatabase db = getWritableDatabase();
		long delete_customer, delete_display;
		String sellerId = String.valueOf(get_userId(email));
		delete_customer = db.delete("customer", "billId = ? and sellerId = ?", new String[]{billId, sellerId});
		delete_display = db.delete("display", "billId = ? and sellerId = ?", new String[]{billId, sellerId});
		return delete_customer != -1 && delete_display != -1;
	}
//    ----------------------------------- Managing Stock -------------------------------------------

	// Delete Bill With Customer Number ============================================================
	public boolean DeleteBillWithCustomerNumber(String number, String email) {
		SQLiteDatabase db = getWritableDatabase();
		long delete_customer, delete_display;
		String sellerId = String.valueOf(get_userId(email));

		String getCustomerId = String.valueOf(get_customersId("", number, get_userId(email)));


		delete_customer = db.delete("customer", "customerId = ? and sellerId = ?", new String[]{getCustomerId, sellerId});
		delete_display = db.delete("display", "customerId = ? and sellerId = ?", new String[]{getCustomerId, sellerId});
		return delete_customer != -1 && delete_display != -1;
	}

	// Deleting bills with Customer Name ===========================================================
	public boolean DeleteBillWithCustomerName(String name, String email) {
		SQLiteDatabase db = getWritableDatabase();
		long delete_customer, delete_display;
		String sellerId = String.valueOf(get_userId(email));
		delete_customer = db.delete("customer", "customerName = ? and sellerId = ?", new String[]{name, sellerId});
		delete_display = db.delete("display", "customerName = ? and sellerId = ?", new String[]{name, sellerId});
		return delete_customer != -1 && delete_display != -1;
	}

	String getCustomerIdFromName(String name) {
		SQLiteDatabase DB = this.getReadableDatabase();
		Cursor customerId_C = DB.rawQuery("SELECT customerId FROM customers WHERE customerName = ? ", new String[]{name});

		customerId_C.moveToFirst();
		if (customerId_C.getCount() > 0) {
			@SuppressLint("Range") String id = String.valueOf(customerId_C.getInt(customerId_C.getColumnIndex("customerId")));
			customerId_C.close();
			return id;
		}
		return "";
	}

	// Deleting bills with Date ====================================================================
	@SuppressLint("Range")
	public boolean DeleteBillWithDate(Cursor data, String email) {
		data.moveToFirst();
		do {
			if (!DeleteBillWithBillID(data.getString(data.getColumnIndex("billId")), email))
				return false;
		} while (data.moveToNext());
		return true;
	}

	// creating extra 2 table for stock ============================================================
	public void CreateTable() {
//		SQLiteDatabase DB = this.getWritableDatabase();
//
//		DB.execSQL("Create TABLE IF NOT EXISTS stock(productID Integer primary key autoincrement ," + // 0
//										           "productName TEXT ," + // 1
//										           "catagory TEXT," + // 2
//										           "purchesPrice TEXT," + // 3
//										           "sellingPrice TEXT," + // 4
//										           "date Date," + // 5
//										           "quantity TEXT," + // 6
//										           "seller TEXT," + // 7
//										           "backup Integer," + // 8
//										           "Gst Integer)"); // 9
//
//		DB.execSQL("Create TABLE IF NOT EXISTS stockQuantity(productName TEXT ," + //0
//										           "quantity TEXT," + //1
//										           "price TEXT," + //2
//										           "seller TEXT ," + //3
//										           "backup Integer," + //4
//										           "stickTrackId Integer primary key autoincrement," + //5
//										           "Gst Integer)"); //6
	}

	// setting current quantity ==[Select * from stockQuantity where sellerId = ? AND productName = ?]
	public Cursor GetProductQuantity(String name, String seller) {

		SQLiteDatabase db = this.getReadableDatabase();

		String sellerId = String.valueOf(get_userId(seller));

		String productID = String.valueOf(get_productId(name, "all"));

		return db.rawQuery("Select * from stockQuantity where sellerId = ? AND productId = ?", new String[]{sellerId, productID});
	}

	// checking if GST is available or not =========================================================
	@SuppressLint("Range")
	public Boolean CheckGstAvailability(String email) {
		Cursor cursor = GetUser(email);
		cursor.moveToFirst();
		return !cursor.getString(cursor.getColumnIndex("gst")).equals("-1");
	}

	// getting current stock quantity ================[Select * from stockQuantity where sellerId = ?]
	// productName | quantity |
	public Cursor GetInventory(String seller) {
		SQLiteDatabase db = this.getReadableDatabase();

		String sellerId = String.valueOf(get_userId(seller));

		return db.rawQuery("Select * " +
										                   "from stockQuantity sq " +
										                   "JOIN products p ON sq.productId = p.productId " +
										                   "where sellerId = ?", new String[]{sellerId});
//		return db.rawQuery("Select * from stockQuantity where sellerId = ?", new String[]{sellerId});
	}

	// getting the cursor of Category ========================[Select * from stock where sellerId = ?]
	// category
	public Cursor GetCategory(String seller) {

		SQLiteDatabase db = this.getReadableDatabase();

		String sellerId = String.valueOf(get_userId(seller));

		return db.rawQuery("Select * from stock s JOIN products p ON s.productId = p.productId where sellerId = ?", new String[]{sellerId});
	}

	@SuppressLint("Range")
	public String getProductName(Integer ID) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("Select * from products where productId = ?", new String[]{String.valueOf(ID)});
		cursor.moveToFirst();
		return cursor.getString(cursor.getColumnIndex("productName"));
	}

	// removing the sold product Quantity ==========================================================
	@SuppressLint("Range")
	public Boolean RemoveSell(int billID, String seller) {

		// p_ID*  |  quantity*  |   price*   | sellerId*   |    GST

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
				contentValues.put("price", stockQuentityCursor.getString(stockQuentityCursor.getColumnIndex("price")));
				contentValues.put("sellerId", sellerId);
				contentValues.put("Gst", displayListCursor.getString(displayListCursor.getColumnIndex("Gst")));

				result = db.update("stockQuantity", contentValues, "sellerId = ? and productId = ? ", new String[]{sellerId, productID});

			} else { // Insert new Record in stockQuantity
				int Sell_qty = -1 * (Integer.parseInt(quantity)); // Making quantity negative so it's easy to add and we can know that user is not managing this stock !

				ContentValues contentValues = new ContentValues();
				contentValues.put("productId", productID);
				contentValues.put("quantity", String.valueOf(Sell_qty));
				contentValues.put("price", String.valueOf(Integer.parseInt(displayListCursor.getString(displayListCursor.getColumnIndex("price")))));
				contentValues.put("sellerId", sellerId);
				contentValues.put("Gst", displayListCursor.getString(displayListCursor.getColumnIndex("Gst")));

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

	// Adding Stock to stock table =================================================================
	public boolean AddStock(String name, String catagory, String pPrice, String sPrice, String date, String quantity, String seller, String gst) {

		SQLiteDatabase db = this.getWritableDatabase();

		String sellerId = String.valueOf(get_userId(seller));

		String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");

		ContentValues cv = new ContentValues();
//		cv.put("productName", name);
//		cv.put("category", catagory);
		cv.put("productId", get_productId(name, catagory));
		cv.put("purchasePrice", pPrice);
		cv.put("sellingPrice", sPrice);
		cv.put("date", formattedDate);
		cv.put("quantity", quantity);
		cv.put("sellerId", sellerId);
		cv.put("Gst", gst);
//		cv.put("backup", 0);

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

				contentValues.put("productId", get_productId(name, catagory));
				contentValues.put("quantity", newQty);
				contentValues.put("price", sPrice);
				contentValues.put("sellerId", sellerId);
				contentValues.put("Gst", gst);

//				contentValues.put("backup", 0);

				long result;
				result = db.update("stockQuantity", contentValues, "sellerId = ? and productId = ? ", new String[]{sellerId, String.valueOf(get_productId(name, catagory))});

				return result != -1;

			} else {
				ContentValues contentValues = new ContentValues();
//				contentValues.put("productName", name);
				contentValues.put("productId", get_productId(name, catagory));
				contentValues.put("quantity", quantity);
				contentValues.put("price", sPrice);
				contentValues.put("sellerId", sellerId);
				contentValues.put("Gst", gst);
//				contentValues.put("backup", 0);
				long result;

				result = db.insert("stockQuantity", null, contentValues);

				return result != -1;
			}
		}
	}

	// View current Stock Quantity ====================[Select * from stockQuantity where seller =?]
	public Cursor ViewStock(String seller) {
		CreateTable();
		SQLiteDatabase db = this.getReadableDatabase();

		String sellerId = String.valueOf(get_userId(seller));

		return db.rawQuery("Select * from stockQuantity sq JOIN products p ON sq.productId = p.productId where sellerId =?", new String[]{sellerId});
	}

	// view when stock is entered in table ==[Select * from stock where seller =? AND productName=?]
	public Cursor ViewProductHistory(String seller, String product) {

		SQLiteDatabase db = this.getReadableDatabase();

		String sellerId = String.valueOf(get_userId(seller));

		return db.rawQuery("SELECT * FROM stock " +
										                   "JOIN products ON stock.productId = products.productId " +
										                   "WHERE sellerId = ? AND products.productName = ?", new String[]{sellerId, product});

//		return db.rawQuery("SELECT * FROM stock " +
//										                   "WHERE sellerId = (SELECT userId FROM users WHERE email = ?) " +
//										                   "AND productId = (SELECT productId FROM products WHERE productName = ?)",
//		                   new String[]{seller, product});
//		return db.rawQuery("Select * from stock where sellerId =? AND productName=? ", new String[]{sellerId, product});
	}

	// view stock history but category wise ====[Select * from stock where seller =? AND catagory=?]
	public Cursor ViewCategoryHistory(String seller, String catagory) {

		SQLiteDatabase db = this.getReadableDatabase();

		return db.rawQuery("SELECT * FROM stock JOIN products ON stock.productId = products.productId WHERE sellerId = ? AND products.category = ?",
		                   new String[]{String.valueOf(get_userId(seller)), catagory});
//		                   return db.rawQuery("SELECT * FROM stock" +
//										                   " WHERE sellerId = (SELECT userId FROM users WHERE email = ?) " +
//										                   "AND productId = (SELECT productId FROM products WHERE catagory = ?)",
//		                   new String[]{seller, catagory});
//		return db.rawQuery("Select * from stock where sellerId =? AND catagory=? ", new String[]{sellerId, catagory});
	}

	// Salse data of a perticular product
	// =[Select SUM(quantity), SUM(price) ,AVG(price) from display where product = ? AND sellerId = ?]
	public Cursor ViewSaleProductHistory(String seller, String product) {
		SQLiteDatabase db = this.getReadableDatabase();

		String sellerId = String.valueOf(get_userId(seller));

		return db.rawQuery("Select SUM(quantity), SUM(price) ,AVG(price) " +
										                   "from display " +
										                   "where productId = (SELECT productId FROM products WHERE productName = ?) " +
										                   "AND sellerId = ?",
		                   new String[]{product, sellerId});
	}

	// Sales data of a particular category
	//[Select SUM(quantity), SUM(price) ,AVG(price) from display where product = ? AND category = ?]
	public Cursor ViewSaleCategoryHistory(String seller, String category) {

		SQLiteDatabase db = this.getReadableDatabase();

		String sellerId = String.valueOf(get_userId(seller));

		return db.rawQuery("Select DISTINCT productName " +
										                   "FROM stock " +
										                   "JOIN products ON stock.productId = products.productId " +
										                   "WHERE sellerId = ? " +
										                   "AND products.category = ? ",
		                   new String[]{sellerId, category});

//		return db.rawQuery("Select DISTINCT productName " +
//										                   "from stock " +
//										                   "where sellerId = ? " +
//										                   "AND productId = (SELECT productId FROM products WHERE category = ?) ",
//		                   new String[]{sellerId, category});
	}


	// Download backup =============================================================================

	@SuppressLint("Range")
	public String DownloadBackup(Context context, String email) {

		String password = "963258741";
		Cursor user = getdata();
		user.moveToFirst();
		boolean passwordFetched = false;
		do {
			if (user.getString(user.getColumnIndex("email")).equals(email)) {
				password = user.getString(user.getColumnIndex("password"));
				passwordFetched = true;
				break;
			}
		} while (user.moveToNext());
		Log.d("ENimesh", "Downlod before password is = " + password);


		while (!passwordFetched) {}

		if (!isPermissionGranted(context)) {
			return "Permission Denied";
		}

		try {
			// Get the path to the app's internal database
			String internalDatabasePath = context.getDatabasePath("Biller").getPath();
			File databaseFile = new File(internalDatabasePath);
			FileInputStream fis = new FileInputStream(databaseFile);

			// Create the backup file on external storage or other location
			String backupPath = context.getExternalFilesDir(null) + "/Backups/";
			File backupFolder = new File(backupPath);
			if (!backupFolder.exists()) {
				if (!backupFolder.mkdirs()) {
					return "Error Creating Directory";
				}
			}
			String backupDatabasePath = backupPath + "Biller_Backup.db";

			Log.d("ENimesh", "Downlod after password is = " + password);
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

//	@SuppressLint("Range")
//	public String DownloadBackup(Context context, String email) {
//		String userPassword = "963258741";
//		Cursor user = getdata();
//		user.moveToFirst();
//		while (user.moveToNext()) {
//			if (user.getString(user.getColumnIndex("email")).equals(email)) {
//				userPassword = user.getString(user.getColumnIndex("password"));
//				break;
//			}
//		}
//
//		if (!isPermissionGranted(context)) {
//			return "Permission Denied";
//		}
//
//		try {
//			// Step 1: Get the path to the app's internal database
//			String internalDatabasePath = context.getDatabasePath("Biller").getPath();
//
//			// Step 2: Open the database file using FileInputStream
//			FileInputStream fis = new FileInputStream(new File(internalDatabasePath));
//
//			// Step 3: Create the backup file path
//			String backupPath = context.getExternalFilesDir(null) + "/Backups/";
//			File backupFolder = new File(backupPath);
//			if (!backupFolder.exists()) {
//				if (!backupFolder.mkdirs()) {
//					Log.e("ENimesh", "Failed to create backup directory");
//					return "Error Creating Directory";
//				}
//			}
//			String backupDatabasePath = backupPath + "Biller_Backup.db";
//
//			// Step 4: Encrypt the database file
//			File encryptedBackupFile = new File(backupDatabasePath);
//			encryptFile(fis, encryptedBackupFile, userPassword);
//
//			return backupDatabasePath; // Backup successful
//		} catch (IOException | GeneralSecurityException e) {
//			e.printStackTrace();
//			return "Error";
//		}
//	}

	//	public String DownloadBackup(Context context) {
//		if (!isPermissionGranted(context)) {
//			return "Permission Denied";
//		}
//
//		try {
//			// Step 1: Get the path to the app's internal database
//			String internalDatabasePath = context.getDatabasePath("Biller").getPath();
//
//			Log.d("ENimesh","internalDatabasePath" + internalDatabasePath);
//
//			// Step 2: Open the database file using FileChannel for efficient file copy
//			File databaseFile = new File(internalDatabasePath);
//			FileInputStream fis = new FileInputStream(databaseFile);
//			FileChannel src = fis.getChannel();
//
//			// Step 3: Create the backup file on external storage or other location
//			String backupPath = context.getExternalFilesDir(null) + "/Backups/";
//			File backupFolder = new File(backupPath);
//			Log.d("ENimesh","backupPath = " + backupPath + " folder exists: " + backupFolder.exists());
//
//			if (!backupFolder.exists()) {
//				if (!backupFolder.mkdirs()) {
//					Log.e("ENimesh", "Failed to create backup directory");
//					return "Error Creating Directory";
//				}
//			}
//
//			Log.d("ENimesh","backupPath = " + backupPath + " folder exists again: " + backupFolder.exists());
//			String backupDatabasePath = backupPath + "Biller_Backup.db";
//			Log.d("ENimesh","backupDatabasePath" + backupDatabasePath);
//			FileOutputStream fos = new FileOutputStream(backupDatabasePath);
//			FileChannel dst = fos.getChannel();
//
//			// Step 4: Copy the database file to the backup location
//			dst.transferFrom(src, 0, src.size());
//
//			// Step 5: Close the channels
//			src.close();
//			dst.close();
//			fis.close();
//			fos.close();
//
//			return backupDatabasePath; // Backup successful
//		} catch (IOException e) {
//			e.printStackTrace();
//			return "Error";
//		}
//	}
	private boolean isPermissionGranted(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			return Environment.isExternalStorageManager();
		} else {
			int writePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
			int readPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
			return writePermission == PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED;
		}
	}


//	public String DownloadBackup(Context context) {
//		try {
//			// Step 1: Get the path to the app's internal database
//			String internalDatabasePath = context.getDatabasePath("Biller").getPath();
//
//			Log.d("ENimesh","internalDatabasePath" + internalDatabasePath);
//
//			// Step 2: Open the database file using FileChannel for efficient file copy
//			File databaseFile = new File(internalDatabasePath);
//			FileInputStream fis = new FileInputStream(databaseFile);
//			FileChannel src = fis.getChannel();
//
//			// Step 3: Create the backup file on external storage or other location
//			String backupPath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.nimeshkadecha.Biller/Backups/";
//			File backupFolder = new File(backupPath);
//			Log.d("ENimesh","backupPath = " + backupPath + "folder" + backupFolder.exists());
//			if (!backupFolder.exists()) {
//				backupFolder.mkdirs();
//			}
//			Log.d("ENimesh","backupPath = " + backupPath + "folder existe again" + backupFolder.exists());
//			String backupDatabasePath = backupPath + "Biller_Backup.db";
//			Log.d("ENimesh","backupDatabasePath" + backupDatabasePath);
//			FileOutputStream fos = new FileOutputStream(backupDatabasePath);
//			FileChannel dst = fos.getChannel();
//
//			// Step 4: Copy the database file to the backup location
//			dst.transferFrom(src, 0, src.size());
//
//			// Step 5: Close the channels
//			src.close();
//			dst.close();
//			fis.close();
//			fos.close();
//
//			return backupDatabasePath; // Backup successful
//		} catch (IOException e) {
//			e.printStackTrace();
//			return "Error";
//		}
//	}

	// download backup but at different location and very frequently called ========================
	public Boolean AutoLocalBackup(Context context) {
		return true;
//		try {
//			// Step 1: Get the path to the app's internal database
//			String internalDatabasePath = context.getDatabasePath("Biller").getPath();
//
//			// Step 2: Open the database file using FileChannel for efficient file copy
//			File databaseFile = new File(internalDatabasePath);
//			FileInputStream fis = new FileInputStream(databaseFile);
//			FileChannel src = fis.getChannel();
//
//			// Step 3: Create the backup file on external storage or other location
//			String backupPath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.nimeshkadecha.Biller/Auto Backup/";
//			File backupFolder = new File(backupPath);
//			if (!backupFolder.exists()) {
//				backupFolder.mkdirs();
//			}
//			String backupDatabasePath = backupPath + "Auto_Biller_Backup.db";
//			FileOutputStream fos = new FileOutputStream(backupDatabasePath);
//			FileChannel dst = fos.getChannel();
//
//			// Step 4: Copy the database file to the backup location
//			dst.transferFrom(src, 0, src.size());
//
//			// Step 5: Close the channels
//			src.close();
//			dst.close();
//			fis.close();
//			fos.close();
//
//			return true; // Backup successful
//		} catch (IOException e) {
//			e.printStackTrace();
//			return false;
//		}
	}

	// upload backup from either login screen or normally ==========================================

	// Upload local backup method with decryption
// Upload local backup method with decryption and validation
	public boolean UploadLocalBackup(Context context, File selectedFile, char[] password) {
		File dbFile = context.getDatabasePath("Biller");
		try {
			FileInputStream fis = new FileInputStream(selectedFile);

			Log.d("ENimesh", "upload password = " + Arrays.toString(password));

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


//	public String UploadLocalBackup(Context context, File selectedFile) {
//		File dbFile = context.getDatabasePath("Biller");
//		try (FileChannel src = new FileInputStream(selectedFile).getChannel();
//		     FileChannel dst = new FileOutputStream(dbFile).getChannel()) {
//			dst.transferFrom(src, 0, src.size());
//			return "True";
//		} catch (IOException e) {
//			return "False";
//		}
//	}

	// Generate secret key from password and salt
	private SecretKey generateKey(char[] password, byte[] salt) throws GeneralSecurityException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		PBEKeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
		SecretKey tmp = factory.generateSecret(spec);
		return new SecretKeySpec(tmp.getEncoded(), "AES");
	}

}
