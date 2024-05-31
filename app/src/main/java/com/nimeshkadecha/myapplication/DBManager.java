package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class DBManager extends SQLiteOpenHelper {
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

		DB.execSQL("CREATE TABLE users(userId INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT UNIQUE, password TEXT, gst TEXT, contact TEXT, address TEXT)");


		DB.execSQL("Create table display(indexs Integer primary key autoincrement," +  // 0
										           "product TEXT ," + //1
										           "price TEXT," + //2
										           "quantity TEXT," + //3
										           "subtotal Float," +//4
										           "customerName TEXT," +//5
										           "customerNumber TEXT," +//6
										           "date Date," +//7
										           "billId Integer ," +//8
										           "seller TEXT," +//9
										           "backup Integer," +//10
										           "Gst Integer)"); //11
//        Customer table
		DB.execSQL("Create TABLE customer(billId Integer primary key," + // 0
										           "customerName TEXT," + // 1
										           "customerNumber TEXT," + // 2
										           "date Date," + // 3
										           "total TEXT," + // 4
										           "seller TEXT," + // 5
										           "backup Integer)"); // 6
		//        stock table


	}

	@Override
	public void onUpgrade(SQLiteDatabase DB, int oldVersion, int newVersion) {
		CreateTable();
		DB.execSQL("drop table if exists stock");
		DB.execSQL("drop table if exists stockQuentity");
		DB.execSQL("drop table if exists users");
		DB.execSQL("drop table if exists display");
		DB.execSQL("drop table if exists customer");
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
		long result;

		result = DB.insert("users", null, contentValues);

		return result != -1;

	}

	//    Login Verification ==================[select * from users where email =? AND password = ?]
	public boolean LoginUser(String email, String password) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        Creating a cursor to check password;
		SQLiteDatabase DB = this.getReadableDatabase();
		@SuppressLint("Recycle") Cursor cursor = DB.rawQuery("select * from users where email =? AND password = ?", new String[]{email, password});
		return cursor.getCount() > 0;
	}

	//    Validate user ========================================[select * from users where email =?]
	public boolean ValidateUser(String email) {
		SQLiteDatabase DB = this.getReadableDatabase();
		@SuppressLint("Recycle") Cursor cursor = DB.rawQuery("select * from users where email =?", new String[]{email});
		return cursor.getCount() > 0;
	}

	//    Getting user info for texting purposes in register ==================[select * from users]
	public Cursor getdata() {
		SQLiteDatabase DB = this.getReadableDatabase();
		return DB.rawQuery("select * from users", null);
	}

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
			long result;

			result = DB.update("users", contentValues, "email =?", new String[]{Email});
			return result != -1;
		} else {
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
			long check;
			check = DB.update("users", contentValues, "email =?", new String[]{email});
			return check != -1;
		} else {
			return false;
		}

	}

	//    Deleting User =======================================[select * from users where email = ?]
	public boolean DeleteUser(String email) {
		SQLiteDatabase DB = this.getWritableDatabase();

		@SuppressLint("Recycle") Cursor cursor = DB.rawQuery("select * from users where email = ?", new String[]{email});

		if (cursor.getCount() > 0) {
			long check, check1, check2;
			check = DB.delete("users", "email = ?", new String[]{email});
			check1 = DB.delete("display", "seller = ?", new String[]{email});
			check2 = DB.delete("customer", "seller = ?", new String[]{email});
			return check != -1 && check1 != -1 && check2 != -1;
		} else {
			return false;
		}

	}

