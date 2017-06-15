package com.example.cristiano.myteam.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.cristiano.myteam.R;
import com.example.cristiano.myteam.adapter.ChatListAdapter;
import com.example.cristiano.myteam.database.LocalDBHelper;
import com.example.cristiano.myteam.request.RequestAction;
import com.example.cristiano.myteam.request.RequestHelper;
import com.example.cristiano.myteam.structure.Chat;
import com.example.cristiano.myteam.structure.Club;
import com.example.cristiano.myteam.structure.Player;
import com.example.cristiano.myteam.structure.Tournament;
import com.example.cristiano.myteam.util.Constant;
import com.example.cristiano.myteam.util.UrlHelper;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by Cristiano on 2017/6/1.
 */

public class ChatFragment extends Fragment {
    private static final String ARG_TOUR = "tournament";
    private static final String ARG_CLUB = "club";
    private static final String ARG_RECV = "receiver";
    private static final String ARG_SELF = "self";

    private final static int CHAT_LIMIT = 30;

    private Tournament tournament;
    private Club club;
    private Player receiver, self;

    private int tournamentID, clubID, receiverID, selfID;

    private LinkedList<Chat> chatArray;

    private View view_chat,view_functions;
    private FloatingActionButton fab_send,fab_more,fab_less,fab_gallery,fab_camera,fab_location;
    private ListView lv_chat;
    private EditText et_message;
    private ChatListAdapter adapter;

    /**
     * when creating a ChatFragment, use tournament, club, receiver to specify whether
     * it's a tournament chat, club chat or private chat. And use self to identify oneself
     * @param tournament  the tournament, if it's a tournament chat. set to null if not.
     * @param club    the club, if it's a tournament/club chat. set to null if not.
     * @param receiver    the receiver player if it's a private chat. set to null if not.
     * @param self    the self player
     * @return  the ChatFragment
     */
    public static ChatFragment newInstance(Tournament tournament, Club club, Player receiver, Player self){
        ChatFragment fragment = new ChatFragment();
        Bundle bundle = new Bundle();
        if ( tournament != null ) {
            bundle.putString(ARG_TOUR,tournament.toJson());
        }
        if ( club != null ) {
            bundle.putString(ARG_CLUB,club.toJson());
        }
        if ( receiver != null ) {
            bundle.putString(ARG_RECV,receiver.toJson());
        }
        bundle.putString(ARG_SELF,self.toJson());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            Gson gson = new Gson();
            if ( bundle.containsKey(ARG_TOUR) ) {
                tournament = gson.fromJson(bundle.getString(ARG_TOUR),Tournament.class);
                tournamentID = tournament.id;
            }
            if ( bundle.containsKey(ARG_CLUB) ) {
                club = gson.fromJson(bundle.getString(ARG_CLUB),Club.class);
                clubID = club.id;
            }
            if ( bundle.containsKey(ARG_RECV) ) {
                receiver = gson.fromJson(bundle.getString(ARG_RECV),Player.class);
                receiverID = receiver.getId();
            }
            self = gson.fromJson(bundle.getString(ARG_SELF),Player.class);
            selfID = self.getId();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view_chat = inflater.inflate(R.layout.fragment_chatroom, container, false);
        showChatPage();
        return view_chat;
    }

    /**
     * render the chat page and show chat messages
     */
    private void showChatPage(){
        loadChatHistory(CHAT_LIMIT,0,0);  // load most recent CHAT_LIMIT chats
        fab_send = (FloatingActionButton) view_chat.findViewById(R.id.fab_send);
        fab_more = (FloatingActionButton) view_chat.findViewById(R.id.fab_more);
        fab_less = (FloatingActionButton) view_chat.findViewById(R.id.fab_less);
        view_functions = view_chat.findViewById(R.id.layout_function_menu);
        et_message = (EditText) view_chat.findViewById(R.id.et_message);
        et_message.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if ( hasFocus ) {
                    if ( adapter != null ) {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        fab_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextMessage(et_message.getText().toString());
            }
        });

