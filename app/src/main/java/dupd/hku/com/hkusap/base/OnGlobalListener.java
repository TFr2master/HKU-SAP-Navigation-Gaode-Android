package dupd.hku.com.hkusap.base;

import com.chad.library.adapter.base.BaseViewHolder;


/**
 * Created by liuwei on 2018/9/10 .
 */
public interface OnGlobalListener {
    <T> void logic(BaseViewHolder helper, T item);
}
