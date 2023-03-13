package com.nimeshkadecha.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
                "subtotal Integer," +//4
                "customerName TEXT," +//5
                "customerNumber TEXT," +//6
                "date Date," +//7
                "billId TEXT ," +//8
                "seller TEXT," +//9
                "backup Integer)");//10
//        Customer table
        DB.execSQL("Create TABLE customer(billId TEXT primary key," +
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
    public boolean resetPassword(String number, String password) {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("password", password);

        Cursor cursor = DB.rawQuery("select * from users where contact = ?", new String[]{number});

        if (cursor.getCount() > 0) {
            long result;

            result = DB.update("users", contentValues, "contact =?", new String[]{number});
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

    //    Deleteing User's Customer DATA ------[select * from users where email = ?] --------------------------
    public boolean DeleteUserData(String email) {
        SQLiteDatabase DB = this.getWritableDatabase();

        Cursor cursor = DB.rawQuery("select * from users where email = ?", new String[]{email});

        if (cursor.getCount() > 0) {
            long check1, check2,check3,check4;
            check1 = DB.delete("display", "seller = ?", new String[]{email});
            check2 = DB.delete("customer", "seller = ?", new String[]{email});
            check3 = DB.delete("stock", "seller = ?", new String[]{email});
            check4 = DB.delete("stockQuentity", "seller = ?", new String[]{email});
            if (check1 == -1 || check2 == -1 || check3 == -1 || check4 == -1 ) {
                return false;
            } else {
//                Resetting Auto increment to 0
                DB.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = ?", new String[]{"display"});
                DB.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = ?", new String[]{"stock"});
                return true;
            }
        } else {
            return false;
        }

    }

    //    Getting Seller COntact NUmber for OTP
//    getting from EMail
    public Cursor Seller_Contact(String email) {
        SQLiteDatabase DB = this.getReadableDatabase();

        Cursor cursor = DB.rawQuery("select contact from users where email =? ", new String[]{email});

        return cursor;
    }

//------------------------------------- Working on customer tables ---------------------------------

    //    ADDING ITEM in list/ in recycilerview / in display TABLE
    public boolean Insert_List(String name, String price, String quantity, String cName, String cNumber, String date, int billId, String email, int state) {
        int pricecustom = Integer.parseInt(price);

        int quentityCustom = Integer.parseInt(quantity);

        int subtotal = pricecustom * quentityCustom;

        SQLiteDatabase DB = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put("Product", name);
        contentValues.put("price", price);
        contentValues.put("quantity", quantity);
        contentValues.put("subtotal", subtotal);
        contentValues.put("customerName", cName);
        contentValues.put("customerNumber", cNumber);
        contentValues.put("date", date);
        contentValues.put("billId", billId);
        contentValues.put("seller", email);
        contentValues.put("backup", state);

        long result;

        result = DB.insert("display", null, contentValues);
        if (result == -1) {
            return false;
        } else {
            return true;
        }

    }

    //    Bill id is unique every time so no need of email -- [select * from display where billId =? ]---
    public Cursor displayList(int billId) {
        SQLiteDatabase DB = this.getReadableDatabase();

        String bID = String.valueOf(billId);

        Cursor cursor = DB.rawQuery("select * from display where billId =? ", new String[]{bID});

        return cursor;

    }

    //    Bill id is unique every time so no need of email -- [select * from display where seller =?]---
    public Cursor getAllCustomer(String email) {
        SQLiteDatabase DB = this.getReadableDatabase();

        Cursor cursor = DB.rawQuery("select * from display where seller =? ", new String[]{email});

        return cursor;
    }

    //    Genarating BILL ID -- [select * from customer]------------------------------
    public int getbillid() {
        SQLiteDatabase DB = this.getReadableDatabase();

        int id = 0;
        try {
            Cursor cursor = DB.rawQuery("select * from customer", null);
            if (cursor.getCount() > 0) {
                cursor.moveToLast();
                id = cursor.getInt(0);
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
        SQLiteDatabase DB = this.getReadableDatabase();

        Cursor cursor = DB.rawQuery("select * from display where date = ? and seller = ?", new String[]{date, email});

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
        SQLiteDatabase DB = this.getReadableDatabase();
        Cursor cursor;
        cursor = DB.rawQuery("Select * from display where seller =? AND  date  BETWEEN ? AND ? ", new String[]{email, date, toDate});

        return cursor;
    }

    //    Inser customer info in customer Table ------------------------------------------
    public boolean InsertCustomer(String billId, String name, String number, String date, String email, int state) {

        int total = 0;

        int ID = Integer.parseInt(billId);

        Cursor cursor = getSubTotal(ID);

        cursor.moveToFirst();
        do {
            total += cursor.getInt(4);
        } while (cursor.moveToNext());


        ContentValues contentValues = new ContentValues();
        contentValues.put("billId", billId);
        contentValues.put("customerName", name);
        contentValues.put("customerNumber", number);
        contentValues.put("date", date);
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

    public boolean InsertCustomerCloud(String billId, String name, String number, String date, String email, int state, String total) {

        int ID = Integer.parseInt(billId);

        ContentValues contentValues = new ContentValues();
        contentValues.put("billId", billId);
        contentValues.put("customerName", name);
        contentValues.put("customerNumber", number);
        contentValues.put("date", date);
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

    //  Updating State of BACKUP ------------------------------------------------------------------------------
    public boolean UpdateBackup(String billID, int state) {
        SQLiteDatabase DB = this.getWritableDatabase();

        //        Getting all values in
        ContentValues contentValues = new ContentValues();
        contentValues.put("backup", state);

        Cursor cursor = DB.rawQuery("SELECT * From display where billid =?", new String[]{billID});

        if (cursor.getCount() > 0) {
            long check;
            check = DB.update("display", contentValues, "billid =?", new String[]{billID});
            if (check == -1) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    //  Updating State of BACKUP ------------------------------------------------------------------------------
    public boolean UpdateBackupcus(String billID, int state) {
        SQLiteDatabase DB = this.getWritableDatabase();

        //        Getting all values in
        ContentValues contentValues = new ContentValues();
        contentValues.put("backup", state);

        Cursor cursor = DB.rawQuery("SELECT * From customer where billid =?", new String[]{billID});

        if (cursor.getCount() > 0) {
            long check;
            check = DB.update("customer", contentValues, "billid =?", new String[]{billID});
            if (check == -1) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    //  Insert into customer automatically
    boolean autoInsertCustomer() {
        SQLiteDatabase DB = this.getWritableDatabase();

        Cursor cursor1 = DB.rawQuery("Select * from display", null);

        boolean check = false;
        cursor1.moveToFirst();
//        int bid = cursor1.getInt(8);
        int bid = -1;
        String customerBID = null;
        String customerName = null;
        String customerNumber = null;
        String BillDATE = null;
        String seler = null;
        int total = 0;
        do {
            if (bid == Integer.parseInt(cursor1.getString(8))) {
                continue;
            } else {
                customerBID = cursor1.getString(8);
                customerName = cursor1.getString(5);
                customerNumber = cursor1.getString(6);
                BillDATE = cursor1.getString(7);
                seler = cursor1.getString(9);
                total += cursor1.getInt(4);

                boolean Insert = InsertCustomer(customerBID, customerName, customerNumber, BillDATE, seler, 1);
                if (Insert) {
                    check = true;
                } else {
                    check = false;
                }
            }
        } while (cursor1.moveToNext());

        if (check) {
            return true;
        } else {
            return false;
        }
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
                "backup Integer)");

        DB.execSQL("Create TABLE IF NOT EXISTS stockQuentity(productName TEXT primary key ," +
                "quentity TEXT," +
                "price TEXT," +
                "seller TEXT," +
                "backup Integer)");
    }

    public boolean updatestatus(String name, String seller) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor getdata = getProductQuentity(name, seller);

        getdata.moveToFirst();
        if (getdata.getCount() > 0) {
            do {
                ContentValues contentValues = new ContentValues();
                contentValues.put("productName", getdata.getString(0));
                contentValues.put("quentity", getdata.getString(1));
                contentValues.put("price", getdata.getString(2));
                contentValues.put("seller", getdata.getString(3));
                contentValues.put("backup", 1);

                long result = db.update("stockQuentity", contentValues, "seller = ? and productName = ? ", new String[]{seller, name});
                if (result == -1) {
                    return false;
                } else {
                    return true;
                }
            } while (getdata.moveToNext());
        } else {
            return false;
        }
    }

    public boolean updateStockStatus(int id,String seller){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor data = viewStockhistory(seller);

        data.moveToFirst();

        if(data.getCount()>0){
             do{
                 ContentValues contentValues = new ContentValues();
//                 contentValues.put("productID", data.getString(0));
                 contentValues.put("productName", data.getString(1));
                 contentValues.put("catagory", data.getString(2));
                 contentValues.put("purchesPrice", data.getString(3));
                 contentValues.put("sellingPrice", data.getString(4));
                 contentValues.put("date", data.getString(5));
                 contentValues.put("quentity", data.getString(6));
                 contentValues.put("seller", data.getString(7));
                 contentValues.put("backup", 1);

                 long result = db.update("stock", contentValues, "seller = ? and productID = ? ", new String[]{seller, String.valueOf(id)});
                 if (result == -1) {
                     return false;
                 } else {
                     return true;
                 }
             }while (data.moveToNext());
        }else{
            return false;
        }

    }

    public Cursor getProductQuentity(String name, String seller) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("Select * from stockQuentity where seller = ? and productName = ?", new String[]{seller, name});

        return c;
    }

    public Cursor getInventory(String seller) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("Select * from stockQuentity where seller = ?", new String[]{seller});

        return c;
    }

    public Boolean removeSell(String id, String seller) {

        SQLiteDatabase db = this.getWritableDatabase();
        String name;
        String quentity;

        Cursor c = displayList(Integer.parseInt(id));

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

                result = db.update("stockQuentity", contentValues, "seller = ? and productName = ? ", new String[]{seller, name});
            } else {
                int Sell_qty = -1 * (Integer.parseInt(quentity));
                ContentValues contentValues = new ContentValues();
                contentValues.put("productName", name);
                contentValues.put("quentity", Sell_qty);
                contentValues.put("price", Integer.parseInt(c.getString(2)));
                contentValues.put("seller", seller);
                contentValues.put("backup", 0);

                result = db.insert("stockQuentity", null, contentValues);
                if (result == -1) {
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

    public boolean AddStock(String name, String catagory, String pPrice, String sPrice, String date, String quentity, String seller) {
        createTable();
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("productName", name);
        cv.put("catagory", catagory);
        cv.put("purchesPrice", pPrice);
        cv.put("sellingPrice", sPrice);
        cv.put("date", date);
        cv.put("quentity", quentity);
        cv.put("seller", seller);
        cv.put("backup", 0);

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

    public Cursor viewStockhistory(String seller) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("Select * from stock where seller =?", new String[]{seller});

        return c;
    }

    public boolean addStockQty(String name,String quentity,String sPrice,String seller){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("productName", name);
        contentValues.put("quentity", quentity);
        contentValues.put("price", sPrice);
        contentValues.put("seller", seller);
        contentValues.put("backup", 1);
        long result;

        result = db.insert("stockQuentity", null, contentValues);

        if (result == -1) {
            Log.d("ENimesh", "Failed to insert in stock table");
            return false;
        } else {
            return true;
        }
    }

    public boolean downloadStock(String name, String catagory, String pPrice, String sPrice, String date, String quentity, String seller) {
        createTable();
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("productName", name);
        cv.put("catagory", catagory);
        cv.put("purchesPrice", pPrice);
        cv.put("sellingPrice", sPrice);
        cv.put("date", date);
        cv.put("quentity", quentity);
        cv.put("seller", seller);
        cv.put("backup", 1);

        long check;

        check = db.insert("stock", null, cv);

        if(check == -1){
            return false;
        }else{
            return true;
        }

    }

}