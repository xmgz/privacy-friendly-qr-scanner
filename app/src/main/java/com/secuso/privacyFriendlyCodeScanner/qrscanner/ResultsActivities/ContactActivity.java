package com.secuso.privacyFriendlyCodeScanner.qrscanner.ResultsActivities;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.secuso.privacyFriendlyCodeScanner.qrscanner.MainActivity;
import com.secuso.privacyFriendlyCodeScanner.qrscanner.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactActivity extends AppCompatActivity {

    ClipboardManager clipboardManager;
    ClipData clipData;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        TextView resultTextContact = (TextView) findViewById(R.id.result_text_contact);
        Button btnProceed = (Button) findViewById(R.id.btnProceed);
        Button btnCancel = (Button) findViewById(R.id.btnCancel);

        Bundle QRData = getIntent().getExtras();//from ResultActivity
        final String contactResult = QRData.getString("Rst");


        // resultTextContact.setText(vCard);


        Pattern pattern = Pattern.compile("((\\n|;|:)(FN:|N:)[0-9a-zA-Z-\\säöüÄÖÜß,]*(\\n|;))");

        Matcher m = pattern.matcher(contactResult);

        String name = "";

        if (m.find()) {
            name = m.group(1).substring(1);

            if (name.startsWith("N:"))
                resultTextContact.setText("Name: " + name.substring(2).replace(';', ' '));
            else if (name.startsWith("FN:"))
                resultTextContact.setText("Name: " + name.substring(3).replace(';', ' '));
            else
                resultTextContact.setText(R.string.noname);
        }
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent ca = new Intent(ContactActivity.this, MainActivity.class);
                startActivity(ca);

            }
        });

        btnProceed.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.choose_action)
                        .setItems(R.array.vcard_array, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:

                                        Uri uri = null;
                                        try {
                                            uri = createVCard();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                            Intent contact = new Intent();
                                           contact.setAction(Intent.ACTION_VIEW);

                                       // Intent contact = new Intent(ContactsContract.Intents.Insert.ACTION);
                                        //contact.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                                      //  Intent contact = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
                                        contact.setType("text/x-vcard");
                                        contact.setData(uri);

                                        String caption = getResources().getStringArray(R.array.vcard_array)[0];
                                        startActivity(Intent.createChooser(contact, caption));
                                        break;

                                    default:
                                }
                            }
                        });
                builder.create().show();
            }
        });

    }

    private Uri createVCard() throws IOException {


        Bundle QRData = getIntent().getExtras();//from ResultActivity
        final String contactResult = QRData.getString("Rst");

        File f = new File(this.getExternalFilesDir(null),"contact.vcf");
        FileWriter fw;
        fw = new FileWriter(f);
        Uri uri = null;
        try (FileOutputStream fop = new FileOutputStream(f)) {

            if (f.exists()) {

                fop.write(contactResult.getBytes());
                //Now read the content of the vCard after writing data into it
                BufferedReader br = null;
                String sCurrentLine;
                br = new BufferedReader(new FileReader("contact.vcf"));

                //close the output stream and buffer reader
                fop.flush();
                fop.close();
                System.out.println("The data has been written");
                Log.e("TESTEST", f.getPath());

                uri = FileProvider.getUriForFile(context, "com.secuso.privacyFriendlyCodeScanner", f);
                grantUriPermission("com.google.android.contacts", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                grantUriPermission("com.android.contacts", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);


            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uri;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share,menu);
        getMenuInflater().inflate(R.menu.copy,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Bundle QRData = getIntent().getExtras();//from ResultActivity
        final String qrurl = QRData.getString("Rst");

        switch (item.getItemId()){
            case R.id.share:
                shareIt(qrurl);
                return true;

            case R.id.copy:
                clipboardManager=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                clipData= ClipData.newPlainText("Text",qrurl);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getApplicationContext(),"Content copied",Toast.LENGTH_LONG).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void shareIt(String result)
    {
        Intent sharingIntent= new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT,result);
        startActivity(Intent.createChooser(sharingIntent,"Share via"));
    }

}
