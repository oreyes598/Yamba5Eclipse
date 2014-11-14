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
package com.twitter.university.android.yamba.view;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * An extremely simple replacement for the SimpleCursorAdapter,
 * for use with the RecyclerView
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SimpleCursorRecyclerViewAdapter
    extends RecyclerView.Adapter<SimpleCursorRecyclerViewAdapter.CursorViewHolder>
{
    public static interface ViewBinder { boolean setViewValue(Context ctxt, Cursor cur, int col, View v); }

    public static interface ItemClickListener { void onItemClicked(ViewGroup v, Cursor cur); }


    class CursorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final View[] views;
        private int pos;

        public CursorViewHolder(View root, int[] subViews) {
            super(root);
            root.setOnClickListener(this);
            views = new View[subViews.length];
            for (int i = 0; i < views.length; i++) { views[i] = root.findViewById(subViews[i]); }
        }

        @Override
        public void onClick(View v) { handleClick((ViewGroup) v, pos); }

        public void setPos(int pos) { this.pos = pos; }
        public View getView(int i) { return views[i]; }
    }


    private final Context ctxt;
    private final String[] colNames;
    private final int rootView;
    private final int[] subViews;

    private ViewBinder viewBinder;
    private ItemClickListener clickListener;

    private Cursor cursor;
    private int idIdx;
    private int[] cols;


    public SimpleCursorRecyclerViewAdapter(
        Context ctxt,
        Cursor cursor,
        String[] colNames,
        int root,
        int[] subViews)
    {
        super();

        if (colNames.length != subViews.length) {
            throw new IllegalArgumentException("arrays colNames and subViews must be the same length");
        }

        this.ctxt = ctxt;
        this.colNames = colNames;
        this.rootView = root;
        this.subViews = subViews;

        setHasStableIds(true);

        setCursor(cursor);
    }

    @Override
    public int getItemCount() {
        return (null == cursor) ? 0 : cursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        return ((null == cursor) || !cursor.moveToPosition(position)) ? 0 : cursor.getLong(idIdx);
    }

    @Override
    public CursorViewHolder onCreateViewHolder(ViewGroup root, int ignore) {
        return new CursorViewHolder(
            LayoutInflater.from(root.getContext()).inflate(rootView, root, false),
            subViews);
    }

    @Override
    public void onBindViewHolder(CursorViewHolder holder, int pos) {
        if (null == cursor) {
            throw new IllegalStateException("called with invalid data");
        }

        if (!cursor.moveToPosition(pos)) {
            throw new IllegalStateException("invalid position: " + pos);
        }

        holder.setPos(pos);
        for (int i = 0; i < cols.length; i++) {
            View v = holder.getView(i);
            if ((null != viewBinder) && (!viewBinder.setViewValue(ctxt, cursor, cols[i], v))) {
                ((TextView) v).setText(cursor.getString(cols[i]));
            }
        }
    }

    public void setItemClickListener(ItemClickListener clickListener) { this.clickListener = clickListener; }

    public void setViewBinder(ViewBinder viewBinder) { this.viewBinder = viewBinder; }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == cursor) { return null; }

        Cursor oldCursor = cursor;
        setCursor(newCursor);

        return oldCursor;
    }

    void handleClick(ViewGroup v, int pos) {
        if ((null != clickListener) && (null != cursor) && cursor.moveToPosition(pos)) {
            clickListener.onItemClicked(v, cursor);
        }
    }

    private void setCursor(Cursor newCursor) {
        if (cursor == newCursor) { return; }

        if (null == newCursor) {
            idIdx = -1;
            cols = null;
        }
        else {
            idIdx = newCursor.getColumnIndexOrThrow("_id");
            cols = new int[colNames.length];
            for (int i = 0; i < colNames.length; i++) { cols[i] = newCursor.getColumnIndex(colNames[i]); }
        }

        cursor = newCursor;

        notifyDataSetChanged();
    }
}