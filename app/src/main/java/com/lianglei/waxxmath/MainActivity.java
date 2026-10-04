package com.lianglei.waxxmath;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lianglei.waxxmath.util.PdfExporter;
import com.lianglei.waxxmath.util.Question;
import com.lianglei.waxxmath.util.QuestionGenerator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends android.app.Activity {

    private static final int REQ_STORAGE = 1001;

    private EditText etRangeStart, etRangeEnd;
    private EditText etAdd, etSub, etMul, etDiv;
    private LinearLayout rowMul, rowDiv;
    private TextView badgeAdd, badgeSub, badgeMul, badgeDiv;
    private TextView chipHorizontal, chipVertical;
    private Button btnNext;

    private boolean hSelected = true;  // 横式默认选中
    private boolean vSelected = false; // 竖式默认未选中
    private int mulSaved = 5, divSaved = 5;
    private boolean pendingGenerate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setupRangeWatchers();
        setupCountWatchers();
        setupChips();
        setupNext();

        refreshNextState();
        refreshMulDivState();
    }

    private void bindViews() {
        etRangeStart = findViewById(R.id.etRangeStart);
        etRangeEnd = findViewById(R.id.etRangeEnd);
        etAdd = findViewById(R.id.etAdd);
        etSub = findViewById(R.id.etSub);
        etMul = findViewById(R.id.etMul);
        etDiv = findViewById(R.id.etDiv);
        rowMul = findViewById(R.id.rowMul);
        rowDiv = findViewById(R.id.rowDiv);
        badgeAdd = findViewById(R.id.badgeAdd);
        badgeSub = findViewById(R.id.badgeSub);
        badgeMul = findViewById(R.id.badgeMul);
        badgeDiv = findViewById(R.id.badgeDiv);
        chipHorizontal = findViewById(R.id.chipHorizontal);
        chipVertical = findViewById(R.id.chipVertical);
        btnNext = findViewById(R.id.btnNext);
    }

    private void setupRangeWatchers() {
        TextWatcher w = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { refreshMulDivState(); refreshNextState(); }
        };
        etRangeStart.addTextChangedListener(w);
        etRangeEnd.addTextChangedListener(w);
    }

    private void setupCountWatchers() {
        TextWatcher w = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { refreshNextState(); }
        };
        etAdd.addTextChangedListener(w);
        etSub.addTextChangedListener(w);
        etMul.addTextChangedListener(w);
        etDiv.addTextChangedListener(w);
    }

    private void setupChips() {
        chipHorizontal.setOnClickListener(v -> {
            hSelected = !hSelected;
            applyChips();
        });
        chipVertical.setOnClickListener(v -> {
            vSelected = !vSelected;
            applyChips();
        });
        applyChips();
    }

    private void applyChips() {
        styleChip(chipHorizontal, hSelected);
        styleChip(chipVertical, vSelected);
    }

    private void styleChip(TextView chip, boolean selected) {
        chip.setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chip.setTextColor(getColor(selected ? R.color.chip_selected_text : R.color.chip_unselected_text));
        chip.setTypeface(Typeface.DEFAULT, selected ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void setupNext() {
        btnNext.setOnClickListener(v -> onNextClicked());
    }

    // ---------- 状态刷新 ----------

    /** 乘法/除法联动：仅当范围两框都填且合法时置灰并归0。 */
    private void refreshMulDivState() {
        boolean rangeActive = hasValidRange();
        if (rangeActive) {
            mulSaved = parse(etMul, 5);
            divSaved = parse(etDiv, 5);
            etMul.setText("0");
            etDiv.setText("0");
            setRowDisabled(rowMul, etMul, badgeMul, true);
            setRowDisabled(rowDiv, etDiv, badgeDiv, true);
        } else {
            setRowDisabled(rowMul, etMul, badgeMul, false);
            setRowDisabled(rowDiv, etDiv, badgeDiv, false);
            if (etMul.getText().toString().equals("0")) etMul.setText(String.valueOf(mulSaved));
            if (etDiv.getText().toString().equals("0")) etDiv.setText(String.valueOf(divSaved));
        }
    }

    private void setRowDisabled(LinearLayout row, EditText edit, TextView badge, boolean disabled) {
        row.setAlpha(disabled ? 0.45f : 1f);
        edit.setEnabled(!disabled);
        badge.setAlpha(disabled ? 0.5f : 1f);
    }

    /**
     * 下一步可用性：
     * - 范围只填一个框，或 起始>结束 → 置灰
     * - 四题总数 = 0 → 置灰
     */
    private void refreshNextState() {
        boolean rangeInvalid = isRangePartiallyFilled() || isRangeStartGreaterEnd();
        int total = totalCount();
        btnNext.setEnabled(!rangeInvalid && total > 0);
        btnNext.setBackgroundResource(!rangeInvalid && total > 0 ? R.drawable.bg_button : R.drawable.bg_button_disabled);
    }

    private boolean isRangePartiallyFilled() {
        boolean hasStart = !etRangeStart.getText().toString().trim().isEmpty();
        boolean hasEnd = !etRangeEnd.getText().toString().trim().isEmpty();
        return hasStart != hasEnd;
    }

    private boolean isRangeStartGreaterEnd() {
        String s = etRangeStart.getText().toString().trim();
        String e = etRangeEnd.getText().toString().trim();
        if (s.isEmpty() || e.isEmpty()) return false;
        try {
            return Integer.parseInt(s) > Integer.parseInt(e);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean hasValidRange() {
        String s = etRangeStart.getText().toString().trim();
        String e = etRangeEnd.getText().toString().trim();
        if (s.isEmpty() || e.isEmpty()) return false;
        try {
            return Integer.parseInt(s) <= Integer.parseInt(e);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private int totalCount() {
        return parse(etAdd, 0) + parse(etSub, 0) + parse(etMul, 0) + parse(etDiv, 0);
    }

    private int parse(EditText et, int def) {
        String s = et.getText().toString().trim();
        if (s.isEmpty()) return def;
        try {
            int v = Integer.parseInt(s);
            return Math.max(0, v);
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    // ---------- 生成流程 ----------

    private void onNextClicked() {
        if (totalCount() > 40) {
            Toast.makeText(this, R.string.toast_over_40, Toast.LENGTH_SHORT).show();
            return;
        }
        if (totalCount() == 0) return; // 按钮已置灰，防御性
        ensureStoragePermission(() -> {
            if (totalCount() <= 20) {
                showDuplicateDialog();
            } else {
                generatePdf(false);
            }
        });
    }

    private void showDuplicateDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.duplicate_title)
                .setMessage(R.string.duplicate_msg)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, (d, w) -> generatePdf(true))
                .setNegativeButton(R.string.no, (d, w) -> generatePdf(false))
                .show();
    }

    private void generatePdf(boolean duplicate) {
        QuestionGenerator.Config cfg = new QuestionGenerator.Config();
        cfg.addCount = parse(etAdd, 0);
        cfg.subCount = parse(etSub, 0);
        cfg.mulCount = parse(etMul, 0);
        cfg.divCount = parse(etDiv, 0);
        cfg.rangeStart = parseNullable(etRangeStart);
        cfg.rangeEnd = parseNullable(etRangeEnd);
        cfg.useHorizontal = hSelected;
        cfg.useVertical = vSelected;

        List<Question> questions = new QuestionGenerator().generate(cfg, hSelected && vSelected);

        if (questions.isEmpty()) {
            Toast.makeText(this, R.string.no_question, Toast.LENGTH_SHORT).show();
            return;
        }

        File dir = new File(Environment.getExternalStorageDirectory(), "waxx");
        String name = "数学题_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".pdf";
        File out = new File(dir, name);

        try {
            PdfExporter.Result res = PdfExporter.export(questions, duplicate, out);
            String msg = getString(R.string.generate_ok) + "：" + out.getAbsolutePath()
                    + "（" + questions.size() + " 题" + (res.duplicated ? "，一式两份" : "") + "）";
            new AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setMessage(msg)
                    .setPositiveButton("好", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.generate_fail) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private Integer parseNullable(EditText et) {
        String s = et.getText().toString().trim();
        if (s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    // ---------- 存储权限 ----------

    private void ensureStoragePermission(Runnable onGranted) {
        if (canWrite()) {
            onGranted.run();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            pendingGenerate = true;
            new AlertDialog.Builder(this)
                    .setTitle(R.string.permission_title)
                    .setMessage(R.string.permission_msg_allfiles)
                    .setPositiveButton(R.string.goto_settings, (d, w) -> {
                        Intent i = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        i.setData(android.net.Uri.parse("package:" + getPackageName()));
                        try {
                            startActivity(i);
                        } catch (Exception ex) {
                            startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_STORAGE);
            pendingGenerate = true;
        }
    }

    private boolean canWrite() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingGenerate) { pendingGenerate = false; onNextClicked(); }
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pendingGenerate && canWrite()) {
            pendingGenerate = false;
            onNextClicked();
        }
    }
}