        fab_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_more.setVisibility(View.GONE);
                view_functions.setVisibility(View.VISIBLE);
            }
        });

        fab_less.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view_functions.setVisibility(View.GONE);
                fab_more.setVisibility(View.VISIBLE);
            }
        });

        et_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ( s != null && s.length() > 0 ) {
                    fab_send.setVisibility(View.VISIBLE);
                } else {
                    fab_send.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void showMessages() {
        lv_chat = (ListView) view_chat.findViewById(R.id.lv_chat);
        adapter = new ChatListAdapter(getContext(),chatArray, self.getId());
        lv_chat.setAdapter(adapter);
    }

    /**
     * send a text chat message and post it to the server
     * @param message the message to be sent
     */
    private void sendTextMessage(String message) {
        if ( message == null || message.length() < 1 ) {
            return;
        }
        DateFormat localDateFormat = Constant.getServerDateFormat();
        final Chat chat = new Chat(0,tournamentID,clubID,receiverID, selfID,self.getDisplayName(),
                Constant.MESSAGE_TYPE_TEXT,message,localDateFormat.format(new Date()));
        RequestAction actionPostChatText = new RequestAction() {
            @Override
            public void actOnPre() {
            }

            @Override
            public void actOnPost(int responseCode, String response) {
                chatArray.offer(chat);  // add to tail
                adapter.notifyDataSetChanged();
                lv_chat.setSelection(adapter.getCount()-1);
                adapter.notifyDataSetChanged();
                et_message.setText("");
            }
        };
        String url;
        if ( tournament != null ) {  // tournament chat
            url = UrlHelper.urlPostTournamentChat(tournament.id,club.id);
        } else if ( club != null ) {// club chat
            url = UrlHelper.urlPostClubChat(club.id);
        } else if ( receiver != null ) { // private chat
            url = UrlHelper.urlPostPrivateChat(receiver.getId());
        } else {
            Toast.makeText(getContext(), "Unknown error!", Toast.LENGTH_SHORT).show();
            Log.e("Chat Error","Unspecified chat type.");
            return;
        }
        RequestHelper.sendPostRequest(url,chat.toJson(),actionPostChatText);
    }

    /**
     * load the according chat messages
     * @param limit number of chat messages to load
     * @param beforeID  if beforeID is 0, ignore this param; else load chat with ID < beforeID
     * @param afterID  if afterID is 0, ignore this param; else load chat with ID > afterID
     */
    private void loadChatHistory(int limit, final int beforeID, final int afterID){
        RequestAction actionGetChat = new RequestAction() {
            @Override
            public void actOnPre() {
            }

            @Override
            public void actOnPost(int responseCode, String response) {
                if ( responseCode == 200 ) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray(Constant.TABLE_CHAT);
                        chatArray = new LinkedList<>();
                        for ( int i = 0; i < jsonArray.length(); i++ ) {
                            JSONObject json = jsonArray.getJSONObject(i);
                            JSONObject jsonChat = json.getJSONObject(Constant.TABLE_CHAT);
                            int id = jsonChat.getInt(Constant.CHAT_ID);
                            int tournamentID, clubID, receiverID, senderID;
                            try {
                                tournamentID = jsonChat.getInt(Constant.CHAT_TOURNAMENT_ID);
                            } catch (JSONException e) {
                                tournamentID = 0;
                            }
                            try {
                                clubID = jsonChat.getInt(Constant.CHAT_CLUB_ID);
                            } catch (JSONException e) {
                                clubID = 0;
                            }
                            try {
                                receiverID = jsonChat.getInt(Constant.CHAT_RECEIVER_ID);
                            } catch (JSONException e) {
                                receiverID = 0;
                            }
                            senderID = jsonChat.getInt(Constant.CHAT_SENDER_ID);
                            String messageType = jsonChat.getString(Constant.CHAT_MESSAGE_TYPE);
                            String messageContent = jsonChat.getString(Constant.CHAT_MESSAGE_CONTENT);
                            String time = jsonChat.getString(Constant.CHAT_TIME);
                            String senderName = json.getString(Constant.CHAT_SENDER_NAME);
                            if ( afterID != 0 ) {
                                // TODO: should sort messages asc in time
                                chatArray.offer(new Chat(id,tournamentID,clubID,receiverID,senderID,
                                        senderName,messageType,messageContent,time));
                            } else {
                                chatArray.push(new Chat(id,tournamentID,clubID,receiverID,senderID,
                                        senderName,messageType,messageContent,time));
                            }

                        }
                        showMessages();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        String url = UrlHelper.urlGetChat(tournamentID,clubID,receiverID,selfID,limit,beforeID,afterID);
        RequestHelper.sendGetRequest(url,actionGetChat);
    }
}
