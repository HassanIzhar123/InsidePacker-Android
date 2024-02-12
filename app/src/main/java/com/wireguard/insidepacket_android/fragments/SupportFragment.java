package com.wireguard.insidepacket_android.fragments;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.collection.CircularArray;
import androidx.fragment.app.Fragment;

import com.wireguard.insidepacket_android.R;
import com.wireguard.insidepacket_android.essentials.SettingsSingleton;
import com.wireguard.insidepacket_android.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SupportFragment extends Fragment {
    View view;
    SettingsSingleton settingsSingleton = SettingsSingleton.getInstance();
    SwitchCompat switchButton;
    RelativeLayout sendLogsToSupportLayout;
    Boolean isSendOnCrashReportTouched = false;
    private final CircularArray<LogLine> logLines = new CircularArray<>();
    private final CircularArray<String> rawLogLines = new CircularArray<>();
    private DateFormat yearFormatter;
    private String year;
    private static final String TAG = "WireGuard/LogViewerActivity";
    private static final Pattern THREADTIME_LINE = Pattern.compile("^(\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3})(?:\\s+[0-9A-Za-z]+)?\\s+(\\d+)\\s+(\\d+)\\s+([A-Z])\\s+(.+?)\\s*: (.*)$");
    ArrayList<LogLine> bufferedLogLines = new ArrayList<>();

    public SupportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.support_fragment, container, false);
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.set(Calendar.MILLISECOND, 0);
        year = String.valueOf(calendar.get(Calendar.YEAR));
        yearFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        switchButton = view.findViewById(R.id.send_crash_switch);
        sendLogsToSupportLayout = view.findViewById(R.id.send_logs_to_support_layout);
        setUi();
        setClickListeners();
        return view;
    }
//    private void streamingLog() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                ProcessBuilder builder = new ProcessBuilder().command("logcat", "-b", "all", "-v", "threadtime", "*:V");
//                builder.environment().put("LC_ALL", "C");
//                Process process = null;
//                try {
//                    process = builder.start();
//                    BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
//
//                    final int[] posStart = {0};
//                    long timeLastNotify = System.nanoTime();
//                    final boolean[] priorModified = {false};
//
//                    long timeout = 1000000000L / 2;
//                    final int MAX_LINES = (1 << 16) - 1;
//                    final int MAX_BUFFERED_LINES = (1 << 14) - 1;
//
//                    while (true) {
//                        String line = stdout.readLine();
//                        if (line == null)
//                            break;
//                        if (rawLogLines.size() >= MAX_LINES)
//                            rawLogLines.popFirst();
//                        rawLogLines.addLast(line);
//                        LogLine logLine = parseLine(line);
//                        if (logLine != null) {
//                            bufferedLogLines.add(logLine);
//                        } else {
//                            if (!bufferedLogLines.isEmpty()) {
//                                bufferedLogLines.get(bufferedLogLines.size() - 1).msg += "\n" + line;
//                            } else if (!logLines.isEmpty()) {
//                                logLines.get(logLines.size() - 1).msg += "\n" + line;
//                                priorModified[0] = true;
//                            }
//                        }
//                        for (LogLine bufferedLine : bufferedLogLines) {
//                            logLines.addLast(bufferedLine);
//                        }
//                    }
//                } catch (IOException e) {
//                    //Log.e(TAG, Log.getStackTraceString(e));
//                } finally {
//                    if (process != null) {
//                        process.destroy();
//                    }
//                }
//            }
//        }).start();
//    }

