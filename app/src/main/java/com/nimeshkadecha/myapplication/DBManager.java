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
        DB.execSQL("CREATE TABLE users(name TEXT,email TEXT primary key,password TEXT ,gst TEXT,contact TEXT ,address TEXT)");

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
        DB.execSQL("Create TABLE customer(billId Integer primary key," +
                "customerName TEXT," +
                "customerNumber TEXT," +
                "date Date," +
                "total TEXT," +
                "seller TEXT," +
                "backup Integer)");
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
                if (data.getString(1).equals(name)) {
                    check_if_already_added = true;
                    Index = data.getString(0); // geting index
                    number_of_product = data.getInt(3);
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
    public int CheckTotal(int billID) {
        Cursor c = DisplayList(billID);
        int total = 0;
        c.moveToFirst();
        if (c.getCount() == 0) {
            return 0;
        }
        do {
            total += c.getInt(4);
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
    public boolean InsertCustomer(int billId, String name, String number, String date, String email, int state) {

        int total = 0;

//        int ID = Integer.parseInt(billId);

        Cursor cursor = GetSubTotal(billId);

        cursor.moveToFirst();
        do {
            total += cursor.getInt(4);
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

        DB.execSQL("Create TABLE IF NOT EXISTS stock(productID Integer primary key autoincrement ," +
                "productName TEXT ," +
                "catagory TEXT," +
                "purchesPrice TEXT," +
                "sellingPrice TEXT," +
                "date Date," +
                "quentity TEXT," +
                "seller TEXT," +
                "backup Integer," +
                "Gst Integer)");

        DB.execSQL("Create TABLE IF NOT EXISTS stockQuentity(productName TEXT ," +
                "quentity TEXT," +
                "price TEXT," +
                "seller TEXT ," +
                "backup Integer," +
                "stickTrackId Integer primary key autoincrement," +
                "Gst Integer)");
    }

    // setting current quantity ==[Select * from stockQuentity where seller = ? AND productName = ?]
    public Cursor GetProductQuantity(String name, String seller) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery("Select * from stockQuentity where seller = ? AND productName = ?", new String[]{seller, name});
    }

    // checking if GST is available or not =========================================================
    public Boolean CheckGstAvailability(String email) {
        Cursor c = GetUser(email);
        c.moveToFirst();
        return !c.getString(3).equals("no");
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
    public Boolean RemoveSell(int id, String seller) {

        SQLiteDatabase db = this.getWritableDatabase();
        String name;
        String quentity;

        Cursor c = DisplayList(id);

        c.moveToFirst();

        long result;

        do {
            name = c.getString(1);
            quentity = c.getString(3);

            Cursor cursor = GetProductQuantity(name, seller);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {

                int qty = Integer.parseInt(cursor.getString(1));

                int newQty = qty - Integer.parseInt(quentity);

                ContentValues contentValues = new ContentValues();
                contentValues.put("productName", name);
                contentValues.put("quentity", newQty);
                contentValues.put("price", cursor.getString(2));
                contentValues.put("seller", seller);
                contentValues.put("backup", 0);
                contentValues.put("Gst", c.getString(11));

                result = db.update("stockQuentity", contentValues, "seller = ? and productName = ? ", new String[]{seller, name});
            } else {

                int Sell_qty = -1 * (Integer.parseInt(quentity));

                ContentValues contentValues = new ContentValues();
                contentValues.put("productName", name);
                contentValues.put("quentity", String.valueOf(Sell_qty));
                contentValues.put("price", String.valueOf(Integer.parseInt(c.getString(2))));
                contentValues.put("seller", seller);
                contentValues.put("backup", 0);
                contentValues.put("Gst", c.getString(11));

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

                int qty = Integer.parseInt(cursor.getString(1));
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