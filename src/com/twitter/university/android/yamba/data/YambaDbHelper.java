/* $Id: $
   Copyright 2012, G. Blake Meike

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.twitter.university.android.yamba.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


class YambaDbHelper extends SQLiteOpenHelper {
    private static final String DB_FILE = "yamba.db";
    private static final int VERSION = 1;

    public static final String TABLE_TIMELINE = "p_timeline";
    public static final String COL_ID = "p_id";
    public static final String COL_TIMESTAMP = "p_timestamp";
    public static final String COL_HANDLE = "p_handle";
    public static final String COL_TWEET = "p_tweet";


    public YambaDbHelper(Context ctxt) {
        super(ctxt, DB_FILE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + TABLE_TIMELINE + " ("
                + COL_ID + " INTEGER PRIMARY KEY,"
                + COL_TIMESTAMP + " INTEGER NOT NULL,"
                + COL_HANDLE + " TEXT NOT NULL,"
                + COL_TWEET + " TEXT NOT NULL)"
            );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_TIMELINE);
        onCreate(db);
    }
}
