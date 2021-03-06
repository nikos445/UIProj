package com.example.nasty.UIProject;

import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import static java.lang.Integer.parseInt;

public class Table_Fragment extends Fragment {
    final FirebaseDatabase TableDatabase = FirebaseDatabase.getInstance();
    final DatabaseReference TableRef = TableDatabase.getReference().child("Tables");
    final DatabaseReference OrderRef = TableDatabase.getReference().child("Orders");
    final String table[][] = new String[1000][5];//MAX: 999 Table and 4 Specifications per table.
    final int[] children = new int[1]; // Tables in Database.
    final int[] YourTable = {0}; // TABLE HOLDER
    View Mview;
    Button TakeAwayButt;
    Button ReserveTableButt;
    Button UnreserveButt;
    Button CheckForTableButt;
    ImageButton AddPersonBtn;
    ImageButton DeletePersonBtn;
    TextView NumberOfPeople;
    ImageView PersonImg;
    TextView TableNum;
    ImageButton Logout_butt;
    int number = 1;
    TelephonyManager mngr;
    int flag = 0;
    String LockStatus;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Mview = inflater.inflate(R.layout.table_activity, container, false);

        /////////////////////////////////IMEI PERMISSION///////////////////////////////////////////
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{"android.permission.READ_PHONE_STATE"},
                1);
        mngr = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        final String imei = mngr.getDeviceId();

        /////////////////////////////////Initialization of IDs //////////////////////////////////////////////
        TakeAwayButt = (Button) Mview.findViewById(R.id.TakeAwaybtn);
        ReserveTableButt = (Button) Mview.findViewById(R.id.ReserveTablebtn);
        CheckForTableButt = (Button) Mview.findViewById(R.id.CheckForTableButton);
        NumberOfPeople = (TextView) Mview.findViewById(R.id.PersNum);
        AddPersonBtn = (ImageButton) Mview.findViewById(R.id.AddPersonBtn);
        DeletePersonBtn = (ImageButton) Mview.findViewById(R.id.DeletePersonBtn);

        PersonImg = (ImageView) Mview.findViewById(R.id.PersonImg);
        TableNum = (TextView) Mview.findViewById(R.id.TableNum);
        UnreserveButt = (Button) Mview.findViewById(R.id.UnreserveButt);

        OrderRef.child(imei).child("ReserveTime").setValue("0");
        OrderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if( dataSnapshot.child(imei).child("Locked").getValue() == null)
                    OrderRef.child(imei).child("Locked").setValue("No");
                if( dataSnapshot.child(imei).child("Locked").getValue() != null)
                    LockStatus = dataSnapshot.child(imei).child("Locked").getValue().toString();
                if (Objects.equals(LockStatus, "Yes")) {
                    getFragmentManager().beginTransaction().replace(R.id.content_frame, new Cart_Fragment()).commit();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        TableRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                children[0] = (int) dataSnapshot.getChildrenCount();

                for (int i = 1; i <= dataSnapshot.getChildrenCount(); i++)  // THIS IS FOR GETTING VALUES FROM DATABASE TO A MATRIX.
                {
                    table[i][1] = dataSnapshot.child(Integer.toString(i)).child("Seats").getValue().toString();
                    table[i][2] = dataSnapshot.child(Integer.toString(i)).child("Reserved").child("Yes").getValue().toString();
                    table[i][3] = dataSnapshot.child(Integer.toString(i)).child("Reserved").child("ID-Phone").getValue().toString();
                    if (Objects.equals(table[i][3], imei)) {
                        flag = 1;
                        YourTable[0] = i;

                        TableNum.setText("You Already Reserved Table Number : " + YourTable[0] + "\n\n...TAP TO ORDER...");

                        ShowCurrTable();

                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        UnreserveButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TableRef.child(Integer.toString(YourTable[0])).child("Reserved").child("ID-Phone").setValue("00000");
                TableRef.child(Integer.toString(YourTable[0])).child("Reserved").child("Yes").setValue("No");
                TableRef.getDatabase().getReference("Orders").child(imei).child("Table").setValue(0);
                getFragmentManager().beginTransaction().replace(R.id.content_frame
                        , new Table_Fragment())
                        .commit();    //START FROM ORDER PAGE
            }
        });

