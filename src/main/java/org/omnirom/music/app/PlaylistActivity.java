/*
 * Copyright (C) 2014 Fastboot Mobile, LLC.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>.
 */

package org.omnirom.music.app;

import android.annotation.TargetApi;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.omnirom.music.app.fragments.PlaylistViewFragment;
import org.omnirom.music.model.Playlist;

import java.util.List;
import java.util.Map;

/**
 * Activity allowing user to view a Playlist contents through
 * a {@link org.omnirom.music.app.fragments.PlaylistViewFragment}
 */
public class PlaylistActivity extends AppActivity {
    private static final String TAG_FRAGMENT = "fragment_inner";

    public static final int BACK_DELAY = ArtistActivity.BACK_DELAY;
    public static final String BITMAP_PLAYLIST_HERO = "playlist_hero";
    private static final String EXTRA_RESTORE_INTENT = "restore_intent";

    private Bundle mInitialIntent; // TODO: Test rotation
    private PlaylistViewFragment mActiveFragment;
    private Handler mHandler = new Handler();

    /**
     * Creates an intent starting this activity with the provided parameters
     * @param context The active context
     * @param playlist The playlist to watch
     * @return An intent to start this activity
     */
    public static Intent craftIntent(Context context, Playlist playlist, Bitmap hero) {
        Intent intent = new Intent(context, PlaylistActivity.class);
        intent.putExtra(PlaylistViewFragment.KEY_PLAYLIST, playlist.getRef());
        Utils.queueBitmap(BITMAP_PLAYLIST_HERO, hero);
        return intent;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_playlist);

        FragmentManager fm = getSupportFragmentManager();
        mActiveFragment = (PlaylistViewFragment) fm.findFragmentByTag(TAG_FRAGMENT);
        if (savedInstance == null) {
            mInitialIntent = getIntent().getExtras();
        } else {
            mInitialIntent = savedInstance.getBundle(EXTRA_RESTORE_INTENT);
        }

        if (mActiveFragment == null) {
            mActiveFragment = new PlaylistViewFragment();
            fm.beginTransaction()
                    .add(R.id.playlist_container, mActiveFragment, TAG_FRAGMENT)
                    .commit();
            mActiveFragment.setArguments(mInitialIntent);
        }

        // Remove title
        setTitle(null);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        if (Utils.hasLollipop()) {
            setEnterSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    View imageHeader = mActiveFragment.getHeroImageView();
                    if (imageHeader != null) {
                        sharedElements.put("itemImage", imageHeader);
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        mActiveFragment.onCreateOptionsMenu(menu, getMenuInflater());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!mActiveFragment.onOptionsItemSelected(item)) {
            if (item.getItemId() == android.R.id.home) {
                finish();
                return true;
            } else {
                return super.onOptionsItemSelected(item);
            }
        } else {
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        if (Utils.hasLollipop()) {
            mActiveFragment.notifyReturnTransition();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        PlaylistActivity.super.onBackPressed();
                    } catch (IllegalStateException ignored) { }
                }
            }, BACK_DELAY);
        } else {
            super.onBackPressed();
        }
    }
}
