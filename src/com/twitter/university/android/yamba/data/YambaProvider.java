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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.twitter.university.android.yamba.YambaContract;

import java.util.Map;


public class YambaProvider extends ContentProvider {
    private static final int MAX_TIMELINE_ITEM_TYPE = 1;
    private static final int TIMELINE_ITEM_TYPE = 2;
    private static final int TIMELINE_DIR_TYPE = 3;

    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        MATCHER.addURI(
            YambaContract.AUTHORITY,
            YambaContract.MaxTimeline.TABLE,
            MAX_TIMELINE_ITEM_TYPE);
        MATCHER.addURI(
            YambaContract.AUTHORITY,
            YambaContract.Timeline.TABLE + "/#",
            TIMELINE_ITEM_TYPE);
        MATCHER.addURI(
            YambaContract.AUTHORITY,
            YambaContract.Timeline.TABLE,
            TIMELINE_DIR_TYPE);
    }

    private static final Map<String, String> PROJ_MAP_MAX_TIMELINE = new ProjectionMap.Builder()
        .addColumn(
            YambaContract.MaxTimeline.Columns.TIMESTAMP,
            "max(" + YambaDbHelper.COL_TIMESTAMP + ")")
        .build()
        .getProjectionMap();

    private static final ColumnMap COL_MAP_TIMELINE = new ColumnMap.Builder()
        .addColumn(
            YambaContract.Timeline.Columns.ID,
            YambaDbHelper.COL_ID,
            ColumnMap.Type.LONG)
        .addColumn(
            YambaContract.Timeline.Columns.TIMESTAMP,
            YambaDbHelper.COL_TIMESTAMP,
            ColumnMap.Type.LONG)
        .addColumn(
            YambaContract.Timeline.Columns.HANDLE,
            YambaDbHelper.COL_HANDLE,
            ColumnMap.Type.STRING)
        .addColumn(
            YambaContract.Timeline.Columns.TWEET,
            YambaDbHelper.COL_TWEET,
            ColumnMap.Type.STRING)
        .build();

    private static final Map<String, String> PROJ_MAP_TIMELINE = new ProjectionMap.Builder()
        .addColumn(YambaContract.Timeline.Columns.ID, YambaDbHelper.COL_ID)
        .addColumn(YambaContract.Timeline.Columns.TIMESTAMP, YambaDbHelper.COL_TIMESTAMP)
        .addColumn(YambaContract.Timeline.Columns.HANDLE, YambaDbHelper.COL_HANDLE)
        .addColumn(YambaContract.Timeline.Columns.TWEET, YambaDbHelper.COL_TWEET)
        .build()
        .getProjectionMap();

    private YambaDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new YambaDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (MATCHER.match(uri)) {
            case MAX_TIMELINE_ITEM_TYPE:
                return YambaContract.MaxTimeline.ITEM_TYPE;
            case TIMELINE_ITEM_TYPE:
                return YambaContract.Timeline.ITEM_TYPE;
            case TIMELINE_DIR_TYPE:
                return YambaContract.Timeline.DIR_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] proj, String sel, String[] selArgs, String sort) {

        long pk = -1;
        Map<String, String> projMap;
        switch (MATCHER.match(uri)) {
            case MAX_TIMELINE_ITEM_TYPE:
                projMap = PROJ_MAP_MAX_TIMELINE;
                break;
            case TIMELINE_ITEM_TYPE:
                pk = ContentUris.parseId(uri);
            case TIMELINE_DIR_TYPE:
                projMap = PROJ_MAP_TIMELINE;
                break;
            default:
                throw new IllegalArgumentException("Unexpected uri: " + uri);
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(YambaDbHelper.TABLE_TIMELINE);

        qb.setProjectionMap(projMap);

        if (0 < pk) { qb.appendWhere(YambaDbHelper.COL_ID + "=" + pk); }

        Cursor c = qb.query(getDb(), proj, sel, selArgs, null, null, sort);

        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] rows) {
        switch (MATCHER.match(uri)) {
            case TIMELINE_DIR_TYPE:
                break;
            default:
                throw new IllegalArgumentException("Unrecognized uri: " + uri);
        }

        SQLiteDatabase db = getDb();
        int count = 0;
        try {
            db.beginTransaction();

            for (ContentValues row: rows) {
                row = COL_MAP_TIMELINE.translateCols(row);
                if (0 < db.insert(YambaDbHelper.TABLE_TIMELINE, null, row)) {
                    count++;
                }
            }

            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }

        if (0 < count) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }

    @Override
    public Uri insert(Uri uri, ContentValues vals) {
        throw new UnsupportedOperationException("delete not supported");
    }

    @Override
    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        throw new UnsupportedOperationException("update not supported");
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        throw new UnsupportedOperationException("delete not supported");
    }

    private SQLiteDatabase getDb() { return dbHelper.getWritableDatabase(); }
}