///////////////////////////////////////////////////// TABLE Fill END /////////////////////////////////////////////////
        CheckForTableButt.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                for (int i = 1; i <= children[0]; i++) {
                    if (!Objects.equals(table[i][3], imei)) {
                        flag = 0;
                    } else {
                        flag = 1;
                        YourTable[0] = i;
                        TableNum.setText("You Reserved Table Number " + YourTable[0] + "\n\n...TAP TO ORDER...");

                        ShowCurrTable();
                        break;
                    }
                }


                for (int i = 1; i <= children[0]; i++) {
                    if (flag == 0) {
                        if (parseInt(table[i][1]) >= number && !Objects.equals(table[i][2], "Yes")) //check for empty table and for fitting
                        {
                            YourTable[0] = i;
                            TableRef.child(Integer.toString(i)).child("Reserved").child("Yes").setValue("Yes");
                            TableRef.child(Integer.toString(i)).child("Reserved").child("ID-Phone").setValue(imei);
                            TableRef.getDatabase().getReference("Orders").child(imei).child("TakeAway").setValue("No");
                            TableRef.getDatabase().getReference("Orders").child(imei).child("Table").setValue(YourTable[0]);
                            Toast.makeText(getActivity().getApplicationContext(), "You Reserved Table Number " + YourTable[0], Toast.LENGTH_SHORT).show();
                            getFragmentManager().beginTransaction().replace(R.id.content_frame
                                    , new Time_n_Day_Fragment())
                                    .commit();

                            flag = 1;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (YourTable[0] == 0) {
                    Toast.makeText(getActivity().getApplicationContext(), "There Is No Table For " + number + " Person. Try Again Later!", Toast.LENGTH_SHORT).show();
                }
            }
        });
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        TableNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.content_frame
                        , new Order_Fragment())
                        .commit();
            }
        });

        ReserveTableButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fadeOutAndHideImage(TakeAwayButt);
                fadeOutAndHideImage(ReserveTableButt);
                fadeIn(CheckForTableButt);
                fadeIn(NumberOfPeople);
                fadeIn(AddPersonBtn);
                fadeIn(DeletePersonBtn);
                fadeIn(PersonImg);
            }
        });

        AddPersonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (number < 10)
                    number++;
                NumberOfPeople.setText(String.valueOf(number));
            }
        });

        DeletePersonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (number > 1)
                    number--;
                NumberOfPeople.setText(String.valueOf(number));
            }
        });

        TakeAwayButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TableRef.getDatabase().getReference("Orders").child(imei).child("TakeAway").setValue("Yes");
                TableRef.getDatabase().getReference("Orders").child(imei).child("Table").setValue(0);
                getFragmentManager().beginTransaction().replace(R.id.content_frame
                        , new Time_n_Day_Fragment())
                        .commit();
            }
        });
        return Mview;
    }

    public void fadeOutAndHideImage(final View img) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(500);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                img.setVisibility(View.GONE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });

        img.startAnimation(fadeOut);
    }

    public void fadeIn(final View img) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setDuration(400);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                img.setVisibility(View.VISIBLE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });

        img.startAnimation(fadeIn);
    }

    public void ShowCurrTable() {
        ReserveTableButt.setEnabled(false);
        TakeAwayButt.setEnabled(false);

        fadeOutAndHideImage(ReserveTableButt);
        fadeOutAndHideImage(TakeAwayButt);

        fadeOutAndHideImage(PersonImg);
        fadeOutAndHideImage(CheckForTableButt);
        fadeOutAndHideImage(AddPersonBtn);
        fadeOutAndHideImage(DeletePersonBtn);
        fadeOutAndHideImage(NumberOfPeople);
        fadeIn(TableNum);
        fadeIn(UnreserveButt);


    }
}



