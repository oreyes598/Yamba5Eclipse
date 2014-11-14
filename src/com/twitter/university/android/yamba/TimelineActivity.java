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

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import com.twitter.university.android.yamba.svc.YambaService;


public class TimelineActivity extends YambaActivity {
    private static final String DETAIL_FRAGMENT = "Timeline.DETAILS";


    private boolean usingFragments;

    @Override
    public void startActivityFromFragment(Fragment frag, Intent i, int code) {
        if (usingFragments) { launchDetailFragment(i.getExtras()); }
        else { super.startActivityFromFragment(frag, i, code); }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        usingFragments = (null != findViewById(R.id.timeline_details));

        if (usingFragments) { addDetailFragment(); }
    }

    @Override
    protected void onResume() {
        super.onResume();
        YambaService.startPolling(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        YambaService.stopPolling(this);
    }

    private void addDetailFragment() {
        FragmentManager mgr = getFragmentManager();

        if (null != mgr.findFragmentByTag(DETAIL_FRAGMENT)) { return; }

        FragmentTransaction xact = mgr.beginTransaction();
        xact.add(
                R.id.timeline_details,
                new TimelineDetailFragment(),
                DETAIL_FRAGMENT);
        xact.commit();
    }

    private void launchDetailFragment(Bundle args) {
        FragmentTransaction xact = getFragmentManager().beginTransaction();
        xact.replace(
                R.id.timeline_details,
                TimelineDetailFragment.newInstance(args),
                DETAIL_FRAGMENT);
        xact.addToBackStack(null);
        xact.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        xact.commit();
    }
}
