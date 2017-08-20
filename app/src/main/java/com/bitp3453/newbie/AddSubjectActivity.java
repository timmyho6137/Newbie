package com.bitp3453.newbie;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddSubjectActivity extends AppCompatActivity {

    EditText subName, lecName;
    NewbieDB newbieDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_subject);
    }
    public void fnSaveSubject(View view){
        subName = (EditText) findViewById(R.id.subName);
        lecName = (EditText) findViewById(R.id.lecName);
        newbieDB = new NewbieDB(getApplicationContext());
        int subColor = R.color.event_color_02;

        Cursor c = newbieDB.getReadableDatabase().rawQuery("SELECT * FROM "+NewbieDB.subTblName+" WHERE "+NewbieDB.colSubName+" = ?", new String[]{subName.getText().toString().trim()});
        if(c.getCount() <1){
            int newId = newbieDB.fnTotalRow(NewbieDB.subTblName)+1;
            if(newbieDB.fnExecuteSql("INSERT INTO "+NewbieDB.subTblName+" VALUES("+newId+", '"+subName.getText().toString().trim()+"', '"+lecName.getText().toString().trim()+"', '"+subColor+"');", getApplicationContext())){
                Toast.makeText(AddSubjectActivity.this, "Subject Insert Success", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AddSubjectActivity.this, AddClassActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }else {
            Toast.makeText(AddSubjectActivity.this, "Subject is already registered", Toast.LENGTH_SHORT).show();
        }
        c.close();
    }
}
