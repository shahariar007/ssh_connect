package com.ms.timerforedtl;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

public class MainActivity2 extends AppCompatActivity {
    Button rasp4, rasp3;
    public TextView result3, result4;
    AutoCompleteTextView pi4EditText;

    String item[] = {
            "uptime -p",
            "sudo shutdown now",
            "sudo reboot",
            "sudo /etc/init.d/apache2 restart",
            "sudo /opt/vc/bin/vcgencmd measure_temp",
            "sudo apt update","sudo apt full-upgrade",
            "sudo vcgencmd get_mem arm && vcgencmd get_mem gpu",
            "ping",
            "sudo netstat -tunlp"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        rasp4 = findViewById(R.id.rasp4);
        rasp3 = findViewById(R.id.rasp3);
        result3 = findViewById(R.id.result3);
        result4 = findViewById(R.id.result4);


        pi4EditText = findViewById(R.id.pi4EditText);

        pi4EditText.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, item));

        rasp4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(pi4EditText.getText().toString()))
                    new Tasks(MainActivity2.this).execute("pi", "PASSWORD", "URL", "22", "PI4", pi4EditText.getText().toString());
                else
                    Toast.makeText(MainActivity2.this, "Empty string", Toast.LENGTH_SHORT).show();
            }
        });

        rasp3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(pi4EditText.getText().toString()))
                    new Tasks(MainActivity2.this).execute("pi", "PASSWORD", "URL", "1022", "PI3", pi4EditText.getText().toString());
                else
                    Toast.makeText(MainActivity2.this, "Empty string", Toast.LENGTH_SHORT).show();
            }
        });


    }


    public String executeRemoteCommand(
            String username,
            String password,
            String hostname,
            String port, String tag, String command) throws Exception {


        JSch jsch = new JSch();
        Session session = jsch.getSession(username, hostname, Integer.parseInt(port));
        session.setPassword(password);
        session.setTimeout(100000);

        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        prop.put("compression.s2c", "zlib,none");
        prop.put("compression.c2s", "zlib,none");
        session.setConfig(prop);

        session.connect();

        // SSH Channel
      //  ChannelExec channelssh = (ChannelExec) session.openChannel("exec");
        ChannelExec channelssh = (ChannelExec) session.openChannel("exec");
        channelssh.setInputStream(null);
        channelssh.setErrStream(System.err);
        ((ChannelExec) channelssh).setPty(true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channelssh.setOutputStream(baos);

        // Execute command
        channelssh.setCommand(command);
        channelssh.connect();
        Thread.sleep(2000);
        channelssh.disconnect();

        if (tag.equals("PI4")) {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    result4.setText(new String(baos.toByteArray()));
                    result4.setMovementMethod(new ScrollingMovementMethod());
                    result4.setTextIsSelectable(false);
                    result4.measure(-1, -1);//you can specific other values.
                    result4.setTextIsSelectable(true);

                }
            });
        } else if (tag.equals("PI3")) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    result3.setText(new String(baos.toByteArray()));
                    result3.setMovementMethod(new ScrollingMovementMethod());
                    result3.setTextIsSelectable(false);
                    result3.measure(-1, -1);//you can specific other values.
                    result3.setTextIsSelectable(true);


                }
            });
        }


        return baos.toString();
    }

    public class Tasks extends AsyncTask<String, Void, String> {
        private ProgressDialog dialog;
        Context context;

        public Tasks(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                executeRemoteCommand(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5]);
            } catch (Exception e) {

                if (strings[4].equals("PI4")) {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            result4.setText(e.getMessage());
                            result4.setMovementMethod(new ScrollingMovementMethod());
                            result4.setTextIsSelectable(false);
                            result4.measure(-1, -1);//you can specific other values.
                            result4.setTextIsSelectable(true);

                        }
                    });
                } else if (strings[4].equals("PI3")) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            result3.setText(e.getMessage());
                            result3.setMovementMethod(new ScrollingMovementMethod());
                            result3.setTextIsSelectable(false);
                            result3.measure(-1, -1);//you can specific other values.
                            result3.setTextIsSelectable(true);

                        }
                    });
                    e.printStackTrace();

                }
            }
                return null;
            }

            @Override
            protected void onPreExecute () {
                super.onPreExecute();
                dialog = new ProgressDialog(context);
                dialog.setMessage("Please Wait....." );
                dialog.show();

            }

            @Override
            protected void onPostExecute (String s){
                super.onPostExecute(s);
                if (dialog != null) {
                    dialog.dismiss();
                }

            }
        }
    }