//------------------------------------- Working on customer tables ---------------------------------

	//    ADDING ITEM in list/ in recyclerview / in display TABLE ==================================
	@SuppressLint("Range")
	public boolean InsertList(String name, String price, String quantity, String cName, String cNumber, String date, int billId, String email, int state, String Gst) {
		int pricecustom = Integer.parseInt(price);

		int quentityCustom = Integer.parseInt(quantity);
		if (Gst.equals("")) {
			Gst = "0";
		}
		float tax = (pricecustom * quentityCustom) * (Integer.parseInt(Gst) / 100f);

		float subtotal = ((pricecustom * quentityCustom) + tax);

		SQLiteDatabase DB = this.getWritableDatabase();

		ContentValues contentValues = new ContentValues();

		//  Checking if the product is same them update the quantity
		Cursor data = DisplayList(billId);

		data.moveToFirst();

		boolean check_if_already_added = false;
		int number_of_product = 0;
		String Index = null;

		if (data.getCount() != 0) {
			do {
				if (data.getString(data.getColumnIndex("product")).equals(name)) {
					check_if_already_added = true;
					Index = data.getString(data.getColumnIndex("indexs")); // geting index
					number_of_product = data.getInt(data.getColumnIndex("quantity"));
				}
			} while (data.moveToNext());
		}

		if (check_if_already_added) {
			number_of_product += 1;
			float taxUpdate = (pricecustom * number_of_product) * (Integer.parseInt(Gst) / 100f);
			subtotal = ((pricecustom * number_of_product) + taxUpdate);

			String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");

			contentValues.put("Product", name);
			contentValues.put("price", price);
			contentValues.put("quantity", number_of_product);
			contentValues.put("subtotal", subtotal);
			contentValues.put("customerName", cName);
			contentValues.put("customerNumber", cNumber);
			contentValues.put("date", formattedDate);
			contentValues.put("billId", billId);
			contentValues.put("seller", email);
			contentValues.put("backup", state);
			contentValues.put("Gst", Gst);

			long result;

			result = DB.update("display", contentValues, "indexs=?", new String[]{Index});
			return result != -1;
		} else {

			String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");

			contentValues.put("Product", name);
			contentValues.put("price", price);
			contentValues.put("quantity", quantity);
			contentValues.put("subtotal", subtotal);
			contentValues.put("customerName", cName);
			contentValues.put("customerNumber", cNumber);
			contentValues.put("date", formattedDate);
			contentValues.put("billId", billId);
			contentValues.put("seller", email);
			contentValues.put("backup", state);
			contentValues.put("Gst", Gst);

			long result;

			result = DB.insert("display", null, contentValues);
			return result != -1;
		}
	}

	//    Bill id is unique every time so no need of email =[select * from display where billId =? ]
	public Cursor DisplayList(int billId) {
		SQLiteDatabase DB = this.getReadableDatabase();
		String bID = String.valueOf(billId);
		return DB.rawQuery("select * from display where billId =? ", new String[]{bID});
	}

	//    Remove from list ====================================[delete from display where index =? ]
	public Cursor RemoveItem(String id) {

		SQLiteDatabase DB = this.getWritableDatabase();

		return DB.rawQuery("delete from display where indexs = ?", new String[]{id});
	}

	//    Remove from list =============================[Update Quantity in display where index =? ]
	public boolean UpdateQuantity(int quentity, float subtotal, int index) {

		SQLiteDatabase DB = this.getWritableDatabase();

		//        Getting all values in
		ContentValues contentValues = new ContentValues();
		contentValues.put("quantity", String.valueOf(quentity));
		contentValues.put("subtotal", subtotal);

		long check;
		check = DB.update("display", contentValues, "indexs =?", new String[]{String.valueOf(index)});
		return check != -1;
	}

	// Checking Total without saving ===============================================================
	@SuppressLint("Range")
	public int CheckTotal(int billID) {
		Cursor c = DisplayList(billID);
		int total = 0;
		c.moveToFirst();
		if (c.getCount() == 0) {
			return 0;
		}
		do {
			total += c.getInt(c.getColumnIndex("subtotal"));
		} while (c.moveToNext());
		return total;
	}

	//    Generating BILL ID ===============================================[select * from customer]
	public int GetBillId() {
		SQLiteDatabase DB = this.getReadableDatabase();

		int id = 0;
		try {
			@SuppressLint("Recycle") Cursor cursor = DB.rawQuery("select * from customer ORDER BY billId ASC", null);
			if (cursor.getCount() > 0) {
				cursor.moveToLast();
				id = Integer.parseInt(cursor.getString(0));
			}
			id++;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}

	//    Fetching all customer ============================[select * from customer where seller =?]
	public Cursor CustomerInformation(String email) {
		SQLiteDatabase DB = this.getReadableDatabase();

		return DB.rawQuery("select * from customer where seller =?", new String[]{email});
	}

	// Fetching single customer =======[select * from customer where seller =? and customerName = ?]
	public Cursor ParticularCustomerInformation(String email, String name) {
		SQLiteDatabase DB = this.getReadableDatabase();

		return DB.rawQuery("select * from customer where seller =? and customerName = ? ", new String[]{email, name});
	}

	//    search based on customer name =[select * from display where customerName = ? and seller=?]
	public Cursor CustomerNameBill(String Name, String email) {
		SQLiteDatabase DB = this.getReadableDatabase();

		return DB.rawQuery("select * from display where customerName = ? and seller=?", new String[]{Name, email});
	}

	//    Search Based on single date =========[select * from display where date = ? and seller = ?]
	public Cursor CustomerDateBill(String date, String email) {

		String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");

		SQLiteDatabase DB = this.getReadableDatabase();

		return DB.rawQuery("select * from display where date = ? and seller = ?", new String[]{formattedDate, email});
	}

	//    Search Based on number =====[select * from display where customerNumber = ? and seller=? ]
	public Cursor CustomerNumberBill(String Number, String email) {
		SQLiteDatabase DB = this.getReadableDatabase();

		return DB.rawQuery("select * from display where customerNumber = ? and seller=? ", new String[]{Number, email});
	}

	//    Search based on billID ===========[select * from display where billId = ? and seller = ? ]
	public Cursor CustomerBillID(int billID, String email) {
		SQLiteDatabase DB = this.getReadableDatabase();

		String billId = String.valueOf(billID);

		return DB.rawQuery("select * from display where billId = ? and seller = ? ", new String[]{billId, email});
	}

	//    Getting Bill TOTAL ==============================[select * from customer where billid = ?]
	public Cursor BillTotal(int billID) {
		SQLiteDatabase DB = this.getReadableDatabase();

		String billId = String.valueOf(billID);

		return DB.rawQuery("select * from customer where billId = ? ", new String[]{billId});
	}

	//    Getting sub total from billID ==================[ select * from display where billId = ? ]
	public Cursor GetSubTotal(int billID) {
		SQLiteDatabase DB = this.getReadableDatabase();

		String billId = String.valueOf(billID);

		return DB.rawQuery("select * from display where billId = ? ", new String[]{billId});
	}

	//Searching in Date Range == [Select * from display where seller =? AND  date  BETWEEN ? AND ? ]
	public Cursor RangeSearch(String date, String toDate, String email) {
		String startDate_formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");
		String endDate_formattedDate = date_convertor.convertDateFormat(toDate, "dd/MM/yyyy", "yyyy-MM-dd");
		SQLiteDatabase DB = this.getReadableDatabase();
		Cursor cursor;
		cursor = DB.rawQuery("Select * from display where seller =? AND  date  BETWEEN ? AND ? ", new String[]{email, startDate_formattedDate, endDate_formattedDate});

		return cursor;
	}

	//    Insert customer info in customer Table ===================================================
	@SuppressLint("Range")
	public boolean InsertCustomer(int billId, String name, String number, String date, String email, int state) {

		int total = 0;

//        int ID = Integer.parseInt(billId);

		Cursor cursor = GetSubTotal(billId);

		cursor.moveToFirst();
		do {
			total += cursor.getInt(cursor.getColumnIndex("subtotal"));
		} while (cursor.moveToNext());

		String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");

		ContentValues contentValues = new ContentValues();
		contentValues.put("billId", billId);
		contentValues.put("customerName", name);
		contentValues.put("customerNumber", number);
		contentValues.put("date", formattedDate);
		contentValues.put("total", total);
		contentValues.put("seller", email);
		contentValues.put("backup", state);

		SQLiteDatabase DB = this.getWritableDatabase();

		long check;
		check = DB.insert("customer", null, contentValues);
		return check != -1;
	}

	// confirming that data is entered =============================================================
	public boolean ConfirmSale(int billId) {
		Cursor cursor = GetSubTotal(billId);

		return cursor.getCount() > 0;
	}

	// Deleting bills with bill id =================================================================
	public boolean DeleteBillWithBillID(String billid, String email) {
		SQLiteDatabase db = getWritableDatabase();
		long delete_customer, delete_display;
		delete_customer = db.delete("customer", "billId = ? and seller = ?", new String[]{billid, email});
		delete_display = db.delete("display", "billId = ? and seller = ?", new String[]{billid, email});
		return delete_customer != -1 && delete_display != -1;
	}

	// Delete Bill With Customer Number ============================================================
	public boolean DeleteBillWithCustomerNumber(String number, String email) {
		SQLiteDatabase db = getWritableDatabase();
		long delete_customer, delete_display;
		delete_customer = db.delete("customer", "customerNumber = ? and seller = ?", new String[]{number, email});
		delete_display = db.delete("display", "customerNumber = ? and seller = ?", new String[]{number, email});
		return delete_customer != -1 && delete_display != -1;
	}

	// Deleting bills with Customer Name ===========================================================
	public boolean DeleteBillWithCustomerName(String name, String email) {
		SQLiteDatabase db = getWritableDatabase();
		long delete_customer, delete_display;
		delete_customer = db.delete("customer", "customerName = ? and seller = ?", new String[]{name, email});
		delete_display = db.delete("display", "customerName = ? and seller = ?", new String[]{name, email});
		return delete_customer != -1 && delete_display != -1;
	}

	// Deleting bills with Date ====================================================================
	public boolean DeleteBillWithDate(Cursor data, String email) {
		data.moveToFirst();

		return DeleteBillWithBillID(data.getString(8), email);

	}

	// Deleting bills with Date range ==============================================================
	public boolean DeleteCustomerWithRangeDate(Cursor data, String email) {
		data.moveToFirst();
		return DeleteBillWithBillID(data.getString(8), email);
	}

//    ----------------------------------- Managing Stock -------------------------------------------

	// creating extra 2 table for stock ============================================================
	public void CreateTable() {
		SQLiteDatabase DB = this.getWritableDatabase();

		DB.execSQL("Create TABLE IF NOT EXISTS stock(productID Integer primary key autoincrement ," + // 0
										           "productName TEXT ," + // 1
										           "catagory TEXT," + // 2
										           "purchesPrice TEXT," + // 3
										           "sellingPrice TEXT," + // 4
										           "date Date," + // 5
										           "quentity TEXT," + // 6
										           "seller TEXT," + // 7
										           "backup Integer," + // 8
										           "Gst Integer)"); // 9

		DB.execSQL("Create TABLE IF NOT EXISTS stockQuentity(productName TEXT ," + //0
										           "quentity TEXT," + //1
										           "price TEXT," + //2
										           "seller TEXT ," + //3
										           "backup Integer," + //4
										           "stickTrackId Integer primary key autoincrement," + //5
										           "Gst Integer)"); //6
	}

	// setting current quantity ==[Select * from stockQuentity where seller = ? AND productName = ?]
	public Cursor GetProductQuantity(String name, String seller) {

		SQLiteDatabase db = this.getReadableDatabase();

		return db.rawQuery("Select * from stockQuentity where seller = ? AND productName = ?", new String[]{seller, name});
	}

	// checking if GST is available or not =========================================================
	@SuppressLint("Range")
	public Boolean CheckGstAvailability(String email) {
		Cursor cursor = GetUser(email);
		cursor.moveToFirst();
		return !cursor.getString(cursor.getColumnIndex("gst")).equals("no");
	}

	// getting current stock quantity ================[Select * from stockQuentity where seller = ?]
	public Cursor GetInventory(String seller) {

		CreateTable();

		SQLiteDatabase db = this.getReadableDatabase();

		return db.rawQuery("Select * from stockQuentity where seller = ?", new String[]{seller});
	}

	// getting the cursor of Category ========================[Select * from stock where seller = ?]
	public Cursor GetCategory(String seller) {

		SQLiteDatabase db = this.getReadableDatabase();

		return db.rawQuery("Select * from stock where seller = ?", new String[]{seller});
	}

	// removing the sold product Quantity ==========================================================
	@SuppressLint("Range")
	public Boolean RemoveSell(int id, String seller) {

		SQLiteDatabase db = this.getWritableDatabase();
		String name;
		String quentity;

		Cursor c = DisplayList(id);

		c.moveToFirst();

		long result;

		do {
			name = c.getString(c.getColumnIndex("product"));
			quentity = c.getString(c.getColumnIndex("quantity"));

			Cursor cursor = GetProductQuantity(name, seller);
			cursor.moveToFirst();
			if (cursor.getCount() > 0) {

				int qty = Integer.parseInt(cursor.getString(cursor.getColumnIndex("quentity")));

				int newQty = qty - Integer.parseInt(quentity);

				ContentValues contentValues = new ContentValues();
				contentValues.put("productName", name);
				contentValues.put("quentity", newQty);
				contentValues.put("price", cursor.getString(cursor.getColumnIndex("price")));
				contentValues.put("seller", seller);
				contentValues.put("backup", 0);
				contentValues.put("Gst", c.getString(c.getColumnIndex("Gst")));

				result = db.update("stockQuentity", contentValues, "seller = ? and productName = ? ", new String[]{seller, name});
			} else {

				int Sell_qty = -1 * (Integer.parseInt(quentity));

				ContentValues contentValues = new ContentValues();
				contentValues.put("productName", name);
				contentValues.put("quentity", String.valueOf(Sell_qty));
				contentValues.put("price", String.valueOf(Integer.parseInt(c.getString(c.getColumnIndex("price")))));
				contentValues.put("seller", seller);
				contentValues.put("backup", 0);
				contentValues.put("Gst", c.getString(c.getColumnIndex("Gst")));
				try {
					result = db.insert("stockQuentity", null, contentValues);
					if (result == -1) {
						return false;
					}
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		} while (c.moveToNext());

		return result != -1;
	}

	// Adding Stock to stock table =================================================================
	public boolean AddStock(String name, String catagory, String pPrice, String sPrice, String date, String quentity, String seller, String gst) {
		CreateTable();
		SQLiteDatabase db = this.getWritableDatabase();

		String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");

		ContentValues cv = new ContentValues();
		cv.put("productName", name);
		cv.put("catagory", catagory);
		cv.put("purchesPrice", pPrice);
		cv.put("sellingPrice", sPrice);
		cv.put("date", formattedDate);
		cv.put("quentity", quentity);
		cv.put("seller", seller);
		cv.put("backup", 0);
		cv.put("Gst", gst);

		long check;

		check = db.insert("stock", null, cv);

		if (check == -1) {
			return false;
		} else {
			Cursor cursor = GetProductQuantity(name, seller);
			cursor.moveToFirst();
			if (cursor.getCount() > 0) {

				@SuppressLint("Range") int qty = Integer.parseInt(cursor.getString(cursor.getColumnIndex("quentity")));
				int newQty = Integer.parseInt(quentity) + qty;

				ContentValues contentValues = new ContentValues();
				contentValues.put("productName", name);
				contentValues.put("quentity", newQty);
				contentValues.put("price", sPrice);
				contentValues.put("seller", seller);
				contentValues.put("backup", 0);
				contentValues.put("Gst", gst);

				long result;
				result = db.update("stockQuentity", contentValues, "seller = ? and productName = ? ", new String[]{seller, name});

				return result != -1;
				
			} else {
				ContentValues contentValues = new ContentValues();
				contentValues.put("productName", name);
				contentValues.put("quentity", quentity);
				contentValues.put("price", sPrice);
				contentValues.put("seller", seller);
				contentValues.put("backup", 0);
				contentValues.put("Gst", gst);
				long result;

				result = db.insert("stockQuentity", null, contentValues);

				return result != -1;
			}
		}
	}

	// View current Stock Quantity ====================[Select * from stockQuentity where seller =?]
	public Cursor ViewStock(String seller) {
		CreateTable();
		SQLiteDatabase db = this.getReadableDatabase();

		return db.rawQuery("Select * from stockQuentity where seller =?", new String[]{seller});
	}

	// view when stock is entered in table ==[Select * from stock where seller =? AND productName=?]
	public Cursor ViewProductHistory(String seller, String product) {

		SQLiteDatabase db = this.getReadableDatabase();

		return db.rawQuery("Select * from stock where seller =? AND productName=? ", new String[]{seller, product});
	}

	// view stock history but category wise ====[Select * from stock where seller =? AND catagory=?]
	public Cursor ViewCategoryHistory(String seller, String catagory) {

		SQLiteDatabase db = this.getReadableDatabase();

		return db.rawQuery("Select * from stock where seller =? AND catagory=? ", new String[]{seller, catagory});
	}

	// Salse data of a perticular product
	// =[Select SUM(quantity), SUM(price) ,AVG(price) from display where product = ? AND seller = ?]
	public Cursor ViewSaleProductHistory(String seller, String product) {
		SQLiteDatabase db = this.getReadableDatabase();

		return db.rawQuery("Select SUM(quantity), SUM(price) ,AVG(price) from display where product = ? AND seller = ?", new String[]{product, seller});
	}

	// Sales data of a particular category
	//[Select SUM(quantity), SUM(price) ,AVG(price) from display where product = ? AND category = ?]
	public Cursor ViewSaleCategoryHistory(String seller, String category) {

		SQLiteDatabase db = this.getReadableDatabase();

		return db.rawQuery("Select DISTINCT productName from stock where seller =? AND catagory=? ", new String[]{seller, category});
	}

	// Download backup =============================================================================
	public String DownloadBackup(Context context) {
		try {
			// Step 1: Get the path to the app's internal database
			String internalDatabasePath = context.getDatabasePath("Biller").getPath();

			// Step 2: Open the database file using FileChannel for efficient file copy
			File databaseFile = new File(internalDatabasePath);
			FileInputStream fis = new FileInputStream(databaseFile);
			FileChannel src = fis.getChannel();

			// Step 3: Create the backup file on external storage or other location
			String backupPath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.nimeshkadecha.Biller/Backup/";
			File backupFolder = new File(backupPath);
			if (!backupFolder.exists()) {
				backupFolder.mkdirs();
			}
			String backupDatabasePath = backupPath + "Biller_Backup.db";
			FileOutputStream fos = new FileOutputStream(backupDatabasePath);
			FileChannel dst = fos.getChannel();

			// Step 4: Copy the database file to the backup location
			dst.transferFrom(src, 0, src.size());

			// Step 5: Close the channels
			src.close();
			dst.close();
			fis.close();
			fos.close();

			return backupDatabasePath; // Backup successful
		} catch (IOException e) {
			e.printStackTrace();
			return "Error";
		}
	}

	// download backup but at different location and very frequently called ========================
	public Boolean AutoLocalBackup(Context context) {
		try {
			// Step 1: Get the path to the app's internal database
			String internalDatabasePath = context.getDatabasePath("Biller").getPath();

			// Step 2: Open the database file using FileChannel for efficient file copy
			File databaseFile = new File(internalDatabasePath);
			FileInputStream fis = new FileInputStream(databaseFile);
			FileChannel src = fis.getChannel();

			// Step 3: Create the backup file on external storage or other location
			String backupPath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.nimeshkadecha.Biller/Auto Backup/";
			File backupFolder = new File(backupPath);
			if (!backupFolder.exists()) {
				backupFolder.mkdirs();
			}
			String backupDatabasePath = backupPath + "Auto_Biller_Backup.db";
			FileOutputStream fos = new FileOutputStream(backupDatabasePath);
			FileChannel dst = fos.getChannel();

			// Step 4: Copy the database file to the backup location
			dst.transferFrom(src, 0, src.size());

			// Step 5: Close the channels
			src.close();
			dst.close();
			fis.close();
			fos.close();

			return true; // Backup successful
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	// upload backup from either login screen or normally ==========================================
	public String UploadLocalBackup(Context context, File selectedFile) {
		File dbFile = context.getDatabasePath("Biller");
		try (FileChannel src = new FileInputStream(selectedFile).getChannel();
		     FileChannel dst = new FileOutputStream(dbFile).getChannel()) {
			dst.transferFrom(src, 0, src.size());
			return "True";
		} catch (IOException e) {
			return "False";
		}
	}
}

//
//package com.nimeshkadecha.myapplication;
//
//import android.annotation.SuppressLint;
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.os.Environment;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.nio.channels.FileChannel;
//
//public class DBManager extends SQLiteOpenHelper {
//    public DBManager(Context context) {
//        super(context, "Biller", null, 1);
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase DB) {
//        // Create normalized tables
//        DB.execSQL("CREATE TABLE users(userId INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT UNIQUE, password TEXT, gst TEXT, contact TEXT, address TEXT)");
//        DB.execSQL("CREATE TABLE products(productId INTEGER PRIMARY KEY AUTOINCREMENT, productName TEXT, category TEXT)");
//        DB.execSQL("CREATE TABLE stock(stockId INTEGER PRIMARY KEY AUTOINCREMENT, productId INTEGER, purchasePrice TEXT, sellingPrice TEXT, date DATE, quantity TEXT, sellerId INTEGER, Gst INTEGER, FOREIGN KEY(productId) REFERENCES products(productId), FOREIGN KEY(sellerId) REFERENCES users(userId))");
//        DB.execSQL("CREATE TABLE stockQuentity(stickTrackId INTEGER PRIMARY KEY AUTOINCREMENT, productName TEXT, quentity TEXT, price TEXT, sellerId INTEGER, Gst INTEGER, FOREIGN KEY(sellerId) REFERENCES users(userId))");
//        DB.execSQL("CREATE TABLE customers(customerId INTEGER PRIMARY KEY AUTOINCREMENT, customerName TEXT, customerNumber TEXT)");
//        DB.execSQL("CREATE TABLE sales(saleId INTEGER PRIMARY KEY AUTOINCREMENT, customerId INTEGER, stockId INTEGER, quantity INTEGER, date DATE, sellerId INTEGER, FOREIGN KEY(customerId) REFERENCES customers(customerId), FOREIGN KEY(stockId) REFERENCES stock(stockId), FOREIGN KEY(sellerId) REFERENCES users(userId))");
//        DB.execSQL("CREATE TABLE display(indexs INTEGER PRIMARY KEY AUTOINCREMENT, productId INTEGER, quantity INTEGER, subtotal REAL, customerId INTEGER, sellerId INTEGER, date DATE, billId INTEGER, Gst INTEGER, FOREIGN KEY(productId) REFERENCES products(productId), FOREIGN KEY(customerId) REFERENCES customers(customerId), FOREIGN KEY(sellerId) REFERENCES users(userId))");
//        DB.execSQL("CREATE TABLE customer(billId INTEGER PRIMARY KEY, customerId INTEGER, date DATE, total REAL, sellerId INTEGER, FOREIGN KEY(customerId) REFERENCES customers(customerId), FOREIGN KEY(sellerId) REFERENCES users(userId))");
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase DB, int oldVersion, int newVersion) {
//        CreateTable();
//        DB.execSQL("DROP TABLE IF EXISTS stock");
//        DB.execSQL("DROP TABLE IF EXISTS stockQuentity");
//        DB.execSQL("DROP TABLE IF EXISTS users");
//        DB.execSQL("DROP TABLE IF EXISTS products");
//        DB.execSQL("DROP TABLE IF EXISTS customers");
//        DB.execSQL("DROP TABLE IF EXISTS sales");
//        DB.execSQL("DROP TABLE IF EXISTS display");
//        DB.execSQL("DROP TABLE IF EXISTS customer");
//    }
//
//    // User Management Methods
//    public boolean RegisterUser(String name, String email, String password, String gst, String contact, String address) {
//        SQLiteDatabase DB = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("name", name);
//        contentValues.put("email", email);
//        contentValues.put("password", password);
//        contentValues.put("gst", gst);
//        contentValues.put("contact", contact);
//        contentValues.put("address", address);
//        long result = DB.insert("users", null, contentValues);
//        return result != -1;
//    }
//
//    public boolean LoginUser(String email, String password) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        Cursor cursor = DB.rawQuery("SELECT * FROM users WHERE email =? AND password = ?", new String[]{email, password});
//        return cursor.getCount() > 0;
//    }
//
//    public boolean ValidateUser(String email) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        Cursor cursor = DB.rawQuery("SELECT * FROM users WHERE email =?", new String[]{email});
//        return cursor.getCount() > 0;
//    }
//
//    public Cursor getdata() {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        return DB.rawQuery("SELECT * FROM users", null);
//    }
//
//    public Cursor GetUser(String email) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        return DB.rawQuery("SELECT * FROM users WHERE email=?", new String[]{email});
//    }
//
//    public boolean ResetPassword(String Email, String password) {
//        SQLiteDatabase DB = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("password", password);
//        Cursor cursor = DB.rawQuery("SELECT * FROM users WHERE email = ?", new String[]{Email});
//        if (cursor.getCount() > 0) {
//            long result = DB.update("users", contentValues, "email =?", new String[]{Email});
//            return result != -1;
//        } else {
//            return false;
//        }
//    }
//
//    public boolean UpdateUser(String name, String email, String password, String gst, String contact, String address) {
//        SQLiteDatabase DB = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("name", name);
//        contentValues.put("password", password);
//        contentValues.put("gst", gst);
//        contentValues.put("contact", contact);
//        contentValues.put("address", address);
//        Cursor cursor = DB.rawQuery("SELECT * FROM users WHERE email =?", new String[]{email});
//        if (cursor.getCount() > 0) {
//            long check = DB.update("users", contentValues, "email =?", new String[]{email});
//            return check != -1;
//        } else {
//            return false;
//        }
//    }
//
//    public boolean DeleteUser(String email) {
//        SQLiteDatabase DB = this.getWritableDatabase();
//        Cursor cursor = DB.rawQuery("SELECT * FROM users WHERE email = ?", new String[]{email});
//        if (cursor.getCount() > 0) {
//            long check = DB.delete("users", "email = ?", new String[]{email});
//            long check1 = DB.delete("display", "sellerId = ?", new String[]{email});
//            long check2 = DB.delete("customer", "sellerId = ?", new String[]{email});
//            long check3 = DB.delete("stock", "sellerId = ?", new String[]{email});
//            long check4 = DB.delete("stockQuentity", "sellerId = ?", new String[]{email});
//            return check != -1 && check1 != -1 && check2 != -1 && check3 != -1 && check4 != -1;
//        } else {
//            return false;
//        }
//    }
//
//    // Sales and Customer Management Methods
//    public boolean InsertList(String name, String price, String quantity, String cName, String cNumber, String date, int billId, String email, int state, String Gst) {
//        int pricecustom = Integer.parseInt(price);
//        int quentityCustom = Integer.parseInt(quantity);
//        if (Gst.equals("")) {
//            Gst = "0";
//        }
//        float tax = (pricecustom * quentityCustom) * (Integer.parseInt(Gst) / 100f);
//        float subtotal = ((pricecustom * quentityCustom) + tax);
//        SQLiteDatabase DB = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//
//        // Check if product exists and get productId
//        Cursor productCursor = DB.rawQuery("SELECT productId FROM products WHERE productName = ?", new String[]{name});
//        int productId = 0;
//        if (productCursor.moveToFirst()) {
//            productId = productCursor.getInt(0);
//        } else {
//            // Insert new product if it doesn't exist
//            contentValues.put("productName", name);
//            productId = (int) DB.insert("products", null, contentValues);
//        }
//        productCursor.close();
//
//        // Check if customer exists and get customerId
//        Cursor customerCursor = DB.rawQuery("SELECT customerId FROM customers WHERE customerName = ? AND customerNumber = ?", new String[]{cName, cNumber});
//        int customerId = 0;
//        if (customerCursor.moveToFirst()) {
//            customerId = customerCursor.getInt(0);
//        } else {
//            // Insert new customer if it doesn't exist
//            contentValues.clear();
//            contentValues.put("customerName", cName);
//            contentValues.put("customerNumber", cNumber);
//            customerId = (int) DB.insert("customers", null, contentValues);
//        }
//        customerCursor.close();
//
//        // Get sellerId
//        Cursor sellerCursor = DB.rawQuery("SELECT userId FROM users WHERE email = ?", new String[]{email});
//        int sellerId = 0;
//        if (sellerCursor.moveToFirst()) {
//            sellerId = sellerCursor.getInt(0);
//        }
//        sellerCursor.close();
//
//        // Check if product is already in the list
//        Cursor data = DisplayList(billId);
//        data.moveToFirst();
//        boolean check_if_already_added = false;
//        int number_of_product = 0;
//        String Index = null;
//
//        if (data.getCount() != 0) {
//            do {
//                if (data.getInt(1) == productId) {
//                    check_if_already_added = true;
//                    Index = data.getString(0);
//                    number_of_product = data.getInt(2);
//                }
//            } while (data.moveToNext());
//        }
//
//        if (check_if_already_added) {
//            number_of_product += 1;
//            float taxUpdate = (pricecustom * number_of_product) * (Integer.parseInt(Gst) / 100f);
//            subtotal = ((pricecustom * number_of_product) + taxUpdate);
//
//            String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");
//            contentValues.clear();
//            contentValues.put("productId", productId);
//            contentValues.put("quantity", number_of_product);
//            contentValues.put("subtotal", subtotal);
//            contentValues.put("customerId", customerId);
//            contentValues.put("sellerId", sellerId);
//            contentValues.put("date", formattedDate);
//            contentValues.put("billId", billId);
//            contentValues.put("Gst", Gst);
//
//            long result = DB.update("display", contentValues, "indexs=?", new String[]{Index});
//            return result != -1;
//        } else {
//            String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");
//            contentValues.clear();
//            contentValues.put("productId", productId);
//            contentValues.put("quantity", quantity);
//            contentValues.put("subtotal", subtotal);
//            contentValues.put("customerId", customerId);
//            contentValues.put("sellerId", sellerId);
//            contentValues.put("date", formattedDate);
//            contentValues.put("billId", billId);
//            contentValues.put("Gst", Gst);
//
//            long result = DB.insert("display", null, contentValues);
//            return result != -1;
//        }
//    }
//
//    public Cursor DisplayList(int billId) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        String bID = String.valueOf(billId);
//        return DB.rawQuery("SELECT d.indexs, p.productName, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst, p.sellingPrice " + "FROM display d " + "JOIN products p ON d.productId = p.productId " + "JOIN customers c ON d.customerId = c.customerId " + "WHERE d.billId = ?", new String[]{bID});
//    }
//
//    public Cursor RemoveItem(String id) {
//        SQLiteDatabase DB = this.getWritableDatabase();
//        return DB.rawQuery("DELETE FROM display WHERE indexs = ?", new String[]{id});
//    }
//
//    public boolean UpdateQuantity(int quentity, float subtotal, int index) {
//        SQLiteDatabase DB = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("quantity", String.valueOf(quentity));
//        contentValues.put("subtotal", subtotal);
//        long check = DB.update("display", contentValues, "indexs =?", new String[]{String.valueOf(index)});
//        return check != -1;
//    }
//
//    public int CheckTotal(int billID) {
//        Cursor c = DisplayList(billID);
//        int total = 0;
//        c.moveToFirst();
//        if (c.getCount() == 0) {
//            return 0;
//        }
//        do {
//            total += c.getInt(4);
//        } while (c.moveToNext());
//        return total;
//    }
//
//    public int GetBillId() {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        int id = 0;
//        try {
//            Cursor cursor = DB.rawQuery("SELECT * FROM customer ORDER BY billId ASC", null);
//            if (cursor.getCount() > 0) {
//                cursor.moveToLast();
//                id = Integer.parseInt(cursor.getString(0));
//            }
//            id++;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return id;
//    }
//
//    public Cursor CustomerInformation(String email) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        return DB.rawQuery("SELECT c.customerId, c.customerName, c.customerNumber FROM customers c JOIN users u ON c.customerId = u.userId WHERE u.email = ?", new String[]{email});
//    }
//
//    public Cursor ParticularCustomerInformation(String email, String name) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        return DB.rawQuery("SELECT c.customerId, c.customerName, c.customerNumber FROM customers c JOIN users u ON c.customerId = u.userId WHERE u.email = ? AND c.customerName = ?", new String[]{email, name});
//    }
//
//    public Cursor CustomerNameBill(String Name, String email) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        return DB.rawQuery("SELECT d.indexs, p.productName, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst, p.sellingPrice FROM display d JOIN products p ON d.productId = p.productId JOIN customers c ON d.customerId = c.customerId JOIN users u ON d.sellerId = u.userId WHERE c.customerName = ? AND u.email = ?", new String[]{Name, email});
//    }
//
//    public Cursor CustomerDateBill(String date, String email) {
//        String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");
//        SQLiteDatabase DB = this.getReadableDatabase();
//        return DB.rawQuery("SELECT d.indexs, p.productName, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst, p.sellingPrice FROM display d JOIN products p ON d.productId = p.productId JOIN customers c ON d.customerId = c.customerId JOIN users u ON d.sellerId = u.userId WHERE d.date = ? AND u.email = ?", new String[]{formattedDate, email});
//    }
//
//    public Cursor CustomerNumberBill(String Number, String email) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        return DB.rawQuery("SELECT d.indexs, p.productName, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst, p.sellingPrice FROM display d JOIN products p ON d.productId = p.productId JOIN customers c ON d.customerId = c.customerId JOIN users u ON d.sellerId = u.userId WHERE c.customerNumber = ? AND u.email = ?", new String[]{Number, email});
//    }
//
//    public Cursor CustomerBillID(int billID, String email) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        String billId = String.valueOf(billID);
//        return DB.rawQuery("SELECT d.indexs, p.productName, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst, p.sellingPrice FROM display d JOIN products p ON d.productId = p.productId JOIN customers c ON d.customerId = c.customerId JOIN users u ON d.sellerId = u.userId WHERE d.billId = ? AND u.email = ?", new String[]{billId, email});
//    }
//
//    public Cursor BillTotal(int billID) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        String billId = String.valueOf(billID);
//        return DB.rawQuery("SELECT * FROM customer WHERE billId = ?", new String[]{billId});
//    }
//
//    public Cursor GetSubTotal(int billID) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        String billId = String.valueOf(billID);
//        return DB.rawQuery("SELECT d.indexs, p.productName, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst, p.sellingPrice " + "FROM display d " + "JOIN products p ON d.productId = p.productId " + "JOIN customers c ON d.customerId = c.customerId " + "WHERE d.billId = ?", new String[]{billId});
//    }
//
//    public Cursor RangeSearch(String date, String toDate, String email) {
//        String startDate_formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");
//        String endDate_formattedDate = date_convertor.convertDateFormat(toDate, "dd/MM/yyyy", "yyyy-MM-dd");
//        SQLiteDatabase DB = this.getReadableDatabase();
//        Cursor cursor = DB.rawQuery("SELECT d.indexs, p.productName, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst, p.sellingPrice FROM display d JOIN products p ON d.productId = p.productId JOIN customers c ON d.customerId = c.customerId JOIN users u ON d.sellerId = u.userId WHERE u.email = ? AND d.date BETWEEN ? AND ?", new String[]{email, startDate_formattedDate, endDate_formattedDate});
//        return cursor;
//    }
//
//    public boolean InsertCustomer(int billId, String name, String number, String date, String email, int state) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//
//        int total = 0;
//        Cursor cursor = GetSubTotal(billId);
//        cursor.moveToFirst();
//        do {
//            total += cursor.getInt(4);
//        } while (cursor.moveToNext());
//
//        String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");
//
//        // Get sellerId and customerId
//        Cursor sellerCursor = DB.rawQuery("SELECT userId FROM users WHERE email = ?", new String[]{email});
//        int sellerId = 0;
//        if (sellerCursor.moveToFirst()) {
//            sellerId = sellerCursor.getInt(0);
//        }
//        sellerCursor.close();
//
//        Cursor customerCursor = DB.rawQuery("SELECT customerId FROM customers WHERE customerName = ? AND customerNumber = ?", new String[]{name, number});
//        int customerId = 0;
//        if (customerCursor.moveToFirst()) {
//            customerId = customerCursor.getInt(0);
//        } else {
//            ContentValues values = new ContentValues();
//            values.put("customerName", name);
//            values.put("customerNumber", number);
//            customerId = (int) DB.insert("customers", null, values);
//        }
//        customerCursor.close();
//
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("billId", billId);
//        contentValues.put("customerId", customerId);
//        contentValues.put("date", formattedDate);
//        contentValues.put("total", total);
//        contentValues.put("sellerId", sellerId);
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        long check = db.insert("customer", null, contentValues);
//        return check != -1;
//    }
//
//    public boolean ConfirmSale(int billId) {
//        Cursor cursor = GetSubTotal(billId);
//        return cursor.getCount() > 0;
//    }
//
//    public boolean DeleteBillWithBillID(String billid, String email) {
//        SQLiteDatabase db = getWritableDatabase();
//        long delete_customer = db.delete("customer", "billId = ? AND sellerId = ?", new String[]{billid, email});
//        long delete_display = db.delete("display", "billId = ? AND sellerId = ?", new String[]{billid, email});
//        return delete_customer != -1 && delete_display != -1;
//    }
//
//    public boolean DeleteBillWithCustomerNumber(String number, String email) {
//        SQLiteDatabase db = getWritableDatabase();
//        long delete_customer = db.delete("customer", "customerId = (SELECT customerId FROM customers WHERE customerNumber = ?)", new String[]{number});
//        long delete_display = db.delete("display", "customerId = (SELECT customerId FROM customers WHERE customerNumber = ?)", new String[]{number});
//        return delete_customer != -1 && delete_display != -1;
//    }
//
//    public boolean DeleteBillWithCustomerName(String name, String email) {
//        SQLiteDatabase db = getWritableDatabase();
//        long delete_customer = db.delete("customer", "customerId = (SELECT customerId FROM customers WHERE customerName = ?)", new String[]{name});
//        long delete_display = db.delete("display", "customerId = (SELECT customerId FROM customers WHERE customerName = ?)", new String[]{name});
//        return delete_customer != -1 && delete_display != -1;
//    }
//
//    public boolean DeleteBillWithDate(Cursor data, String email) {
//        data.moveToFirst();
//        return DeleteBillWithBillID(data.getString(8), email);
//    }
//
//    public boolean DeleteCustomerWithRangeDate(Cursor data, String email) {
//        data.moveToFirst();
//        return DeleteBillWithBillID(data.getString(8), email);
//    }
//
//    // Stock Management Methods
//    public void CreateTable() {
//        SQLiteDatabase DB = this.getWritableDatabase();
//        DB.execSQL("CREATE TABLE IF NOT EXISTS stock(productID INTEGER PRIMARY KEY AUTOINCREMENT, productName TEXT, catagory TEXT, purchesPrice TEXT, sellingPrice TEXT, date DATE, quentity TEXT, seller TEXT, Gst INTEGER)");
//        DB.execSQL("CREATE TABLE IF NOT EXISTS stockQuentity(stickTrackId INTEGER PRIMARY KEY AUTOINCREMENT, productName TEXT, quentity TEXT, price TEXT, seller TEXT, Gst INTEGER)");
//    }
//
//    public Cursor GetProductQuantity(String name, String seller) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery("SELECT * FROM stockQuentity WHERE sellerId = (SELECT userId FROM users WHERE email = ?) AND productName = ?", new String[]{seller, name});
//        return cursor;
//    }
//
//    public Boolean CheckGstAvailability(String email) {
//        Cursor c = GetUser(email);
//        c.moveToFirst();
//        return !c.getString(4).equals("no");
//    }
//
//    public Cursor GetInventory(String seller) {
//        CreateTable();
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery("SELECT * FROM stockQuentity WHERE sellerId = (SELECT userId FROM users WHERE email = ?)", new String[]{seller});
//    }
//
//    public Cursor GetCategory(String seller) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery("SELECT * FROM stock WHERE sellerId = (SELECT userId FROM users WHERE email = ?)", new String[]{seller});
//    }
//
//    public Boolean RemoveSell(int id, String seller) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        SQLiteDatabase DB = this.getReadableDatabase();
//
//        String name;
//        String quentity;
//
//        Cursor c = DisplayList(id);
//        c.moveToFirst();
//
//        long result;
//        do {
//            name = c.getString(1);
//            quentity = c.getString(2);
//
//            Cursor cursor = GetProductQuantity(name, seller);
//            cursor.moveToFirst();
//            if (cursor.getCount() > 0) {
//                int qty = Integer.parseInt(cursor.getString(1));
//                int newQty = qty - Integer.parseInt(quentity);
//
//                ContentValues contentValues = new ContentValues();
//                contentValues.put("productName", name);
//                contentValues.put("quentity", newQty);
//                contentValues.put("price", cursor.getString(3));
//                contentValues.put("sellerId", (int) DB.rawQuery("SELECT userId FROM users WHERE email = ?", new String[]{seller}).getLong(0));
//                contentValues.put("Gst", cursor.getString(5));
//
//                result = db.update("stockQuentity", contentValues, "sellerId = ? AND productName = ?", new String[]{String.valueOf(DB.rawQuery("SELECT userId FROM users WHERE email = ?", new String[]{seller}).getLong(0)), name});
//            } else {
//                int Sell_qty = -1 * (Integer.parseInt(quentity));
//                ContentValues contentValues = new ContentValues();
//                contentValues.put("productName", name);
//                contentValues.put("quentity", String.valueOf(Sell_qty));
//                contentValues.put("price", String.valueOf(Integer.parseInt(c.getString(2))));
//                contentValues.put("sellerId", (int) DB.rawQuery("SELECT userId FROM users WHERE email = ?", new String[]{seller}).getLong(0));
//                contentValues.put("Gst", c.getString(9));
//
//                try {
//                    result = db.insert("stockQuentity", null, contentValues);
//                    if (result == -1) {
//                        return false;
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return false;
//                }
//            }
//        } while (c.moveToNext());
//
//        return result != -1;
//    }
//
//    public boolean AddStock(String name, String catagory, String pPrice, String sPrice, String date, String quentity, String seller, String gst) {
//        CreateTable();
//        SQLiteDatabase db = this.getWritableDatabase();
//        String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");
//
//        // Get sellerId and productId
//        Cursor sellerCursor = db.rawQuery("SELECT userId FROM users WHERE email = ?", new String[]{seller});
//        int sellerId = 0;
//        if (sellerCursor.moveToFirst()) {
//            sellerId = sellerCursor.getInt(0);
//        }
//        sellerCursor.close();
//
//        Cursor productCursor = db.rawQuery("SELECT productId FROM products WHERE productName = ?", new String[]{name});
//        int productId = 0;
//        if (productCursor.moveToFirst()) {
//            productId = productCursor.getInt(0);
//        } else {
//            ContentValues values = new ContentValues();
//            values.put("productName", name);
//            values.put("category", catagory);
//            productId = (int) db.insert("products", null, values);
//        }
//        productCursor.close();
//
//        ContentValues cv = new ContentValues();
//        cv.put("productId", productId);
//        cv.put("purchasePrice", pPrice);
//        cv.put("sellingPrice", sPrice);
//        cv.put("date", formattedDate);
//        cv.put("quantity", quentity);
//        cv.put("sellerId", sellerId);
//        cv.put("Gst", gst);
//        long check = db.insert("stock", null, cv);
//
//        if (check == -1) {
//            return false;
//        } else {
//            Cursor cursor = GetProductQuantity(name, seller);
//            cursor.moveToFirst();
//            if (cursor.getCount() > 0) {
//                int qty = Integer.parseInt(cursor.getString(1));
//                int newQty = Integer.parseInt(quentity) + qty;
//
//                ContentValues contentValues = new ContentValues();
//                contentValues.put("productName", name);
//                contentValues.put("quentity", newQty);
//                contentValues.put("price", sPrice);
//                contentValues.put("sellerId", sellerId);
//                contentValues.put("Gst", gst);
//
//                long result = db.update("stockQuentity", contentValues, "sellerId = ? and productName = ?", new String[]{String.valueOf(sellerId), name});
//
//                return result != -1;
//            } else {
//                ContentValues contentValues = new ContentValues();
//                contentValues.put("productName", name);
//                contentValues.put("quentity", quentity);
//                contentValues.put("price", sPrice);
//                contentValues.put("sellerId", sellerId);
//                contentValues.put("Gst", gst);
//
//                long result = db.insert("stockQuentity", null, contentValues);
//                return result != -1;
//            }
//        }
//    }
//
//    public Cursor ViewStock(String seller) {
//        CreateTable();
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery("SELECT * FROM stockQuentity WHERE sellerId = (SELECT userId FROM users WHERE email = ?)", new String[]{seller});
//    }
//
//    public Cursor ViewProductHistory(String seller, String product) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery("SELECT * FROM stock WHERE sellerId = (SELECT userId FROM users WHERE email = ?) AND productName = ?", new String[]{seller, product});
//    }
//
//    public Cursor ViewCategoryHistory(String seller, String catagory) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery("SELECT * FROM stock WHERE sellerId = (SELECT userId FROM users WHERE email = ?) AND catagory = ?", new String[]{seller, catagory});
//    }
//
//    public Cursor ViewSaleProductHistory(String seller, String product) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery("SELECT SUM(quantity), SUM(price), AVG(price) FROM display WHERE productId = (SELECT productId FROM products WHERE productName = ?) AND sellerId = (SELECT userId FROM users WHERE email = ?)", new String[]{product, seller});
//    }
//
//    public Cursor ViewSaleCategoryHistory(String seller, String category) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery("SELECT DISTINCT productName FROM products WHERE productId IN (SELECT productId FROM stock WHERE sellerId = (SELECT userId FROM users WHERE email = ?) AND catagory = ?)", new String[]{seller, category});
//    }
//
//    // Backup and Restore Methods
//    public String DownloadBackup(Context context) {
//        try {
//            String internalDatabasePath = context.getDatabasePath("Biller").getPath();
//            File databaseFile = new File(internalDatabasePath);
//            FileInputStream fis = new FileInputStream(databaseFile);
//            FileChannel src = fis.getChannel();
//
//            String backupPath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.nimeshkadecha.Biller/Backup/";
//            File backupFolder = new File(backupPath);
//            if (!backupFolder.exists()) {
//                backupFolder.mkdirs();
//            }
//            String backupDatabasePath = backupPath + "Biller_Backup.db";
//            FileOutputStream fos = new FileOutputStream(backupDatabasePath);
//            FileChannel dst = fos.getChannel();
//
//            dst.transferFrom(src, 0, src.size());
//
//            src.close();
//            dst.close();
//            fis.close();
//            fos.close();
//
//            return backupDatabasePath;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "Error";
//        }
//    }
//
//    public Boolean AutoLocalBackup(Context context) {
//        try {
//            String internalDatabasePath = context.getDatabasePath("Biller").getPath();
//            File databaseFile = new File(internalDatabasePath);
//            FileInputStream fis = new FileInputStream(databaseFile);
//            FileChannel src = fis.getChannel();
//
//            String backupPath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.nimeshkadecha.Biller/Auto Backup/";
//            File backupFolder = new File(backupPath);
//            if (!backupFolder.exists()) {
//                backupFolder.mkdirs();
//            }
//            String backupDatabasePath = backupPath + "Auto_Biller_Backup.db";
//            FileOutputStream fos = new FileOutputStream(backupDatabasePath);
//            FileChannel dst = fos.getChannel();
//
//            dst.transferFrom(src, 0, src.size());
//
//            src.close();
//            dst.close();
//            fis.close();
//            fos.close();
//
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public String UploadLocalBackup(Context context, File selectedFile) {
//        File dbFile = context.getDatabasePath("Biller");
//        try (FileChannel src = new FileInputStream(selectedFile).getChannel(); FileChannel dst = new FileOutputStream(dbFile).getChannel()) {
//            dst.transferFrom(src, 0, src.size());
//            return "True";
//        } catch (IOException e) {
//            return "False";
//        }
//    }
//}

//
//package com.nimeshkadecha.myapplication;
//
//import android.annotation.SuppressLint;
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.os.Environment;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.nio.channels.FileChannel;
//
//public class DBManager extends SQLiteOpenHelper {
//    public DBManager(Context context) {
//        super(context, "Biller", null, 1);
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase DB) {
//        // Create normalized tables
//        DB.execSQL("CREATE TABLE users(userId INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT UNIQUE, password TEXT, gst TEXT, contact TEXT, address TEXT)");
//
//        DB.execSQL("CREATE TABLE products(productId INTEGER PRIMARY KEY AUTOINCREMENT, productName TEXT, category TEXT)");
//
//        DB.execSQL("CREATE TABLE stock(stockId INTEGER PRIMARY KEY AUTOINCREMENT, productId INTEGER, purchasePrice TEXT, sellingPrice TEXT, date DATE, quantity TEXT, sellerId INTEGER, Gst INTEGER, FOREIGN KEY(productId) REFERENCES products(productId), FOREIGN KEY(sellerId) REFERENCES users(userId))");
//
//        DB.execSQL("CREATE TABLE stockQuentity(stickTrackId INTEGER PRIMARY KEY AUTOINCREMENT, productName TEXT, quentity TEXT, price TEXT, sellerId INTEGER, Gst INTEGER, FOREIGN KEY(sellerId) REFERENCES users(userId))");
//
//        DB.execSQL("CREATE TABLE customers(customerId INTEGER PRIMARY KEY AUTOINCREMENT, customerName TEXT, customerNumber TEXT)");
//
//        DB.execSQL("CREATE TABLE sales(saleId INTEGER PRIMARY KEY AUTOINCREMENT, customerId INTEGER, stockId INTEGER, quantity INTEGER, date DATE,  sellerId INTEGER, FOREIGN KEY(customerId) REFERENCES customers(customerId), FOREIGN KEY(stockId) REFERENCES stock(stockId), FOREIGN KEY(sellerId) REFERENCES users(userId))");
//
//        DB.execSQL("CREATE TABLE display(indexs INTEGER PRIMARY KEY AUTOINCREMENT, productId INTEGER, quantity INTEGER, subtotal REAL, customerId INTEGER, sellerId INTEGER, date DATE, billId INTEGER, Gst INTEGER, FOREIGN KEY(productId) REFERENCES products(productId), FOREIGN KEY(customerId) REFERENCES customers(customerId), FOREIGN KEY(sellerId) REFERENCES users(userId))");
//
//        DB.execSQL("CREATE TABLE customer(billId INTEGER PRIMARY KEY, customerId INTEGER, date DATE, total REAL, sellerId INTEGER, FOREIGN KEY(customerId) REFERENCES customers(customerId), FOREIGN KEY(sellerId) REFERENCES users(userId))");
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase DB, int oldVersion, int newVersion) {
//        CreateTable();
//        DB.execSQL("DROP TABLE IF EXISTS stock");
//        DB.execSQL("DROP TABLE IF EXISTS stockQuentity");
//        DB.execSQL("DROP TABLE IF EXISTS users");
//        DB.execSQL("DROP TABLE IF EXISTS products");
//        DB.execSQL("DROP TABLE IF EXISTS customers");
//        DB.execSQL("DROP TABLE IF EXISTS sales");
//        DB.execSQL("DROP TABLE IF EXISTS display");
//        DB.execSQL("DROP TABLE IF EXISTS customer");
//    }
//
//    // User Management Methods
//    public boolean RegisterUser(String name, String email, String password, String gst, String contact, String address) {
//        SQLiteDatabase DB = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("name", name);
//        contentValues.put("email", email);
//        contentValues.put("password", password);
//        contentValues.put("gst", gst);
//        contentValues.put("contact", contact);
//        contentValues.put("address", address);
//        long result = DB.insert("users", null, contentValues);
//        return result != -1;
//    }
//
//    public boolean LoginUser(String email, String password) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        Cursor cursor = DB.rawQuery("SELECT * FROM users WHERE email =? AND password = ?", new String[]{email, password});
//        return cursor.getCount() > 0;
//    }
//
//    public boolean ValidateUser(String email) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        Cursor cursor = DB.rawQuery("SELECT * FROM users WHERE email =?", new String[]{email});
//        return cursor.getCount() > 0;
//    }
//
//    public Cursor getdata() {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        return DB.rawQuery("SELECT * FROM users", null);
//    }
//
//    public Cursor GetUser(String email) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        return DB.rawQuery("SELECT * FROM users WHERE email=?", new String[]{email});
//    }
//
//    public boolean ResetPassword(String Email, String password) {
//        SQLiteDatabase DB = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("password", password);
//        Cursor cursor = DB.rawQuery("SELECT * FROM users WHERE email = ?", new String[]{Email});
//        if (cursor.getCount() > 0) {
//            long result = DB.update("users", contentValues, "email =?", new String[]{Email});
//            return result != -1;
//        } else {
//            return false;
//        }
//    }
//
//    public boolean UpdateUser(String name, String email, String password, String gst, String contact, String address) {
//        SQLiteDatabase DB = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("name", name);
//        contentValues.put("password", password);
//        contentValues.put("gst", gst);
//        contentValues.put("contact", contact);
//        contentValues.put("address", address);
//        Cursor cursor = DB.rawQuery("SELECT * FROM users WHERE email =?", new String[]{email});
//        if (cursor.getCount() > 0) {
//            long check = DB.update("users", contentValues, "email =?", new String[]{email});
//            return check != -1;
//        } else {
//            return false;
//        }
//    }
//
//    public boolean DeleteUser(String email) {
//        SQLiteDatabase DB = this.getWritableDatabase();
//        Cursor cursor = DB.rawQuery("SELECT * FROM users WHERE email = ?", new String[]{email});
//        if (cursor.getCount() > 0) {
//            long check = DB.delete("users", "email = ?", new String[]{email});
//            long check1 = DB.delete("display", "sellerId = ?", new String[]{email});
//            long check2 = DB.delete("customer", "sellerId = ?", new String[]{email});
//            long check3 = DB.delete("stock", "sellerId = ?", new String[]{email});
//            long check4 = DB.delete("stockQuentity", "sellerId = ?", new String[]{email});
//            return check != -1 && check1 != -1 && check2 != -1 && check3 != -1 && check4 != -1;
//        } else {
//            return false;
//        }
//    }
//
//    // Sales and Customer Management Methods
//    public boolean InsertList(String name, String price, String quantity, String cName, String cNumber, String date, int billId, String email, int state, String Gst) {
//        int pricecustom = Integer.parseInt(price);
//        int quentityCustom = Integer.parseInt(quantity);
//        if (Gst.equals("")) {
//            Gst = "0";
//        }
//        float tax = (pricecustom * quentityCustom) * (Integer.parseInt(Gst) / 100f);
//        float subtotal = ((pricecustom * quentityCustom) + tax);
//        SQLiteDatabase DB = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//
//        // Check if product exists and get productId
//        Cursor productCursor = DB.rawQuery("SELECT productId FROM products WHERE productName = ?", new String[]{name});
//        int productId = 0;
//        if (productCursor.moveToFirst()) {
//            productId = productCursor.getInt(0);
//        } else {
//            // Insert new product if it doesn't exist
//            contentValues.put("productName", name);
//            productId = (int) DB.insert("products", null, contentValues);
//        }
//        productCursor.close();
//
//        // Check if customer exists and get customerId
//        Cursor customerCursor = DB.rawQuery("SELECT customerId FROM customers WHERE customerName = ? AND customerNumber = ?", new String[]{cName, cNumber});
//        int customerId = 0;
//        if (customerCursor.moveToFirst()) {
//            customerId = customerCursor.getInt(0);
//        } else {
//            // Insert new customer if it doesn't exist
//            contentValues.clear();
//            contentValues.put("customerName", cName);
//            contentValues.put("customerNumber", cNumber);
//            customerId = (int) DB.insert("customers", null, contentValues);
//        }
//        customerCursor.close();
//
//        // Get sellerId
//        Cursor sellerCursor = DB.rawQuery("SELECT userId FROM users WHERE email = ?", new String[]{email});
//        int sellerId = 0;
//        if (sellerCursor.moveToFirst()) {
//            sellerId = sellerCursor.getInt(0);
//        }
//        sellerCursor.close();
//
//        // Check if product is already in the list
//        Cursor data = DisplayList(billId);
//        data.moveToFirst();
//        boolean check_if_already_added = false;
//        int number_of_product = 0;
//        String Index = null;
//
//        if (data.getCount() != 0) {
//            do {
//                if (data.getInt(1) == productId) {
//                    check_if_already_added = true;
//                    Index = data.getString(0);
//                    number_of_product = data.getInt(2);
//                }
//            } while (data.moveToNext());
//        }
//
//        if (check_if_already_added) {
//            number_of_product += 1;
//            float taxUpdate = (pricecustom * number_of_product) * (Integer.parseInt(Gst) / 100f);
//            subtotal = ((pricecustom * number_of_product) + taxUpdate);
//
//            String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");
//            contentValues.clear();
//            contentValues.put("productId", productId);
//            contentValues.put("quantity", number_of_product);
//            contentValues.put("subtotal", subtotal);
//            contentValues.put("customerId", customerId);
//            contentValues.put("sellerId", sellerId);
//            contentValues.put("date", formattedDate);
//            contentValues.put("billId", billId);
//            contentValues.put("Gst", Gst);
//
//            long result = DB.update("display", contentValues, "indexs=?", new String[]{Index});
//            return result != -1;
//        } else {
//            String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");
//            contentValues.clear();
//            contentValues.put("productId", productId);
//            contentValues.put("quantity", quantity);
//            contentValues.put("subtotal", subtotal);
//            contentValues.put("customerId", customerId);
//            contentValues.put("sellerId", sellerId);
//            contentValues.put("date", formattedDate);
//            contentValues.put("billId", billId);
//            contentValues.put("Gst", Gst);
//
//            long result = DB.insert("display", null, contentValues);
//            return result != -1;
//        }
//    }
//
//    public Cursor DisplayList(int billId) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        String bID = String.valueOf(billId);
//        return DB.rawQuery("SELECT d.indexs, p.productName, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst, p.sellingPrice " +
//                "FROM display d " +
//                "JOIN products p ON d.productId = p.productId " +
//                "JOIN customers c ON d.customerId = c.customerId " +
//                "WHERE d.billId = ?", new String[]{bID});
//    }
//
//    public Cursor RemoveItem(String id) {
//        SQLiteDatabase DB = this.getWritableDatabase();
//        return DB.rawQuery("DELETE FROM display WHERE indexs = ?", new String[]{id});
//    }
//
//    public boolean UpdateQuantity(int quentity, float subtotal, int index) {
//        SQLiteDatabase DB = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("quantity", String.valueOf(quentity));
//        contentValues.put("subtotal", subtotal);
//        long check = DB.update("display", contentValues, "indexs =?", new String[]{String.valueOf(index)});
//        return check != -1;
//    }
//
//    public int CheckTotal(int billID) {
//        Cursor c = DisplayList(billID);
//        int total = 0;
//        c.moveToFirst();
//        if (c.getCount() == 0) {
//            return 0;
//        }
//        do {
//            total += c.getInt(4);
//        } while (c.moveToNext());
//        return total;
//    }
//
//    public int GetBillId() {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        int id = 0;
//        try {
//            Cursor cursor = DB.rawQuery("SELECT * FROM customer ORDER BY billId ASC", null);
//            if (cursor.getCount() > 0) {
//                cursor.moveToLast();
//                id = Integer.parseInt(cursor.getString(0));
//            }
//            id++;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return id;
//    }
//
//    public Cursor CustomerInformation(String email) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        return DB.rawQuery("SELECT c.customerId, c.customerName, c.customerNumber FROM customers c JOIN users u ON c.customerId = u.userId WHERE u.email = ?", new String[]{email});
//    }
//
//    public Cursor ParticularCustomerInformation(String email, String name) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        return DB.rawQuery("SELECT c.customerId, c.customerName, c.customerNumber FROM customers c JOIN users u ON c.customerId = u.userId WHERE u.email = ? AND c.customerName = ?", new String[]{email, name});
//    }
//
//    public Cursor CustomerNameBill(String Name, String email) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        return DB.rawQuery("SELECT d.indexs, p.productName, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst FROM display d JOIN products p ON d.productId = p.productId JOIN customers c ON d.customerId = c.customerId JOIN users u ON d.sellerId = u.userId WHERE c.customerName = ? AND u.email = ?", new String[]{Name, email});
//    }
//
//    public Cursor CustomerDateBill(String date, String email) {
//        String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");
//        SQLiteDatabase DB = this.getReadableDatabase();
//        return DB.rawQuery("SELECT d.indexs, p.productName, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst FROM display d JOIN products p ON d.productId = p.productId JOIN customers c ON d.customerId = c.customerId JOIN users u ON d.sellerId = u.userId WHERE d.date = ? AND u.email = ?", new String[]{formattedDate, email});
//    }
//
//    public Cursor CustomerNumberBill(String Number, String email) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        return DB.rawQuery("SELECT d.indexs, p.productName, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst FROM display d JOIN products p ON d.productId = p.productId JOIN customers c ON d.customerId = c.customerId JOIN users u ON d.sellerId = u.userId WHERE c.customerNumber = ? AND u.email = ?", new String[]{Number, email});
//    }
//
//    public Cursor CustomerBillID(int billID, String email) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        String billId = String.valueOf(billID);
//        return DB.rawQuery("SELECT d.indexs, p.productName, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst FROM display d JOIN products p ON d.productId = p.productId JOIN customers c ON d.customerId = c.customerId JOIN users u ON d.sellerId = u.userId WHERE d.billId = ? AND u.email = ?", new String[]{billId, email});
//    }
//
//    public Cursor BillTotal(int billID) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        String billId = String.valueOf(billID);
//        return DB.rawQuery("SELECT * FROM customer WHERE billId = ?", new String[]{billId});
//    }
//
//    public Cursor GetSubTotal(int billID) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        String billId = String.valueOf(billID);
//        return DB.rawQuery("SELECT * FROM display WHERE billId = ?", new String[]{billId});
//    }
//
//    public Cursor RangeSearch(String date, String toDate, String email) {
//        String startDate_formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");
//        String endDate_formattedDate = date_convertor.convertDateFormat(toDate, "dd/MM/yyyy", "yyyy-MM-dd");
//        SQLiteDatabase DB = this.getReadableDatabase();
//        Cursor cursor = DB.rawQuery("SELECT d.indexs, p.productName, d.quantity, d.subtotal, c.customerName, c.customerNumber, d.date, d.billId, d.Gst FROM display d JOIN products p ON d.productId = p.productId JOIN customers c ON d.customerId = c.customerId JOIN users u ON d.sellerId = u.userId WHERE u.email = ? AND d.date BETWEEN ? AND ?", new String[]{email, startDate_formattedDate, endDate_formattedDate});
//        return cursor;
//    }
//
//    public boolean InsertCustomer(int billId, String name, String number, String date, String email, int state) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        int total = 0;
//        Cursor cursor = GetSubTotal(billId);
//        cursor.moveToFirst();
//        do {
//            total += cursor.getInt(4);
//        } while (cursor.moveToNext());
//
//        String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");
//
//        // Get sellerId and customerId
//        Cursor sellerCursor = DB.rawQuery("SELECT userId FROM users WHERE email = ?", new String[]{email});
//        int sellerId = 0;
//        if (sellerCursor.moveToFirst()) {
//            sellerId = sellerCursor.getInt(0);
//        }
//        sellerCursor.close();
//
//        Cursor customerCursor = DB.rawQuery("SELECT customerId FROM customers WHERE customerName = ? AND customerNumber = ?", new String[]{name, number});
//        int customerId = 0;
//        if (customerCursor.moveToFirst()) {
//            customerId = customerCursor.getInt(0);
//        } else {
//            ContentValues values = new ContentValues();
//            values.put("customerName", name);
//            values.put("customerNumber", number);
//            customerId = (int) DB.insert("customers", null, values);
//        }
//        customerCursor.close();
//
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("billId", billId);
//        contentValues.put("customerId", customerId);
//        contentValues.put("date", formattedDate);
//        contentValues.put("total", total);
//        contentValues.put("sellerId", sellerId);
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        long check = db.insert("customer", null, contentValues);
//        return check != -1;
//    }
//
//    public boolean ConfirmSale(int billId) {
//        Cursor cursor = GetSubTotal(billId);
//        return cursor.getCount() > 0;
//    }
//
//    public boolean DeleteBillWithBillID(String billid, String email) {
//        SQLiteDatabase db = getWritableDatabase();
//        long delete_customer = db.delete("customer", "billId = ? AND sellerId = ?", new String[]{billid, email});
//        long delete_display = db.delete("display", "billId = ? AND sellerId = ?", new String[]{billid, email});
//        return delete_customer != -1 && delete_display != -1;
//    }
//
//    public boolean DeleteBillWithCustomerNumber(String number, String email) {
//        SQLiteDatabase db = getWritableDatabase();
//        long delete_customer = db.delete("customer", "customerId = (SELECT customerId FROM customers WHERE customerNumber = ?)", new String[]{number});
//        long delete_display = db.delete("display", "customerId = (SELECT customerId FROM customers WHERE customerNumber = ?)", new String[]{number});
//        return delete_customer != -1 && delete_display != -1;
//    }
//
//    public boolean DeleteBillWithCustomerName(String name, String email) {
//        SQLiteDatabase db = getWritableDatabase();
//        long delete_customer = db.delete("customer", "customerId = (SELECT customerId FROM customers WHERE customerName = ?)", new String[]{name});
//        long delete_display = db.delete("display", "customerId = (SELECT customerId FROM customers WHERE customerName = ?)", new String[]{name});
//        return delete_customer != -1 && delete_display != -1;
//    }
//
//    public boolean DeleteBillWithDate(Cursor data, String email) {
//        data.moveToFirst();
//        return DeleteBillWithBillID(data.getString(8), email);
//    }
//
//    public boolean DeleteCustomerWithRangeDate(Cursor data, String email) {
//        data.moveToFirst();
//        return DeleteBillWithBillID(data.getString(8), email);
//    }
//
//    // Stock Management Methods
//    public void CreateTable() {
//        SQLiteDatabase DB = this.getWritableDatabase();
//        DB.execSQL("CREATE TABLE IF NOT EXISTS stock(productID INTEGER PRIMARY KEY AUTOINCREMENT, productName TEXT, catagory TEXT, purchesPrice TEXT, sellingPrice TEXT, date DATE, quentity TEXT, seller TEXT, Gst INTEGER)");
//        DB.execSQL("CREATE TABLE IF NOT EXISTS stockQuentity(stickTrackId INTEGER PRIMARY KEY AUTOINCREMENT, productName TEXT, quentity TEXT, price TEXT, seller TEXT, Gst INTEGER)");
//    }
//
//    public Cursor GetProductQuantity(String name, String seller) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery("SELECT * FROM stockQuentity WHERE sellerId = (SELECT userId FROM users WHERE email = ?) AND productName = ?", new String[]{seller, name});
//        return cursor;
//    }
//
//    public Boolean CheckGstAvailability(String email) {
//        Cursor c = GetUser(email);
//        c.moveToFirst();
//        return !c.getString(4).equals("no");
//    }
//
//    public Cursor GetInventory(String seller) {
//        CreateTable();
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery("SELECT * FROM stockQuentity WHERE sellerId = (SELECT userId FROM users WHERE email = ?)", new String[]{seller});
//    }
//
//    public Cursor GetCategory(String seller) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery("SELECT * FROM stock WHERE sellerId = (SELECT userId FROM users WHERE email = ?)", new String[]{seller});
//    }
//
//    public Boolean RemoveSell(int id, String seller) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        SQLiteDatabase DB = this.getReadableDatabase();
//        String name;
//        String quentity;
//
//        Cursor c = DisplayList(id);
//        c.moveToFirst();
//
//        long result;
//        do {
//            name = c.getString(1);
//            quentity = c.getString(2);
//
//            Cursor cursor = GetProductQuantity(name, seller);
//            cursor.moveToFirst();
//            if (cursor.getCount() > 0) {
//                int qty = Integer.parseInt(cursor.getString(1));
//                int newQty = qty - Integer.parseInt(quentity);
//
//                ContentValues contentValues = new ContentValues();
//                contentValues.put("productName", name);
//                contentValues.put("quentity", newQty);
//                contentValues.put("price", cursor.getString(3));
//                contentValues.put("sellerId", (int) DB.rawQuery("SELECT userId FROM users WHERE email = ?", new String[]{seller}).getLong(0));
//                contentValues.put("Gst", cursor.getString(5));
//
//                result = db.update("stockQuentity", contentValues, "sellerId = ? AND productName = ?", new String[]{String.valueOf(DB.rawQuery("SELECT userId FROM users WHERE email = ?", new String[]{seller}).getLong(0)), name});
//            } else {
//                int Sell_qty = -1 * (Integer.parseInt(quentity));
//                ContentValues contentValues = new ContentValues();
//                contentValues.put("productName", name);
//                contentValues.put("quentity", String.valueOf(Sell_qty));
//                contentValues.put("price", String.valueOf(Integer.parseInt(c.getString(2))));
//                contentValues.put("sellerId", (int) DB.rawQuery("SELECT userId FROM users WHERE email = ?", new String[]{seller}).getLong(0));
//                contentValues.put("Gst", c.getString(9));
//
//                try {
//                    result = db.insert("stockQuentity", null, contentValues);
//                    if (result == -1) {
//                        return false;
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return false;
//                }
//            }
//        } while (c.moveToNext());
//
//        return result != -1;
//    }
//
//    public boolean AddStock(String name, String catagory, String pPrice, String sPrice, String date, String quentity, String seller, String gst) {
//        CreateTable();
//        SQLiteDatabase db = this.getWritableDatabase();
//        String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");
//
//        // Get sellerId and productId
//        Cursor sellerCursor = db.rawQuery("SELECT userId FROM users WHERE email = ?", new String[]{seller});
//        int sellerId = 0;
//        if (sellerCursor.moveToFirst()) {
//            sellerId = sellerCursor.getInt(0);
//        }
//        sellerCursor.close();
//
//        Cursor productCursor = db.rawQuery("SELECT productId FROM products WHERE productName = ?", new String[]{name});
//        int productId = 0;
//        if (productCursor.moveToFirst()) {
//            productId = productCursor.getInt(0);
//        } else {
//            ContentValues values = new ContentValues();
//            values.put("productName", name);
//            values.put("category", catagory);
//            productId = (int) db.insert("products", null, values);
//        }
//        productCursor.close();
//
//        ContentValues cv = new ContentValues();
//        cv.put("productId", productId);
//        cv.put("purchasePrice", pPrice);
//        cv.put("sellingPrice", sPrice);
//        cv.put("date", formattedDate);
//        cv.put("quantity", quentity);
//        cv.put("sellerId", sellerId);
//        cv.put("Gst", gst);
//        long check = db.insert("stock", null, cv);
//
//        if (check == -1) {
//            return false;
//        } else {
//            Cursor cursor = GetProductQuantity(name, seller);
//            cursor.moveToFirst();
//            if (cursor.getCount() > 0) {
//                int qty = Integer.parseInt(cursor.getString(1));
//                int newQty = Integer.parseInt(quentity) + qty;
//
//                ContentValues contentValues = new ContentValues();
//                contentValues.put("productName", name);
//                contentValues.put("quentity", newQty);
//                contentValues.put("price", sPrice);
//                contentValues.put("sellerId", sellerId);
//                contentValues.put("Gst", gst);
//
//                long result = db.update("stockQuentity", contentValues, "sellerId = ? and productName = ?", new String[]{String.valueOf(sellerId), name});
//
//                return result != -1;
//            } else {
//                ContentValues contentValues = new ContentValues();
//                contentValues.put("productName", name);
//                contentValues.put("quentity", quentity);
//                contentValues.put("price", sPrice);
//                contentValues.put("sellerId", sellerId);
//                contentValues.put("Gst", gst);
//
//                long result = db.insert("stockQuentity", null, contentValues);
//                return result != -1;
//            }
//        }
//    }
//
//    public Cursor ViewStock(String seller) {
//        CreateTable();
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery("SELECT * FROM stockQuentity WHERE sellerId = (SELECT userId FROM users WHERE email = ?)", new String[]{seller});
//    }
//
//    public Cursor ViewProductHistory(String seller, String product) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery("SELECT * FROM stock WHERE sellerId = (SELECT userId FROM users WHERE email = ?) AND productName = ?", new String[]{seller, product});
//    }
//
//    public Cursor ViewCategoryHistory(String seller, String catagory) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery("SELECT * FROM stock WHERE sellerId = (SELECT userId FROM users WHERE email = ?) AND catagory = ?", new String[]{seller, catagory});
//    }
//
//    public Cursor ViewSaleProductHistory(String seller, String product) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery("SELECT SUM(quantity), SUM(price), AVG(price) FROM display WHERE productId = (SELECT productId FROM products WHERE productName = ?) AND sellerId = (SELECT userId FROM users WHERE email = ?)", new String[]{product, seller});
//    }
//
//    public Cursor ViewSaleCategoryHistory(String seller, String category) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery("SELECT DISTINCT productName FROM products WHERE productId IN (SELECT productId FROM stock WHERE sellerId = (SELECT userId FROM users WHERE email = ?) AND catagory = ?)", new String[]{seller, category});
//    }
//
//    // Backup and Restore Methods
//    public String DownloadBackup(Context context) {
//        try {
//            String internalDatabasePath = context.getDatabasePath("Biller").getPath();
//            File databaseFile = new File(internalDatabasePath);
//            FileInputStream fis = new FileInputStream(databaseFile);
//            FileChannel src = fis.getChannel();
//
//            String backupPath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.nimeshkadecha.Biller/Backup/";
//            File backupFolder = new File(backupPath);
//            if (!backupFolder.exists()) {
//                backupFolder.mkdirs();
//            }
//            String backupDatabasePath = backupPath + "Biller_Backup.db";
//            FileOutputStream fos = new FileOutputStream(backupDatabasePath);
//            FileChannel dst = fos.getChannel();
//
//            dst.transferFrom(src, 0, src.size());
//
//            src.close();
//            dst.close();
//            fis.close();
//            fos.close();
//
//            return backupDatabasePath;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "Error";
//        }
//    }
//
//    public Boolean AutoLocalBackup(Context context) {
//        try {
//            String internalDatabasePath = context.getDatabasePath("Biller").getPath();
//            File databaseFile = new File(internalDatabasePath);
//            FileInputStream fis = new FileInputStream(databaseFile);
//            FileChannel src = fis.getChannel();
//
//            String backupPath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.nimeshkadecha.Biller/Auto Backup/";
//            File backupFolder = new File(backupPath);
//            if (!backupFolder.exists()) {
//                backupFolder.mkdirs();
//            }
//            String backupDatabasePath = backupPath + "Auto_Biller_Backup.db";
//            FileOutputStream fos = new FileOutputStream(backupDatabasePath);
//            FileChannel dst = fos.getChannel();
//
//            dst.transferFrom(src, 0, src.size());
//
//            src.close();
//            dst.close();
//            fis.close();
//            fos.close();
//
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public String UploadLocalBackup(Context context, File selectedFile) {
//        File dbFile = context.getDatabasePath("Biller");
//        try (FileChannel src = new FileInputStream(selectedFile).getChannel();
//             FileChannel dst = new FileOutputStream(dbFile).getChannel()) {
//            dst.transferFrom(src, 0, src.size());
//            return "True";
//        } catch (IOException e) {
//            return "False";
//        }
//    }
//}
