package pers.lonestar.pixelcanvas.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import pers.lonestar.pixelcanvas.R;
import pers.lonestar.pixelcanvas.activity.MainActivity;
import pers.lonestar.pixelcanvas.adapter.MainCanvasAdapter;
import pers.lonestar.pixelcanvas.infostore.BmobCanvas;

public class WorldFragmnet extends Fragment {
    List<BmobCanvas> bmobCanvasList = new ArrayList<>();
    private RecyclerView recyclerView;
    private BmobQuery<BmobCanvas> loadMoreQuery = new BmobQuery<>();
    private MainCanvasAdapter adapter;
    private View view;
    private int querySkip = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_world, container, false);
        return view;
    }

    private void initData() {
        loadMoreQuery.order("-createdAt");
        loadMoreQuery.setLimit(10);
        loadMoreQuery.setSkip(0);
        loadMoreQuery.findObjects(new FindListener<BmobCanvas>() {
            @Override
            public void done(List<BmobCanvas> list, BmobException e) {
                if (e == null) {
                    bmobCanvasList.clear();
                    bmobCanvasList.addAll(list);
                    Log.d("TEST", "已获得数据，进行刷新，数据大小：" + bmobCanvasList.size());
                    adapter.setLoadState(adapter.LOADING_COMPLETE);
                } else {
                    Toast.makeText(MainActivity.getInstance(), "数据获取失败，请检查网络设置", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView = view.findViewById(R.id.word_recyclerview);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.getInstance(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new MainCanvasAdapter(bmobCanvasList);
        recyclerView.setAdapter(adapter);
        initData();
    }
}
