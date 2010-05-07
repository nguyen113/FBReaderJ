/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader.network;

import java.util.*;

import android.app.Activity;
import android.app.ListActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.IBinder;
import android.view.*;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.BaseAdapter;
import android.net.Uri;
import android.content.Intent;
import android.content.DialogInterface;
import android.graphics.Bitmap;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.image.ZLImage;

import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.dialogs.ZLAndroidDialogManager;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.*;


public class NetworkLibraryActivity extends NetworkBaseActivity {

	private boolean myInitialized;

	private NetworkTree myTree;
	private SearchItemTree mySearchItem;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
	}

	private void prepareView() {
		if (!myInitialized) {
			myInitialized = true;
			myTree = NetworkLibrary.Instance().getTree();
			mySearchItem = NetworkView.Instance().getSearchItemTree();
			setListAdapter(new LibraryAdapter());
			getListView().invalidateViews();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		final NetworkView networkView = NetworkView.Instance();
		if (!networkView.isInitialized()) {
			final Handler handler = new Handler() {
				public void handleMessage(Message message) {
					prepareView();
				}
			};
			((ZLAndroidDialogManager)ZLAndroidDialogManager.Instance()).wait("loadingNetworkLibrary", new Runnable() {
				public void run() {
					networkView.initialize();
					handler.sendEmptyMessage(0);
				}
			}, this);
		} else {
			prepareView();
		}
	}


	private final class LibraryAdapter extends BaseAdapter {

		public final int getCount() {
			return myTree.subTrees().size() + 1; // subtrees + <search item>
		}

		public final NetworkTree getItem(int position) {
			final int size = myTree.subTrees().size();
			if (position >= 0 && position < size) {
				return (NetworkTree) myTree.subTrees().get(position);
			} else if (position == size) {
				return mySearchItem;
			}
			return null;
		}

		public final long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, final ViewGroup parent) {
			final NetworkTree tree = getItem(position);
			return setupNetworkTreeItemView(convertView, parent, tree);
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		addMenuItem(menu, 1, "networkSearch", R.drawable.ic_menu_networksearch);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		final boolean searchInProgress = NetworkView.Instance().containsItemsLoadingRunnable(NetworkSearchActivity.SEARCH_RUNNABLE_KEY);
		menu.findItem(1).setEnabled(!searchInProgress);
		return true;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				return onSearchRequested();
			default:
				return true;
		}
	}

	@Override
	public boolean onSearchRequested() {
		if (NetworkView.Instance().containsItemsLoadingRunnable(NetworkSearchActivity.SEARCH_RUNNABLE_KEY)) {
			return false;
		}
		final NetworkLibrary library = NetworkLibrary.Instance();
		startSearch(library.NetworkSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	@Override
	public void onModelChanged() {
		getListView().invalidateViews();
	}
}
