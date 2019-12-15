package dupd.hku.com.hkusap.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * author: 13060393903@163.com
 * created on: 2018/07/25 9:24
 * description:
 */
public abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder {

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bindPosition(final int position, T t);
}