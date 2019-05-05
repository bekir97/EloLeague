package com.example.modegamer.Fragments.LoL;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.androidadvance.topsnackbar.TSnackbar;
import com.example.modegamer.Adapters.AdapterStatus;
import com.example.modegamer.DesignClasses.SnackBarUX;
import com.example.modegamer.Devicedata.Preferences;
import com.example.modegamer.Items.ItemStatus;
import com.example.modegamer.MainActivity;
import com.example.modegamer.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import javax.annotation.Nullable;

public class StatusLoLFragment extends Fragment implements View.OnClickListener {

    private String TAG = "StatusLoLFragment";
    private MainActivity activity;
    private Preferences preferences;
    private View view;
    private Context context;


    //Firebase References
    private FirebaseFirestore fireStore;

    //StatusRecycler References
    private ArrayList<ItemStatus> itemStatusArray;
    private RecyclerView recyclerStatus;
    private ItemStatus itemStatus;
    private AdapterStatus adapterStatus;
    private ConstraintLayout constraintLayout;

    private Button btnOnline,btnMate,btnOffline;
    private String username,status;


   @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle  savedInstanceState){
        view=inflater.inflate(R.layout.fragm_status_lol,container,false);
        preferences=new Preferences(getContext());
        activity=(MainActivity) getActivity();
        context=getContext();
        fireStore=FirebaseFirestore.getInstance();
        username=preferences.getUserName();
        itemStatusArray=new ArrayList<>();
        adapterStatus=new AdapterStatus(itemStatusArray,activity,context);
        init();

        new AsyncGetDb().execute();

        return view;
    }

    private void init(){

        btnOnline=view.findViewById(R.id.btn_online);
        btnOnline.setOnClickListener(this);
        btnMate=view.findViewById(R.id.btn_play_game);
        btnOffline=view.findViewById(R.id.btn_offline);
        btnMate.setOnClickListener(this);
        btnOffline.setOnClickListener(this);
        constraintLayout=view.findViewById(R.id.status_constraint);
        recyclerStatus=view.findViewById(R.id.recycler_status);
        recyclerStatus.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerStatus.hasFixedSize();
    }



    private void statusChange(){
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd,MMM hh:mm");
            String dateTime = simpleDateFormat.format(calendar.getTime());

            itemStatus=new ItemStatus(username,status,dateTime);
            fireStore.collection("Games")
                    .document("LoL")
                    .collection("Status")
                    .document(username)
                    .set(itemStatus);
            TSnackbar tSnackbar= TSnackbar.make(constraintLayout, "Status Changed "+status, TSnackbar.LENGTH_LONG).setActionTextColor(Color.WHITE);
            View snackView =tSnackbar.getView();
            TextView textView = (TextView) snackView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            snackView.setBackgroundColor(Color.parseColor("#a3d022"));
            tSnackbar.setIconLeft(R.drawable.ic_notification,35);
            tSnackbar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getStatusDb(){
        try {
            fireStore.collection("Games")
                    .document("LoL")
                    .collection("Status")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    itemStatusArray.clear();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot doc:queryDocumentSnapshots) {
                            String user= Objects.requireNonNull(doc.get("username")).toString();
                            String stat= Objects.requireNonNull(doc.get("gameStatus")).toString();
                            String dat= Objects.requireNonNull(doc.get("dateStatus")).toString();

                            ItemStatus itemStatus=new ItemStatus(user,stat,dat);
                            itemStatusArray.add(itemStatus);
                            recyclerStatus.setAdapter(adapterStatus);
                            adapterStatus.notifyDataSetChanged();
                        }
                    }else{
                        Log.d(TAG, "onEvent: getStatusDb > queryDocumentSnapshots is null");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class AsyncStatus extends AsyncTask<Void,Void,Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            statusChange();
            getStatusDb();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public  class AsyncGetDb extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            getStatusDb();
            return null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_online:{
                status="Online";
                new AsyncStatus().execute();
                break;
            }
            case R.id.btn_play_game:{
                status="Looking For PlayMate";
                new AsyncStatus().execute();
                break;
            }
            case R.id.btn_offline:{
                status="Offline";
                new AsyncStatus().execute();
                break;
            }
            default:break;
        }
    }
}
