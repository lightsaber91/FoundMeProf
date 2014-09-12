/*
 * Copyright (C) 2014 - Simone Martucci <martucci.simone.91@gmail.com>
 * Copyright (C) 2014 - Mattia Mancini <mattia.mancini.1991@gmail.com>
 *
 * This file is part of Foundme Professore.
 *
 * Foundme Professore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foundme Professore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foundme Professore.  If not, see <http://www.gnu.org/licenses/>.
 */

package foundme.uniroma2.it.professore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class ReadMessageActivity extends Activity {

    private static ListView lvMessaggi;
    private static String Title;
    private static String toEdit;
    private static String[] messageIds;
    private static Context context;
    private static SwipeRefreshLayout swipeMsg;
    private static ActionMode.Callback modeCallBack;
    private static View viewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_message);
        context = this;

        Bundle passed = getIntent().getExtras();
        Title =  passed.getString(Variables_it.COURSE);

        swipeMsg = (SwipeRefreshLayout) findViewById(R.id.swipe_msg);
        swipeMsg.setEnabled(false);
        lvMessaggi = (ListView) findViewById(R.id.lvmes);

        modeCallBack = new ActionMode.Callback() {

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        if(!toEdit.equalsIgnoreCase(Variables_it.NO_MSG)) {
                            try {
                                deleteMsg(toEdit);
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        toEdit = null;
                        mode.finish();
                        break;
                }
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
                viewList.setBackgroundColor(Color.TRANSPARENT);
                mode = null;
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.setTitle(Variables_it.OPTION);
                mode.getMenuInflater().inflate(R.menu.context, menu);
                return true;
            }
        };

        try {
            getMsg(Title, true);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void deleteMsg(String mid) throws ExecutionException, InterruptedException {
        new Connection(context, true, Variables_it.DELETION, Variables_it.DEL_MSG_OK, Title)
                .execute(Variables_it.DEL_MSG, Variables_it.MSG, mid);
    }

    public static void getMsg(String Title, boolean en) throws ExecutionException, InterruptedException {
        new Connection(context, en, Variables_it.SHOW_MSG, Variables_it.NO_MSG, Variables_it.GET)
                .execute(Variables_it.READING, Variables_it.COURSE, Title);
    }

    public static void populateView(String[] result){
        messageIds = new String[result.length];
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();

        for(int i=0; i<result.length; i++) {
            Map<String, String> datum = new HashMap<String, String>(2);
            if (i == 0 && result[0].equalsIgnoreCase(Variables_it.NO_MSG)) {
                datum.put("Msg", result[0]);
                datum.put("Num", "");
                data.add(datum);
                messageIds[0] = result[0];
                break;
            }
            else {
                String[] items = result[i].split(",");
                String ap = "[" + items[0] + "] " + items[1];
                datum.put("Msg", ap);
                datum.put("Num", "Messaggio Visualizzato: "+items[2]+" volte");
                data.add(datum);
                messageIds[i] = items[3];
            }
        }
        //creo l'adapter
        SimpleAdapter adapter = new SimpleAdapter(context, data, android.R.layout.simple_list_item_2, new String[] {"Msg","Num"}, new int[] {android.R.id.text1,
                android.R.id.text2});
        //inserisco i dati
        lvMessaggi.setAdapter(adapter);

        swipeMsg.setColorSchemeColors(0xff429874, 0xffffffff, 0xff429874, 0xffffffff);
        swipeMsg.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeMsg.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeMsg.setRefreshing(false);
                        try {
                            getMsg(Title, false);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, 3000);
            }
        });

        lvMessaggi.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {
                toEdit = messageIds[position];
                for (int j = 0; j < parent.getChildCount(); j++)
                    parent.getChildAt(j).setBackgroundColor(Color.TRANSPARENT);

                view.setBackgroundColor(0xff429874);
                ((Activity)context).startActionMode(modeCallBack);
                viewList = view;
                return true;
            }
        });

        lvMessaggi.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0)
                    swipeMsg.setEnabled(true);
                else
                    swipeMsg.setEnabled(false);
            }
        });
    }
}
