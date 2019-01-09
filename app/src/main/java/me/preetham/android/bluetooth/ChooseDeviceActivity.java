package me.preetham.android.bluetooth;

import android.app.AutomaticZenRule;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ChooseDeviceActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 123;
    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private ListView paired_device_list_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        enableBluetoothVerbose();

        paired_device_list_view = findViewById(R.id.listview_paired_devices);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public void enableBluetoothVerbose() {
        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
}
