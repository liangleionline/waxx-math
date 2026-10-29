package com.lianglei.waxxmath;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lianglei.waxxmath.util.PdfExporter;
import com.lianglei.waxxmath.util.Question;
import com.lianglei.waxxmath.util.QuestionGenerator;
import com.lianglei.waxxmath.util.TopicCatalog;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends android.app.Activity {

    private static final int REQ_STORAGE = 1001;
    private static final int MAX_TOTAL = 40;

    private TextView chipGrade, chipHorizontal, chipVertical;
    private LinearLayout topicList;
    private TextView tvSelected;
    private Button btnGenerate;

    private int grade = 1;       // 1..5
    private boolean spring = false; // false=上学期 true=下学期
    private boolean hSelected = true;
    private boolean vSelected = false;

    private final Map<TopicCatalog.Topic, Integer> counts = new LinkedHashMap<>();
    private boolean pendingGenerate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chipGrade = findViewById(R.id.chipGrade);
        chipHorizontal = findViewById(R.id.chipHorizontal);
        chipVertical = findViewById(R.id.chipVertical);
        topicList = findViewById(R.id.topicList);
        tvSelected = findViewById(R.id.tvSelected);
        btnGenerate = findViewById(R.id.btnGenerate);

        chipGrade.setOnClickListener(v -> showGradePicker());
        chipHorizontal.setOnClickListener(v -> { hSelected = !hSelected; applyFormat(); });
        chipVertical.setOnClickListener(v -> { vSelected = !vSelected; applyFormat(); });
        btnGenerate.setOnClickListener(v -> onGenerate());

        applyGradeLabel();
        applyFormat();
        rebuildTopics();
        refreshBottom();
    }

    // ---------- 年级选择 ----------
    private void showGradePicker() {
        final String[] items = new String[TopicCatalog.GRADE_NAMES.length];
        final boolean[] springs = new boolean[TopicCatalog.GRADE_NAMES.length];
        int checked = 0;
        for (int i = 0; i < items.length; i++) {
            int g = i + 1;
            if (TopicCatalog.hasSpring(g)) {
                items[i] = TopicCatalog.GRADE_NAMES[i] + "（上/下学期）";
            } else {
                items[i] = TopicCatalog.GRADE_NAMES[i] + "（全学期）";
            }
            springs[i] = true;
        }
        new AlertDialog.Builder(this)
                .setTitle("选择年级")
                .setSingleChoiceItems(items, grade - 1, (d, which) -> {
                    grade = which + 1;
                    if (!TopicCatalog.hasSpring(grade)) {
                        spring = false; // 4/5 年级无学期
                        d.dismiss();
                        afterGradeChanged();
                    } else {
                        // 选择年级后再选学期
                        d.dismiss();
                        showSemesterPicker();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showSemesterPicker() {
        String[] items = {"上学期", "下学期"};
        new AlertDialog.Builder(this)
                .setTitle("选择学期")
                .setSingleChoiceItems(items, spring ? 1 : 0, (d, which) -> {
                    spring = (which == 1);
                    d.dismiss();
                    afterGradeChanged();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void afterGradeChanged() {
        applyGradeLabel();
        rebuildTopics();
        refreshBottom();
    }

    private void applyGradeLabel() {
        String g = TopicCatalog.GRADE_NAMES[grade - 1];
        String s = TopicCatalog.hasSpring(grade) ? (spring ? "下学期" : "上学期") : "全学期";
        chipGrade.setText(g + " | " + s + "  ▼");
    }

    // ---------- 题型卡片 ----------
    private void rebuildTopics() {
        topicList.removeAllViews();
        counts.clear();
        LayoutInflater inf = LayoutInflater.from(this);
        List<TopicCatalog.Topic> topics = TopicCatalog.topicsFor(grade, spring);
        for (TopicCatalog.Topic t : topics) {
            View card = inf.inflate(R.layout.item_topic, topicList, false);
            ((TextView) card.findViewById(R.id.tvTopicTitle)).setText(t.title);
            ((TextView) card.findViewById(R.id.tvTopicExample)).setText(t.example);
            final TextView tvCount = card.findViewById(R.id.tvCount);
            tvCount.setText("0");
            ImageButton minus = card.findViewById(R.id.btnMinus);
            ImageButton plus = card.findViewById(R.id.btnPlus);
            minus.setOnClickListener(v -> changeCount(t, -1, tvCount));
            plus.setOnClickListener(v -> changeCount(t, +1, tvCount));
            topicList.addView(card);
        }
    }

    private void changeCount(TopicCatalog.Topic t, int delta, TextView tvCount) {
        int cur = counts.getOrDefault(t, 0);
        int next = cur + delta;
        if (next < 0) next = 0;
        int total = totalCount() - cur + next;
        if (total > MAX_TOTAL) {
            Toast.makeText(this, R.string.toast_over_40, Toast.LENGTH_SHORT).show();
            return;
        }
        counts.put(t, next);
        tvCount.setText(String.valueOf(next));
        refreshBottom();
    }

    private int totalCount() {
        int s = 0;
        for (int v : counts.values()) s += v;
        return s;
    }

    private void refreshBottom() {
        int n = totalCount();
        tvSelected.setText("已选择 " + n + " 题");
        btnGenerate.setEnabled(n > 0);
        btnGenerate.setBackgroundResource(n > 0 ? R.drawable.bg_button : R.drawable.bg_button_disabled);
    }

    private void applyFormat() {
        chipHorizontal.setBackgroundResource(hSelected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipHorizontal.setTextColor(getColor(hSelected ? R.color.chip_selected_text : R.color.chip_unselected_text));
        chipVertical.setBackgroundResource(vSelected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipVertical.setTextColor(getColor(vSelected ? R.color.chip_selected_text : R.color.chip_unselected_text));
    }

    // ---------- 生成 ----------
    private void onGenerate() {
        int total = totalCount();
        if (total == 0) return;
        ensureStoragePermission(() -> {
            if (total <= 20) {
                new AlertDialog.Builder(this)
                        .setTitle("一式两份")
                        .setMessage("是否在纸张下半部分再生成一份相同题目？")
                        .setPositiveButton("是", (d, w) -> doGenerate(true))
                        .setNegativeButton("否", (d, w) -> doGenerate(false))
                        .setCancelable(false)
                        .show();
            } else {
                doGenerate(false);
            }
        });
    }

    private void doGenerate(boolean duplicate) {
        boolean randomize = hSelected && vSelected;
        List<Question> qs = new QuestionGenerator().generate(counts, hSelected, vSelected, randomize);
        if (qs.isEmpty()) {
            Toast.makeText(this, "请至少选择一道题目", Toast.LENGTH_SHORT).show();
            return;
        }
        File dir = new File(Environment.getExternalStorageDirectory(), "waxx");
        String name = "数学题_" + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new java.util.Date()) + ".pdf";
        File out = new File(dir, name);
        try {
            PdfExporter.Result res = PdfExporter.export(qs, duplicate, out);
            showResultDialog(res);
        } catch (Exception e) {
            Toast.makeText(this, "生成失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showResultDialog(PdfExporter.Result res) {
        final File file = res.file;
        new AlertDialog.Builder(this)
                .setTitle("已生成")
                .setMessage("共 " + res.total + " 题" + (res.duplicated ? "（一式两份）" : "")
                        + "\n已保存到：" + file.getAbsolutePath())
                .setPositiveButton("确定", null)
                .setNeutralButton("分享文件", (d, w) -> sharePdf(file))
                .show();
    }

    private void sharePdf(File file) {
        try {
            Uri uri = WaxxFileProvider.uriFor(file);
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("application/pdf");
            i.putExtra(Intent.EXTRA_STREAM, uri);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(i, "分享 PDF"));
        } catch (Exception e) {
            Toast.makeText(this, "分享失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ---------- 权限 ----------
    private boolean canWrite() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void ensureStoragePermission(Runnable onGranted) {
        if (canWrite()) { onGranted.run(); return; }
        pendingGenerate = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            new AlertDialog.Builder(this)
                    .setTitle("需要存储权限")
                    .setMessage("需要“所有文件访问”权限才能将 PDF 保存到手机根目录 waxx 文件夹")
                    .setPositiveButton("去设置", (d, w) -> {
                        Intent i = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        i.setData(Uri.parse("package:" + getPackageName()));
                        try { startActivity(i); }
                        catch (Exception ex) {
                            startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_STORAGE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (pendingGenerate) { pendingGenerate = false; onGenerate(); }
        } else {
            Toast.makeText(this, "未获得存储权限，无法保存PDF", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pendingGenerate && canWrite()) {
            pendingGenerate = false;
            onGenerate();
        }
    }
}
