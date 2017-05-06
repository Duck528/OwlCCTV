package com.duck.owlcctv.viewmodel;


import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.duck.owlcctv.R;
import com.duck.owlcctv.activity.MenuActivity;
import com.duck.owlcctv.activity.VideoViewActivity;
import com.duck.owlcctv.adapter.RecordedArrayAdapter;
import com.duck.owlcctv.model.Recorded;
import com.duck.owlcctv.util.FileUtil;
import com.duck.owlcctv.util.OwlSettings;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordedViewModel implements BaseViewModel {
    private static final String TAG = "[RecordedViewModel]";
    private final AppCompatActivity activity;

    public RecordedViewModel(AppCompatActivity activity) {
        this.activity = activity;
    }

    private void listRecords() {
        File fileDir = new File(OwlSettings.saveDir);
        File[] files = fileDir.listFiles();

        List<Recorded> recordeds = new ArrayList<>();
        for (File f : files) {
            if (f.getName().endsWith(".mp4") && f.canRead()) {
                Recorded r = new Recorded();
                r.setName(f.getName());
                r.setThumb(ThumbnailUtils.createVideoThumbnail(
                        f.getPath(),
                        MediaStore.Images.Thumbnails.MICRO_KIND));
                r.setFileSize(FileUtil.getFileSize(f));
                r.setReadableFileSize(FileUtil.getReadableFileSize(f));
                Date lastModified = new Date(f.lastModified());
                SimpleDateFormat format = new SimpleDateFormat("yyyy. MM. yy. HH:mm", Locale.KOREA);
                r.setLastModified(format.format(lastModified));
                r.setPath(f.getPath());
                recordeds.add(r);
            }
        }

        final ListView listView = (ListView)activity.findViewById(R.id.lvRecorded);
        RecordedArrayAdapter adapter = new RecordedArrayAdapter(recordeds);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Recorded r = (Recorded) parent.getAdapter().getItem(position);
                Intent intent = new Intent(parent.getContext(), VideoViewActivity.class);
                intent.putExtra("videoPath", r.getPath());
                parent.getContext().startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showSelectMode();
                return true;
            }
        });
    }

    private void showSelectMode() {
        displayBotMenu(true);
        ListView listView = (ListView)activity.findViewById(R.id.lvRecorded);
        int len = listView.getCount();
        for (int i=0; i<len; i++) {
            View item = listView.getChildAt(i);
            CheckBox checkBox = (CheckBox)item.findViewById(R.id.cbxSelected);
            checkBox.setVisibility(View.VISIBLE);
        }
    }

    private void displayBotMenu(boolean isVisibile) {
        RelativeLayout botMenu = (RelativeLayout) activity.findViewById(R.id.botMenu);
        if (isVisibile) {
             botMenu.setVisibility(View.VISIBLE);
        } else {
            botMenu.setVisibility(View.GONE);
        }
    }

    /**
     * 선택된 레코드를 모두 삭제한다
     * 사용자에게 다시한번 삭제할지 의사를 확인한다
     */
    public void deleteSelectedItem() {
        final ListView listView = (ListView)activity.findViewById(R.id.lvRecorded);
        RecordedArrayAdapter adapter = (RecordedArrayAdapter)listView.getAdapter();
        final List<Recorded> selectedRecords = adapter.getSelectedRecords();
        int nSelected = selectedRecords.size();
        String msg;
        if (nSelected == 1) {
            msg = "파일을 삭제하시겠습니까?";
        } else {
            msg = nSelected + "개 파일을 삭제하시겠습니까?";
        }

        final AlertDialog.Builder alertDlg = new AlertDialog.Builder(activity);
        alertDlg.setTitle("파일 삭제");
        alertDlg.setMessage(msg).setCancelable(true);
        alertDlg.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (Recorded r : selectedRecords) {
                    File f = new File(r.getPath());
                    if (!f.delete()) {
                        Log.d(TAG, f.getPath() + " not deleted");
                    }
                }
                selectedRecords.clear();
                // 일단은 이렇게 null로 한 뒤 다시 리스팅하는 방식을 취하지만
                // 나중에 시간이되면 개별적으로 삭제하는 루틴으로 바꾼다
                listView.setAdapter(null);
                listRecords();
                displayBotMenu(false);
            }
        });
        alertDlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // 알림창 보여주기
        AlertDialog dialog = alertDlg.create();
        dialog.show();
    }

    /**
     * 메뉴로 이동한다
     */
    public void navToMenu() {
        Intent intent = new Intent(activity, MenuActivity.class);
        activity.startActivity(intent);
    }

    /**
     * 모든 레코드를 선택한다
     */
    public void selectAll() {
        ListView listView = (ListView)activity.findViewById(R.id.lvRecorded);
        for (int i=0, len=listView.getCount(); i<len; i++) {
            View v = listView.getChildAt(i);
            CheckBox checkBox = (CheckBox)v.findViewById(R.id.cbxSelected);
            checkBox.performClick();
        }
    }

    public void cancel() {
        ListView listView = (ListView)activity.findViewById(R.id.lvRecorded);
        for (int i=0, len=listView.getCount(); i<len; i++) {
            View v = listView.getChildAt(i);
            CheckBox checkBox = (CheckBox)v.findViewById(R.id.cbxSelected);
            // 만약, 체크박스가 선택되어 있다면 모두 취소하고 안보이게 만든다
            if (checkBox.isChecked()) {
                checkBox.performClick();
            }
            checkBox.setVisibility(View.GONE);
        }
        // 하단 메뉴를 안보이게 한다
        this.displayBotMenu(false);
    }

    @Override
    public void onCreate() {
        this.listRecords();
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }
}
