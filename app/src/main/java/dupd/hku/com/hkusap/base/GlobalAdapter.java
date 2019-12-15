package dupd.hku.com.hkusap.base;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * Created by liuwei on 2018/9/10 .
 */

public class GlobalAdapter<T> extends BaseQuickAdapter<T,BaseViewHolder> {
    private OnGlobalListener listener;

    public GlobalAdapter(int layoutResId, @Nullable List<T> data, OnGlobalListener listener) {
        super(layoutResId, data);
        this.listener = listener;
    }

    private GlobalAdapter(int layoutResId, @Nullable List<T> data) {
        super(layoutResId, data);
    }

    private GlobalAdapter(@Nullable List<T> data) {
        super(data);
    }

    private GlobalAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(BaseViewHolder helper, T item) {
        listener.logic(helper,item);
    }

}
