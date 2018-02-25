package info.sayederfanarefin.evm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class login extends AppCompatActivity {

    EditText et;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Button login = (Button) findViewById(R.id.login);
        et = (EditText) findViewById(R.id.pin);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(et.getText().toString().equals("666")){

                    Intent i = new Intent(login.this, home.class);
                    startActivity(i);
                    finish();

                }
            }
        });
        Intent i = new Intent(login.this, home.class);
        startActivity(i);
        finish();
    }
}
