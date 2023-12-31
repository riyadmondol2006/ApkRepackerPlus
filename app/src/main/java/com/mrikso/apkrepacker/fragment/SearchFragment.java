package com.mrikso.apkrepacker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.fragment.app.Fragment;

import com.duy.ide.database.SQLHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jecelyin.common.utils.UIUtils;
import com.jecelyin.editor.v2.utils.ExtGrep;
import com.jecelyin.editor.v2.utils.GrepBuilder;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.AppEditorActivity;
import com.mrikso.apkrepacker.ui.autocompleteeidttext.CustomAdapter;
import com.mrikso.apkrepacker.ui.prererence.Preference;
import com.mrikso.apkrepacker.utils.StringUtils;
import com.mrikso.apkrepacker.utils.ThemeWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchFragment extends Fragment {

    public static final String TAG = "SearchFragment";

    private String path;
    private CheckBox mCaseSensitiveCheckBox;
    private CheckBox mRegexCheckBox;
    private CheckBox mWholeWordsOnlyCheckBox;
    private CheckBox mRecursivelyCheckBox;
    private CheckBox mFilesCheckBox;
    private AppCompatAutoCompleteTextView findText;
    private GrepBuilder builder;
    private ImageButton clearBtn;
    private FloatingActionButton searchBtn;
    private boolean filesMode, regex, wordsOnly, matchcase, recursivlu;
    private LinearLayout searchOptions;
    private MaterialButton mAddExt;
    private List<String> extList;
    private ChipGroup chipGroup;
    private Context mContext;
    private Preference mPtef;
    private Map<String, Boolean> extMap = new HashMap<>();
    private CustomAdapter adapter;
    private SQLHelper dbHelper;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            path = bundle.getString("curDirect");
        }
        View view = inflater.inflate(R.layout.fragment_search_settings, container, false);
        mContext = view.getContext();
        mPtef = Preference.getInstance(mContext);
        loadPrefs();
        dbHelper = new SQLHelper(mContext);
        chipGroup = view.findViewById(R.id.ext_group);
        mAddExt = view.findViewById(R.id.button_add_ext);
        findText = view.findViewById(R.id.search_text);
        searchBtn = view.findViewById(R.id.fab_go_search);
        mFilesCheckBox = view.findViewById(R.id.search_file_cb);
        mCaseSensitiveCheckBox = view.findViewById(R.id.ignore_case_cb);
        mRegexCheckBox = view.findViewById(R.id.regular_exp_cb);
        mWholeWordsOnlyCheckBox = view.findViewById(R.id.whole_words_only_cb);
        mRecursivelyCheckBox = view.findViewById(R.id.recursively_cb);
        clearBtn = view.findViewById(R.id.button_clear);
        searchOptions = view.findViewById(R.id.search_options_layout);

        return view;
    }

    private List<String> getData() {
        //   List<String> dataList = new ArrayList<String>();
        List<String> items = SQLHelper.getInstance(mContext).getFindKeywordsAdnFile(filesMode);
        return items;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        extList = new ArrayList<>();
        builder = GrepBuilder.start();
        mFilesCheckBox.setChecked(filesMode);
        mRegexCheckBox.setChecked(regex);
        mCaseSensitiveCheckBox.setChecked(matchcase);
        mRecursivelyCheckBox.setChecked(recursivlu);
        mWholeWordsOnlyCheckBox.setChecked(wordsOnly);
        searchOptions.setVisibility(filesMode ? View.GONE : View.VISIBLE);
        adapter = new CustomAdapter(mContext, getData());
        findText.setAdapter(adapter);
        findText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                clearBtn.setVisibility(s.toString().isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
        mFilesCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mPtef.setFilesMode(true);
                filesMode = true;
                adapter = new CustomAdapter(mContext, getData());
                findText.setAdapter(adapter);
                searchOptions.setVisibility(View.GONE);
            } else {
                filesMode = false;
                mPtef.setFilesMode(false);
                adapter = new CustomAdapter(mContext, getData());
                findText.setAdapter(adapter);
                searchOptions.setVisibility(View.VISIBLE);
            }
        });
        mRegexCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mPtef.setRegexMode(regex);
                regex = true;
                UIUtils.toast(App.getContext(), R.string.use_regex_to_find_tip);
            } else {
                regex = false;
                mPtef.setRegexMode(regex);
            }
        });
        mCaseSensitiveCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mPtef.setMatchCaseMode(true);
                matchcase = true;
            } else {
                matchcase = false;
                mPtef.setMatchCaseMode(false);
            }
        });
        mRecursivelyCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mPtef.setRecursivelyMode(true);
                recursivlu = true;
            } else {
                mPtef.setRecursivelyMode(false);
                recursivlu = false;
            }
        });
        mWholeWordsOnlyCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mPtef.setWholeWordsOnlyMode(true);
                wordsOnly = true;
            } else {
                wordsOnly = false;
                mPtef.setWholeWordsOnlyMode(false);
            }
        });
        setChipGroup();
        searchBtn.setOnClickListener(v -> {
            if (!com.jecelyin.common.utils.StringUtils.isEmpty(findText.getText().toString()) | !extList.isEmpty()) {
                StringUtils.hideKeyboard(this);
                if (filesMode) {
                    adapter.addValue(findText.getText().toString());
                    dbHelper.clearFindKeywordAndFiles(true);
                    for (String item : adapter.getDataList()) {
                        dbHelper.addFindKeywordAndFiles(item, true);
                    }
                    Bundle parasms = new Bundle();
                    parasms.putString("searchFileName", findText.getText().toString());
                    parasms.putStringArrayList("expensions", getCheckedChips());
                    mPtef.setExt(extMap);
                    parasms.putString("curDirect", path);
                    parasms.putBoolean("findFiles", filesMode);
                    AppEditorActivity appEditorActivity = AppEditorActivity.getInstance();
                    appEditorActivity.setSearchArguments(parasms);
                    appEditorActivity.mViewPager.setCurrentItem(3);
                } else {

                    if (!matchcase) {
                        builder.ignoreCase();
                    }

                    if (wordsOnly) {
                        builder.wordRegex();
                    }

                    builder.setRegex(findText.getText().toString(), regex);
                    if (recursivlu) {
                        builder.recurseDirectories();
                    }
                    adapter.addValue(findText.getText().toString());
                    dbHelper.clearFindKeywordAndFiles(false);
                    for (String item : adapter.getDataList()) {
                        dbHelper.addFindKeywordAndFiles(item, false);
                    }
                    mPtef.setExt(extMap);
                    builder.setExeption(getCheckedChips());
                    builder.addFile(path);
                    ExtGrep grep = builder.build();
                    doInFiles(grep);
                }
            } else {
                UIUtils.toast(mContext, getResources().getString(R.string.toast_error_empty_find));
            }
        });
        mAddExt.setOnClickListener(v -> {
            UIUtils.showInputDialog(mContext, R.string.dialog_add_extensions, 0, null, EditorInfo.TYPE_CLASS_TEXT, new UIUtils.OnShowInputCallback() {
                @Override
                public void onConfirm(CharSequence input) {
                    extList.add(input.toString());
                    addChips(input.toString());
                    //setTag(extList);
                }
            });
        });
        clearBtn.setOnClickListener(v -> findText.setText(""));
    }

    private void addChips(String tagName) {
        //for (int index = 0; index < tagList.size(); index++) {
        // final String tagName = tagList.get(index);
        //  final ChipGroup chipGroup = view.findViewById(R.id.ext_group);
        Chip chip = new Chip(mContext);
        int paddingDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        chip.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
        chip.setText(tagName);
        chip.setCheckable(true);
        chip.setChecked(true);
        chip.setClickable(true);
        chip.setTextColor(getResources().getColor(R.color.white));
        chip.setChipBackgroundColorResource(R.color.light_accent);
        //Added click listener on close icon to remove tag from ChipGroup
        chip.setOnLongClickListener(v -> {
            //tagList.remove(tagName);
            extList.remove(tagName);
            chipGroup.removeView(chip);
            return true;
        });
        chipGroup.addView(chip);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getCheckedChips();
        mPtef.setExt(extMap);
        dbHelper.clearFindKeywordAndFiles(filesMode);
        for (String item : adapter.getDataList()) {
            dbHelper.addFindKeywordAndFiles(item, filesMode);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPrefs();
    }

    private void loadPrefs() {
        //ToDo load inputed values..
        filesMode = mPtef.isFilesMode();
        regex = mPtef.isRegexMode();
        matchcase = mPtef.isMatchCaseMode();
        wordsOnly = mPtef.isWholeWordsOnlyMode();
        recursivlu = mPtef.isRecursivelyMode();
        extMap = mPtef.getExt();
    }

    private void setChipGroup() {
        for (Map.Entry val : extMap.entrySet()) {
            Chip chip = new Chip(mContext);
            int paddingDp = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 10,
                    getResources().getDisplayMetrics()
            );
            chip.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
            chip.setText(val.getKey().toString());
            chip.setCheckable(true);
            chip.setChecked((boolean) val.getValue());
            chip.setClickable(true);
            chip.setTextColor(getResources().getColor(R.color.white));
            chip.setChipBackgroundColorResource(ThemeWrapper.isLightTheme() ? R.color.light_accent : R.color.dark_accent);
            chip.setOnLongClickListener(v -> {
                //tagList.remove(tagName);
                extMap.remove(val.getKey().toString());
                chipGroup.removeView(chip);
                return true;
            });
            //add chips to view
            chipGroup.addView(chip);
        }

    }

    private ArrayList<String> getCheckedChips() {
        ///   final ChipGroup chipGroup = view.findViewById(R.id.ext_group);
        ArrayList<String> arrayList = new ArrayList<>();
        int chipsCount = chipGroup.getChildCount();
        if (chipsCount == 0) {
            //msg += " None!!";
        } else {
            int i = 0;
            while (i < chipsCount) {
                Chip chip = (Chip) chipGroup.getChildAt(i);
                extMap.put(chip.getText().toString(), chip.isChecked());
                if (chip.isChecked()) {
                    arrayList.add(chip.getText().toString());
                }
                i++;
            }
        }
        return arrayList;
    }

    private void doInFiles(ExtGrep grep) {
        StringUtils.setGreap(grep);
        Bundle bundle = new Bundle();
        bundle.putString("curDirect", path);
        AppEditorActivity appEditorActivity = AppEditorActivity.getInstance();
        appEditorActivity.setSearchArguments(bundle);
        appEditorActivity.mViewPager.setCurrentItem(3);
    }
}
