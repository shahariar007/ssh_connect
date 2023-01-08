package com.ms.timerforedtl;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

public class MainActivity2 extends AppCompatActivity {
    Button rasp4, rasp3, shutdown;
    public TextView result3, result4;
    AutoCompleteTextView pi4EditText;
    Session mSessionPI4;
    ChannelExec channel;
    Session mSessionPI3;
    String IP = "";
    String PASSWORD = "";
    ImageView settings;

    String USERID = "pi";


    String HOST_KEY = "HOST";
    String PASSWORD_KEY = "PASSWORD";
    String USERID_KEY = "USERID";
// cct
    String item[] = {
            "uptime -p",
            "sudo shutdown now",
            "sudo reboot",
            "sudo /etc/init.d/apache2 restart",
            "sudo /opt/vc/bin/vcgencmd measure_temp",
            "sudo apt update", "sudo apt full-upgrade",
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
        shutdown = findViewById(R.id.shutdown);
        settings = findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setup();
            }
        });
        dataSet();
        pi4EditText = findViewById(R.id.pi4EditText);

        pi4EditText.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, item));

        rasp4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(pi4EditText.getText().toString()))
                    new Tasks(MainActivity2.this).execute(USERID, PASSWORD, IP, "22", "PI4", pi4EditText.getText().toString());
                else
                    Toast.makeText(MainActivity2.this, "Empty string", Toast.LENGTH_SHORT).show();
            }
        });

        rasp3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(pi4EditText.getText().toString()))
                    new Tasks(MainActivity2.this).execute(USERID, PASSWORD, IP, "1022", "PI3", pi4EditText.getText().toString());
                else
                    Toast.makeText(MainActivity2.this, "Empty string", Toast.LENGTH_SHORT).show();
            }
        });

        shutdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Tasks(MainActivity2.this).execute(USERID, PASSWORD, IP, "22", "PI4", item[1]);
                new Tasks(MainActivity2.this).execute(USERID, PASSWORD, IP, "1022", "PI3", item[1]);

            }
        });


    }


    public String executeRemoteCommand(
            String username,
            String password,
            String hostname,
            String port, String tag, String command) throws Exception {

        if (mSessionPI4 == null) {
            JSch jsch = new JSch();
            mSessionPI4 = jsch.getSession(username, hostname, Integer.parseInt(port));
            mSessionPI4.setPassword(password);
            mSessionPI4.setTimeout(1000000);

            // Avoid asking for key confirmation
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            prop.put("compression.s2c", "zlib,none");
            prop.put("compression.c2s", "zlib,none");
            prop.put("PreferredAuthentications", "password");
            mSessionPI4.setConfig(prop);
            mSessionPI4.connect();
        }
        channel = (ChannelExec) mSessionPI4.openChannel("exec");
        channel.setCommand(command);

        channel.setInputStream(null);
        channel.setErrStream(System.err);
        ((ChannelExec) channel).setPty(true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channel.setOutputStream(baos);


        InputStream commandOutput = channel.getExtInputStream();

        StringBuilder outputBuffer = new StringBuilder();
        StringBuilder errorBuffer = new StringBuilder();

        InputStream in = channel.getInputStream();
        InputStream err = channel.getExtInputStream();

        channel.connect();
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) break;
                outputBuffer.append(new String(tmp, 0, i));
            }
            while (err.available() > 0) {
                int i = err.read(tmp, 0, 1024);
                if (i < 0) break;
                errorBuffer.append(new String(tmp, 0, i));
            }
            if (channel.isClosed()) {
                if ((in.available() > 0) || (err.available() > 0)) continue;
                System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (tag.equals("PI4")) {
                        result4.setText(outputBuffer.toString());
                    } else {
                        result3.setText(outputBuffer.toString());
                    }


                }
            });
        }

        channel.disconnect();

        if (tag.equals("PI4")) {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    result4.setText(outputBuffer.toString());
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

                    result3.setText(outputBuffer.toString());
                    result3.setMovementMethod(new ScrollingMovementMethod());
                    result3.setTextIsSelectable(false);
                    result3.measure(-1, -1);//you can specific other values.
                    result3.setTextIsSelectable(true);
                }
            });
        }


        return outputBuffer.toString();
    }


    public String executeRemoteCommandShellTest(
            String username,
            String password,
            String hostname,
            String port, String tag, String command) throws Exception {

        if (mSessionPI4 == null) {
            JSch jsch = new JSch();
            mSessionPI4 = jsch.getSession(username, hostname, Integer.parseInt(port));
            mSessionPI4.setPassword(password);
            mSessionPI4.setTimeout(1000000);

            // Avoid asking for key confirmation
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            prop.put("compression.s2c", "zlib,none");
            prop.put("compression.c2s", "zlib,none");
            prop.put("PreferredAuthentications", "password");
            mSessionPI4.setConfig(prop);
            mSessionPI4.connect();
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ChannelShell channel = (ChannelShell) mSessionPI4.openChannel("shell");
        ((ChannelShell) channel).setPtyType("dumb");
        //((ChannelShell)channel).setPty(true);
        channel.setOutputStream(outputStream, true);
        PrintStream stream = new PrintStream(channel.getOutputStream());
        channel.connect();
        stream.println(command);
        stream.flush();
        //stream.close();

        StringBuilder outputBuffer = new StringBuilder();

        InputStream in = channel.getInputStream();
        InputStream err = channel.getExtInputStream();

        Thread.sleep(1000);

        byte[] tmp = new byte[1024];
        int stepCount = 0;
        while (true) {
            Log.d("MainActivity2", "in.available()" + in.available());

            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) break;
                outputBuffer.append(new String(tmp, 0, i));
                Log.d("MainActivity2", "available: " + i);

                Log.d("MainActivity2", "executeRemoteCommandShellTest: ");
                stepCount = 0;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        result4.setText(outputBuffer.toString());
                    }
                });
            }
            if (channel.isClosed()) {
                if ((in.available() > 0) || (err.available() > 0)) continue;
                System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    result4.setText(outputBuffer.toString());
                }
            });
            Thread.sleep(1000);
            stepCount++;
            if (stepCount == 10) {
                break;
            }

        }
        return "";
    }

    public String executeRemoteCommandPI3(
            String username,
            String password,
            String hostname,
            String port, String tag, String command) throws Exception {

        if (mSessionPI3 == null) {
            JSch jsch = new JSch();
            mSessionPI3 = jsch.getSession(username, hostname, Integer.parseInt(port));
            mSessionPI3.setPassword(password);
            mSessionPI3.setTimeout(1000000);

            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            prop.put("compression.s2c", "zlib,none");
            prop.put("compression.c2s", "zlib,none");
            mSessionPI3.setConfig(prop);
            mSessionPI3.connect();
        }

        ChannelExec channel = (ChannelExec) mSessionPI3.openChannel("exec");
        channel.setCommand(command);

        InputStream commandOutput = channel.getExtInputStream();


        StringBuilder outputBuffer = new StringBuilder();
        StringBuilder errorBuffer = new StringBuilder();

        InputStream in = channel.getInputStream();
        InputStream err = channel.getExtInputStream();

        channel.connect();
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) break;
                outputBuffer.append(new String(tmp, 0, i));
            }
            while (err.available() > 0) {
                int i = err.read(tmp, 0, 1024);
                if (i < 0) break;
                errorBuffer.append(new String(tmp, 0, i));
            }
            if (channel.isClosed()) {
                if ((in.available() > 0) || (err.available() > 0)) continue;
                System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (tag.equals("PI4")) {
                        result4.setText(outputBuffer.toString());
                    } else {
                        result3.setText(outputBuffer.toString());
                    }


                }
            });
            Log.d("MainActivity2", "executeRemoteCommandPI3: ");
        }

        channel.disconnect();

        if (tag.equals("PI4")) {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    result4.setText(outputBuffer.toString());
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

                    result3.setText(outputBuffer.toString());
                    result3.setMovementMethod(new ScrollingMovementMethod());
                    result3.setTextIsSelectable(false);
                    result3.measure(-1, -1);//you can specific other values.
                    result3.setTextIsSelectable(true);


                }
            });
        }


        return outputBuffer.toString();
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
                if (strings[4].equals("PI4")) {
                    //  executeRemoteCommand(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5]);
                    executeRemoteCommandShellTest(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5]);
                } else
                    executeRemoteCommandPI3(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5]);


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
        protected void onPreExecute() {
            super.onPreExecute();
            // dialog = new ProgressDialog(context);
            //dialog.setMessage("Please Wait....." );
            // dialog.show();

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (dialog != null) {
                dialog.dismiss();
            }

        }
    }

    public void setup() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Setup");
        View v = LayoutInflater.from(this).inflate(R.layout.setup_layout, null, false);
        alert.setView(v);

        EditText host = v.findViewById(R.id.host);
        EditText userName = v.findViewById(R.id.userName);
        EditText password = v.findViewById(R.id.password);
        host.setText(IP);
        userName.setText(USERID);
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (!TextUtils.isEmpty(host.getText().toString()) && !TextUtils.isEmpty(userName.getText().toString()) && !TextUtils.isEmpty(password.getText().toString())) {
                    PreferenceManager.getDefaultSharedPreferences(MainActivity2.this).edit().putString(HOST_KEY, host.getText().toString()).apply();
                    PreferenceManager.getDefaultSharedPreferences(MainActivity2.this).edit().putString(USERID_KEY, userName.getText().toString()).apply();
                    PreferenceManager.getDefaultSharedPreferences(MainActivity2.this).edit().putString(PASSWORD_KEY, password.getText().toString()).apply();
                    dataSet();
                } else {
                    Toast.makeText(MainActivity2.this, "Non of Field is Empty", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

       //HO
        // home
        alert.show();
    }

    public void dataSet() {
        IP = PreferenceManager.getDefaultSharedPreferences(MainActivity2.this).getString(HOST_KEY, "");
        USERID = PreferenceManager.getDefaultSharedPreferences(MainActivity2.this).getString(USERID_KEY, "");
        PASSWORD = PreferenceManager.getDefaultSharedPreferences(MainActivity2.this).getString(PASSWORD_KEY, "");
        Log.d("TAG", "dataSet: " + IP + USERID+PASSWORD);
    }
}