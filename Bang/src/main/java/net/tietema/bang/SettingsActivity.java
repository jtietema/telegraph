package net.tietema.bang;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import com.actionbarsherlock.app.ActionBar;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.google.inject.Inject;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Input user account settings
 * @author Jeroen Tietema <jeroen@tietema.net>
 */
@ContentView(R.layout.settings)
public class SettingsActivity extends RoboSherlockActivity implements TextWatcher {
    @Inject                     private SharedPreferences   preferences;
    @InjectView(R.id.username)  private EditText            username;
    @InjectView(R.id.password)  private EditText            password;
                                private boolean             dirty = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username.addTextChangedListener(this);
        password.addTextChangedListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dirty) {
            ((BangApplication) getApplication()).post(new SettingsChangedEvent());
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Const.EMAIL, username.getText().toString());
        editor.putString(Const.PASSWORD, password.getText().toString());
        editor.commit();
        dirty = true;
    }
}