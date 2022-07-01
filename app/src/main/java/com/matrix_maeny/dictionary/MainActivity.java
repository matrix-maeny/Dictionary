package com.matrix_maeny.dictionary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.matrix_maeny.dictionary.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private RequestQueue requestQueue;

    private String searchQuery = "";

    private final Handler handler = new Handler();
    private ProgressDialog processDialog;

    private PartsOfSpeechAdapter adapter;
    private List<PartsOfSpeechModel> list;

    private String audioUrl = null;
    private String phonetic = null;
    private String emptyTvMessage = "Search a word";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initialize();
    }


    private void initialize() {
        list = new ArrayList<>();
        adapter = new PartsOfSpeechAdapter(MainActivity.this, list);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        binding.recyclerView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(MainActivity.this);
        processDialog = new ProgressDialog(MainActivity.this);
        processDialog.setTitle("Loading..");

        binding.goBtn.setOnClickListener(goBtnListener);
        binding.searchView.setOnQueryTextListener(queryTextListener);
        binding.soundIv.setOnClickListener(audioClickListener);
    }

    View.OnClickListener goBtnListener = v -> {
        searchPin();
        binding.searchView.setQuery(searchQuery, false);
    };
    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            searchPin();

            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    View.OnClickListener audioClickListener = v -> {
        MediaPlayer player = new MediaPlayer();

        player.setOnPreparedListener(mp -> {
            try {
                mp.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        });

        player.setOnCompletionListener(mp -> {
            try {
                mp.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        });

        if (audioUrl != null) {
            try {
                player.setDataSource(audioUrl);
                player.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Audio not available", Toast.LENGTH_SHORT).show();
        }
    };


    private void searchPin() {
        emptyTvMessage = "Search a word";
        dialogStart("Fetching definitions", true);

        phonetic = audioUrl = null;
        if (checkSearchQuery()) {
            binding.searchView.clearFocus();
            new Thread() {
                public void run() {
                    getDataFromQuery(searchQuery);
                }
            }.start();
        }


    }

    private void getDataFromQuery(String searchQuery) {
        requestQueue.getCache().clear();
        final String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + searchQuery;


        JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                String definitions = "Definition : \n\n", partsOfSpeech = null;

                try {
                    list.clear();

                    for (int j = 0; j < response.length(); j++) {
                        JSONObject object = response.getJSONObject(j);
                        JSONArray phoneticsArray = object.getJSONArray("phonetics");


                        if (phonetic == null || audioUrl == null) {

                            for (int i = 0; i < phoneticsArray.length(); i++) {

                                JSONObject ph = phoneticsArray.getJSONObject(i);

                                try {
                                    if (!ph.getString("text").equals("")) {
                                        if (phonetic == null) {
                                            phonetic = ph.getString("text");
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    if (!ph.getString("audio").equals("")) {
                                        if (audioUrl == null) {
                                            audioUrl = ph.getString("audio");
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }

                        JSONArray meaningsArray = object.getJSONArray("meanings");

                        for (int i = 0; i < meaningsArray.length(); i++) {
                            definitions = "Definitions : \n\n";

                            JSONObject meaningObject = meaningsArray.getJSONObject(i);

                            partsOfSpeech = meaningObject.getString("partOfSpeech");

                            JSONArray defArray = meaningObject.getJSONArray("definitions");

                            int index = 1;
                            for (int k = 0; k < defArray.length(); k++) {
                                JSONObject defObj = defArray.getJSONObject(k);

                                try {
                                    definitions += index + ". " + defObj.getString("definition") + "\n\n";
                                    index++;
                                    definitions += "Ex: " + defObj.getString("example") + "\n\n";
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } finally {
                                    definitions += "\n";
                                }


                            }

                            list.add(new PartsOfSpeechModel(partsOfSpeech, definitions));

                        }
                    }


                    setData(true);


                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(() -> Toast.makeText(MainActivity.this, "Error: Main.224", Toast.LENGTH_SHORT).show());
                }

                dialogStart(null, false);
            }
        }, error -> {

            String msg = "Word not found";

            if (error.getMessage() != null) {
                if (error.getMessage().contains("UnknownHostException")) {
                    msg = "No Internet Connection";
                } else {
                    msg = "Nothing found";
                }
            }

            String finalMsg = msg;
            emptyTvMessage = finalMsg;
            handler.post(() -> {
                Toast.makeText(MainActivity.this, finalMsg, Toast.LENGTH_LONG).show();
            });

            list.clear();
            setData(false);
            dialogStart(null, false);
        });

        requestQueue.add(arrayRequest);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setData(boolean shouldShow) {
        handler.post(() -> {

            if (shouldShow) {
                binding.titleTv.setText(searchQuery);
                binding.phoneticTv.setText(phonetic);
                binding.wordLayout.setVisibility(View.VISIBLE);
            } else {
                binding.wordLayout.setVisibility(View.GONE);

            }

            binding.emptyTv.setText(emptyTvMessage);
            if (shouldShow && !list.isEmpty()) {
                binding.emptyTv.setVisibility(View.GONE);
            } else {
                binding.emptyTv.setVisibility(View.VISIBLE);

            }
            adapter.notifyDataSetChanged();
        });
    }


    private boolean checkSearchQuery() {
        try {
            searchQuery = binding.searchView.getQuery().toString().trim();
            if (!searchQuery.equals("")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter Pin code", Toast.LENGTH_SHORT).show();
        return false;
    }


    private void dialogStart(String msg, boolean shouldEnable) {
        if (shouldEnable) {
            processDialog.setMessage(msg);
            handler.post(() -> {
                processDialog.show();

            });
        } else {
            handler.post(() -> {
                processDialog.dismiss();
            });
        }
    }


}