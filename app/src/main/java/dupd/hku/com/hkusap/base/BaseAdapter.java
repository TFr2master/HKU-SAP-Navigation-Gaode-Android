package dupd.hku.com.hkusap.base;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * author: 13060393903@163.com
 * created on: 2018/08/10 11:08
 * description:
 */
public abstract class BaseAdapter<T> extends RecyclerView.Adapter<BaseViewHolder<T>> {
    protected List<T> mTList = new ArrayList<>();
    protected OnItemClickListener<T> mOnItemClickListener;

    public BaseAdapter() {
    }

    public BaseAdapter(List<T> tList) {
        if (tList == null) return;
        mTList.clear();
        mTList.addAll(tList);
    }

    public List<T> getTList() {
        return mTList;
    }

    @Override
    public int getItemCount() {
        return mTList.size();
    }

    @Override
    public void onBindViewHolder(BaseViewHolder<T> holder, int position) {
        if (holder == null) return;
        holder.bindPosition(position, mTList.get(position));
    }

    public void refresh(List<T> tList) {
        tList = tList == null ? new ArrayList<T>() : tList;
        mTList.clear();
        mTList.addAll(tList);
        notifyDataSetChanged();
    }

    public void loadMore(List<T> tList) {
        tList = tList == null ? new ArrayList<T>() : tList;
        mTList.addAll(tList);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener<T> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener<T> {
        void onItemClick(int position, T t);
    }
}