//    private void streamingLog() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                ProcessBuilder builder = new ProcessBuilder().command("logcat", "-b", "all", "-v", "threadtime", "*:V");
//                builder.environment().put("LC_ALL", "C");
//                Process process = null;
//                try {
//                    process = builder.start();
//                    BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
//
//                    final int[] posStart = {0};
//                    long timeLastNotify = System.nanoTime();
//                    final boolean[] priorModified = {false};
//                    ArrayList<LogLine> bufferedLogLines = new ArrayList<>();
//                    long timeout = 1000000000L / 2;
//                    final int MAX_LINES = (1 << 16) - 1;
//                    final int MAX_BUFFERED_LINES = (1 << 14) - 1;
//
//                    while (true) {
//                        String line = stdout.readLine();
//                        if (line == null)
//                            break;
//                        if (rawLogLines.size() >= MAX_LINES)
//                            rawLogLines.popFirst();
//                        rawLogLines.addLast(line);
//                        LogLine logLine = parseLine(line);
//                        if (logLine != null) {
//                            bufferedLogLines.add(logLine);
//                        } else {
//                            if (!bufferedLogLines.isEmpty()) {
//                                bufferedLogLines.get(bufferedLogLines.size() - 1).msg += "\n" + line;
//                            } else if (!logLines.isEmpty()) {
//                                logLines.get(logLines.size() - 1).msg += "\n" + line;
//                                priorModified[0] = true;
//                            }
//                        }
//                        long timeNow = System.nanoTime();
//                        if (bufferedLogLines.size() < MAX_BUFFERED_LINES && (timeNow - timeLastNotify) < timeout && stdout.ready())
//                            continue;
//                        timeout = 1000000000L * 5 / 2;
//                        timeLastNotify = timeNow;
//
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (priorModified[0]) {
//                                    priorModified[0] = false;
//                                }
//                                int fullLen = logLines.size() + bufferedLogLines.size();
//                                if (fullLen >= MAX_LINES) {
//                                    int numToRemove = fullLen - MAX_LINES + 1;
//                                    logLines.removeFromStart(numToRemove);
//                                    posStart[0] -= numToRemove;
//                                }
//                                for (LogLine bufferedLine : bufferedLogLines) {
//                                    logLines.addLast(bufferedLine);
//                                }
//                                bufferedLogLines.clear();
//                                posStart[0] = logLines.size();
//
//                            }
//                        });
//                    }
//                } catch (IOException e) {
//                    //Log.e(TAG, Log.getStackTraceString(e));
//                } finally {
//                    if (process != null) {
//                        process.destroy();
//                    }
//                }
//            }
//        }).start();
//    }

    private LogLine parseLine(String line) {
        Matcher m = THREADTIME_LINE.matcher(line);
        Log.e("matches", "" + (m.matches()));
        if (m.matches()) {
            return new LogLine(Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), parseTime(m.group(1)), m.group(4), m.group(5), m.group(6));
        }
        return null;
    }

    private void sendEmailWithLogs(String logs) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "your_email"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "WireGuard Logs");
            intent.putExtra(Intent.EXTRA_TEXT, logs);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No Email application found!", Toast.LENGTH_SHORT).show();
        }
    }

    private Date parseTime(String timeStr) {
        try {
            return yearFormatter.parse(year + "-" + timeStr);
        } catch (ParseException e) {
            //Log.e(TAG, Log.getStackTraceString(e));
            return null;
        }
    }

    private void setUi() {
        switchButton.setChecked(settingsSingleton.getSettings().getSendCrashReports());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setClickListeners() {
        switchButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isSendOnCrashReportTouched = true;
                return false;
            }
        });
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isSendOnCrashReportTouched) {
                    isSendOnCrashReportTouched = false;
                    settingsSingleton.getSettings().setSendCrashReports(isChecked);
                    new Utils().saveSettings(getContext(), settingsSingleton.getSettings());
                }
            }
        });
        sendLogsToSupportLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder logs = fetchLastLogs();
                sendEmailWithLogs(logs.toString());
            }
        });
    }

    private StringBuilder fetchLastLogs() {
        StringBuilder logsBuilder = new StringBuilder();
        try {
            ProcessBuilder builder = new ProcessBuilder().command("logcat", "-b", "all", "-v", "threadtime", "-t", String.valueOf(50), "*:V");
            builder.environment().put("LC_ALL", "C");
            Process process = builder.start();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = stdout.readLine()) != null) {
                LogLine logLine = parseLine(line);
                if (logLine != null) {
                    if (logLine.tag.equals("WireGuard/GoBackend/wgpreconf")) {
                        logsBuilder.append(line).append("\n");
                    }
                }
            }
            process.destroy();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new StringBuilder(logsBuilder.toString());
    }

    private static class LogLine {
        int pid;
        int tid;
        Date time;
        String level;
        String tag;
        String msg;

        @NonNull
        @Override
        public String toString() {
            return "LogLine{" + "pid=" + pid + ", tid=" + tid + ", time=" + time + ", level='" + level + '\'' + ", tag='" + tag + '\'' + ", msg='" + msg + '\'' + '}';
        }

        LogLine(int pid, int tid, Date time, String level, String tag, String msg) {
            this.pid = pid;
            this.tid = tid;
            this.time = time;
            this.level = level;
            this.tag = tag;
            this.msg = msg;
        }
    }
}
