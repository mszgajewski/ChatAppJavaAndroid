package com.mszgajewski.chatappjavaandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mszgajewski.chatappjavaandroid.messages.MessagesAdapter;
import com.mszgajewski.chatappjavaandroid.messages.MessagesList;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private final List<MessagesList> messagesLists = new ArrayList<>();
    private String phone;
    private String email;
    private String name;
    private int unseenMessages = 0;
    private String lastMessage = "";
    private String chatKey = "";
    private boolean dataSet = false;
    private RecyclerView messagesRecyclerView;
    private MessagesAdapter messagesAdapter;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://chatappjavaandroid-default-rtdb.europe-west1.firebasedatabase.app/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CircleImageView userProfilePic = findViewById(R.id.userProfilePic);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);

        phone = getIntent().getStringExtra("phone");
        email = getIntent().getStringExtra("email");
        name = getIntent().getStringExtra("name");

        messagesRecyclerView.setHasFixedSize(true);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        messagesAdapter = new MessagesAdapter(messagesLists, MainActivity.this);
        messagesRecyclerView.setAdapter(messagesAdapter);


        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("ładowanie...");
        progressDialog.show();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final String profilePicUrl = snapshot.child("users").child(phone).child("profile_pic").getValue(String.class);

                if (!profilePicUrl.isEmpty()){
                    Picasso.get().load(profilePicUrl).into(userProfilePic);

                }

                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                messagesLists.clear();
                unseenMessages = 0;
                lastMessage ="";
                chatKey = "";

                for (DataSnapshot dataSnapshot : snapshot.child("users").getChildren()){
                    final String getMobile = dataSnapshot.getKey();

                    dataSet = false;
                    if (!getMobile.equals(phone)){
                        final String getName = dataSnapshot.child("name").getValue(String.class);
                        final String getProfilePic = dataSnapshot.child("profile_pic").getValue(String.class);

                        databaseReference.child("chat").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                int getChatCounts = (int) snapshot.getChildrenCount();

                                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()){
                                    final String getKey = dataSnapshot1.getKey();
                                    chatKey = getKey;

                                    if (dataSnapshot1.hasChild("user_1") && dataSnapshot1.hasChild("user_2") && dataSnapshot1.hasChild("messages")){
                                        final String getUserOne = dataSnapshot1.child("user_1").getValue(String.class);
                                        final String getUserTwo = dataSnapshot1.child("user_2").getValue(String.class);

                                        if ((getUserOne.equals(getMobile) && getUserTwo.equals(phone)) || (getUserOne.equals(phone) && getUserTwo.equals(getMobile))){
                                            for(DataSnapshot chatDataSnapshot : dataSnapshot1.child("messages").getChildren()){
                                                final long getMessageKey = Long.parseLong(chatDataSnapshot.getKey());
                                                final long getLastSeenMessage = Long.parseLong(MemoryData.getLastMsgTS(MainActivity.this,getKey));

                                                lastMessage = chatDataSnapshot.child("msg").getValue(String.class);
                                                if (getMessageKey > getLastSeenMessage){
                                                    unseenMessages++;
                                                }
                                            }
                                        }
                                    }
                                }
                                if (!dataSet){
                                    dataSet = true;
                                    MessagesList messagesList = new MessagesList(getName, getMobile,getProfilePic,lastMessage, unseenMessages, chatKey);
                                    messagesLists.add(messagesList);
                                    messagesAdapter.updateData(messagesLists);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}