package org.ymkm.pwchart;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import passwordchart.ymkm.org.passwordchart.R;

public class TopAct extends AppCompatActivity {

    @BindView(R.id.tv_seed)
    EditText tvSeed;
    @BindView(R.id.tv_pw)
    EditText tvPw;
    @BindView(R.id.use_numbers)
    Switch useNumbers;
    @BindView(R.id.use_symbols)
    Switch useSymbols;
    @BindView(R.id.tv_generated_password)
    TextView tvGeneratedPassword;
    @BindView(R.id.generated_password)
    LinearLayout generatedPassword;
    @BindView(R.id.tv_chart_number)
    TextView tvChartNumber;
    @BindView(R.id.char_table)
    GridLayout charTable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top);
        ButterKnife.bind(this);
        setupViews();
    }


    @OnCheckedChanged({R.id.use_numbers, R.id.use_symbols})
    public void onChartTypeChanged(@NonNull final CompoundButton btn, final boolean isChecked) {
        switch (btn.getId()) {
            case R.id.use_numbers:
                mChartType = isChecked ? (mChartType | PWChartUtils.CHART_TYPE_INCLUDE_NUMBERS)
                                     : (mChartType & ~PWChartUtils.CHART_TYPE_INCLUDE_NUMBERS);
                break;
            case R.id.use_symbols:
                mChartType = isChecked ? (mChartType | PWChartUtils.CHART_TYPE_INCLUDE_SYMBOLS)
                                     : (mChartType & ~PWChartUtils.CHART_TYPE_INCLUDE_SYMBOLS);
                break;
        }
        updateChart();
        updatePassword();
    }

    @OnTextChanged({R.id.tv_pw, R.id.tv_seed})
    public void onInputChanged() {
        updateChart();
        updatePassword();
    }

    @OnClick(R.id.tv_generated_password)
    public void onPwClicked() {
        final String pw = tvGeneratedPassword.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(pw, pw);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, R.string.toast_copied_pw_to_clipboard_text, Toast.LENGTH_SHORT).show();
    }


    private void updateChart() {
        tvChartNumber.setVisibility(TextUtils.isEmpty(tvSeed.getText())
                                            ? View.INVISIBLE
                                            : View.VISIBLE);
        charTable.setVisibility(TextUtils.isEmpty(tvSeed.getText())
                                        ? View.INVISIBLE
                                        : View.VISIBLE);

        if (TextUtils.isEmpty(tvSeed.getText())) {
            return;
        }
        final String seedStr = tvSeed.getText().toString();
        final long chartNum = PWChartUtils.chartNumber(seedStr, mChartType);
        tvChartNumber.setText(TextUtils.expandTemplate(getString(R.string.pwchart_number), String.valueOf(chartNum)));
        updateCharTable(seedStr, useNumbers.isChecked(), useSymbols.isChecked());
    }

    private void updatePassword() {
        generatedPassword.setVisibility(TextUtils.isEmpty(tvSeed.getText()) || TextUtils.isEmpty(tvPw.getText())
                                                ? View.INVISIBLE
                                                : View.VISIBLE);
        if (TextUtils.isEmpty(tvPw.getText()) || TextUtils.isEmpty(tvSeed.getText())) {
            return;
        }

        final String seedStr = tvSeed.getText().toString();
        final String pw = tvPw.getText().toString().toUpperCase(Locale.getDefault());
        final String genPw = PWChartUtils.genPasswordWithSeed(seedStr, pw, useNumbers.isChecked(), useSymbols.isChecked());
        tvGeneratedPassword.setText(genPw);
    }

    private void updateCharTable(@NonNull final String seed, final boolean useNumbers, final boolean useSymbols) {
        charTable.setVisibility(TextUtils.isEmpty(tvSeed.getText())
                                        ? View.INVISIBLE
                                        : View.VISIBLE);
        final ArrayMap<Character, String> map = PWChartUtils.getTable(seed, useNumbers, useSymbols);
        for (int i = 0; i < charTable.getChildCount(); ++i) {
            final Button b = (Button) charTable.getChildAt(i);
            final char charMap = b.getText().toString().charAt(0);
            b.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View v) {
                    if (charTable.getTag() != null) {
                        ((View) charTable.getTag()).setActivated(false);
                    }
                    if (mShownPopup != null) {
                        ((TextView) mShownPopup.getContentView()).setText(map.get(charMap));
                    }
                    else {
                        final TextView tv = (TextView) LayoutInflater.from(TopAct.this).inflate(R.layout.char_popup, null);
                        tv.setText(map.get(charMap));
                        tv.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                        mShownPopup = new PopupWindow(tv, tv.getMeasuredWidth(), tv.getMeasuredHeight());
                        charTable.setTag(b);
                    }
                    mShownPopup.setOutsideTouchable(true);
                    mShownPopup.showAtLocation(v, Gravity.CENTER, 0, 0);
                    mShownPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {

                        @Override
                        public void onDismiss() {
                            if (charTable.getTag() != null) {
                                ((View) charTable.getTag()).setActivated(false);
                            }
                        }
                    });
                    charTable.setTag(b);
                    b.setActivated(true);
                }
            });
        }
    }


    private void setupViews() {
        updateChart();
        updatePassword();
    }


    private PopupWindow mShownPopup;
    private int mChartType = PWChartUtils.CHART_TYPE_NORMAL;
}
