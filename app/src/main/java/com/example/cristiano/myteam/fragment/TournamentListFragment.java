package com.example.cristiano.myteam.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cristiano.myteam.R;
import com.example.cristiano.myteam.adapter.TournamentListAdapter;
import com.example.cristiano.myteam.request.RequestAction;
import com.example.cristiano.myteam.request.RequestHelper;
import com.example.cristiano.myteam.structure.Club;
import com.example.cristiano.myteam.structure.Player;
import com.example.cristiano.myteam.structure.Tournament;
import com.example.cristiano.myteam.util.Constant;
import com.example.cristiano.myteam.util.UrlHelper;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Cristiano on 2017/4/19.
 *
 * this fragment presents the list of tournaments the club participates in
 */

public class TournamentListFragment extends Fragment {
    private static final String ARG_TOURS = "tournaments";
    private static final String ARG_CLUB = "club";
    private static final String ARG_PLAYER = "player";
    private static final String TAG = "TournamentListFragment";

    private ArrayList<Tournament> tournaments;
    private Club club;
    private Player player;

    private TextView tv_name, tv_info;
    private View view;

    public static TournamentListFragment newInstance(Club club, Player player){
        TournamentListFragment fragment = new TournamentListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_CLUB,club.toJson());
        bundle.putString(ARG_PLAYER,player.toJson());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            Gson gson = new Gson();
            club = gson.fromJson(bundle.getString(ARG_CLUB),Club.class);
            player = gson.fromJson(bundle.getString(ARG_PLAYER),Player.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tournament_list, container, false);
        loadTournamentList();
        return view;
    }

    private void loadTournamentList(){
        RequestAction actionGetTournaments = new RequestAction() {
            @Override
            public void actOnPre() {
            }

            @Override
            public void actOnPost(int responseCode, String response) {
                tournaments = new ArrayList<>();
                if ( responseCode == 200 ) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonTournaments = jsonObject.getJSONArray(Constant.TOURNAMENT_LIST);
                        for ( int i = 0; i < jsonTournaments.length(); i++ ) {
                            JSONObject jsonTournament = jsonTournaments.getJSONObject(i);
                            int tournamentID = jsonTournament.getInt(Constant.TOURNAMENT_ID);
                            String name = jsonTournament.getString(Constant.TOURNAMENT_NAME);
                            String info = jsonTournament.getString(Constant.TOURNAMENT_INFO);
                            tournaments.add(new Tournament(tournamentID,name,info));
                            Log.d(TAG,"loaded tournament:"+tournaments.get(i).name);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String message = jsonObject.getString(Constant.KEY_MSG);
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
                    }
                }
                showTournamentList();
            }
        };
        String url = UrlHelper.urlTournamentsByClub(club.id);
        RequestHelper.sendGetRequest(url,actionGetTournaments);
    }

    /**
     * fill the ListView with tournaments
     */
    private void showTournamentList() {
        ListView lv_tournament = (ListView) view.findViewById(R.id.lv_clubTournaments);
        Button btn_addTournament = (Button) view.findViewById(R.id.btn_addTournament);

        ArrayList<HashMap<String,Object>> tournamentList = new ArrayList<>();
        for ( Tournament tournament : tournaments ) {
            HashMap<String,Object> tournamentMap = new HashMap<>();
            tournamentMap.put(Constant.TOURNAMENT_NAME,tournament.name);
            tournamentMap.put(Constant.TOURNAMENT_INFO,tournament.info);
            tournamentList.add(tournamentMap);
        }
        TournamentListAdapter tournamentListAdapter = new TournamentListAdapter(getContext(),R.layout.layout_card_tournament,tournamentList);
        lv_tournament.setAdapter(tournamentListAdapter);
        // show the tournament page
        lv_tournament.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TournamentFragment tournamentFragment = TournamentFragment.newInstance(tournaments.get(position),club,player);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frame_content,tournamentFragment,Constant.FRAGMENT_TOURNAMENT);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        btn_addTournament.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            showCreateTournamentDialog();
            }
        });
    }

    /**
     * show a pop-up dialog to let the user create a new tournament
     */
    private void showCreateTournamentDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.layout_reg_name_info,null);
        tv_name = (TextView) dialogView.findViewById(R.id.et_name);
        tv_info = (TextView) dialogView.findViewById(R.id.et_info);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(R.string.create_tournament);
        dialogBuilder.setPositiveButton(R.string.label_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tournamentName = tv_name.getText().toString();
                String tournamentInfo = tv_info.getText().toString();
                if ( tournamentName.equals("") ) {
                    Toast.makeText(getContext(), R.string.error_empty_tour_name, Toast.LENGTH_SHORT).show();
                    return;
                }
                if ( tournamentInfo.equals("") ) {
                    Toast.makeText(getContext(), R.string.error_empty_tour_info, Toast.LENGTH_SHORT).show();
                    return;
                }
                int tournamentID = 0;
                Tournament regTournament = new Tournament(tournamentID,tournamentName,tournamentInfo);
                RequestAction actionPostRegTournament = new RequestAction() {
                    @Override
                    public void actOnPre() {

                    }

                    @Override
                    public void actOnPost(int responseCode, String response) {
                        if ( responseCode == 201 ) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONObject jsonTournament = jsonObject.getJSONObject(Constant.TABLE_TOURNAMENT);
                                int tournamentID = jsonTournament.getInt(Constant.TOURNAMENT_ID);
                                String name = jsonTournament.getString(Constant.TOURNAMENT_NAME);
                                String info = jsonTournament.getString(Constant.TOURNAMENT_INFO);
                                Tournament newTournament = new Tournament(tournamentID,name,info);
                                tournaments.add(newTournament);
                                String message = jsonObject.getString(Constant.KEY_MSG);
                                Toast.makeText(getContext(),message,Toast.LENGTH_LONG).show();
                                showTournamentList();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(),response,Toast.LENGTH_LONG).show();
                            }
                        } else {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String message = jsonObject.getString(Constant.KEY_MSG);
                                Toast.makeText(getContext(),message,Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(),response,Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                };
                String url = UrlHelper.urlRegTournamentFromClub(club.id,player.getId());
                RequestHelper.sendPostRequest(url,regTournament.toJson(),actionPostRegTournament);
            }
        });
        dialogBuilder.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(true);
        dialogBuilder.show();

    }
}
