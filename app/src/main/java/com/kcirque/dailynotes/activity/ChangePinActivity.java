package com.kcirque.dailynotes.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kcirque.dailynotes.R;
import com.kcirque.dailynotes.utils.SharedPref;

public class ChangePinActivity extends AppCompatActivity {

    private SharedPref sharedPref;
    private EditText oldPinEditText;
    private EditText newPinEditText;
    private EditText confirmPinEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lock_note_dialog);
        if (getSupportActionBar()!=null)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Change Pin");

        oldPinEditText = findViewById(R.id.old_pin_edit_text);
        newPinEditText = findViewById(R.id.new_pin_edit_text);
        confirmPinEditText = findViewById(R.id.confirm_pin_edit_text);
        sharedPref = new SharedPref(this);
        if (sharedPref.getPin() != null) {
            oldPinEditText.setVisibility(View.VISIBLE);
        } else {
            oldPinEditText.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.change_pin_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change:
                savePin();
                break;
            case R.id.action_clear:
                clearPin();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearPin() {
        sharedPref.putPin(null);
        Toast.makeText(this, "Clear pin", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void savePin() {
        if (sharedPref.getPin() != null) {
            changePin();
        } else {
            setPin();
        }
    }

    private void setPin() {
        if (newPinEditText.getText().toString().length() < 4) {
            newPinEditText.setError("Pin must have 4 digit");
            return;
        }
        if (!newPinEditText.getText().toString().equals(confirmPinEditText.getText().toString())) {
            newPinEditText.setError("pin does not match");
            confirmPinEditText.setError("pin does not match");
            return;
        }
        sharedPref.putPin(newPinEditText.getText().toString());
        Toast.makeText(this, "Pin Changed", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void changePin() {
        if (!oldPinEditText.getText().toString().equals(sharedPref.getPin())) {
            oldPinEditText.setError("old pin does not match");
            return;
        }
        setPin();
    }
}
