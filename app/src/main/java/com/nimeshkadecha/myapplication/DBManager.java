package com.nimeshkadecha.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

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
        createTable();
        DB.execSQL("drop table if exists stock");
        DB.execSQL("drop table if exists stockQuentity");
        DB.execSQL("drop table if exists users");
        DB.execSQL("drop table if exists display");
        DB.execSQL("drop table if exists customer");
    }

    //    Register User ------------------------------------------------------------------------------
    public boolean registerUser(String name, String email, String password, String gst, String contact, String address) {
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

        if (result == -1) {
            return false;
        } else {
            return true;
        }

    }

    //    Login Verification ------------------[select * from users where email =? AND password = ?]---------
    public boolean loginUser(String email, String password) {
//        SQLiteDatabase DB = this.getReadableDatabase();
//        Creating a cursor to check password;
        SQLiteDatabase DB = this.getReadableDatabase();
        Cursor cursor = DB.rawQuery("select * from users where email =? AND password = ?", new String[]{email, password});
        if (cursor.getCount() > 0) {
            return true;
        } else {
            return false;
        }
    }

    //    Validate user
    public boolean validateUser(String email) {
        SQLiteDatabase DB = this.getReadableDatabase();
        Cursor cursor = DB.rawQuery("select * from users where email =?", new String[]{email});
        if (cursor.getCount() > 0) {
            return true;
        } else {
            return false;
        }
    }

    //    Geting existing user info for texting purposes in register ------- [select * from users] --
    public Cursor getdata() {
        SQLiteDatabase DB = this.getReadableDatabase();
        Cursor cursor = DB.rawQuery("select * from users", null);
        return cursor;
    }

    //    Getting specifick user all DATA --------[select * from users where email=?] -----------
    public Cursor GetUser(String email) {
        SQLiteDatabase DB = this.getReadableDatabase();
        Cursor cursor = DB.rawQuery("select * from users where email=?", new String[]{email});
        return cursor;
    }

    //    Reset Password! -----------------[select * from users where contact = ?]----------------------
    public boolean resetPassword(String Email, String password) {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("password", password);

        Cursor cursor = DB.rawQuery("select * from users where email = ?", new String[]{Email});

        if (cursor.getCount() > 0) {
            long result;

            result = DB.update("users", contentValues, "email =?", new String[]{Email});
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    //    UPdate Data -----------------------------[SELECT * From users where email =?]-------------
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

        Cursor cursor = DB.rawQuery("SELECT * From users where email =?", new String[]{email});

        if (cursor.getCount() > 0) {
            long check;
            check = DB.update("users", contentValues, "email =?", new String[]{email});
            if (check == -1) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }

    }

    //    Deleteing User------- [select * from users where email = ?] --------------------------
    public boolean DeleteUser(String email) {
        SQLiteDatabase DB = this.getWritableDatabase();

        Cursor cursor = DB.rawQuery("select * from users where email = ?", new String[]{email});

        if (cursor.getCount() > 0) {
            long check, check1, check2;
            check = DB.delete("users", "email = ?", new String[]{email});
            check1 = DB.delete("display", "seller = ?", new String[]{email});
            check2 = DB.delete("customer", "seller = ?", new String[]{email});
            if (check == -1 || check1 == -1 || check2 == -1) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }

    }

//------------------------------------- Working on customer tables ---------------------------------

    //    ADDING ITEM in list/ in recycilerview / in display TABLE
    public boolean Insert_List(String name, String price, String quantity, String cName, String cNumber, String date, int billId, String email, int state,String Gst) {
        int pricecustom = Integer.parseInt(price);

        int quentityCustom = Integer.parseInt(quantity);
        if(Gst.equals("")){
            Gst = "0";
        }
        float tax =  (pricecustom * quentityCustom) * (Integer.parseInt(Gst) / 100f);
        Log.d("ENimesh","tax = "+tax);

        float subtotal =  ((pricecustom * quentityCustom) + tax);
        Log.d("ENimesh","tax + subtotal = "+subtotal);

        SQLiteDatabase DB = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        //  Checking if the product is same them update the quantity
        Cursor data = displayList(billId);

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
            float taxUpdate =  (pricecustom *  number_of_product) * (Integer.parseInt(Gst) / 100f);
            subtotal = ((pricecustom * number_of_product) + taxUpdate );

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
            if (result == -1) {
                return false;
            } else {
                return true;
            }
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
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        }
    }

    //    Bill id is unique every time so no need of email -- [select * from display where billId =? ]---
    public Cursor displayList(int billId) {
        SQLiteDatabase DB = this.getReadableDatabase();
        String bID = String.valueOf(billId);
        Cursor cursor = DB.rawQuery("select * from display where billId =? ", new String[]{bID});
        return cursor;
    }

    //    Remove from list -- [delete from display where index =? ]---
    public Cursor removeItem(String id) {
        SQLiteDatabase DB = this.getWritableDatabase();

        Cursor c = DB.rawQuery("delete from display where indexs = ?", new String[]{id});

        return c;
    }

    //    Remove from list -- [Update Quantity in display where index =? ]---
    public boolean updateQuentity(int quentity, float subtotal, int index) {

        SQLiteDatabase DB = this.getWritableDatabase();

        //        Getting all values in
        ContentValues contentValues = new ContentValues();
        contentValues.put("quantity", String.valueOf(quentity));
        contentValues.put("subtotal", subtotal);

        long check;
        check = DB.update("display", contentValues, "indexs =?", new String[]{String.valueOf(index)});
        if (check == -1) {
            return false;
        } else {
            return true;
        }
    }

    // Checking Total without saving -- [
    public int checkTotal(int billID) {
        SQLiteDatabase DB = this.getReadableDatabase();

        Cursor c = DB.rawQuery("Select * from display where billId =?", new String[]{String.valueOf(billID)});

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

    //    Genarating BILL ID -- [select * from customer]------------------------------
    public int getbillid() {
        SQLiteDatabase DB = this.getReadableDatabase();

        int id = 0;
        try {
            Cursor cursor = DB.rawQuery("select * from customer ORDER BY billId ASC", null);
            if (cursor.getCount() > 0) {
                cursor.moveToLast();
                id = Integer.parseInt(cursor.getString(0));
            }
            id++;
        } catch (Exception e) {
            Log.d("ENimesh", "Error is = " + e);
        }
        return id;
    }

    //    Fetching all customer --- [select * from customer where seller =?]---------------
    public Cursor cusInfo(String email) {
        SQLiteDatabase DB = this.getReadableDatabase();

        Cursor cursor = DB.rawQuery("select * from customer where seller =?", new String[]{email});

        return cursor;
    }

    public Cursor individualCustomerInfo(String email, String name) {
        SQLiteDatabase DB = this.getReadableDatabase();

        Cursor cursor = DB.rawQuery("select * from customer where seller =? and customerName = ? ", new String[]{email, name});

        return cursor;
    }

    //    search based on customer name ---[select * from display where customerName = ? and seller=?]-------
    public Cursor CustomerNameBill(String Name, String email) {
        SQLiteDatabase DB = this.getReadableDatabase();

        Cursor cursor = DB.rawQuery("select * from display where customerName = ? and seller=?", new String[]{Name, email});

        return cursor;
    }

    //    Search Based on single date ---[select * from display where date = ? and seller = ?]----------

    public Cursor CustomerDateBill(String date, String email) {

        String formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");

        SQLiteDatabase DB = this.getReadableDatabase();

        Cursor cursor = DB.rawQuery("select * from display where date = ? and seller = ?", new String[]{formattedDate, email});

        return cursor;
    }

    //    Search Based on number --- [select * from display where customerNumber = ? and seller=? ] --
    public Cursor Customernumberbill(String Number, String email) {
        SQLiteDatabase DB = this.getReadableDatabase();

        Cursor cursor = DB.rawQuery("select * from display where customerNumber = ? and seller=? ", new String[]{Number, email});

        return cursor;
    }

    //    Search baced on billID ---- [select * from display where billId = ? and seller = ? ] ------
    public Cursor CustomerBillID(int billID, String email) {
        SQLiteDatabase DB = this.getReadableDatabase();

        String billId = String.valueOf(billID);

        Cursor cursor = DB.rawQuery("select * from display where billId = ? and seller = ? ", new String[]{billId, email});

        return cursor;
    }

    //    Getting Bill TOTAL ------------ [select * from customer where billid = ?] ----------------
    public Cursor billTotal(int billID) {
        SQLiteDatabase DB = this.getReadableDatabase();

        String billId = String.valueOf(billID);

        Cursor cursor = DB.rawQuery("select * from customer where billId = ? ", new String[]{billId});

        return cursor;
    }

    //    Getting sub total from billID ------------- [ select * from display where billId = ? ] --
    public Cursor getSubTotal(int billID) {
        SQLiteDatabase DB = this.getReadableDatabase();

        String billId = String.valueOf(billID);

        Cursor cursor = DB.rawQuery("select * from display where billId = ? ", new String[]{billId});

        return cursor;
    }

    //Searching in an range of DATE ---[Select * from display where seller =? AND  date  BETWEEN ? AND ? ]------
    public Cursor rangeSearch(String date, String toDate, String email) {
        String startDate_formattedDate = date_convertor.convertDateFormat(date, "dd/MM/yyyy", "yyyy-MM-dd");
        String endDate_formattedDate = date_convertor.convertDateFormat(toDate, "dd/MM/yyyy", "yyyy-MM-dd");
        SQLiteDatabase DB = this.getReadableDatabase();
        Cursor cursor;
        cursor = DB.rawQuery("Select * from display where seller =? AND  date  BETWEEN ? AND ? ", new String[]{email, startDate_formattedDate, endDate_formattedDate});

        return cursor;
    }

    //    Inser customer info in customer Table ------------------------------------------
    public boolean InsertCustomer(int billId, String name, String number, String date, String email, int state) {

        int total = 0;

//        int ID = Integer.parseInt(billId);

        Cursor cursor = getSubTotal(billId);

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
        if (check == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean ConfirmSale(int billId) {
        Cursor cursor = getSubTotal(billId);

        if (cursor.getCount() > 0) {
            return true;
        } else {
            return false;
        }

    }

    public boolean DeleteBillWithBillID(String billid, String email) {
        SQLiteDatabase db = getWritableDatabase();
        long delete_customer, delete_display;
        delete_customer = db.delete("customer", "billId = ? and seller = ?", new String[]{billid, email});
        delete_display = db.delete("display", "billId = ? and seller = ?", new String[]{billid, email});
        if (delete_customer != -1 && delete_display != -1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean DeleteBillWithCustomerNumber(String number, String email) {
        SQLiteDatabase db = getWritableDatabase();
        long delete_customer, delete_display;
        delete_customer = db.delete("customer", "customerNumber = ? and seller = ?", new String[]{number, email});
        delete_display = db.delete("display", "customerNumber = ? and seller = ?", new String[]{number, email});
        if (delete_customer != -1 && delete_display != -1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean DeleteBillWithCustomerName(String name, String email) {
        SQLiteDatabase db = getWritableDatabase();
        long delete_customer, delete_display;
        delete_customer = db.delete("customer", "customerName = ? and seller = ?", new String[]{name, email});
        delete_display = db.delete("display", "customerName = ? and seller = ?", new String[]{name, email});
        if (delete_customer != -1 && delete_display != -1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean DeleteBillWithDate(Cursor data, String email) {
        data.moveToFirst();

        boolean check = false;
        do {
            boolean check_in_loop = DeleteBillWithBillID(data.getString(8), email);
            if (!check_in_loop) {

                return false;
            } else {
                check = true;
            }
        } while (data.moveToNext());

        return check;
    }

    public boolean DeletCustomerWithRangeDate(Cursor data, String email) {
        data.moveToFirst();

        boolean check = false;
        do {
            boolean check_in_loop = DeleteBillWithBillID(data.getString(8), email);
            if (!check_in_loop) {
                return false;
            } else {
                check = true;
            }
        } while (data.moveToNext());

        return check;
    }

//    ----------------------------------- Managing Stock -------------------------------------------

    public void createTable() {
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

    public Cursor getProductQuentity(String name, String seller) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("Select * from stockQuentity where seller = ? AND productName = ?", new String[]{seller, name});

        return c;
    }

    public Boolean checkGstAvailability(String email) {
        Cursor c = GetUser(email);
        c.moveToFirst();
        if (c.getString(3).equals("no")) {
            return false;
        } else {
            return true;
        }
    }

    public Cursor getInventory(String seller) {

        createTable();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("Select * from stockQuentity where seller = ?", new String[]{seller});

        return c;
    }

    public Cursor getCategory(String seller) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("Select * from stock where seller = ?", new String[]{seller});

        return c;
    }

    public String getGstOfProduct(String productName,String seller){
        Cursor data = getProductQuentity(productName,seller);
        return data.getString(6);
    }
    public Boolean removeSell(int id, String seller) {

        SQLiteDatabase db = this.getWritableDatabase();
        String name;
        String quentity;

        Cursor c = displayList(id);

        c.moveToFirst();

        long result;

        do {
            name = c.getString(1);
            quentity = c.getString(3);

            Cursor cursor = getProductQuentity(name, seller);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {

                int qty = Integer.parseInt(cursor.getString(1));

                int newQty = qty - Integer.parseInt(quentity);
                Log.d("ENimesh", "new Qty = " + newQty);

                ContentValues contentValues = new ContentValues();
                contentValues.put("productName", name);
                contentValues.put("quentity", newQty);
                contentValues.put("price", cursor.getString(2));
                contentValues.put("seller", seller);
                contentValues.put("backup", 0);
                contentValues.put("Gst", c.getString(11));

                result = db.update("stockQuentity", contentValues, "seller = ? and productName = ? ", new String[]{seller, name});
            } else {
                Log.d("ENimesh", "In else in remove selse");
                int Sell_qty = -1 * (Integer.parseInt(quentity));
                Log.d("ENimesh", "Quentity = " + Sell_qty);
                ContentValues contentValues = new ContentValues();
                contentValues.put("productName", name);
                contentValues.put("quentity", String.valueOf(Sell_qty));
                contentValues.put("price", String.valueOf(Integer.parseInt(c.getString(2))));
                contentValues.put("seller", seller);
                contentValues.put("backup", 0);
                contentValues.put("Gst", c.getString(11));


                Log.d("ENimesh", "CV = " + contentValues);
                try {
                    result = db.insert("stockQuentity", null, contentValues);
                    Log.d("ENimesh", "result = " + result);
                    if (result == -1) {
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("ENimesh", "exception = " + e);
                    return false;
                }

            }

        } while (c.moveToNext());

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean AddStock(String name, String catagory, String pPrice, String sPrice, String date, String quentity, String seller,String gst) {
        createTable();
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
            Log.d("ENimesh", "Failed to insert in stock table");
            return false;
        } else {
            Cursor cursor = getProductQuentity(name, seller);
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

                if (result == -1) {
                    Log.d("ENimesh", "Failed to Update in stockQuentity table");
                    return false;

                } else {
                    return true;
                }


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

                if (result == -1) {
                    Log.d("ENimesh", "Failed to insert in stock table");
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

    public Cursor viewStock(String seller) {
        createTable();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("Select * from stockQuentity where seller =?", new String[]{seller});

        return c;
    }

    public Cursor viewProductHistory(String seller, String product) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("Select * from stock where seller =? AND productName=? ", new String[]{seller, product});

        return c;
    }

    public Cursor viewCategoryHistory(String seller, String catagory) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("Select * from stock where seller =? AND catagory=? ", new String[]{seller, catagory});

        return c;
    }

    public Cursor viewSlaseProductHistory(String seller, String product) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("Select SUM(quantity), SUM(price) ,AVG(price) from display where product = ? AND seller = ?", new String[]{product, seller});
        return c;
    }

    public Cursor viewSlaseCategoryHistory(String seller, String catagory) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("Select DISTINCT productName from stock where seller =? AND catagory=? ", new String[]{seller, catagory});

        return c;
    }

    public String downloadBackup(Context context) {
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
            Log.d("ENimesh", "ERROR Is: " + e.toString());
            e.printStackTrace();
            return "Error";
        }
    }

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
            Log.d("ENimesh", "ERROR Is: " + e);
            e.printStackTrace();
            return false;
        }
    }

    public String UploadLocalBackup(Context context, File selectedFile) {
        File dbFile = context.getDatabasePath("Biller");
        try (FileChannel src = new FileInputStream(selectedFile).getChannel();
             FileChannel dst = new FileOutputStream(dbFile).getChannel()) {
            dst.transferFrom(src, 0, src.size());

            Log.d("ENimesh", "Database imported from: " + selectedFile.getAbsolutePath());
            return "True";
        } catch (IOException e) {
            Log.e("ENimesh", "Error importing database: " + e.getMessage());
            return "False";
        }
    }
}