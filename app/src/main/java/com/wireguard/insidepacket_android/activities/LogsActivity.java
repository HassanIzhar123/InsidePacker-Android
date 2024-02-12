package com.wireguard.insidepacket_android.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.collection.CircularArray;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wireguard.insidepacket_android.R;

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

public class LogsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private LogEntryAdapter logAdapter;
    private final CircularArray<LogLine> logLines = new CircularArray<>();
    private final CircularArray<String> rawLogLines = new CircularArray<>();
    private DateFormat yearFormatter;
    private String year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);
        recyclerView = findViewById(R.id.recycler_view);
        AppCompatButton buttonSave = findViewById(R.id.button_save);
        ImageView backBtn = findViewById(R.id.backButton);
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.set(Calendar.MILLISECOND, 0);
        year = String.valueOf(calendar.get(Calendar.YEAR));
        logAdapter = new LogEntryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(LogsActivity.this));
        recyclerView.setAdapter(logAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(LogsActivity.this, LinearLayoutManager.VERTICAL));
        yearFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
//        streamingLog();
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                saveLogs();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }



    private void saveLogs() {
        StringBuilder logsBuilder = new StringBuilder();
        int startIndex = Math.max(0, logLines.size() - 50); // Start index for fetching last 50 logs
        for (int i = startIndex; i < logLines.size(); i++) {
            LogLine logLine = logLines.get(i);
            logsBuilder.append(logLine.time).append(" ").append(logLine.tag).append(": ").append(logLine.msg).append("\n");
        }
        String logs = logsBuilder.toString();
        sendEmailWithLogs(logs);
    }

    private void sendEmailWithLogs(String logs) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "WireGuard Logs");
        emailIntent.putExtra(Intent.EXTRA_TEXT, logs);
        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        } catch (ActivityNotFoundException ex) {
            // Handle case where no email app is available
            Log.e(TAG, "No email app found", ex);
        }
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
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                boolean isScrolledToBottomAlready = recyclerView != null && !recyclerView.canScrollVertically(1);
//                                if (priorModified[0]) {
//                                    logAdapter.notifyItemChanged(posStart[0] - 1);
//                                    priorModified[0] = false;
//                                }
//                                int fullLen = logLines.size() + bufferedLogLines.size();
//                                if (fullLen >= MAX_LINES) {
//                                    int numToRemove = fullLen - MAX_LINES + 1;
//                                    logLines.removeFromStart(numToRemove);
//                                    logAdapter.notifyItemRangeRemoved(0, numToRemove);
//                                    posStart[0] -= numToRemove;
//                                }
//                                for (LogLine bufferedLine : bufferedLogLines) {
//                                    logLines.addLast(bufferedLine);
//                                }
//                                bufferedLogLines.clear();
//                                logAdapter.notifyItemRangeInserted(posStart[0], logLines.size() - posStart[0]);
//                                posStart[0] = logLines.size();
//                                if (isScrolledToBottomAlready && recyclerView != null) {
//                                    recyclerView.scrollToPosition(logLines.size() - 1);
//                                }
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
        if (m.matches()) {
            return new LogLine(
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)),
                    parseTime(m.group(1)),
                    m.group(4),
                    m.group(5),
                    m.group(6)
            );
        }
        return null;
    }

    private Date parseTime(String timeStr) {
        try {
            return yearFormatter.parse(year + "-" + timeStr);
        } catch (ParseException e) {
            //Log.e(TAG, Log.getStackTraceString(e));
            return null;
        }
    }

    private class LogEntryAdapter extends RecyclerView.Adapter<LogEntryAdapter.ViewHolder> {


        private static class ViewHolder extends RecyclerView.ViewHolder {
            TextView logDate;
            TextView logMsg;


            ViewHolder(View view) {
                super(view);
                logDate = view.findViewById(R.id.log_date);
                logMsg = view.findViewById(R.id.log_msg);
            }
        }

        @NonNull
        @Override
        public LogEntryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.log_viewer_entry, parent, false);
            return new LogEntryAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LogEntryAdapter.ViewHolder holder, int position) {
            LogLine logLine = logLines.get(position);
            holder.logDate.setText(logLine.time.toString());
            SpannableString spannable = new SpannableString(logLine.tag + ": " + logLine.msg);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, logLine.tag.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            switch (logLine.level) {
                case "V":
                case "D":
                    spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.debug_tag_color)), 0, logLine.tag.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                case "E":
                    spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.error_tag_color)), 0, logLine.tag.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                case "I":
                    spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.info_tag_color)), 0, logLine.tag.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                case "W":
                    spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.warning_tag_color)), 0, logLine.tag.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                default:
                    spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.purple)), 0, logLine.tag.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
            }
            holder.logMsg.setText(spannable);
        }

        @Override
        public int getItemCount() {
            return logLines.size();
        }
    }

    private static class LogLine {
        int pid;
        int tid;
        Date time;
        String level;
        String tag;
        String msg;

        LogLine(int pid, int tid, Date time, String level, String tag, String msg) {
            this.pid = pid;
            this.tid = tid;
            this.time = time;
            this.level = level;
            this.tag = tag;
            this.msg = msg;
        }
    }

    private static final String TAG = "WireGuard/LogViewerActivity";
    private static final Pattern THREADTIME_LINE = Pattern.compile("^(\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3})(?:\\s+[0-9A-Za-z]+)?\\s+(\\d+)\\s+(\\d+)\\s+([A-Z])\\s+(.+?)\\s*: (.*)$");
}
