package com.example.easygardening;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.easygardening.Adapter.CustomAdapter;
import com.example.easygardening.Utils.AppConstants;
import com.example.easygardening.Utils.DataConverter;
import com.example.easygardening.database.DatabaseInstance;
import com.example.easygardening.model.FieldModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ClickCallbacks {
    private RecyclerView recyclerView;
    private List<FieldModel> fieldModelList = new ArrayList<>();
    private FloatingActionButton addButton;
    private AlertDialog.Builder alertBuilder;
    private CustomAdapter adapter;
    private DatabaseInstance databaseInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();
        fieldModelList.clear();

        databaseInstance = DatabaseInstance.getDatabaseInstance(getApplicationContext());
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                fieldModelList = databaseInstance.databaseDao().getAll();
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter = new CustomAdapter(fieldModelList, MainActivity.this);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setAdapter(adapter);
            }
        }, 1000);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewField();
            }
        });

        ItemTouchHelper.SimpleCallback itemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    deleteField(fieldModelList.get(viewHolder.getAdapterPosition()));
                }
            }
        };

        new ItemTouchHelper(itemTouchCallback).attachToRecyclerView(recyclerView);
    }

    private void addNewField() {
        alertBuilder = new AlertDialog.Builder(this);
        final View view = LayoutInflater.from(this).inflate(R.layout.layout_add_new_plant, null);
        final EditText address = view.findViewById(R.id.ev_address);
        final EditText date = view.findViewById(R.id.ev_date);
        final Spinner spinner = view.findViewById(R.id.sp_typeOfField);

        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, AppConstants.typeOfFieldsArray));

        alertBuilder.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int type = spinner.getSelectedItemPosition();
                FieldModel fieldModel = new FieldModel(type);
                fieldModel.address = address.getText().toString();
                fieldModel.date = date.getText().toString();
                addField(fieldModel);
                Toast.makeText(MainActivity.this, "New " + spinner.getSelectedItem() + " field added", Toast.LENGTH_SHORT).show();
            }
        });
        alertBuilder.setTitle("Add New Field");
        alertBuilder.setMessage("Fill all the details to add new field");
        alertBuilder.setView(view);
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

    }

    public List<FieldModel> getFieldModelList() {
        if (!fieldModelList.isEmpty()) {
            fieldModelList.clear();
        }
        return databaseInstance.databaseDao().getAll();
    }

    public void addField(final FieldModel fieldModel) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                databaseInstance.databaseDao().insertField(fieldModel);
                //Toast.makeText(MainActivity.this, "New Field added", Toast.LENGTH_SHORT).show();
            }
        });
        fieldModelList.add(fieldModel);
        adapter.notifyDataSetChanged();

    }

    private void initUi() {
        recyclerView = findViewById(R.id.recylerView);
        addButton = findViewById(R.id.floatingActionButton);
    }

    private void deleteField(final FieldModel fieldModel) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                databaseInstance.databaseDao().deleteField(fieldModel);
            }
        });
        fieldModelList.remove(fieldModel);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Plot Deleted", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onItemClicked(int position, int imageNo) {
        if (imageNo > 0) {

            if (!fieldModelList.get(position).imageResource.get(imageNo - 1).isPressed()) {
                Intent intent = new Intent(this, AddPlantActivity.class);
                intent.putExtra("POSITION1", position);
                intent.putExtra("POSITION2", imageNo);
                startActivityForResult(intent, 1);
            }
        } else {
            showFieldInfo(position);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        assert data != null;
        Bundle bundle = data.getExtras();
        final int position, pos2, imageResource;
        if (requestCode == 1) {
            assert bundle != null;
            position = bundle.getInt("POSITION1");
            pos2 = bundle.getInt("POSITION2") - 1;
            imageResource = bundle.getInt("IMAGE_RESOURCE");

            fieldModelList.get(position).imageResource.get(pos2).setPressed(true);
            fieldModelList.get(position).imageResource.get(pos2).setResource(imageResource);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    databaseInstance.databaseDao().updateList(fieldModelList.get(position));
                }
            });
            adapter.notifyDataSetChanged();
        }
    }


    private void showFieldInfo(int position) {
        TextView address, type, date;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_field_info, null);
        address = view.findViewById(R.id.tv_address);
        // type=view.findViewById(R.id.tv_type);
        date = view.findViewById(R.id.tv_date);

        address.setText("Address: " + fieldModelList.get(position).address);
        date.setText("Date: " + fieldModelList.get(position).date);
        builder.setView(view);
        builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setTitle("Field Details");
        alertDialog.show();
    }


}


