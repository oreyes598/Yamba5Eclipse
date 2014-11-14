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
package com.twitter.university.android.yamba;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.twitter.university.android.yamba.view.SimpleCursorRecyclerViewAdapter;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LollipopTimelineFragment extends Fragment
    implements LoaderCallbacks<Cursor>, SimpleCursorRecyclerViewAdapter.ItemClickListener
{
    private static final int TIMELINE_LOADER = 666;

    private static final String[] FROM = new String[] {
        YambaContract.Timeline.Columns.HANDLE,
        YambaContract.Timeline.Columns.TIMESTAMP,
        YambaContract.Timeline.Columns.TWEET,
    };

    private static final int[] TO = new int[] {
        R.id.timeline_handle,
        R.id.timeline_time,
        R.id.timeline_tweet,
    };

    static class TimelineBinder implements SimpleCursorRecyclerViewAdapter.ViewBinder {
        @Override
        public boolean setViewValue(Context ctxt, Cursor c, int idx, View v) {
            if (R.id.timeline_time != v.getId()) { return false; }

            CharSequence s = "long ago";
            long t = c.getLong(idx);
            if (0 < t) { s = DateUtils.getRelativeTimeSpanString(t); }
            ((TextView) v).setText(s);

            return true;
        }
    }


    private SimpleCursorRecyclerViewAdapter adapter;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
            getActivity(),
            YambaContract.Timeline.URI,
            null,
            null,
            null,
            YambaContract.Timeline.Columns.TIMESTAMP + " DESC" );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> l, Cursor c) {
        adapter.swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> c) {
        adapter.swapCursor(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle state) {
        Context ctxt = getActivity();

        View v = inflater.inflate(R.layout.fragment_timeline, root, false);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.timeline_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(ctxt));
        recyclerView.hasFixedSize();

        adapter = new SimpleCursorRecyclerViewAdapter(ctxt, null, FROM, R.layout.row_timeline, TO);
        adapter.setViewBinder(new TimelineBinder());
        adapter.setItemClickListener(this);
        recyclerView.setAdapter(adapter);

        getLoaderManager().initLoader(TIMELINE_LOADER, null, this);

        return v;
    }

    public void onItemClicked(ViewGroup v, Cursor c) {
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
            getActivity(),
            Pair.create(v.findViewById(TO[0]), "handle"),
            Pair.create(v.findViewById(TO[1]), "time"),
            Pair.create(v.findViewById(TO[2]), "tweet"));

        Intent i = TimelineDetailFragment.marshallDetails(
            getActivity(),
            c.getLong(c.getColumnIndex(YambaContract.Timeline.Columns.TIMESTAMP)),
            c.getString(c.getColumnIndex(YambaContract.Timeline.Columns.HANDLE)),
            c.getString(c.getColumnIndex(YambaContract.Timeline.Columns.TWEET)));

        startActivity(i, options.toBundle());
    }
}
